package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.CrucibleMold;
import com.gregtech.gregtech.api.addon.GregTechMaterials;
import com.gregtech.gregtech.api.addon.platform.AddonItems;
import com.gregtech.gregtech.api.addon.platform.BehaviorState;
import com.gregtech.gregtech.api.addon.platform.BoundPart;
import com.gregtech.gregtech.api.addon.platform.MachineBehaviorContext;
import com.gregtech.gregtech.api.material.GTValues;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Molten pool, heat and blow logic shared by both loaders.
 * Only the argon tank touches loader fluid types, so that part stays in the platform behavior.
 */
public final class AodConverterCore {
    public static final String LID_PART = "aod_lid";
    public static final int ARGON_TANK = 0;
    public static final int CHARGE_SLOT = 0;

    /** Refining temperature from the card. */
    private static final long AOD_K = 1943L;
    /** Molybdenum must melt first, so this is the floor for the 316L row. */
    private static final long MOLYBDENUM_K = 2896L;
    private static final long AMBIENT_K = 293L;
    private static final long KU_PER_GAS_UNIT = 1000L;
    private static final int MB_PER_GAS_UNIT = 100;
    private static final long MAX_GAS_UNITS = 64L;
    private static final long U = GTValues.U;

    private static final String K_TEMP = "temp.k";
    private static final String K_HEAT_CARRY = "heat.carry";
    private static final String K_DRIFT = "drift.ticks";
    private static final String K_KU_CARRY = "ku.carry";
    private static final String K_AIR = "gas.air";
    private static final String K_ARGON = "gas.argon";
    private static final String K_SLAG = "slag.pending";

    private AodConverterCore() {}

    private record Charge(String material, String poolKey, long meltAtK) {}

    private static final List<Charge> CHARGES = List.of(
            new Charge("CarburizedStainlessSteel", "pool.carburized", 0L),
            new Charge("Ferrosilicon", "pool.ferrosilicon", 0L),
            new Charge("Quicklime", "pool.quicklime", AOD_K),
            new Charge("Fluorite", "pool.fluorite", AOD_K),
            new Charge("Molybdenum", "pool.molybdenum", 0L));

    private record PourTarget(String material, String poolKey) {}

    private static final List<PourTarget> POUR_ORDER = List.of(
            new PourTarget("StainlessSteel316L", "pool.stainless_316l"),
            new PourTarget("StainlessSteel", "pool.stainless"),
            new PourTarget("CarburizedStainlessSteel", "pool.carburized"),
            new PourTarget("Molybdenum", "pool.molybdenum"),
            new PourTarget("Ferrosilicon", "pool.ferrosilicon"));

    /** The loader half of the argon tank. */
    public interface ArgonTank {
        int availableMillibuckets();

        boolean drainMillibuckets(int amount);
    }

    public static boolean canInsert(MachineBehaviorContext context, int slot, ItemStack stack) {
        return slot == CHARGE_SLOT && chargeOf(stack) != null;
    }

    public static boolean isCharge(ItemStack stack) {
        return chargeOf(stack) != null;
    }

    public static boolean canFill(MachineBehaviorContext context, int tank) {
        return tank == ARGON_TANK;
    }

    public static long temperature(MachineBehaviorContext context) {
        long temperature = context.state().getLong(K_TEMP);
        return temperature > 0 ? temperature : AMBIENT_K;
    }

    public static long contentAmount(MachineBehaviorContext context) {
        BehaviorState state = context.state();
        long amount = 0;
        for (Charge charge : CHARGES) amount += state.getLong(charge.poolKey());
        amount += state.getLong("pool.stainless");
        amount += state.getLong("pool.stainless_316l");
        return amount;
    }

    public static void tick(MachineBehaviorContext context, AodTier tier, ArgonTank argon) {
        BehaviorState state = context.state();
        if (!context.structureFormed()) {
            context.working(false);
            return;
        }
        boolean lidOpen = lidOpen(context);
        long temperature = temperature(context);

        long heat = context.energy();
        if (heat > 0) {
            context.consume(heat);
            long carry = state.getLong(K_HEAT_CARRY) + heat;
            long perKelvin = Math.max(1L, tier.heatPerKelvin());
            temperature += carry / perKelvin;
            state.setLong(K_HEAT_CARRY, carry % perKelvin);
            state.setLong(K_TEMP, temperature);
            context.working(true);
        }

        drift(state, lidOpen);
        temperature = temperature(context);

        long ku = context.energy("KU");
        if (ku > 0) {
            context.consume("KU", ku);
            injectGas(state, ku, lidOpen, argon);
            context.working(true);
        }

        if (meltCharge(context, state, temperature)) context.working(true);
        if (react(context, state, temperature, lidOpen)) context.working(true);
        flushSlag(context, state);

        if (lidOpen || temperature < AOD_K) {
            state.setLong(K_AIR, 0L);
            state.setLong(K_ARGON, 0L);
        }

        long meltDown = tier.meltDownK();
        if (meltDown > 0 && temperature > meltDown) {
            meltDown(context);
            return;
        }

        state.setLong(K_TEMP, temperature);
        context.progress((float) (Math.max(0L, temperature - AMBIENT_K) / (double) (3200L - AMBIENT_K)));
    }

