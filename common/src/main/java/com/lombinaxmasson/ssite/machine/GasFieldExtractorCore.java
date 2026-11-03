package com.lombinaxmasson.ssite.machine;

import com.lombinaxmasson.ssite.worldgen.GasFieldPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Shared constants and well scan for the KU gas-field extraction rig. */
public final class GasFieldExtractorCore {

    public static final String MACHINE_TYPE = "gas_field_extractor";

    public static final long KU_PER_TICK = 16, KU_CAPACITY = 6_400;
    public static final int TANK_CAPACITY = 8_000;
    public static final int WELL_RATE = 4;
    public static final int SCAN_RADIUS = 5, SCAN_DEPTH = 6;
    public static final int RESCAN_TICKS = 40;

    public static final int ST_UNFORMED = 0, ST_NO_FIELD = 1, ST_NO_WELLS = 2, ST_WAIT_KU = 3,
            ST_OUTPUT_BLOCKED = 4, ST_QUOTA = 5, ST_WORKING = 6;

    public static final String K_FORMED = "structure.formed", K_STATUS = "extractor.status",
            K_WELLS = "extractor.wells", K_BUDGET = "extractor.budget", K_SCAN_TICK = "extractor.scan_tick";

    private static final ResourceLocation GAS_SPRING = ResourceLocation.tryBuild("gt6ce", "fluid_spring_gas_natural_gas");

    private GasFieldExtractorCore() {}

    public static int countWells(Level level, BlockPos controller) {
        int wells = 0;
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                for (int dy = -SCAN_DEPTH; dy <= -1; dy++) {
                    BlockPos pos = controller.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) continue;
                    if (GAS_SPRING.equals(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()))) wells++;
                }
            }
        }
        return wells;
    }

    /** Wells are the only source; the field budget and the output tank both cap the same tick. */
    public static int wanted(int wells, GasFieldPlan plan, int room) {
        if (wells <= 0 || plan == null || room <= 0) return 0;
        long wellsRate = (long) wells * WELL_RATE;
        long capped = Math.min(wellsRate, plan.quotaPerTick());
        return (int) Math.min(capped, room);
    }

}
