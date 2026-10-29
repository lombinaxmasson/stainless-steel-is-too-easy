package com.lombinaxmasson.ssite.machine;

import gregtech.api.addon.platform.BehaviorState;
import gregtech.api.material.GTValues;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Pure-JVM checks for {@link AodConverterCore}, {@link VarFurnaceCore} and {@link MoltenPool}.
 * Invoked by Gradle's {@code unitTest} task with assertions enabled.
 */
public final class MoltenPoolTests {
    private static final long U = GTValues.U;

    private MoltenPoolTests() {}

    public static void main(String[] args) {
        everyPoolHasAnOutlet();
        aodAirBlow();
        aodArgonBlow();
        aod316LRequiresMolybdenumHeat();
        aodDoesNotDrainArgonWhileWarmingUp();
        varSpongeAndAlloys();
        unformedCrucibleStillCoolsAndVents();
        overtemperatureAndResidualHeat();
        redstoneOnlyMovesTheLid();
        largeGasNeedsOxygen();
        largeLayoutMatchesPorts();
        inspectionReportsPools();
        System.out.println("MoltenPoolTests: all 12 checks passed.");
    }

    private static void inspectionReportsPools() {
        State state = new State();
        state.setLong("pool.titanium", 2 * U);
        state.setLong("pool.carbon", U);
        state.setLong("pool.niobium_titanium", 3 * U);
        state.setLong(MoltenPool.K_ARGON, 4);
        var rows = VarFurnaceCore.INSTANCE.contents(state);
        check(rows.size() == 3, "only non-empty pools are inspected");
        check(rows.get(0).material().equals("NiobiumTitanium"), "products are listed first");
        check(rows.get(1).material().equals("Titanium"), "the pour order is kept");
        check(rows.get(2).material().equals("Carbon"), "skimmed leftovers close the list without duplicates");
        check(VarFurnaceCore.INSTANCE.argon(state) == 4, "argon units are inspected");
        check(VarFurnaceCore.INSTANCE.air(state) == 0, "the VAR has no air buffer");
        check(VarFurnaceCore.INSTANCE.temperatureLimitK() == VarFurnaceCore.MAX_TEMP_K, "VAR reports its limit");
    }

    private static void overtemperatureAndResidualHeat() {
        for (boolean open : new boolean[]{false, true}) {
            check(MoltenPool.failure(4800, 4800, open) == MoltenPool.Failure.NONE, "safe at threshold");
            check(MoltenPool.failure(4801, 4800, open) == (open ? MoltenPool.Failure.MELTDOWN : MoltenPool.Failure.EXPLOSION),
                    "overtemperature follows actual lid state");
        }
        State state = new State();
        state.setLong(MoltenPool.K_TEMP, 4800);
        MoltenPool.addHeat(state, 128, 128, Long.MAX_VALUE);
        check(state.getLong(MoltenPool.K_TEMP) == 4801, "VAR heat is not clamped to its failure limit");
        MoltenPool.addHeat(state, Long.MAX_VALUE, 1, Long.MAX_VALUE);
        check(state.getLong(MoltenPool.K_TEMP) == Long.MAX_VALUE, "heat cannot wrap to a cold temperature");
        State carry = new State();
        MoltenPool.addHeat(carry, 127, 128, Long.MAX_VALUE);
        check(MoltenPool.temperature(carry) == 293, "fractional HU retained");
        MoltenPool.addHeat(carry, 1, 128, Long.MAX_VALUE);
        check(MoltenPool.temperature(carry) == 294, "retained HU becomes heat");
        State melt = new State();
        melt.setLong("pool.titanium_sponge", U);
        melt.setLong(MoltenPool.K_ARGON, 1);
        check(VarFurnaceCore.react(melt, 2000), "warm pool reacts with no new energy or enable flag");
        State aod = new State();
        aod.setLong("pool.carburized", 8 * U); aod.setLong("pool.molybdenum", U);
        aod.setLong("pool.ferrosilicon", U); aod.setLong("pool.fluorite", U); aod.setLong(MoltenPool.K_ARGON, 1);
        check(!AodConverterCore.react(aod, 3000, true, 2896), "open lid cannot use stale argon for 316L");
    }

    private static void redstoneOnlyMovesTheLid() {
        for (boolean signal : new boolean[]{false, true}) for (boolean manual : new boolean[]{false, true}) {
            check(SsiteLidPart.LidBehavior.redstoneOpen(0, signal, manual) == manual, "manual mode ignores redstone");
            check(SsiteLidPart.LidBehavior.redstoneOpen(1, signal, manual) == signal, "high signal opens");
            check(SsiteLidPart.LidBehavior.redstoneOpen(2, signal, manual) != signal, "low signal opens");
        }
        for (var machine : new SsiteMachines().addonMachines())
            if (machine instanceof gregtech.api.addon.machine.BehaviorMachine behavior
                    && !behavior.casing().machineType().equals(FluidizedBedCore.MACHINE_TYPE))
                check(behavior.toggles().isEmpty(), "furnaces have no processing enable switch");
    }

