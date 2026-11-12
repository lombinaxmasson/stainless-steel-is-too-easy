package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.machine.IrradiationReactorCore;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Draws the IR-03 shielded vessel from the controller block. The other four
 * structure cells keep their ordinary blocks for collision and behaviour but
 * use empty models, so the OBJ is the only visible shell.
 */
public final class IrradiationReactorRenderer implements BlockEntityRenderer<BlockEntity> {
    public static final String MODEL = "/assets/ssite/obj/irradiation_reactor.obj";
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild("ssite", "textures/machine/irradiation_reactor.png");
    private static final ResourceLocation CONTROLLER_FRONT =
            ResourceLocation.tryBuild("ssite", "textures/block/machine/irradiation_reactor_controller/front.png");
    private static final ResourceLocation CONTROLLER_BACK =
            ResourceLocation.tryBuild("ssite", "textures/block/machine/irradiation_reactor_controller/back.png");
    private static final ResourceLocation CONTROLLER_SIDE =
            ResourceLocation.tryBuild("ssite", "textures/block/machine/irradiation_reactor_controller/side.png");
    private static final ResourceLocation CONTROLLER_TOP =
            ResourceLocation.tryBuild("ssite", "textures/block/machine/irradiation_reactor_controller/top.png");
    private static final ResourceLocation CONTROLLER_BOTTOM =
            ResourceLocation.tryBuild("ssite", "textures/block/machine/irradiation_reactor_controller/bottom.png");
    private static final float[][] FRONT = {{0, 1, 0}, {1, 1, 0}, {1, 0, 0}, {0, 0, 0}};
    private static final float[][] BACK = {{1, 1, 1}, {0, 1, 1}, {0, 0, 1}, {1, 0, 1}};
    private static final float[][] EAST = {{1, 1, 1}, {1, 1, 0}, {1, 0, 0}, {1, 0, 1}};
    private static final float[][] WEST = {{0, 1, 0}, {0, 1, 1}, {0, 0, 1}, {0, 0, 0}};
    private static final float[][] UP = {{0, 1, 0}, {1, 1, 0}, {1, 1, 1}, {0, 1, 1}};
    private static final float[][] DOWN = {{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}};
    private static final float[][] UV = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
    /** model.json: the controller cell (3,1,0) maps to the controller block minimum. */
    private static final float OFFSET_X = -3.0F;
    private static final float OFFSET_Y = -1.0F;
    private static final float OFFSET_Z = 0.0F;
    private static final float DOOR_LIFT = 1.24F;
    private static final float WHEEL_TURNS = 180.0F;
    /** 5 rpm at 20 ticks per second. */
    private static final double SPIN_DEGREES_PER_TICK = 1.5D;

    private static final class Assets {
        private static final ObjMesh MESH = ObjMesh.load(MODEL);
        private static final ObjVboMesh<Batch> VBO = new ObjVboMesh<>(Batch.class, MESH, IrradiationReactorRenderer::batch);
    }

    private final MeshSink sink;
    private final FluidizedBedRenderer meshRenderer;
    private final Map<BlockEntity, Door> doors = new WeakHashMap<>();

    public IrradiationReactorRenderer(MeshSink sink) {
        this.sink = sink;
        meshRenderer = new FluidizedBedRenderer(sink);
    }

    @Override
    public void render(BlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        if (!(entity instanceof BehaviorMachineClientView view)
                || !IrradiationReactorCore.MACHINE_TYPE.equals(view.machineType())) return;
        Level level = entity.getLevel();
        if (level == null) return;
        ObjMesh mesh = Assets.MESH;
        if (mesh.groups().isEmpty()) return;

        Direction facing = entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        // The controller cell is enclosed by the vessel; read the exposed neighbours for light.
        for (Direction side : Direction.values()) {
            int sample = LevelRenderer.getLightColor(level, entity.getBlockPos().relative(side));
            light = Math.max(light & 0xFFFF, sample & 0xFFFF)
                    | Math.max(light >>> 16, sample >>> 16) << 16;
        }

        // Before the controller, casings and heat port come together, the controller
        // draws its own cabinet. The vessel only replaces it once the structure formed.
        if (!view.getBoolean(IrradiationReactorCore.K_FORMED)) {
            drawControllerCube(pose, buffers, light, facing);
            return;
        }

        long state = view.getLong(IrradiationReactorCore.K_STATE);
        boolean running = state == IrradiationReactorCore.WORK;
        boolean done = state == IrradiationReactorCore.DONE;
        double now = level.getGameTime() + partialTick;
        float door = doors.computeIfAbsent(entity, ignored -> new Door())
                .advance(now, view.getBoolean(IrradiationReactorCore.K_DOOR_OPEN));

        int packedLight = light;
        pose.pushPose();
        try {
            pose.translate(0.5D, 0.0D, 0.5D);
            pose.mulPose(Axis.YP.rotationDegrees(-yaw(facing)));
            pose.translate(-0.5D, 0.0D, -0.5D);
            pose.translate(OFFSET_X, OFFSET_Y, OFFSET_Z);
            if (Assets.VBO.draw(buffers, TEXTURE, sink -> drawVbo(sink, pose, now, running, done, door, packedLight)))
                return;

            VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
            for (ObjMesh.Group group : mesh.groups()) {
                Batch batch = batch(group.name());
                if (!batch.visible(running, done)) continue;
                pose.pushPose();
                try {
                    poseBatch(pose, batch, now, running, door);
                    meshRenderer.draw(pose, vertices, group, packedLight);
                } finally {
                    pose.popPose();
                }
            }
        } finally {
            pose.popPose();
        }
    }

