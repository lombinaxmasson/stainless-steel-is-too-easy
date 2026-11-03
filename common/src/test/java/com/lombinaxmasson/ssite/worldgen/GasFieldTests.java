package com.lombinaxmasson.ssite.worldgen;

import com.lombinaxmasson.ssite.machine.GasFieldExtractorCore;
import com.lombinaxmasson.ssite.machine.GasFieldLedger;

/** Pure checks for the deterministic field layout and the shared per-tick extraction budget. */
public final class GasFieldTests {

    private GasFieldTests() {}

    public static void main(String[] args) {
        deterministicPlan();
        chunksShareOnePlan();
        springGridMatchesCount();
        planFitsOwnRegion();
        ledgerSharedAcrossExtractors();
        ledgerResetsEachTick();
        ledgerSeparatesFields();
        extractorWantedIsCapped();
        System.out.println("GasFieldTests: all checks passed");
    }

    private static void deterministicPlan() {
        GasFieldPlan plan = findPlan(0L);
        check(plan != null, "expected a field for the chosen seed");
        check(plan.equals(GasFieldPlan.at(0L, plan.anchorChunkX(), plan.anchorChunkZ())),
                "same seed and anchor must reproduce the plan");
    }

    private static void chunksShareOnePlan() {
        GasFieldPlan plan = findPlan(0L);
        for (int dx = 0; dx < GasFieldPlan.REGION_CHUNKS; dx++) {
            for (int dz = 0; dz < GasFieldPlan.REGION_CHUNKS; dz++) {
                GasFieldPlan other = GasFieldPlan.at(0L, plan.anchorChunkX() + dx, plan.anchorChunkZ() + dz);
                check(plan.equals(other), "every chunk in the field region must share one plan");
            }
        }
    }

    private static void springGridMatchesCount() {
        GasFieldPlan plan = findPlan(0L);
        int counted = 0;
        for (int x = plan.minX(); x <= plan.maxX(); x++) {
            for (int z = plan.minZ(); z <= plan.maxZ(); z++) {
                for (int y = plan.minY(); y <= plan.maxY(); y++) {
                    if (plan.isSpring(x, y, z)) counted++;
                }
            }
        }
        check(counted == plan.springCount(), "spring grid and advertised count must agree");
        check(plan.springCount() >= 100, "a field must contain at least a hundred spring cells");
        check(plan.quotaPerTick() >= 1, "field quota must be positive");
        check(plan.quotaPerTick() == Math.max(1, plan.springCount() * GasFieldPlan.FLUID_PER_BLOCK
                / GasFieldPlan.HOST_SPRING_ROLL), "quota must follow the host spring rate");
    }

    private static void planFitsOwnRegion() {
        GasFieldPlan plan = findPlan(0L);
        int anchorX = plan.anchorChunkX() << 4, anchorZ = plan.anchorChunkZ() << 4;
        check(plan.minX() >= anchorX && plan.maxX() < anchorX + GasFieldPlan.REGION_BLOCKS,
                "field must stay inside its region on X");
        check(plan.minZ() >= anchorZ && plan.maxZ() < anchorZ + GasFieldPlan.REGION_BLOCKS,
                "field must stay inside its region on Z");
        check(plan.minY() >= -64 && plan.maxY() < 0, "field must stay underground");
        check(plan.ownsChunk(plan.anchorChunkX(), plan.anchorChunkZ())
                && !plan.ownsChunk(plan.anchorChunkX() + GasFieldPlan.REGION_CHUNKS, plan.anchorChunkZ()),
                "field ownership must stop at the region edge");
    }

    private static void ledgerSharedAcrossExtractors() {
        GasFieldLedger.reset();
        GasFieldPlan plan = findPlan(0L);
        int quota = plan.quotaPerTick();
        int first = Math.max(1, quota / 2);
        check(GasFieldLedger.request("minecraft:overworld", 100L, plan, first), "first extractor fits");
        check(!GasFieldLedger.request("minecraft:overworld", 100L, plan, quota - first + 1),
                "second extractor must not exceed the shared field cap");
        check(GasFieldLedger.request("minecraft:overworld", 100L, plan, quota - first),
                "remaining quota is shared, not duplicated");
        check(GasFieldLedger.used("minecraft:overworld", 100L, plan) == quota,
                "both extractors draw from one ledger");
    }

    private static void ledgerResetsEachTick() {
        GasFieldLedger.reset();
        GasFieldPlan plan = findPlan(0L);
        check(GasFieldLedger.request("minecraft:overworld", 100L, plan, plan.quotaPerTick()),
                "full quota fits in one tick");
        check(GasFieldLedger.used("minecraft:overworld", 101L, plan) == 0,
                "next tick starts with a fresh budget");
        check(GasFieldLedger.request("minecraft:overworld", 101L, plan, plan.quotaPerTick()),
                "the same budget is available again next tick");
    }

    private static void ledgerSeparatesFields() {
        GasFieldLedger.reset();
        GasFieldPlan first = findPlan(0L);
        GasFieldPlan second = null;
        for (int step = GasFieldPlan.REGION_CHUNKS; second == null && step < 400; step += GasFieldPlan.REGION_CHUNKS) {
            second = GasFieldPlan.at(1L, first.anchorChunkX() + step, first.anchorChunkZ());
        }
        check(second != null && !first.equals(second), "expected a second, different field");
        check(GasFieldLedger.request("minecraft:overworld", 200L, first, first.quotaPerTick()),
                "first field is full");
        check(GasFieldLedger.request("minecraft:overworld", 200L, second, second.quotaPerTick()),
                "second field keeps its own budget");
    }

    private static void extractorWantedIsCapped() {
        GasFieldPlan plan = findPlan(0L);
        check(GasFieldExtractorCore.wanted(0, plan, Integer.MAX_VALUE) == 0, "no wells means no output");
        check(GasFieldExtractorCore.wanted(1, plan, 0) == 0, "no tank room means no output");
        int wells = plan.quotaPerTick() / GasFieldExtractorCore.WELL_RATE + 10;
        check(GasFieldExtractorCore.wanted(wells, plan, Integer.MAX_VALUE) <= plan.quotaPerTick(),
                "extractor output must be capped by the field quota");
        check(GasFieldExtractorCore.wanted(wells, plan, 3) == 3, "tank room must cap output");
    }

    private static GasFieldPlan findPlan(long seed) {
        for (int x = 0; x < 256; x++) {
            for (int z = 0; z < 256; z++) {
                GasFieldPlan plan = GasFieldPlan.at(seed, x * GasFieldPlan.REGION_CHUNKS, z * GasFieldPlan.REGION_CHUNKS);
                if (plan != null) return plan;
            }
        }
        throw new AssertionError("no gas field found in the search window");
    }

    private static GasFieldPlan findPlan(long seed, int chunkX, int chunkZ) {
        return GasFieldPlan.at(seed, chunkX, chunkZ);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
