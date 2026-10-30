package com.lombinaxmasson.ssite.machine;

import gregtech.content.data.GregTechTags;
import gregtech.engine.energy.EnergyBlockViews;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

/** Physical connections shared by selection shapes and the pipe renderer. Never loads chunks. */
public final class HeatTransportConnections {
    private HeatTransportConnections() {}

    public static boolean isType(Block block, String type) {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        return id != null && "ssite".equals(id.getNamespace()) && type.equals(id.getPath());
    }

    public static boolean isMachine(String type) { return HeatTransportMachines.isMachine(type); }

    public static boolean loaded(BlockGetter level, BlockPos pos) {
        return !(level instanceof Level world) || world.hasChunkAt(pos);
    }

    public static int mask(BlockGetter level, BlockPos pos) {
        boolean plate = isType(level.getBlockState(pos).getBlock(), HeatTransportMachines.PLATE);
        int mask = 0;
        for (Direction direction : Direction.values()) {
            if (plate && direction.getAxis() == Direction.Axis.Y) continue;
            BlockPos next = pos.relative(direction);
            if (!loaded(level, next)) continue;
            Block block = level.getBlockState(next).getBlock();
            boolean connected = isType(block, HeatTransportMachines.DUCT)
                    || direction.getAxis() != Direction.Axis.Y && isType(block, HeatTransportMachines.PLATE)
                    || isOutlet(level, next, direction.getOpposite());
            if (!connected) {
                var entity = level.getBlockEntity(next);
                var energy = entity == null ? null : EnergyBlockViews.view(entity);
                connected = energy != null && energy.isEnergyEmittingTo(
                        GregTechTags.Energy.HU, direction.getOpposite(), true);
            }
            if (connected) mask |= 1 << direction.get3DDataValue();
        }
        return mask;
    }

    private static boolean isOutlet(BlockGetter level, BlockPos pos, Direction toPipe) {
        if (toPipe.getAxis() == Direction.Axis.Y
                || !isType(level.getBlockState(pos).getBlock(), FluidizedBedStructure.DUCT)) return false;
        BlockPos controller = pos.relative(toPipe.getOpposite(), 4);
        if (!loaded(level, controller)) return false;
        var state = level.getBlockState(controller);
        return isType(state.getBlock(), FluidizedBedCore.MACHINE_TYPE)
                && state.hasProperty(HorizontalDirectionalBlock.FACING)
                && state.getValue(HorizontalDirectionalBlock.FACING) == toPipe.getOpposite();
    }
}
