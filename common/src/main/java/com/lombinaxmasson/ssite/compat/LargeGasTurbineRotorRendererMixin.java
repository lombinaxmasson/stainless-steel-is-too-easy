package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.client.LargeGasTurbineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import gregtech.content.blockentity.machine.LargeGasTurbineControllerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The shared GT6CE renderer still handles the steam turbine and the gas fallback. */
@Mixin(targets = "gregtech.client.LargeTurbineRotorRenderer", remap = false)
public abstract class LargeGasTurbineRotorRendererMixin {
    // The vanilla override is SRG-named in the Forge distribution.
    @Inject(method = {
            "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            "m_6922_(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
    }, at = @At("HEAD"), cancellable = true, require = 1)
    private void ssite$renderGasTurbine(BlockEntity entity, float partialTick, PoseStack pose,
                                        MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
        if (entity instanceof LargeGasTurbineControllerBlockEntity gas
                && LargeGasTurbineRenderer.renderReplacement(gas, partialTick, pose, buffers, light)) {
            ci.cancel();
        }
    }

    // NeoForge culls through the renderer. Forge uses the controller's box.
    public AABB getRenderBoundingBox(BlockEntity entity) {
        if (entity instanceof LargeGasTurbineControllerBlockEntity gas && LargeGasTurbineRenderer.usesObj(gas)) {
            return LargeGasTurbineRenderer.bounds(gas.getBlockPos(), gas.front());
        }
        return new AABB(entity.getBlockPos());
    }
}