    private static void largeGasNeedsOxygen() {
        State state = new State();
        Gases gas = new Gases(10, 0);
        LargeAodCore.supply(state, gas, false, 2000);
        check(gas.argon == 10 && state.getLong(MoltenPool.K_ARGON) == 0, "argon alone cannot blow large AOD");
        gas.oxygen = 10;
        LargeAodCore.supply(state, gas, false, 1200);
        check(gas.argon == 10 && gas.oxygen == 10, "cold furnace preserves both gases");
        LargeAodCore.supply(state, gas, false, 2000);
        check(gas.argon == 0 && gas.oxygen == 0 && state.getLong(MoltenPool.K_ARGON) == 1, "closed mixture consumes both gases");
        state = new State(); gas = new Gases(100, 40);
        LargeAodCore.supply(state, gas, true, 2000);
        check(gas.argon == 100 && gas.oxygen == 0 && state.getLong(AodConverterCore.K_AIR) == 4, "open oxygen route preserves tank argon");
    }

    private static void largeLayoutMatchesPorts() {
        for (AodTier tier : AodTier.values()) {
            var layout = LargeAodStructure.layout(tier);
            long heat = layout.cells().stream().filter(c -> c.role() == gregtech.api.multiblock.StructureRole.ENERGY_INPUT).count();
            check(heat == 9, "all nine HU cells accept energy");
            check(layout.cells().stream().filter(c -> c.match().toString().equals(tier.largeCasing())).count() == 21,
                    "twelve wall cells and nine sole cells share the tier casing");
            for (var cell : layout.cells()) if (cell.role() == gregtech.api.multiblock.StructureRole.ENERGY_INPUT)
                check(cell.up() == -1 && Math.abs(cell.right()) <= 1 && cell.back() >= 1 && cell.back() <= 3, "3x3 sole under vessel");
            check(layout.cells().stream().anyMatch(c -> c.right() == 2 && c.up() == 0 && c.back() == 1
                    && c.match().toString().equals(LargeAodStructure.OXYGEN)), "O2 at model owner (2,1,-1)");
            check(layout.cells().stream().anyMatch(c -> c.right() == 0 && c.up() == 3 && c.back() == 2
                    && c.match().toString().equals(LargeAodStructure.LID)), "feed at model owner (0,4,0)");
        }
        check(new SsiteMachines().layoutParts().stream().noneMatch(p -> p.name().equals("large_aod_heat")),
                "dedicated heat block is no longer registered");
    }

    private static final class Gases implements LargeAodCore.GasSupply {
        int argon, oxygen;
        Gases(int argon, int oxygen) { this.argon = argon; this.oxygen = oxygen; }
        public int availableMillibuckets() { return argon; }
        public int oxygenMillibuckets() { return oxygen; }
        public boolean drainMillibuckets(int amount) { if (argon < amount) return false; argon -= amount; return true; }
        public boolean drainOxygen(int amount) { if (oxygen < amount) return false; oxygen -= amount; return true; }
    }

    private static void everyPoolHasAnOutlet() {
        for (AodTier tier : AodTier.values()) new AodConverterCore(tier);
        check(VarFurnaceCore.INSTANCE != null, "VAR pool constructed");
    }

    private static void aodAirBlow() {
        State state = new State();
        state.setLong("pool.carburized", 9L * U);
        state.setLong("pool.ferrosilicon", U);
        state.setLong("pool.quicklime", 2L * U);
        Tank tank = new Tank(0);
        AodConverterCore.injectGas(state, 4000L, true, AodConverterCore.AOD_K, tank);
        check(state.getLong(AodConverterCore.K_AIR) == 4L, "4000 KU blows 4 air units");
        check(AodConverterCore.react(state, AodConverterCore.AOD_K, true, AodConverterCore.MOLYBDENUM_K),
                "air blow reacts at 1943 K");
        check(state.getLong("pool.stainless") == 5L * U, "air blow yields 5 U stainless steel");
        check(state.getLong(AodConverterCore.K_SLAG) == 4L * U, "air blow queues 4 U slag");
        check(state.getLong(AodConverterCore.K_AIR) == 0L, "air units are spent");
    }

    private static void aodArgonBlow() {
        State state = new State();
        state.setLong("pool.carburized", 9L * U);
        state.setLong("pool.ferrosilicon", U);
        state.setLong("pool.quicklime", U);
        state.setLong("pool.fluorite", U);
        Tank tank = new Tank(500);
        AodConverterCore.injectGas(state, 1000L, false, AodConverterCore.AOD_K, tank);
        check(tank.mb == 490 && state.getLong(MoltenPool.K_ARGON) == 1L, "1000 KU draws 10 mB = 1 AOD argon unit");
        check(AodConverterCore.react(state, AodConverterCore.AOD_K, false, AodConverterCore.MOLYBDENUM_K),
                "closed argon blow reacts with 1 argon unit");
        check(state.getLong("pool.stainless") == 9L * U, "argon blow yields 9 U stainless steel");
        check(state.getLong(AodConverterCore.K_SLAG) == U, "argon blow queues 1 U slag");
        check(state.getLong(MoltenPool.K_ARGON) == 0L, "argon unit is spent");
    }