    static void drawVbo(ObjVboMesh.Draw<Batch> sink, PoseStack pose, double now,
                                boolean running, boolean done, float door, int light) {
        for (Batch batch : Batch.values()) {
            if (!batch.visible(running, done)) continue;
            pose.pushPose();
            try {
                poseBatch(pose, batch, now, running, door);
                sink.batch(batch, pose, light, 0xFFFFFF);
            } finally {
                pose.popPose();
            }
        }
    }
    private void drawControllerCube(PoseStack pose, MultiBufferSource buffers, int light, Direction facing) {
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw(facing)));
        pose.translate(-0.5D, 0.0D, -0.5D);
        drawFace(pose, buffers, CONTROLLER_FRONT, FRONT, 0.0F, 0.0F, -1.0F, light);
        drawFace(pose, buffers, CONTROLLER_BACK, BACK, 0.0F, 0.0F, 1.0F, light);
        drawFace(pose, buffers, CONTROLLER_SIDE, EAST, 1.0F, 0.0F, 0.0F, light);
        drawFace(pose, buffers, CONTROLLER_SIDE, WEST, -1.0F, 0.0F, 0.0F, light);
        drawFace(pose, buffers, CONTROLLER_TOP, UP, 0.0F, 1.0F, 0.0F, light);
        drawFace(pose, buffers, CONTROLLER_BOTTOM, DOWN, 0.0F, -1.0F, 0.0F, light);
        pose.popPose();
    }

    private void drawFace(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture, float[][] corners,
                          float nx, float ny, float nz, int light) {
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        Matrix4f position = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        Vector3f point = new Vector3f();
        Vector3f facing = new Vector3f(nx, ny, nz);
        normal.transform(facing).normalize();
        for (int corner = 0; corner < 4; corner++) {
            point.set(corners[corner][0], corners[corner][1], corners[corner][2]);
            position.transformPosition(point);
            sink.vertex(vertices, point.x, point.y, point.z, UV[corner][0], UV[corner][1],
                    facing.x, facing.y, facing.z, light);
        }
    }

    static Batch batch(String group) {
        return switch (group) {
            case "sample_carousel", "target_capsules", "drive_shaft" -> Batch.SPIN;
            case "door_panel", "locking_dogs" -> Batch.DOOR;
            case "handwheel" -> Batch.WHEEL;
            case "status_green", "chamber_lights" -> Batch.ACTIVE;
            case "status_amber" -> Batch.DONE;
            case "status_red" -> Batch.IDLE;
            default -> Batch.STATIC;
        };
    }

    enum Batch {
        STATIC, SPIN, DOOR, WHEEL, ACTIVE, DONE, IDLE;

        boolean visible(boolean running, boolean done) {
            return switch (this) {
                case ACTIVE -> running;
                case DONE -> done;
                case IDLE -> !running && !done;
                default -> true;
            };
        }
    }

    private static void poseBatch(PoseStack pose, Batch batch, double now, boolean running, float door) {
        if (batch == Batch.DOOR || batch == Batch.WHEEL) {
            pose.translate(0.0F, DOOR_LIFT * door, 0.0F);
            if (batch == Batch.WHEEL) {
                pose.translate(1.5F, 1.5F, 0.06F);
                pose.mulPose(Axis.ZP.rotationDegrees(WHEEL_TURNS * door));
                pose.translate(-1.5F, -1.5F, -0.06F);
            }
            return;
        }
        if (running && batch == Batch.SPIN) {
            pose.translate(1.5F, 0.0F, 2.5F);
            pose.mulPose(Axis.YP.rotationDegrees((float) ((now * SPIN_DEGREES_PER_TICK) % 360.0D)));
            pose.translate(-1.5F, 0.0F, -2.5F);
        }
    }

    /** The OBJ front is -Z, matching the north-facing blockstate rotation of zero degrees. */
    private static float yaw(Direction facing) {
        return switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity entity) {
        return true;
    }

    private static final class Door {
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
            float step = (float) (delta / 32.0D);
            progress = progress < target ? Math.min(target, progress + step)
                    : Math.max(target, progress - step);
            return progress;
        }
    }
}
