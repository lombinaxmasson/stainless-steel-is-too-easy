package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.addon.platform.MachineBehaviorContext;

import java.util.List;

/**
 * Water-cooled copper arc furnace. The copper wall grows its own skull, so the hull never melts;
 * sponge metals and the two hottest AOD alloys only exist as liquid inside this pool.
 */
public final class VarFurnaceCore extends MoltenPool {
    public static final String MACHINE_TYPE = "var_furnace";

    static final long MAX_TEMP_K = 4800L;
    private static final long HEAT_PER_KELVIN = 128L;
    static final int MB_PER_GAS_UNIT = 100;

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

    private static final List<Pool> POURS = List.of(
            new Pool("TantalumHafniumCarbide", "pool.ta4hfc5"),
            new Pool("NiobiumTitanium", "pool.niobium_titanium"),
            new Pool("Hafnium", "pool.hafnium"),
            new Pool("Zirconium", "pool.zirconium"),
            new Pool("Titanium", "pool.titanium"),
            new Pool("Tantalum", "pool.tantalum"),
            new Pool("Niobium", "pool.niobium"));

    private static final List<Pool> SKIMS = List.of(
            new Pool("Carbon", "pool.carbon"),
            new Pool("TitaniumSponge", "pool.titanium_sponge"),
            new Pool("ZirconiumSponge", "pool.zirconium_sponge"),
            new Pool("HafniumSponge", "pool.hafnium_sponge"));

    public static final VarFurnaceCore INSTANCE = new VarFurnaceCore();

    private VarFurnaceCore() {
        super(CHARGES, POURS, SKIMS, MAX_TEMP_K);
    }

    @Override
    public void tick(MachineBehaviorContext context, ArgonTank argon) {
        if (!formed(context)) return;
        BehaviorState state = context.state();
        boolean lidOpen = lidOpen(context);

        boolean heated = heat(context, HEAT_PER_KELVIN, MAX_TEMP_K);
        drift(state, lidOpen);
        long temperature = temperature(state);

        autoArgon(state, lidOpen, temperature, heated, argon);

        if (meltCharge(context, temperature)) context.working(true);
        if (!lidOpen && react(state, temperature)) context.working(true);

        if (lidOpen || temperature <= AMBIENT_K) vent(state);

        context.progress((float) (Math.max(0L, temperature - AMBIENT_K) / (double) (MAX_TEMP_K - AMBIENT_K)));
    }

    /** Keeps the closed, live furnace topped up with argon from the tank. */
    static void autoArgon(BehaviorState state, boolean lidOpen, long temperature, boolean heated, ArgonTank argon) {
        if (lidOpen || (!heated && temperature <= AMBIENT_K)) return;
        long stored = state.getLong(K_ARGON);
        state.setLong(K_ARGON, stored + drawArgon(argon, MAX_GAS_UNITS - stored, MB_PER_GAS_UNIT));
    }

    /** Pools are in GT units; argon is in units of one recipe U, 100 mB each. */
    static boolean react(BehaviorState state, long temperature) {
        long argon = state.getLong(K_ARGON);
        boolean reacted = false;

        long tantalum = state.getLong("pool.tantalum");
        long hafnium = state.getLong("pool.hafnium");
        long carbon = state.getLong("pool.carbon");
        if (temperature >= 4263L && tantalum >= 4L * U && hafnium >= U && carbon >= 5L * U && argon >= 2) {
            state.setLong("pool.tantalum", tantalum - 4L * U);
            state.setLong("pool.hafnium", hafnium - U);
            state.setLong("pool.carbon", carbon - 5L * U);
            argon -= 2;
            state.setLong(K_ARGON, argon);
            state.setLong("pool.ta4hfc5", state.getLong("pool.ta4hfc5") + 10L * U);
            reacted = true;
        }

        long niobium = state.getLong("pool.niobium");
        long titanium = state.getLong("pool.titanium");
        if (temperature >= 2750L && niobium >= U && titanium >= U && argon >= 1) {
            state.setLong("pool.niobium", niobium - U);
            state.setLong("pool.titanium", titanium - U);
            argon -= 1;
            state.setLong(K_ARGON, argon);
            state.setLong("pool.niobium_titanium", state.getLong("pool.niobium_titanium") + 2L * U);
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
        if (sponge < U || argon < 1) return false;
        state.setLong(spongePool, sponge - U);
        state.setLong(metalPool, state.getLong(metalPool) + U);
        state.setLong(K_ARGON, argon - 1);
        return true;
    }
}
