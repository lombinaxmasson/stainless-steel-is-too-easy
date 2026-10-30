package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.machine.HeatTransportCore;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import gregtech.api.addon.platform.BehaviorState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Synchronous access to neighboring HU buffers without retaining behavior callback contexts. */
@Mixin(targets = "gregtech.content.blockentity.machine.BehaviorMachineBlockEntity", remap = false)
public abstract class HeatTransportAccessMixin extends BlockEntity implements HeatTransportCore.RuntimeAccess {
    protected HeatTransportAccessMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Shadow private long stored;
    @Shadow public abstract BehaviorState state();
    @Invoker("emitTo") protected abstract long ssite$emitHeat(BlockPos target, Direction side, long size, long amount);

    @Override public long thermalStored() { return stored; }
    @Override public BehaviorState thermalState() { return state(); }
    @Override public boolean thermalConsume(long amount) {
        if (amount < 0 || amount > stored) return false;
        stored -= amount;
        var entity = (BlockEntity) (Object) this;
        if (amount > 0 && !entity.isRemoved() && entity.getLevel() != null
                && entity.getLevel().hasChunkAt(entity.getBlockPos())) entity.setChanged();
        return true;
    }
    @Override public long thermalEmit(BlockPos target, long amount) {
        return ssite$emitHeat(target, Direction.DOWN, 1, amount);
    }

    @Unique private boolean ssite$isTransportHost() {
        var view = (BehaviorMachineClientView) (Object) this;
        return "ssite".equals(view.addonId()) && HeatTransportCore.isTransport(view.machineType());
    }

    @Override public void setChanged() {
        if (!ssite$isTransportHost()) {
            super.setChanged();
            return;
        }
        // These HU buffers have no comparator output. Vanilla's comparator notification walks
        // through a solid neighbor and can read an unloaded second chunk when merely saving state.
        if (level != null && !isRemoved() && level.hasChunkAt(worldPosition))
            level.blockEntityChanged(worldPosition);
    }

    @Redirect(method = "tickServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            remap = true))
    private boolean ssite$updateTransportIndicatorsWithoutLoadingNeighbors(Level level, BlockPos pos,
                                                                           BlockState state, int flags) {
        // RUNNING/LIT do not change a duct or plate's connections or collision. The host's normal
        // UPDATE_ALL would notify and reshape neighbors, reading chunks that routing rejected.
        return level.setBlock(pos, state,
                ssite$isTransportHost() ? Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE : flags);
    }
}
