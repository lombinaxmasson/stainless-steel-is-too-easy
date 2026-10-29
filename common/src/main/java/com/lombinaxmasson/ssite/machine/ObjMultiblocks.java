package com.lombinaxmasson.ssite.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/** Shared host adapters for the addon's formed OBJ structures. */
public final class ObjMultiblocks {
    private ObjMultiblocks() {}

    public static boolean isMachine(String type) {
        return LargeAodStructure.tier(type) != null || FluidizedBedCore.MACHINE_TYPE.equals(type);
    }

    public static boolean isPart(String name) {
        return name.startsWith("large_aod_") || name.startsWith("fluidized_bed_");
    }

    public static Map<BlockPos, Block> preview(String type) {
        var tier = LargeAodStructure.tier(type);
        if (tier != null) return LargeAodStructure.preview(tier);
        if (FluidizedBedCore.MACHINE_TYPE.equals(type)) return FluidizedBedStructure.preview();
        return Map.of();
    }

    public static String messagePrefix(String type) {
        return FluidizedBedCore.MACHINE_TYPE.equals(type) ? "ssite.fluidized_bed" : "ssite.aod";
    }
}
