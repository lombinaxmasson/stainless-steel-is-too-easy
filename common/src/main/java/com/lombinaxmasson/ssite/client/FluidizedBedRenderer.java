package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.machine.FluidizedBedCore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3f;

/** Fixed tower, flue and heat table, rendered from the front control panel. */
public final class FluidizedBedRenderer implements BlockEntityRenderer<BlockEntity> {
    public static final String MODEL = "/assets/ssite/obj/fluidized_bed_combustor.obj";
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild("ssite", "textures/machine/fluidized_bed_combustor.png");
    private final MeshSink sink;

    public FluidizedBedRenderer(MeshSink sink) { this.sink = sink; }

    @Override public void render(BlockEntity entity, float tick, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
        if (!(entity instanceof BehaviorMachineClientView view)
                || !FluidizedBedCore.MACHINE_TYPE.equals(view.machineType())
                || !view.getBoolean("structure.formed") || entity.getLevel() == null) return;
        Direction facing = entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        light = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        pose.pushPose();
        transform(pose, facing);
        for (var group : ObjMesh.load(MODEL).groups()) draw(pose, vertices, group, light);
        pose.popPose();
    }

    static void transform(PoseStack pose, Direction facing) {
        float yaw = switch (facing) { case EAST -> 90; case SOUTH -> 180; case WEST -> 270; default -> 0; };
        pose.translate(.5, 0, .5);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(-.5, 0, -.5);
        // Original art coordinates map to the controller's right/up/back frame.
        pose.translate(.5, 0, 2.5);
    }

    void draw(PoseStack pose, VertexConsumer vertices, ObjMesh.Group group, int light) {
        Vector3f p = new Vector3f(), n = new Vector3f();
        float[] data = group.triangles();
        for (int start = 0; start + 23 < data.length; start += 24) {
            // Minecraft's entityCutout consumes QUADS: one OBJ triangle becomes A,B,C,C.
            for (int corner = 0; corner < 4; corner++) {
                int i = start + Math.min(corner, 2) * 8;
                pose.last().pose().transformPosition(p.set(data[i], data[i+1], data[i+2]));
                pose.last().normal().transform(n.set(data[i+5], data[i+6], data[i+7])).normalize();
                sink.vertex(vertices, p.x, p.y, p.z, data[i+3], data[i+4], n.x, n.y, n.z, light);
            }
        }
    }

    @Override public boolean shouldRenderOffScreen(BlockEntity entity) { return true; }
}
