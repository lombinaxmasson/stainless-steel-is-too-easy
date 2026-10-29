package com.lombinaxmasson.ssite.compat;

import com.lombinaxmasson.ssite.client.MachineRenderBounds;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import gregtech.client.BehaviorMachineClients;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

/**
 * GregTech's addon dispatcher keeps the vanilla unit cube and the default off-screen flag, so the
 * addon renderer's own bounds are never consulted. Forward both (NeoForge culls by the renderer).
 */
@Mixin(targets = "gregtech.client.BehaviorMachineRenderer", remap = false)
public abstract class LargeAodRendererBoundsMixin {
    public AABB getRenderBoundingBox(BlockEntity entity) {
        AABB box = MachineRenderBounds.of(entity, null);
        return box == null ? new AABB(entity.getBlockPos()) : box;
    }

    public boolean shouldRenderOffScreen(BlockEntity entity) {
        BlockEntityRenderer<BlockEntity> renderer = ssite$renderer(entity);
        return renderer != null && renderer.shouldRenderOffScreen(entity);
    }

    private static BlockEntityRenderer<BlockEntity> ssite$renderer(BlockEntity entity) {
        return entity instanceof BehaviorMachineClientView view ? BehaviorMachineClients.renderer(view) : null;
    }
}
