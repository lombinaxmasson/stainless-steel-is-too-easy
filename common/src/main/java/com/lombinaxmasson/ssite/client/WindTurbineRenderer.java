package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.machine.WindTurbineCore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import gregtech.api.addon.platform.BehaviorMachineClientView;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

/** The casing is baked into chunks; only the detachable rotor uses this renderer. */
public final class WindTurbineRenderer implements BlockEntityRenderer<BlockEntity> {
    public static final String MODEL = "/assets/ssite/obj/wind_turbine_rotor.obj";
    private static final ResourceLocation RESOURCE = ResourceLocation.tryBuild("ssite", "obj/wind_turbine_rotor.obj");
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild("ssite", "textures/block/machine/wind_turbine.png");
    private static final double RADIUS = 5.201D;
    private static final Map<BlockEntity, Rotation> ROTATIONS = new WeakHashMap<>();
    private final FluidizedBedRenderer meshRenderer;

    public WindTurbineRenderer(MeshSink sink) {
        meshRenderer = new FluidizedBedRenderer(sink);
    }

    public static void reload(ResourceManager resources) {
        ROTATIONS.clear();
        try {
            ObjMesh.reload(MODEL, resources.getResourceOrThrow(RESOURCE).open());
        } catch (IOException error) {
            throw new IllegalStateException("Could not reload wind turbine rotor", error);
        }
    }

    @Override
    public void render(BlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        if (!(entity instanceof BehaviorMachineClientView view)
                || !WindTurbineCore.MACHINE_TYPE.equals(view.machineType())) return;
        if (entity.isRemoved() || entity.getLevel() == null || !view.getBoolean(WindTurbineCore.K_ROTOR)) {
            ROTATIONS.remove(entity);
            return;
        }
        Direction facing = entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        double now = entity.getLevel().getGameTime() + partialTick;
        long speed = Math.max(0L, Math.min(1000L, view.getLong(WindTurbineCore.K_SPEED)));
        double angle = ROTATIONS.computeIfAbsent(entity, ignored -> new Rotation()).advance(now, speed);
        light = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));
        var vertices = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        draw(pose, vertices, facing, angle, light);
    }

    void draw(PoseStack pose, VertexConsumer vertices, Direction facing, double angle, int light) {
        pose.pushPose();
        pose.translate(.5D, .5D, .5D);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw(facing)));
        pose.translate(0D, 0D, -1.05D);
        pose.mulPose(Axis.ZP.rotationDegrees((float) angle));
        pose.translate(-.5D, -.5D, .55D);
        for (var group : ObjMesh.load(MODEL).groups()) meshRenderer.draw(pose, vertices, group, light);
        pose.popPose();
    }

    /** Full rotation envelope, including the journal; the head remains in its own unit cube. */
    public static AABB bounds(BlockPos pos, Direction facing) {
        double low = .5D - RADIUS, high = .5D + RADIUS;
        AABB local = switch (facing) {
            case SOUTH -> new AABB(low, low, 0D, high, high, 1.85D);
            case EAST -> new AABB(0D, low, low, 1.85D, high, high);
            case WEST -> new AABB(-.85D, low, low, 1D, high, high);
            default -> new AABB(low, low, -.85D, high, high, 1D);
        };
        return local.move(pos);
    }

    @Override
    public boolean shouldRender(BlockEntity entity, Vec3 camera) {
        return !entity.isRemoved() && entity.getLevel() != null
                && entity.getBlockPos().getCenter().distanceToSqr(camera) < 128D * 128D;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    private static float yaw(Direction facing) {
        return switch (facing) {
            case EAST -> 90F;
            case SOUTH -> 180F;
            case WEST -> 270F;
            default -> 0F;
        };
    }

    static final class Rotation {
        private double last = Double.NaN;
        private double angle;
        private double velocity;

        double advance(double now, long speed) {
            double target = speed * .003D;
            if (!Double.isFinite(last) || now < last || now - last > 100D) {
                last = now;
                velocity = target;
                return angle;
            }
            double elapsed = now - last;
            double blend = 1D - Math.exp(-elapsed / 8D);
            double next = velocity + (target - velocity) * blend;
            angle = (angle + (velocity + next) * .5D * elapsed) % 360D;
            velocity = next;
            last = now;
            return angle;
        }
    }
}
