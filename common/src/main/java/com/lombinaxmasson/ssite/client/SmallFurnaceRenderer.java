package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.machine.AodTier;
import com.lombinaxmasson.ssite.machine.LargeAodStructure;
import com.lombinaxmasson.ssite.machine.VarFurnaceCore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import gregtech.api.addon.platform.PartClientView;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Draws small AOD/VAR models and the formed large AOD. The lid's synced {@code open}
 * flag drives animation; an incomplete large AOD keeps its ordinary baked block models.
 */
public final class SmallFurnaceRenderer implements BlockEntityRenderer<BlockEntity> {
    private static final float LID_PIVOT_X = 0.5F;
    private static final float LID_PIVOT_Y = 1.16F;
    private static final float LID_PIVOT_Z = 0.86F;
    /** Ticks for a full open or close motion. */
    private static final double ANIMATION_TICKS = 12.0D;

    private final MeshSink sink;
    private final Map<BlockEntity, Animation> animations = new WeakHashMap<>();

    public SmallFurnaceRenderer(MeshSink sink) {
        this.sink = sink;
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        if (!(blockEntity instanceof BehaviorMachineClientView view)) return;
        FurnaceModel model = FurnaceModel.of(view.machineType());
        if (model == null) return;
        if (model.kind() == Kind.LARGE_AOD && !view.getBoolean("structure.formed")) {
            animations.remove(blockEntity);
            return;
        }
        Level level = blockEntity.getLevel();
        if (level == null) return;
        ObjMesh mesh = ObjMesh.load(model.mesh());
        if (mesh.groups().isEmpty()) return;

        Direction facing = blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        // The opaque controller cell has no skylight. Sample its exposed front, where the
        // casting port is visible, instead of shading the entire vessel from inside a cube.
        if (model.kind() == Kind.LARGE_AOD)
            packedLight = LevelRenderer.getLightColor(level, blockEntity.getBlockPos().relative(facing));
        BlockPos lidPos = model.kind() == Kind.LARGE_AOD
                ? LargeAodStructure.world(blockEntity.getBlockPos(), facing, 0, 3, 2)
                : blockEntity.getBlockPos().above();
        BlockEntity lidEntity = level.getBlockEntity(lidPos);
        boolean open = !(lidEntity instanceof PartClientView part) || part.getBoolean("open");
        float progress = animations.computeIfAbsent(blockEntity, ignored -> new Animation())
                .advance(level.getGameTime() + partialTick, open);

        // The OBJ winding is already outward-facing. This pass consumes QUADS, so draw()
        // expands each source triangle to a complete four-vertex primitive.
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(model.texture()));

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw(facing)));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        if (model.kind() == Kind.LARGE_AOD) poseStack.translate(0, -1, 2);
        for (ObjMesh.Group group : mesh.groups()) {
            if (model.kind() == Kind.LARGE_AOD && group.name().equals("bath") && view.getLong("pool.amount") <= 0) continue;
            poseStack.pushPose();
            poseGroup(poseStack, group.name(), model.kind(), progress);
            draw(poseStack, buffer, group, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    /** The OBJ front is -Z, which is the north-facing blockstate rotation of 0 degrees. */
    private static float yaw(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static void poseGroup(PoseStack poseStack, String group, Kind kind, float progress) {
        if ("lid".equals(group)) {
            if (kind == Kind.LARGE_AOD) {
                poseStack.translate(.5, 5.1, 1.84);
                poseStack.mulPose(Axis.XP.rotationDegrees(progress * 105));
                poseStack.translate(-.5, -5.1, -1.84);
                return;
            }
            poseStack.translate(LID_PIVOT_X, LID_PIVOT_Y, LID_PIVOT_Z);
            poseStack.mulPose(Axis.XP.rotationDegrees(lidAngle(kind, progress)));
            poseStack.translate(-LID_PIVOT_X, -LID_PIVOT_Y, -LID_PIVOT_Z);
            return;
        }
        if (kind != Kind.VAR) return;
        float lift = Math.min(1.0F, progress / 0.3F) * 0.3F;
        float slide = clamp((progress - 0.3F) / 0.3F);
        float shiftX = 0.32F * slide;
        float shiftZ = -0.32F * slide;
        switch (group) {
            case "electrode" -> poseStack.translate(shiftX, lift, shiftZ);
            case "suspension" -> {
                float scale = (0.33F - lift) / 0.33F;
                poseStack.translate(shiftX, 1.94F * (1.0F - scale), shiftZ);
                poseStack.scale(1.0F, scale, 1.0F);
            }
            case "carriage" -> {
                float scale = (0.41F + shiftZ) / 0.41F;
                poseStack.translate(shiftX, 0.0F, 0.14F * (1.0F - scale));
                poseStack.scale(1.0F, 1.0F, scale);
            }
            default -> {
            }
        }
    }

    private static float lidAngle(Kind kind, float progress) {
        if (kind == Kind.AOD) return progress * 90.0F;
        return clamp((progress - 0.6F) / 0.4F) * 90.0F;
    }

    private static float clamp(float value) {
        return value < 0.0F ? 0.0F : Math.min(value, 1.0F);
    }

    private void draw(PoseStack poseStack, VertexConsumer buffer, ObjMesh.Group group, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        Vector3f point = new Vector3f();
        Vector3f facing = new Vector3f();
        float[] data = group.triangles();
        for (int triangle = 0; triangle + 23 < data.length; triangle += 24) {
            // Blaze3D indexes QUADS as (0,1,2), (2,3,0). Emit A,B,C,C so the first
            // triangle is unchanged and the second has zero area. Raw A,B,C streams
            // make adjacent faces and even separate animation groups join together.
            for (int corner = 0; corner < 4; corner++) {
                int i = triangle + Math.min(corner, 2) * 8;
                point.set(data[i], data[i + 1], data[i + 2]);
                position.transformPosition(point);
                facing.set(data[i + 5], data[i + 6], data[i + 7]);
                normal.transform(facing);
                facing.normalize();
                sink.vertex(buffer, point.x, point.y, point.z, data[i + 3], data[i + 4],
                        facing.x, facing.y, facing.z, packedLight);
            }
        }
    }

    private enum Kind {
        AOD, VAR, LARGE_AOD
    }

    private record FurnaceModel(Kind kind, String mesh, ResourceLocation texture) {
        private static FurnaceModel of(String machineType) {
            AodTier large = LargeAodStructure.tier(machineType);
            if (large != null) return new FurnaceModel(Kind.LARGE_AOD, "/assets/ssite/obj/large_aod.obj",
                    texture("large_aod/" + large.suffix()));
            if (VarFurnaceCore.MACHINE_TYPE.equals(machineType)) {
                return new FurnaceModel(Kind.VAR, "/assets/ssite/obj/var.obj", texture("var"));
            }
            AodTier tier = AodTier.byMachineType(machineType);
            if (tier == null) return null;
            return new FurnaceModel(Kind.AOD, "/assets/ssite/obj/aod.obj", texture("aod/" + tier.suffix()));
        }

        private static ResourceLocation texture(String path) {
            return ResourceLocation.tryBuild("ssite", "textures/machine/" + path + ".png");
        }
    }

    @Override public boolean shouldRenderOffScreen(BlockEntity entity) {
        return entity instanceof BehaviorMachineClientView view && LargeAodStructure.tier(view.machineType()) != null;
    }

    /** Client-only easing state; a machine loaded for the first time snaps to its current pose. */
    private static final class Animation {
        private double last = Double.NaN;
        private float progress;

        float advance(double now, boolean open) {
            float target = open ? 1.0F : 0.0F;
            if (Double.isNaN(last)) {
                last = now;
                progress = target;
                return progress;
            }
            double delta = now - last;
            last = now;
            if (delta < 0.0D || delta > 20.0D) delta = 0.0D;
            float step = (float) (delta / ANIMATION_TICKS);
            progress = progress < target ? Math.min(target, progress + step)
                    : Math.max(target, progress - step);
            return progress;
        }
    }
}