    private static void aod316LRequiresMolybdenumHeat() {
        State state = new State();
        state.setLong("pool.carburized", 8L * U);
        state.setLong("pool.molybdenum", U);
        state.setLong("pool.ferrosilicon", U);
        state.setLong("pool.fluorite", U);
        state.setLong(MoltenPool.K_ARGON, 1L);
        check(!AodConverterCore.react(state, 2800L, false, AodConverterCore.MOLYBDENUM_K),
                "316L waits for molybdenum melting point");
        check(AodConverterCore.react(state, AodConverterCore.MOLYBDENUM_K, false, AodConverterCore.MOLYBDENUM_K),
                "316L reacts at 2896 K");
        check(state.getLong("pool.stainless_316l") == 9L * U, "316L yields 9 U");
        check(state.getLong(MoltenPool.K_ARGON) == 0L, "316L spends 1 argon unit");
    }

    private static void aodDoesNotDrainArgonWhileWarmingUp() {
        State state = new State();
        Tank tank = new Tank(1000);
        AodConverterCore.injectGas(state, 2000L, false, 1200L, tank);
        check(tank.mb == 1000, "argon tank untouched below 1943 K");
        check(state.getLong(MoltenPool.K_ARGON) == 0L, "no argon stored below 1943 K");
        check(state.getLong(AodConverterCore.K_AIR) == 2L, "KU blows air while warming up");
    }

    private static void varSpongeAndAlloys() {
        State state = new State();
        state.setLong("pool.titanium_sponge", U);
        state.setLong("pool.niobium", U);
        state.setLong("pool.tantalum", 4L * U);
        state.setLong("pool.hafnium", U);
        state.setLong("pool.carbon", 5L * U);
        Tank tank = new Tank(400);
        VarFurnaceCore.autoArgon(state, false, 4300L, true, tank);
        check(tank.mb == 0 && state.getLong(MoltenPool.K_ARGON) == 4L, "autoArgon draws 4 units from 400 mB");
        check(VarFurnaceCore.react(state, 4300L), "VAR reacts sponge and Ta4HfC5");
        check(state.getLong("pool.ta4hfc5") == 10L * U, "Ta4HfC5 yields 10 U");
        // Sponge remelts after the NbTi check in the same tick, so the fresh Ti waits one tick for NbTi.
        check(state.getLong("pool.titanium") == U, "sponge remelts to 1 U liquid titanium");
        check(VarFurnaceCore.react(state, 4300L), "second tick alloys NbTi with remaining argon");
        check(state.getLong("pool.niobium_titanium") == 2L * U, "NbTi yields 2 U");
        check(state.getLong(MoltenPool.K_ARGON) == 0L, "all 4 argon units spent across Ta4HfC5(2) + sponge(1) + NbTi(1)");
    }

    private static void unformedCrucibleStillCoolsAndVents() {
        State state = new State();
        state.setLong(MoltenPool.K_TEMP, 2000L);
        state.setLong(MoltenPool.K_ARGON, 5L);
        state.setLong(AodConverterCore.K_AIR, 3L);
        AodConverterCore core = new AodConverterCore(AodTier.STEEL);
        for (int i = 0; i < 5; i++) core.unformed(state);
        check(state.getLong(MoltenPool.K_TEMP) == 1999L, "missing lid cools at open-lid rate (1 K per 5 ticks)");
        check(state.getLong(MoltenPool.K_ARGON) == 0L, "missing lid vents stored argon");
        check(state.getLong(AodConverterCore.K_AIR) == 0L, "missing lid vents stored air");
    }

    private static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError("Failed: " + what);
    }

    private static final class Tank implements MoltenPool.ArgonTank {
        int mb;

        Tank(int mb) {
            this.mb = mb;
        }

        @Override
        public int availableMillibuckets() {
            return mb;
        }

        @Override
        public boolean drainMillibuckets(int amount) {
            if (amount <= 0 || mb < amount) return false;
            mb -= amount;
            return true;
        }
    }

    private static final class State implements BehaviorState {
        private final Map<String, Long> longs = new HashMap<>();
        private final Map<String, Integer> ints = new HashMap<>();
        private final Map<String, Boolean> bools = new HashMap<>();
        private final Map<String, String> strings = new HashMap<>();

        @Override public int getInt(String k) { return ints.getOrDefault(k, 0); }
        @Override public void setInt(String k, int v) { ints.put(k, v); }
        @Override public long getLong(String k) { return longs.getOrDefault(k, 0L); }
        @Override public void setLong(String k, long v) { longs.put(k, v); }
        @Override public boolean getBoolean(String k) { return bools.getOrDefault(k, false); }
        @Override public void setBoolean(String k, boolean v) { bools.put(k, v); }
        @Override public String getString(String k) { return strings.getOrDefault(k, ""); }
        @Override public void setString(String k, String v) { strings.put(k, v); }
        @Override public ItemStack item(String k) { return null; }
        @Override public void setItem(String k, ItemStack s) {}
    }
}
