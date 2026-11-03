package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.worldgen.GasFieldPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side per-tick budget shared by every extractor on one gas field. The model has no
 * drainable reserve, so only the current tick's usage is kept; a restart cannot duplicate gas.
 */
public final class GasFieldLedger {

    /** Bump when the budget key or accounting semantics change. */
    public static final int FORMAT = 1;

    private static final int MAX_ENTRIES = 256;
    private static final Map<String, Entry> LEDGERS = new HashMap<>();

    private GasFieldLedger() {}

    public static boolean request(Level level, BlockPos pos, int amount) {
        GasFieldPlan plan = GasFieldPlan.at(level, pos);
        if (plan == null) return false;
        return request(level.dimension().location().toString(), level.getGameTime(), plan, amount);
    }

    public static int remaining(Level level, BlockPos pos) {
        GasFieldPlan plan = GasFieldPlan.at(level, pos);
        if (plan == null) return 0;
        String key = key(level.dimension().location().toString(), plan);
        Entry entry = LEDGERS.get(key);
        if (entry == null || entry.tick != level.getGameTime()) return plan.quotaPerTick();
        return Math.max(0, plan.quotaPerTick() - entry.used);
    }

    /** Pure overload used by tests; {@code dimension} may be any stable field namespace. */
    public static boolean request(String dimension, long gameTime, GasFieldPlan plan, int amount) {
        if (plan == null) return false;
        if (amount <= 0) return true;
        String key = key(dimension, plan);
        Entry entry = LEDGERS.get(key);
        if (entry == null || entry.tick != gameTime) {
            entry = new Entry(gameTime);
            LEDGERS.put(key, entry);
        }
        if (entry.used + amount > plan.quotaPerTick()) return false;
        entry.used += amount;
        prune(gameTime);
        return true;
    }

    public static int used(String dimension, long gameTime, GasFieldPlan plan) {
        Entry entry = LEDGERS.get(key(dimension, plan));
        return entry == null || entry.tick != gameTime ? 0 : entry.used;
    }

    public static void reset() {
        LEDGERS.clear();
    }

    private static String key(String dimension, GasFieldPlan plan) {
        return dimension + "@" + plan.anchorChunkX() + "," + plan.anchorChunkZ();
    }

    private static void prune(long gameTime) {
        if (LEDGERS.size() <= MAX_ENTRIES) return;
        LEDGERS.entrySet().removeIf(entry -> gameTime - entry.getValue().tick > 40);
    }

    private static final class Entry {
        private final long tick;
        private int used;

        private Entry(long tick) {
            this.tick = tick;
        }
    }
}