    /** Offers the pool to a faucet or mold, one material at a time. */
    public static boolean pour(MachineBehaviorContext context, CrucibleMold mold, int moldSide) {
        if (mold == null) return false;
        BehaviorState state = context.state();
        long temperature = temperature(context);
        for (PourTarget target : POUR_ORDER) {
            long amount = state.getLong(target.poolKey());
            if (amount <= 0) continue;
            GregTechMaterials.Material material = GregTechMaterials.find(target.material());
            if (!material.valid()) continue;
            long accepted = mold.fillMold(material, amount, temperature, moldSide);
            if (accepted > 0) {
                state.setLong(target.poolKey(), amount - accepted);
                return true;
            }
        }
        return false;
    }

    public static boolean lidOpen(MachineBehaviorContext context) {
        BoundPart lid = lidPart(context);
        if (lid == null) return false;
        var partContext = lid.context();
        return partContext != null && partContext.state().getBoolean("open");
    }

    private static BoundPart lidPart(MachineBehaviorContext context) {
        for (BoundPart part : context.parts()) {
            if (LID_PART.equals(part.name())) return part;
        }
        return null;
    }

    private static void drift(BehaviorState state, boolean lidOpen) {
        int ticks = state.getInt(K_DRIFT) + 1;
        if (ticks < (lidOpen ? 5 : 10)) {
            state.setInt(K_DRIFT, ticks);
            return;
        }
        state.setInt(K_DRIFT, 0);
        long temperature = state.getLong(K_TEMP);
        if (temperature > AMBIENT_K) state.setLong(K_TEMP, temperature - 1L);
    }

    private static void injectGas(BehaviorState state, long ku, boolean lidOpen, ArgonTank argon) {
        long units = ku / KU_PER_GAS_UNIT;
        long carry = state.getLong(K_KU_CARRY) + ku % KU_PER_GAS_UNIT;
        units += carry / KU_PER_GAS_UNIT;
        state.setLong(K_KU_CARRY, carry % KU_PER_GAS_UNIT);
        if (units <= 0) return;

        if (!lidOpen) {
            int available = argon.availableMillibuckets();
            long wanted = Math.min(units, MAX_GAS_UNITS) * MB_PER_GAS_UNIT;
            int use = (int) Math.min(wanted, available);
            use -= use % MB_PER_GAS_UNIT;
            if (use > 0 && argon.drainMillibuckets(use)) {
                int accepted = use / MB_PER_GAS_UNIT;
                state.setLong(K_ARGON, Math.min(MAX_GAS_UNITS, state.getLong(K_ARGON) + accepted));
                units -= accepted;
            }
        }
        if (units > 0) {
            state.setLong(K_AIR, Math.min(MAX_GAS_UNITS, state.getLong(K_AIR) + units));
        }
    }

    private interface SlotSink {
        void set(ItemStack stack);
    }

    /** Melts one item from the menu slot, then from the open lid's top-feed slot. */
    private static boolean meltCharge(MachineBehaviorContext context, BehaviorState state, long temperature) {
        if (meltFrom(state, temperature, context.item(CHARGE_SLOT),
                stack -> context.setItem(CHARGE_SLOT, stack))) {
            return true;
        }
        BoundPart lid = lidPart(context);
        if (lid == null || lid.context() == null || lid.slots() <= 0 || !lidOpen(context)) return false;
        var lidContext = lid.context();
        return meltFrom(state, temperature, lid.item(0), stack -> lidContext.setItem(0, stack));
    }

