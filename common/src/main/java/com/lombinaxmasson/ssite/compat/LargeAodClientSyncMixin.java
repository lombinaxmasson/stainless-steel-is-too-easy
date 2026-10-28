package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Refresh the controller's baked cube when its synced formation state changes. */
@Mixin(targets = "gregtech.content.blockentity.machine.BehaviorMachineBlockEntity", remap = false)
public abstract class LargeAodClientSyncMixin extends BlockEntity {
    @Unique private boolean ssite$formed;

    protected LargeAodClientSyncMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "readClientState", at = @At("RETURN"))
    private void ssite$refresh(CompoundTag tag, CallbackInfo ci) {
        if (level == null || !level.isClientSide) return;
        var view = (BehaviorMachineClientView) this;
        if (!view.addonId().equals("ssite") || LargeAodStructure.tier(view.machineType()) == null) return;
        boolean formed = view.getBoolean("structure.formed");
        if (formed == ssite$formed) return;
        ssite$formed = formed;
        requestModelDataUpdate();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
}
