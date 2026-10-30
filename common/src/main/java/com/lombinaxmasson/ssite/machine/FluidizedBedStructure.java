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
import java.util.List;
import java.util.ArrayList;

/** Combustion tower, cyclone and rear HU outlet; external heat transport is independently placed. */
public final class FluidizedBedStructure {
    public static final int FORMED_DESIGN = 1;
    public static final String CASING = "fluidized_bed_casing";
    public static final String DUCT = "fluidized_bed_duct";
    public static final String HEAT_PLATE = "fluidized_bed_heat_plate";
    public static final String FOUNDATION = "fluidized_bed_foundation";
    public static final BlockPos HEAT_OUTLET = new BlockPos(0, 0, 4);
    private static final Map<BlockPos, String> PARTS = createParts();

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
        // The rear outlet replaces one skid cell. Pipes beyond this face are not structure cells.
        put(cells, HEAT_OUTLET.getX(), HEAT_OUTLET.getY(), HEAT_OUTLET.getZ(), DUCT);
        return Collections.unmodifiableMap(cells);
    }

    private static void put(Map<BlockPos, String> cells, int x, int y, int z, String part) {
        cells.put(new BlockPos(x, y, z), part);
    }

    public static StructureLayout layout() {
        var builder = StructureLayout.builder(FluidizedBedCore.MACHINE_TYPE);
        PARTS.forEach((p, name) -> builder.cell(p.getX(), p.getY(), p.getZ(), CellMatch.part(name), StructureRole.CASING));
        // Preserve the open combustion chamber. No external transport cells are claimed.
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

    public static BlockPos heatOutlet(BlockPos controller, Direction facing) {
        return world(controller, facing, HEAT_OUTLET.getX(), HEAT_OUTLET.getY(), HEAT_OUTLET.getZ());
    }

    public static BlockPos heatOutletTarget(BlockPos controller, Direction facing) {
        return heatOutlet(controller, facing).relative(facing.getOpposite());
    }

    /** Legacy placement helper only; these cells are no longer claimed by the tower structure. */
    @Deprecated public static List<BlockPos> heatPlates() {
        List<BlockPos> result = new ArrayList<>();
        for (int x = 5; x <= 7; x++) for (int z = 1; z <= 3; z++) result.add(new BlockPos(x, 1, z));
        return List.copyOf(result);
    }

    @Deprecated public static BlockPos heatTarget(BlockPos controller, Direction facing, int index) {
        BlockPos p = heatPlates().get(index);
        return world(controller, facing, p.getX(), p.getY() + 1, p.getZ());
    }
}
