package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.media.RecordData;
import com.lombinaxmasson.ssite.media.RecordServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Imported media retain vanilla storage and extraction without needing a fixed song component. */
@Mixin(JukeboxBlockEntity.class)
public abstract class RecordJukeboxInventoryMixin extends BlockEntity {
    protected RecordJukeboxInventoryMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void ssite$acceptImported(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> result) {
        if (RecordData.isRecord(stack)) {
            result.setReturnValue(slot == 0 && RecordData.get(stack) != null
                    && ((JukeboxBlockEntity)(Object)this).getItem(0).isEmpty());
        }
    }

    @Inject(method = "canTakeItem", at = @At("HEAD"), cancellable = true)
    private void ssite$keepPlayingImported(Container target, int slot, ItemStack stack,
                                            CallbackInfoReturnable<Boolean> result) {
        if (level != null && !level.isClientSide && RecordData.isRecord(stack)) {
            // A hopper may tick before the nearby-player scan after a chunk is loaded.
            RecordServer.changed(level, worldPosition);
            if (RecordServer.isPlaying(level, worldPosition)) result.setReturnValue(false);
        }
    }

    @Inject(method = "setTheItem", at = @At("RETURN"))
    private void ssite$changedImported(ItemStack stack, CallbackInfo result) {
        if (level != null && !level.isClientSide) RecordServer.changed(level, worldPosition);
    }
}
