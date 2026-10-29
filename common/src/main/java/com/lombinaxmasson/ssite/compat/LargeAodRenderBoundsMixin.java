package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.client.MachineRenderBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Forge frustum-tests a block entity by its own box. GregTech's default is the collision shape of
 * the 1x1x1 controller cell, so the vessel disappears as soon as the camera looks past the casting
 * port. Widen it to the model; every other behavior machine keeps the block's own shape.
 */
@Mixin(targets = "gregtech.content.blockentity.machine.BehaviorMachineBlockEntity", remap = false)
public abstract class LargeAodRenderBoundsMixin extends BlockEntity {
    protected LargeAodRenderBoundsMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public AABB getRenderBoundingBox() {
        AABB box = MachineRenderBounds.of(this, null);
        if (box != null) return box;
        if (level == null) return new AABB(getBlockPos());
        return getBlockState().getCollisionShape(level, getBlockPos()).bounds().move(getBlockPos());
    }
}
