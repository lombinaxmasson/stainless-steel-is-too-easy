package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.HeatTransportConnections;
import com.lombinaxmasson.ssite.machine.HeatTransportMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** A pipe is selectable only where its insulated core and connected arms are drawn. */
@Mixin(targets = "gregtech.content.block.machine.BehaviorMachineBlock", remap = false)
public abstract class HeatTransportShapeMixin extends Block {
    @Unique private static final VoxelShape[] SSITE_PIPE_SHAPES = ssite$pipeShapes();
    protected HeatTransportShapeMixin(Properties properties) { super(properties); }

    @Override public boolean hasDynamicShape() {
        return HeatTransportConnections.isType(this, HeatTransportMachines.DUCT) || super.hasDynamicShape();
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return HeatTransportConnections.isType(this, HeatTransportMachines.DUCT)
                ? SSITE_PIPE_SHAPES[HeatTransportConnections.mask(level, pos)] : super.getShape(state, level, pos, context);
    }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return HeatTransportConnections.isType(this, HeatTransportMachines.DUCT)
                ? SSITE_PIPE_SHAPES[HeatTransportConnections.mask(level, pos)] : super.getCollisionShape(state, level, pos, context);
    }
    @Override public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return HeatTransportConnections.isType(this, HeatTransportMachines.DUCT)
                ? Shapes.empty() : super.getOcclusionShape(state, level, pos);
    }
    @Override public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return HeatTransportConnections.isType(this, HeatTransportMachines.DUCT)
                ? 0 : super.getLightBlock(state, level, pos);
    }
    @Unique private static VoxelShape[] ssite$pipeShapes() {
        VoxelShape[] shapes = new VoxelShape[64];
        for (int mask = 0; mask < shapes.length; mask++) {
            VoxelShape shape = Shapes.box(.20, .20, .20, .80, .80, .80);
            for (Direction d : Direction.values()) if ((mask & 1 << d.get3DDataValue()) != 0) {
                shape = Shapes.or(shape, Shapes.box(d == Direction.WEST ? 0 : .20,
                        d == Direction.DOWN ? 0 : .20, d == Direction.NORTH ? 0 : .20,
                        d == Direction.EAST ? 1 : .80, d == Direction.UP ? 1 : .80,
                        d == Direction.SOUTH ? 1 : .80));
            }
            shapes[mask] = shape.optimize();
        }
        return shapes;
    }
}
