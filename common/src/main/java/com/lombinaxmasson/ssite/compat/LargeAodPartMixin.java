package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import com.lombinaxmasson.ssite.machine.ObjMultiblocks;
import gregtech.content.blockentity.machine.BehaviorMachineBlockEntity;
import gregtech.engine.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Addon designs are not accepted by the host layout catalog; use its binding lifecycle. */
@Mixin(targets = "gregtech.content.blockentity.machine.MultiblockPortBlockEntity", remap = false)
public abstract class LargeAodPartMixin extends BlockEntity {
    protected LargeAodPartMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    @Shadow public abstract void applyStructureDesign(int design);
    @Shadow public abstract void restoreStructureDesign();
    @Shadow public abstract boolean isBoundTo(BlockPos owner);
    @Shadow public abstract int structureDesign();
    @Unique private int ssite$previousDesign;

    @Unique private boolean ssite$largePart() {
        var id = BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock());
        return id.getNamespace().equals("ssite") && ObjMultiblocks.isPart(id.getPath());
    }

    @Inject(method = "bind", at = @At("RETURN"))
    private void ssite$hideBoundPart(BlockPos owner, MultiblockLayout.Role role, CallbackInfo ci) {
        if (level == null || level.isClientSide || !ssite$largePart() || !isBoundTo(owner)) return;
        // GT6CE 1.0.2 also binds incomplete shells for tool routing. Binding alone no longer
        // means that the controller's OBJ is visible; the formation hook handles that edge.
        if (level.getBlockEntity(owner) instanceof BehaviorMachineBlockEntity machine
                && machine.addonId().equals("ssite") && ObjMultiblocks.isMachine(machine.machineType())
                && machine.structureFormed())
            applyStructureDesign(LargeAodStructure.FORMED_DESIGN);
    }

    @Inject(method = "onLoad", at = @At("RETURN"))
    private void ssite$restoreLoadedPart(CallbackInfo ci) {
        if (level != null && !level.isClientSide && ssite$largePart()) restoreStructureDesign();
    }

    @Inject(method = "loadDesign", at = @At("HEAD"))
    private void ssite$beforeDesign(CompoundTag tag, CallbackInfo ci) {
        ssite$previousDesign = structureDesign();
    }

    @Inject(method = "loadDesign", at = @At("RETURN"))
    private void ssite$refreshPart(CompoundTag tag, CallbackInfo ci) {
        // ModelData refresh alone does not invalidate an already compiled chunk mesh.
        if (level != null && level.isClientSide && ssite$largePart() && ssite$previousDesign != structureDesign())
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
}
