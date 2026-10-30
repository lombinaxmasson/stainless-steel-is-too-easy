package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.machine.HeatTransportConnections;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/** World-axis HU pipe arms and a flush, upward-facing modular heating block. */
public final class HeatTransportRenderer implements BlockEntityRenderer<BlockEntity> {
    public static final String DUCT_MODEL = "/assets/ssite/obj/heat_duct.obj";
    public static final String PLATE_MODEL = "/assets/ssite/obj/heating_plate.obj";
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild("ssite", "textures/machine/fluidized_bed_combustor.png");
    private final FluidizedBedRenderer meshRenderer;

    public HeatTransportRenderer(MeshSink sink) { meshRenderer = new FluidizedBedRenderer(sink); }

    @Override public void render(BlockEntity entity, float tick, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
        if (!(entity instanceof BehaviorMachineClientView view) || !"ssite".equals(view.addonId())
                || !HeatTransportConnections.isMachine(view.machineType()) || entity.getLevel() == null) return;
        boolean duct = "heat_duct".equals(view.machineType());
        int mask = duct ? HeatTransportConnections.mask(entity.getLevel(), entity.getBlockPos()) : 0;
        // Native machine blocks may be opaque. Read the exposed surroundings for the narrow pipe.
        for (Direction side : Direction.values()) {
            int sample = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(side));
            light = Math.max(light & 0xFFFF, sample & 0xFFFF) | Math.max(light >>> 16, sample >>> 16) << 16;
        }
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        draw(pose, vertices, ObjMesh.load(duct ? DUCT_MODEL : PLATE_MODEL), mask, light);
    }

    static boolean groupVisible(String group, int mask) {
        boolean arm = group.startsWith("arm_"), cap = group.startsWith("cap_");
        if (!arm && !cap) return true;
        Direction direction = Direction.byName(group.substring(4));
        if (direction == null) return false;
        boolean connected = (mask & (1 << direction.get3DDataValue())) != 0;
        return arm == connected;
    }

    void draw(PoseStack pose, VertexConsumer vertices, ObjMesh mesh, int mask, int light) {
        for (var group : mesh.groups())
            if (groupVisible(group.name(), mask)) meshRenderer.draw(pose, vertices, group, light);
    }
}
