package com.lombinaxmasson.ssite.machine;

import gregtech.api.multiblock.CellMatch;
import gregtech.api.multiblock.StructureLayout;
import gregtech.api.multiblock.StructureRole;
import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** A 3x3 stainless-steel platform; the controller sits in the centre and scans the cave below. */
public final class GasFieldStructure {
    /** Host GT6 large-machine wall; stainless steel is deliberately expensive. */
    public static final String WALL = "gt6ce:tank_wall";
    private static final Map<BlockPos, String> PARTS = createParts();

    private GasFieldStructure() {}

    private static Map<BlockPos, String> createParts() {
        Map<BlockPos, String> cells = new LinkedHashMap<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                cells.put(new BlockPos(x, 0, z), WALL);
            }
        }
        return Collections.unmodifiableMap(cells);
    }

    public static StructureLayout layout() {
        var builder = StructureLayout.builder(GasFieldExtractorCore.MACHINE_TYPE);
        PARTS.forEach((pos, part) -> builder.cell(pos.getX(), pos.getY(), pos.getZ(),
                CellMatch.block(part), StructureRole.CASING));
        return builder.build();
    }

    public static Map<BlockPos, String> parts() {
        return PARTS;
    }
}
