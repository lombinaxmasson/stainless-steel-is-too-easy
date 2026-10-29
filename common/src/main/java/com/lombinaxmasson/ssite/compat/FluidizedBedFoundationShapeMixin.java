package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.FluidizedBedStructure;
import gregtech.content.blockentity.machine.MultiblockPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** The formed steel skid is low; the nine foundations supporting heat plates stay solid. */
@Mixin(targets = "gregtech.content.block.machine.MultiblockPortBlock", remap = false)
public abstract class FluidizedBedFoundationShapeMixin extends Block {
    @Unique private static final VoxelShape SSITE_SKID = Shapes.box(0, 0, 0, 1, .28, 1);

    protected FluidizedBedFoundationShapeMixin(Properties properties) { super(properties); }

    @Override public boolean hasDynamicShape() {
        // BlockState's context-free collision cache cannot represent a BE-dependent formed shape.
        return ssite$isPart(this, FluidizedBedStructure.FOUNDATION) || super.hasDynamicShape();
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                         CollisionContext context) {
        return ssite$lowSkid(level, pos) ? SSITE_SKID : super.getShape(state, level, pos, context);
    }

    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                                  CollisionContext context) {
        return ssite$lowSkid(level, pos) ? SSITE_SKID : super.getCollisionShape(state, level, pos, context);
    }

    @Unique private boolean ssite$lowSkid(BlockGetter level, BlockPos pos) {
        if (!ssite$isPart(this, FluidizedBedStructure.FOUNDATION)
                || !(level.getBlockEntity(pos) instanceof MultiblockPortBlockEntity part)
                || part.structureDesign() != FluidizedBedStructure.FORMED_DESIGN) return false;
        // The owner binding is intentionally server-only in GT. This adjacent block relation is
        // available on both sides and exactly identifies the nine full-height table foundations.
        return !ssite$isPart(level.getBlockState(pos.above()).getBlock(), FluidizedBedStructure.HEAT_PLATE);
    }

    @Unique private static boolean ssite$isPart(Block block, String name) {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        return id != null && "ssite".equals(id.getNamespace()) && name.equals(id.getPath());
    }
}
