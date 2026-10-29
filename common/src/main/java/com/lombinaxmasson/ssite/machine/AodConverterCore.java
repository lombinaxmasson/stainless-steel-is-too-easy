package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.material.GregTechMaterials;
import gregtech.api.addon.platform.AddonItems;
import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** AOD converter pool: hull heat capacity and melt-down from {@link AodTier}, KU blowing and the three refining rows. */
public class AodConverterCore extends MoltenPool {
    /** Refining temperature from the card. */
    static final long AOD_K = 1943L;
    /** Molybdenum must melt first, so this is the floor for the 316L row. */
    static final long MOLYBDENUM_K = 2896L;
    static final long KU_PER_GAS_UNIT = 1000L;
    /** AOD dilution gas is cheap: one unit per batch of 9 is 10 mB. */
    static final int MB_PER_GAS_UNIT = 10;

    static final String K_KU_CARRY = "ku.carry";
    static final String K_AIR = "gas.air";
    static final String K_SLAG = "slag.pending";

    private static final List<Charge> CHARGES = List.of(
            new Charge("CarburizedStainlessSteel", "pool.carburized", 0L),
            new Charge("Ferrosilicon", "pool.ferrosilicon", 0L),
            new Charge("Quicklime", "pool.quicklime", AOD_K),
            new Charge("Fluorite", "pool.fluorite", AOD_K),
            new Charge("Molybdenum", "pool.molybdenum", 0L));

    private static final List<Pool> POURS = List.of(
            new Pool("StainlessSteel316L", "pool.stainless_316l"),
            new Pool("StainlessSteel", "pool.stainless"),
            new Pool("CarburizedStainlessSteel", "pool.carburized"),
            new Pool("Molybdenum", "pool.molybdenum"),
            new Pool("Ferrosilicon", "pool.ferrosilicon"));

    private static final List<Pool> SKIMS = List.of(
            new Pool("Quicklime", "pool.quicklime"),
            new Pool("Fluorite", "pool.fluorite"));

    private static volatile long molybdenumMeltK;

    protected final AodTier tier;

    public AodConverterCore(AodTier tier) {
        super(CHARGES, POURS, SKIMS, AOD_K);
        this.tier = tier;
    }

    @Override
    protected void vent(BehaviorState state) {
        super.vent(state);
        state.setLong(K_AIR, 0L);
    }

    @Override public long air(BehaviorState state) { return state.getLong(K_AIR); }

    @Override public long temperatureLimitK() { return tier.meltDownK(); }

    @Override
    public void tick(MachineBehaviorContext context, ArgonTank argon) {
        if (overheat(context, tier.meltDownK())) return;
        if (!formed(context)) return;
        BehaviorState state = context.state();
        boolean lidOpen = lidOpen(context);

        heat(context, heatPerKelvin(), Long.MAX_VALUE);
        if (overheat(context, tier.meltDownK())) return;
        drift(state, lidOpen);
        long temperature = temperature(state);

        supplyGas(context, argon, lidOpen, temperature);

        if (meltCharge(context, temperature)) context.working(true);
        if (refine(state, temperature, lidOpen)) context.working(true);
        flushSlag(context, state);

        if (lidOpen || temperature < AOD_K) vent(state);

        context.setProgress((float) Math.min(1, Math.max(0L, temperature - AMBIENT_K) / (double) (3200L - AMBIENT_K)));
    }

    protected long heatPerKelvin() { return tier.heatPerKelvin(); }

    protected void supplyGas(MachineBehaviorContext context, ArgonTank argon, boolean open, long temperature) {
        long ku = context.auxiliaryStored("KU");
        if (ku > 0 && context.consume("KU", ku)) {
            injectGas(context.state(), ku, open, temperature, argon);
            context.working(true);
        }
    }

    protected boolean refine(BehaviorState state, long temperature, boolean open) {
        return react(state, temperature, open, molybdenumMeltK());
    }

