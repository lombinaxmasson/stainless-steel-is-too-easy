package com.lombinaxmasson.ssite.machine;

import gregtech.api.multiblock.CellMatch;
import gregtech.api.multiblock.StructureLayout;
import gregtech.api.multiblock.StructureRole;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Three shielded casings and a top heat outlet around the controller. */
public final class IrradiationReactorStructure {
    public static final String CASING = "irradiation_reactor_casing";
    public static final String HEAT_PORT = "irradiation_reactor_heat_port";
    private static final Map<BlockPos, String> PARTS = createParts();

    private IrradiationReactorStructure() {}

    private static Map<BlockPos, String> createParts() {
        Map<BlockPos, String> cells = new LinkedHashMap<>();
        put(cells, -1, 0, 0, CASING);
        put(cells, 1, 0, 0, CASING);
        put(cells, 0, 0, 1, CASING);
        // The only heat exit sits on top; the owning behavior sends HU one block further up.
        put(cells, 0, 1, 0, HEAT_PORT);
        return Collections.unmodifiableMap(cells);
    }

    private static void put(Map<BlockPos, String> cells, int x, int y, int z, String part) {
        cells.put(new BlockPos(x, y, z), part);
    }

    public static StructureLayout layout() {
        var builder = StructureLayout.builder(IrradiationReactorCore.MACHINE_TYPE);
        PARTS.forEach((p, name) -> builder.cell(p.getX(), p.getY(), p.getZ(), CellMatch.part(name), StructureRole.CASING));
        return builder.build();
    }

    public static Map<BlockPos, String> parts() {
        return PARTS;
    }

    public static BlockPos world(BlockPos controller, Direction facing, int right, int up, int back) {
        return controller.relative(facing.getClockWise(), right).above(up).relative(facing.getOpposite(), back);
    }

    public static BlockPos shield(BlockPos controller, Direction facing) {
        return world(controller, facing, 1, 0, 0);
    }

    public static BlockPos heatPort(BlockPos controller, Direction facing) {
        return world(controller, facing, 0, 1, 0);
    }

    public static BlockPos heatPortTarget(BlockPos controller, Direction facing) {
        return heatPort(controller, facing).above();
    }
}
