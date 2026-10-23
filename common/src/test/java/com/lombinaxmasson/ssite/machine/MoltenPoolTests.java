package com.lombinaxmasson.ssite.machine;

import com.gregtech.gregtech.api.addon.platform.BehaviorState;
import com.gregtech.gregtech.api.material.GTValues;
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
        System.out.println("MoltenPoolTests: all 7 checks passed.");
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
        check(tank.mb == 400 && state.getLong(MoltenPool.K_ARGON) == 1L, "1000 KU draws 100 mB = 1 argon unit");
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