    /**
     * Every 1000 KU blows one gas unit. With the lid closed at refining heat the unit comes from
     * the argon tank when it has 10 mB; otherwise it is air. Below refining heat the argon stays in the tank.
     */
    static void injectGas(BehaviorState state, long ku, boolean lidOpen, long temperature, ArgonTank argon) {
        long units = ku / KU_PER_GAS_UNIT;
        long carry = state.getLong(K_KU_CARRY) + ku % KU_PER_GAS_UNIT;
        units += carry / KU_PER_GAS_UNIT;
        state.setLong(K_KU_CARRY, carry % KU_PER_GAS_UNIT);
        if (units <= 0) return;

        if (!lidOpen && temperature >= AOD_K) {
            long room = MAX_GAS_UNITS - state.getLong(K_ARGON);
            long drawn = drawArgon(argon, Math.min(units, room), MB_PER_GAS_UNIT);
            state.setLong(K_ARGON, state.getLong(K_ARGON) + drawn);
            units -= drawn;
        }
        if (units > 0) {
            state.setLong(K_AIR, Math.min(MAX_GAS_UNITS, state.getLong(K_AIR) + units));
        }
    }

    /** One batch per call. Pools are in GT units; gas is in units of one recipe U. */
    static boolean react(BehaviorState state, long temperature, boolean lidOpen, long molybdenumK) {
        if (temperature < AOD_K) return false;
        long carburized = state.getLong("pool.carburized");
        long ferrosilicon = state.getLong("pool.ferrosilicon");
        long quicklime = state.getLong("pool.quicklime");
        long fluorite = state.getLong("pool.fluorite");
        long molybdenum = state.getLong("pool.molybdenum");
        long air = state.getLong(K_AIR);
        long argon = state.getLong(K_ARGON);

        if (!lidOpen && temperature >= molybdenumK
                && carburized >= 8L * U && molybdenum >= U
                && ferrosilicon >= U && fluorite >= U && argon >= 1) {
            state.setLong("pool.carburized", carburized - 8L * U);
            state.setLong("pool.molybdenum", molybdenum - U);
            state.setLong("pool.ferrosilicon", ferrosilicon - U);
            state.setLong("pool.fluorite", fluorite - U);
            state.setLong(K_ARGON, argon - 1);
            state.setLong("pool.stainless_316l", state.getLong("pool.stainless_316l") + 9L * U);
            state.setLong(K_SLAG, state.getLong(K_SLAG) + U);
            return true;
        }

        if (carburized < 9L * U || ferrosilicon < U) return false;

        if (!lidOpen && argon >= 1 && quicklime >= U && fluorite >= U) {
            state.setLong("pool.carburized", carburized - 9L * U);
            state.setLong("pool.ferrosilicon", ferrosilicon - U);
            state.setLong("pool.quicklime", quicklime - U);
            state.setLong("pool.fluorite", fluorite - U);
            state.setLong(K_ARGON, argon - 1);
            state.setLong("pool.stainless", state.getLong("pool.stainless") + 9L * U);
            state.setLong(K_SLAG, state.getLong(K_SLAG) + U);
            return true;
        }

        if (air >= 4 && quicklime >= 2L * U) {
            state.setLong("pool.carburized", carburized - 9L * U);
            state.setLong("pool.ferrosilicon", ferrosilicon - U);
            state.setLong("pool.quicklime", quicklime - 2L * U);
            state.setLong(K_AIR, air - 4);
            state.setLong("pool.stainless", state.getLong("pool.stainless") + 5L * U);
            state.setLong(K_SLAG, state.getLong(K_SLAG) + 4L * U);
            return true;
        }
        return false;
    }

    protected void flushSlag(MachineBehaviorContext context, BehaviorState state) {
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

    private static long molybdenumMeltK() {
        if (molybdenumMeltK > 0) return molybdenumMeltK;
        GregTechMaterials.Material material = material("Molybdenum");
        if (!material.valid()) return MOLYBDENUM_K;
        molybdenumMeltK = Math.max(material.meltingPoint(), MOLYBDENUM_K);
        return molybdenumMeltK;
    }
}
