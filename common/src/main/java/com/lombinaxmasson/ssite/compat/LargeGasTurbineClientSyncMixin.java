package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.client.LargeGasTurbineRenderer;
import gregtech.content.blockentity.machine.LargeGasTurbineControllerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

/** Refresh the baked housing only when an update packet changes which model draws it. */
@Mixin(targets = "gregtech.engine.multiblock.tracking.ClientFormedSync", remap = false)
public abstract class LargeGasTurbineClientSyncMixin {
    @Unique
    private static final Map<LargeGasTurbineControllerBlockEntity, Boolean> ssite$objVisible = new WeakHashMap<>();

    // GTEnergyBlockEntity calls read after loading the native formed/active rotor flags.
    @Inject(method = "read(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("RETURN"))
    private static void ssite$refreshTurbine(BlockEntity entity, CompoundTag tag, CallbackInfo ci) {
        if (!(entity instanceof LargeGasTurbineControllerBlockEntity gas)
                || gas.getLevel() == null || !gas.getLevel().isClientSide) return;
        boolean visible = LargeGasTurbineRenderer.usesObj(gas);
        if (Boolean.TRUE.equals(ssite$objVisible.put(gas, visible)) != visible) {
            LargeGasTurbineRenderer.refreshBlocks(gas);
        }
    }
}
