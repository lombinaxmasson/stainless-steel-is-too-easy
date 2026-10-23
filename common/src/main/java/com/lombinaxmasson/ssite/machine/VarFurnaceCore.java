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
 * Water-cooled copper arc furnace. The copper wall grows its own skull, so the hull never melts;
 * sponge metals and the two hottest AOD alloys only exist as liquid inside this pool.
 */
public final class VarFurnaceCore {
    public static final String MACHINE_TYPE = "var_furnace";
    public static final int ARGON_TANK = 0;
    public static final int CHARGE_SLOT = 0;

    private static final long AMBIENT_K = 293L;
    private static final long MAX_TEMP_K = 4800L;
    private static final long HEAT_PER_KELVIN = 128L;
    private static final long U = GTValues.U;
    private static final long MAX_GAS_UNITS = 64L;
    private static final int MB_PER_GAS_UNIT = 100;

    private static final String K_TEMP = "temp.k";
    private static final String K_HEAT_CARRY = "heat.carry";
    private static final String K_DRIFT = "drift.ticks";
    private static final String K_ARGON = "gas.argon";

    private VarFurnaceCore() {}

    private record Charge(String material, String poolKey, long meltAtK) {}

    private static final List<Charge> CHARGES = List.of(
            new Charge("TitaniumSponge", "pool.titanium_sponge", 1941L),
            new Charge("ZirconiumSponge", "pool.zirconium_sponge", 2128L),
            new Charge("HafniumSponge", "pool.hafnium_sponge", 2506L),
            new Charge("Titanium", "pool.titanium", 1941L),
            new Charge("Zirconium", "pool.zirconium", 2128L),
            new Charge("Hafnium", "pool.hafnium", 2506L),
            new Charge("Niobium", "pool.niobium", 2750L),
            new Charge("Tantalum", "pool.tantalum", 3290L),
            new Charge("Carbon", "pool.carbon", 3800L));

    private record PourTarget(String material, String poolKey) {}

    private static final List<PourTarget> POUR_ORDER = List.of(
            new PourTarget("TantalumHafniumCarbide", "pool.ta4hfc5"),
            new PourTarget("NiobiumTitanium", "pool.niobium_titanium"),
            new PourTarget("Hafnium", "pool.hafnium"),
            new PourTarget("Zirconium", "pool.zirconium"),
            new PourTarget("Titanium", "pool.titanium"),
            new PourTarget("Tantalum", "pool.tantalum"),
            new PourTarget("Niobium", "pool.niobium"));

    private static final String[] PRODUCT_POOLS = {"pool.niobium_titanium", "pool.ta4hfc5"};

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
        for (String pool : PRODUCT_POOLS) amount += state.getLong(pool);
        return amount;
    }

    public static void tick(MachineBehaviorContext context, ArgonTank argon) {
        BehaviorState state = context.state();
        if (!context.structureFormed()) {
            context.working(false);
            return;
        }
        boolean lidOpen = lidOpen(context);
        long temperature = temperature(context);

        boolean heated = false;
        long heat = context.energy();
        if (heat > 0) {
            context.consume(heat);
            long carry = state.getLong(K_HEAT_CARRY) + heat;
            temperature = Math.min(MAX_TEMP_K, temperature + carry / HEAT_PER_KELVIN);
            state.setLong(K_HEAT_CARRY, carry % HEAT_PER_KELVIN);
            state.setLong(K_TEMP, temperature);
            context.working(true);
            heated = true;
        }

        drift(state, lidOpen);
        temperature = temperature(context);

        autoArgon(state, lidOpen, temperature, heated, argon);

        if (meltCharge(context, state, temperature)) context.working(true);
        if (!lidOpen && react(state, temperature)) context.working(true);

        if (lidOpen || temperature <= AMBIENT_K) state.setLong(K_ARGON, 0L);

        state.setLong(K_TEMP, temperature);
        context.progress((float) (Math.max(0L, temperature - AMBIENT_K) / (double) (MAX_TEMP_K - AMBIENT_K)));
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
            if (AodConverterCore.LID_PART.equals(part.name())) return part;
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

    private static void autoArgon(BehaviorState state, boolean lidOpen, long temperature,
                                  boolean heated, ArgonTank argon) {
        if (lidOpen || (!heated && temperature <= AMBIENT_K)) return;
        long stored = state.getLong(K_ARGON);
        if (stored >= MAX_GAS_UNITS) return;
        int wanted = (int) ((MAX_GAS_UNITS - stored) * MB_PER_GAS_UNIT);
        int use = Math.min(wanted, argon.availableMillibuckets());
        use -= use % MB_PER_GAS_UNIT;
        if (use <= 0) return;
        if (argon.drainMillibuckets(use)) {
            state.setLong(K_ARGON, stored + use / MB_PER_GAS_UNIT);
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

    private static boolean react(BehaviorState state, long temperature) {
        long argon = state.getLong(K_ARGON);
        boolean reacted = false;

        long tantalum = state.getLong("pool.tantalum");
        long hafnium = state.getLong("pool.hafnium");
        long carbon = state.getLong("pool.carbon");
        if (temperature >= 4263L && tantalum >= 4L * U && hafnium >= U && carbon >= 5L * U && argon >= 2L * U) {
            state.setLong("pool.tantalum", tantalum - 4L * U);
            state.setLong("pool.hafnium", hafnium - U);
            state.setLong("pool.carbon", carbon - 5L * U);
            state.setLong(K_ARGON, argon - 2L * U);
            state.setLong("pool.ta4hfc5", state.getLong("pool.ta4hfc5") + 10L * U);
            argon -= 2L * U;
            reacted = true;
        }

        long niobium = state.getLong("pool.niobium");
        long titanium = state.getLong("pool.titanium");
        if (temperature >= 2750L && niobium >= U && titanium >= U && argon >= U) {
            state.setLong("pool.niobium", niobium - U);
            state.setLong("pool.titanium", titanium - U);
            state.setLong(K_ARGON, argon - U);
            state.setLong("pool.niobium_titanium", state.getLong("pool.niobium_titanium") + 2L * U);
            argon -= U;
            reacted = true;
        }

        reacted |= remelt(state, "pool.titanium_sponge", "pool.titanium", 1941L, temperature);
        reacted |= remelt(state, "pool.zirconium_sponge", "pool.zirconium", 2128L, temperature);
        reacted |= remelt(state, "pool.hafnium_sponge", "pool.hafnium", 2506L, temperature);
        return reacted;
    }

    private static boolean remelt(BehaviorState state, String spongePool, String metalPool,
                                  long meltAtK, long temperature) {
        if (temperature < meltAtK) return false;
        long sponge = state.getLong(spongePool);
        long argon = state.getLong(K_ARGON);
        if (sponge < U || argon < U) return false;
        state.setLong(spongePool, sponge - U);
        state.setLong(metalPool, state.getLong(metalPool) + U);
        state.setLong(K_ARGON, argon - U);
        return true;
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
        return material.valid() && material.meltingPoint() > 0 ? material.meltingPoint() : MAX_TEMP_K;
    }
}
