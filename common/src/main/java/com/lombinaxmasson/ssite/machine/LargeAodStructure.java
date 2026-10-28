package com.lombinaxmasson.ssite.machine;

import gregtech.api.multiblock.CellMatch;
import gregtech.api.multiblock.StructureLayout;
import gregtech.api.multiblock.StructureRole;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** OBJ coordinates: controller is the casting port (0,1,-2), not the vessel centre. */
public final class LargeAodStructure {
    /** Native part design restored automatically when the controller releases its claims. */
    public static final int FORMED_DESIGN = 1;
    public static final String OXYGEN = "large_aod_oxygen";
    public static final String ARGON = "large_aod_argon";
    public static final String LID = "large_aod_lid";
    private LargeAodStructure() {}

    public static AodTier tier(String type) {
        for (AodTier tier : AodTier.values()) if (tier.largeMachineType().equals(type)) return tier;
        return null;
    }

    public static StructureLayout layout(AodTier tier) {
        var layout = StructureLayout.builder(tier.largeMachineType());
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
            cell(layout, x, 0, z, tier.largeCasing(), StructureRole.ENERGY_INPUT);
        // Four structural columns inside the circular shell; no cube corners outside the OBJ.
        for (int y = 1; y <= 3; y++) {
            cell(layout, -1, y, 0, tier.largeCasing(), StructureRole.CASING);
            cell(layout, 1, y, 0, tier.largeCasing(), StructureRole.CASING);
            cell(layout, 0, y, -1, tier.largeCasing(), StructureRole.CASING);
            cell(layout, 0, y, 1, tier.largeCasing(), StructureRole.CASING);
            layout.cell(0, y - 1, 2, CellMatch.air(), StructureRole.AIR);
        }
        cell(layout, 2, 1, -1, OXYGEN, StructureRole.CASING);
        cell(layout, 2, 1, 1, ARGON, StructureRole.CASING);
        cell(layout, 0, 4, 0, LID, StructureRole.CASING);
        return layout.build();
    }

    private static void cell(StructureLayout.Builder layout, int x, int y, int z,
                             String part, StructureRole role) {
        layout.cell(x, y - 1, z + 2, CellMatch.part(part), role);
    }

    /** Shared by the native recipe-viewer preview and builder's wand. */
    public static Map<BlockPos, Block> preview(AodTier tier) {
        Map<BlockPos, Block> cells = new LinkedHashMap<>();
        for (var cell : layout(tier).cells()) {
            Block block = cell.match() instanceof CellMatch.Part part
                    ? BuiltInRegistries.BLOCK.get(ResourceLocation.tryBuild("ssite", part.name())) : Blocks.AIR;
            cells.put(new BlockPos(cell.right(), cell.up(), cell.back()), block);
        }
        return Collections.unmodifiableMap(cells);
    }

    /** Converts right/up/back offsets from the casting controller into world coordinates. */
    public static BlockPos world(BlockPos controller, Direction facing, int right, int up, int back) {
        return controller.relative(facing.getClockWise(), right).above(up).relative(facing.getOpposite(), back);
    }
}
