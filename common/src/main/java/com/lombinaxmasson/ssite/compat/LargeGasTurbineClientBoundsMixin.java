package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.client.LargeGasTurbineRenderer;
import gregtech.content.blockentity.machine.LargeGasTurbineControllerBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "gregtech.content.blockentity.machine.LargeGasTurbineControllerBlockEntity", remap = false)
public abstract class LargeGasTurbineClientBoundsMixin {
    public AABB getRenderBoundingBox() {
        var gas = (LargeGasTurbineControllerBlockEntity) (Object) this;
        return LargeGasTurbineRenderer.usesObj(gas)
                ? LargeGasTurbineRenderer.bounds(gas.getBlockPos(), gas.front()) : new AABB(gas.getBlockPos());
    }

    @Inject(method = "onLoad()V", at = @At("RETURN"))
    private void ssite$refreshLoadedTurbine(CallbackInfo ci) {
        var gas = (LargeGasTurbineControllerBlockEntity) (Object) this;
        if (gas.getLevel() != null && gas.getLevel().isClientSide && LargeGasTurbineRenderer.usesObj(gas)) {
            LargeGasTurbineRenderer.refreshBlocks(gas);
        }
    }

    // A neighboring chunk can still show the housing after its controller has been unloaded.
    @Inject(method = {"setRemoved()V", "m_7651_()V"}, at = @At("RETURN"), require = 1)
    private void ssite$refreshRemovedTurbine(CallbackInfo ci) {
        var gas = (LargeGasTurbineControllerBlockEntity) (Object) this;
        if (gas.getLevel() != null && gas.getLevel().isClientSide && gas.isRotorFormed()) {
            LargeGasTurbineRenderer.refreshBlocks(gas);
        }
    }
}
