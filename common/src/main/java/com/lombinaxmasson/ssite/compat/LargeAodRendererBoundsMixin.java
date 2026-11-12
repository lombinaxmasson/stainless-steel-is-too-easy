package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.client.MachineRenderBounds;
import com.lombinaxmasson.ssite.machine.WindTurbineCore;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import gregtech.client.BehaviorMachineClients;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * GregTech's addon dispatcher keeps the vanilla unit cube and the default off-screen flag, so the
 * addon renderer's own bounds are never consulted. Forward both (NeoForge culls by the renderer).
 */
@Mixin(targets = "gregtech.client.BehaviorMachineRenderer", remap = false)
public abstract class LargeAodRendererBoundsMixin implements BlockEntityRenderer<BlockEntity> {
    // Forge releases use the vanilla override's SRG name; dev and NeoForge use Mojang names.
    @Inject(method = {
            "shouldRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z",
            "m_142756_(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z"
    }, at = @At("HEAD"), cancellable = true, require = 1)
    private void ssite$windDistance(BlockEntity entity, Vec3 camera, CallbackInfoReturnable<Boolean> result) {
        if (entity instanceof BehaviorMachineClientView view && "ssite".equals(view.addonId())
                && WindTurbineCore.MACHINE_TYPE.equals(view.machineType())) {
            BlockEntityRenderer<BlockEntity> renderer = ssite$renderer(entity);
            result.setReturnValue(renderer != null && renderer.shouldRender(entity, camera));
        }
    }

    public AABB getRenderBoundingBox(BlockEntity entity) {
        AABB box = MachineRenderBounds.of(entity, null);
        return box == null ? new AABB(entity.getBlockPos()) : box;
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity entity) {
        BlockEntityRenderer<BlockEntity> renderer = ssite$renderer(entity);
        return renderer != null && renderer.shouldRenderOffScreen(entity);
    }

    private static BlockEntityRenderer<BlockEntity> ssite$renderer(BlockEntity entity) {
        return entity instanceof BehaviorMachineClientView view ? BehaviorMachineClients.renderer(view) : null;
    }
}
