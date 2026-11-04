package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.machine.AirReactorCore;
import com.lombinaxmasson.ssite.machine.ChemicalLoopingCore;
import com.lombinaxmasson.ssite.machine.FuelReactorCore;
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

/** Both chemical-looping beds are 1x1 logic blocks with larger static OBJ vessels. */
public final class ChemicalLoopingRenderer implements BlockEntityRenderer<BlockEntity> {
    public static final String FUEL_MODEL = "/assets/ssite/obj/fuel_reactor.obj";
    public static final String AIR_MODEL = "/assets/ssite/obj/air_reactor.obj";
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final ResourceLocation FUEL_TEXTURE =
            ResourceLocation.tryBuild("ssite", "textures/machine/fuel_reactor.png");
    private static final ResourceLocation AIR_TEXTURE =
            ResourceLocation.tryBuild("ssite", "textures/machine/air_reactor.png");
    private final MeshSink sink;

    public ChemicalLoopingRenderer(MeshSink sink) { this.sink = sink; }

    @Override public void render(BlockEntity entity, float tick, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
        if (!(entity instanceof BehaviorMachineClientView view) || entity.getLevel() == null) return;
        String type = view.machineType();
        boolean fuel = FuelReactorCore.MACHINE_TYPE.equals(type);
        if (!fuel && !AirReactorCore.MACHINE_TYPE.equals(type)) return;
        Direction facing = entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        light = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));
        boolean active = fuel ? view.getBoolean(FuelReactorCore.K_LIT)
                : view.getLong(AirReactorCore.K_STATE) == AirReactorCore.WORK;
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutout(fuel ? FUEL_TEXTURE : AIR_TEXTURE));
        pose.pushPose();
        transform(pose, facing);
        for (var group : ObjMesh.load(fuel ? FUEL_MODEL : AIR_MODEL).groups()) {
            int groupLight = active && ("bed".equals(group.name()) || "particles".equals(group.name()))
                    ? FULL_BRIGHT : light;
            draw(pose, vertices, group, groupLight);
        }
        pose.popPose();
    }

    static void transform(PoseStack pose, Direction facing) {
        float yaw = switch (facing) { case EAST -> 90; case SOUTH -> 180; case WEST -> 270; default -> 0; };
        pose.translate(.5, 0, .5);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        pose.translate(-.5, 0, -.5);
    }

    void draw(PoseStack pose, VertexConsumer vertices, ObjMesh.Group group, int light) {
        Vector3f p = new Vector3f(), n = new Vector3f();
        float[] data = group.triangles();
        for (int start = 0; start + 23 < data.length; start += 24) {
            // Minecraft's entityCutout consumes QUADS: one OBJ triangle becomes A,B,C,C.
            for (int corner = 0; corner < 4; corner++) {
                int i = start + Math.min(corner, 2) * 8;
                pose.last().pose().transformPosition(p.set(data[i], data[i + 1], data[i + 2]));
                pose.last().normal().transform(n.set(data[i + 5], data[i + 6], data[i + 7])).normalize();
                sink.vertex(vertices, p.x, p.y, p.z, data[i + 3], data[i + 4], n.x, n.y, n.z, light);
            }
        }
    }

    @Override public boolean shouldRenderOffScreen(BlockEntity entity) { return true; }
}
