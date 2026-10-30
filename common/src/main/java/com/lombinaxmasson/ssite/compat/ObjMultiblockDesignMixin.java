package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import com.lombinaxmasson.ssite.machine.ObjMultiblocks;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import gregtech.content.blockentity.machine.MultiblockPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Visual formation is independent of the host's persistent tool-routing claims. */
@Mixin(targets = "gregtech.content.blockentity.machine.BehaviorMachineBlockEntity", remap = false)
public abstract class ObjMultiblockDesignMixin extends BlockEntity {
    protected ObjMultiblockDesignMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "structureFormedChanged", at = @At("RETURN"))
    private void ssite$syncFormedPartDesigns(boolean formed, CallbackInfo ci) {
        if (level == null || level.isClientSide) return;
        var view = (BehaviorMachineClientView) this;
        if (!"ssite".equals(view.addonId()) || !ObjMultiblocks.isMachine(view.machineType())) return;
        Direction front = getBlockState().hasProperty(HorizontalDirectionalBlock.FACING)
                ? getBlockState().getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        for (var entry : ObjMultiblocks.preview(view.machineType()).entrySet()) {
            BlockPos local = entry.getKey();
            BlockPos target = worldPosition.relative(front.getClockWise(), local.getX()).above(local.getY())
                    .relative(front.getOpposite(), local.getZ());
            if (!level.hasChunkAt(target)
                    || !(level.getBlockEntity(target) instanceof MultiblockPortBlockEntity part)
                    || !part.isBoundTo(worldPosition)) continue;
            if (formed) part.applyStructureDesign(LargeAodStructure.FORMED_DESIGN);
            else part.restoreStructureDesign();
        }
    }
}