    private static boolean meltFrom(BehaviorState state, long temperature, ItemStack stack, SlotSink sink) {
        Charge charge = chargeOf(stack);
        if (charge == null) return false;
        if (temperature < meltPoint(charge)) return false;
        long units = unitOf(stack);
        if (units <= 0) return false;
        ItemStack remaining = stack.copy();
        remaining.shrink(1);
        sink.set(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        state.setLong(charge.poolKey(), state.getLong(charge.poolKey()) + units);
        return true;
    }

    private static boolean react(MachineBehaviorContext context, BehaviorState state, long temperature, boolean lidOpen) {
        if (temperature < AOD_K) return false;
        long carburized = state.getLong("pool.carburized");
        long ferrosilicon = state.getLong("pool.ferrosilicon");
        long quicklime = state.getLong("pool.quicklime");
        long fluorite = state.getLong("pool.fluorite");
        long molybdenum = state.getLong("pool.molybdenum");
        long air = state.getLong(K_AIR);
        long argon = state.getLong(K_ARGON);

        if (temperature >= molybdenumMeltK()
                && carburized >= 8L * U && molybdenum >= U
                && ferrosilicon >= U && fluorite >= U && argon >= U) {
            state.setLong("pool.carburized", carburized - 8L * U);
            state.setLong("pool.molybdenum", molybdenum - U);
            state.setLong("pool.ferrosilicon", ferrosilicon - U);
            state.setLong("pool.fluorite", fluorite - U);
            state.setLong(K_ARGON, argon - U);
            state.setLong("pool.stainless_316l", state.getLong("pool.stainless_316l") + 9L * U);
            state.setLong(K_SLAG, state.getLong(K_SLAG) + U);
            return true;
        }

        if (carburized < 9L * U || ferrosilicon < U) return false;

        if (!lidOpen && argon >= U && quicklime >= U && fluorite >= U) {
            state.setLong("pool.carburized", carburized - 9L * U);
            state.setLong("pool.ferrosilicon", ferrosilicon - U);
            state.setLong("pool.quicklime", quicklime - U);
            state.setLong("pool.fluorite", fluorite - U);
            state.setLong(K_ARGON, argon - U);
            state.setLong("pool.stainless", state.getLong("pool.stainless") + 9L * U);
            state.setLong(K_SLAG, state.getLong(K_SLAG) + U);
            return true;
        }

        if (air >= 4L * U && quicklime >= 2L * U) {
            state.setLong("pool.carburized", carburized - 9L * U);
            state.setLong("pool.ferrosilicon", ferrosilicon - U);
            state.setLong("pool.quicklime", quicklime - 2L * U);
            state.setLong(K_AIR, air - 4L * U);
            state.setLong("pool.stainless", state.getLong("pool.stainless") + 5L * U);
            state.setLong(K_SLAG, state.getLong(K_SLAG) + 4L * U);
            return true;
        }
        return false;
    }

    private static void flushSlag(MachineBehaviorContext context, BehaviorState state) {
        long pending = state.getLong(K_SLAG);
        while (pending >= U) {
            int count = (int) Math.min(pending / U, 64L);
            ItemStack slag = AddonItems.stack("dust", "AodSlag", count);
            if (slag.isEmpty()) return;
            ItemStack left = context.insertOutput(slag);
            long accepted = (count - (left.isEmpty() ? 0 : left.getCount())) * U;
            if (accepted <= 0) return;
            pending -= accepted;
            state.setLong(K_SLAG, pending);
            if (accepted < count * U) return;
        }
    }

    private static void meltDown(MachineBehaviorContext context) {
        if (context.level().isClientSide) return;
        for (BoundPart part : context.parts()) {
            var partContext = part.context();
            if (partContext != null) context.level().destroyBlock(partContext.pos(), false);
        }
        context.level().destroyBlock(context.pos(), false);
    }

    private static Charge chargeOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        var composition = AddonItems.composition(stack);
        if (composition.units().size() != 1) return null;
        String material = composition.units().get(0).material();
        for (Charge charge : CHARGES) {
            if (charge.material().equals(material)) return charge;
        }
        return null;
    }

    private static long unitOf(ItemStack stack) {
        var composition = AddonItems.composition(stack);
        return composition.units().isEmpty() ? 0L : composition.units().get(0).amount();
    }

    private static long meltPoint(Charge charge) {
        if (charge.meltAtK() > 0) return charge.meltAtK();
        GregTechMaterials.Material material = GregTechMaterials.find(charge.material());
        return material.valid() && material.meltingPoint() > 0 ? material.meltingPoint() : AOD_K;
    }

    private static long molybdenumMeltK() {
        GregTechMaterials.Material material = GregTechMaterials.find("Molybdenum");
        return material.valid() && material.meltingPoint() > MOLYBDENUM_K
                ? material.meltingPoint() : MOLYBDENUM_K;
    }
}
