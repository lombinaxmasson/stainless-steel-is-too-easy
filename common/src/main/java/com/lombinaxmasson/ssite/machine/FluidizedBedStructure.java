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
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/** Fixed, continuous combustor + short hot-gas duct + nine-cell heating table. */
public final class FluidizedBedStructure {
    public static final int FORMED_DESIGN = 1;
    public static final String CASING = "fluidized_bed_casing";
    public static final String DUCT = "fluidized_bed_duct";
    public static final String HEAT_PLATE = "fluidized_bed_heat_plate";
    public static final String FOUNDATION = "fluidized_bed_foundation";
    private static final Map<BlockPos, String> PARTS = createParts();
    private static final List<BlockPos> HEAT_CELLS = heatCells();

    private FluidizedBedStructure() {}

    private static Map<BlockPos, String> createParts() {
        Map<BlockPos, String> cells = new LinkedHashMap<>();
        // Rigid base below the seven-by-five industrial assembly. The front central cell is the controller.
        for (int x = -3; x <= 3; x++) for (int z = 0; z <= 4; z++)
            if (x != 0 || z != 0) put(cells, x, 0, z, FOUNDATION);
        // Four columns inside the main cylindrical shell, with a top ring and cyclone spine.
        for (int y = 1; y <= 6; y++) {
            put(cells, -1, y, 1, CASING); put(cells, -1, y, 3, CASING);
            put(cells, -2, y, 2, CASING); put(cells, 0, y, 2, CASING);
        }
        for (int x = -1; x <= 0; x++) for (int z = 2; z <= 3; z++) put(cells, x, 7, z, CASING);
        for (int y = 1; y <= 6; y++) put(cells, 2, y, 3, CASING);
        for (int z = 2; z <= 3; z++) put(cells, 2, 4, z, CASING);
        put(cells, 1, 6, 3, DUCT);
        // The declared short duct turns from the back of the combustor toward the table.
        for (int x = 0; x <= 4; x++) put(cells, x, 0, 4, DUCT);
        for (int z = 2; z <= 3; z++) put(cells, 4, 0, z, DUCT);
        for (int x = 5; x <= 7; x++) for (int z = 1; z <= 3; z++) {
            put(cells, x, 0, z, FOUNDATION);
            put(cells, x, 1, z, HEAT_PLATE);
        }
        return Collections.unmodifiableMap(cells);
    }

    private static void put(Map<BlockPos, String> cells, int x, int y, int z, String part) {
        cells.put(new BlockPos(x, y, z), part);
    }

    private static List<BlockPos> heatCells() {
        List<BlockPos> result = new ArrayList<>();
        for (int x = 5; x <= 7; x++) for (int z = 1; z <= 3; z++) result.add(new BlockPos(x, 1, z));
        return List.copyOf(result);
    }

    /** Local offsets of the nine emitting plates; their top neighbors are the only HU targets. */
    public static List<BlockPos> heatPlates() { return HEAT_CELLS; }

    public static StructureLayout layout() {
        var builder = StructureLayout.builder(FluidizedBedCore.MACHINE_TYPE);
        PARTS.forEach((p, name) -> builder.cell(p.getX(), p.getY(), p.getZ(), CellMatch.part(name), StructureRole.CASING));
        // Preserve the open combustion chamber. No cells above the heating table are claimed.
        for (int y = 1; y <= 6; y++) builder.cell(-1, y, 2, CellMatch.air(), StructureRole.AIR);
        return builder.build();
    }

    public static Map<BlockPos, Block> preview() {
        Map<BlockPos, Block> result = new LinkedHashMap<>();
        for (var cell : layout().cells()) {
            Block block = cell.match() instanceof CellMatch.Part p
                    ? BuiltInRegistries.BLOCK.get(ResourceLocation.tryBuild("ssite", p.name())) : Blocks.AIR;
            result.put(new BlockPos(cell.right(), cell.up(), cell.back()), block);
        }
        return Collections.unmodifiableMap(result);
    }

    public static BlockPos world(BlockPos controller, Direction facing, int right, int up, int back) {
        return controller.relative(facing.getClockWise(), right).above(up).relative(facing.getOpposite(), back);
    }

    public static BlockPos heatTarget(BlockPos controller, Direction facing, int index) {
        BlockPos p = HEAT_CELLS.get(index);
        return world(controller, facing, p.getX(), p.getY() + 1, p.getZ());
    }
}
