package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Checks the actual gas turbine vertex stream without creating a client window. */
public final class LargeGasTurbineRenderTests {
    private static final int TRIANGLES = 6_400;
    private static final int MOVING_TRIANGLES = 892;
    private static final int LIGHT = 0x00D00030;
    private static final int ROTOR_RGB = 0xA280C0;
    private static final int WHITE = 0xFFFFFF;
    private static final float EPSILON = 1E-4F;
    private static final float ANGLE = 37;
    private static final Set<String> MOVING_GROUPS = Set.of("rotor", "output_coupling");
    private static final Set<String> HOT_GROUPS = Set.of("combustion_core", "combustors", "combustor_cover",
            "turbine_shell", "turbine_cover", "turbine_bands", "turbine_cover_bands",
            "exhaust_collector", "rear_exhaust");

    private record Vertex(float x, float y, float z, float u, float v,
                          float nx, float ny, float nz, int light, int rgb) {}

    private static final class Capture implements MeshSink {
        private List<Vertex> vertices;

        @Override
        public void vertex(VertexConsumer buffer, float x, float y, float z, float u, float v,
                           float nx, float ny, float nz, int packedLight) {
            vertex(buffer, x, y, z, u, v, nx, ny, nz, packedLight, WHITE);
        }

        @Override
        public void vertex(VertexConsumer buffer, float x, float y, float z, float u, float v,
                           float nx, float ny, float nz, int packedLight, int rgb) {
            vertices.add(new Vertex(x, y, z, u, v, nx, ny, nz, packedLight, rgb));
        }

        List<Vertex> draw(LargeGasTurbineRenderer renderer, Direction front, float angle, float heat) {
            vertices = new ArrayList<>(TRIANGLES * 4);
            PoseStack pose = new PoseStack();
            renderer.draw(pose, null, front, angle, heat, LIGHT, ROTOR_RGB);
            check(pose.last().pose().equals(new PoseStack().last().pose()), "Draw restores the caller's pose");
            check(vertices.size() == TRIANGLES * 4, "Full model submits one QUAD per triangle");
            return vertices;
        }
    }

    public static void main(String[] args) {
        List<ObjMesh.Group> groups = ObjMesh.load(LargeGasTurbineRenderer.MODEL).groups();
        check(groups.size() == 32, "Packaged OBJ has all 32 named groups");
        check(groups.stream().mapToInt(g -> g.triangles().length / 24).sum() == TRIANGLES,
                "Packaged OBJ uses the approved 6,400-triangle mesh");
        check(groups.stream().filter(g -> MOVING_GROUPS.contains(g.name()))
                .mapToInt(g -> g.triangles().length / 24).sum() == MOVING_TRIANGLES,
                "Rotor and coupling fit the 892-triangle animation budget");

        Capture capture = new Capture();
        LargeGasTurbineRenderer renderer = new LargeGasTurbineRenderer(capture);
        for (Direction front : Direction.values()) checkFacing(renderer, capture, groups, front);
        checkHeat(renderer, capture, groups);
        checkTurbineVbo(renderer, capture, groups);
        checkReactorVbo();
        checkTextures();
        checkMotion();
        System.out.println("LargeGasTurbineRenderTests: mesh, six facings, QUADS, tint, motion and both VBO draw plans passed.");
    }

    private static void checkTurbineVbo(LargeGasTurbineRenderer renderer, Capture capture, List<ObjMesh.Group> groups) {
        var batches = ObjVboMesh.partition(LargeGasTurbineRenderer.Batch.class, groups, LargeGasTurbineRenderer::batch);
        check(batches.size() == 7, "Turbine combines groups into seven draws");
        for (Direction front : Direction.values()) {
            var streamed = capture.draw(renderer, front, ANGLE, .75F);
            var firstVertex = new java.util.IdentityHashMap<ObjMesh.Group, Integer>();
            int offset = 0;
            for (var group : groups) {
                firstVertex.put(group, offset);
                offset += group.triangles().length / 6;
            }
            var pose = new PoseStack();
            LargeGasTurbineRenderer.drawVbo((batch, stack, light, rgb) -> {
                for (var group : batches.get(batch)) {
                    Vertex expected = streamed.get(firstVertex.get(group));
                    float[] mesh = group.triangles();
                    var vertex = stack.last().pose().transformPosition(new Vector3f(mesh[0], mesh[1], mesh[2]));
                    var normal = stack.last().normal().transform(new Vector3f(mesh[5], mesh[6], mesh[7])).normalize();
                    check(close(vertex, new Vector3f(expected.x, expected.y, expected.z)), "VBO position matches stream");
                    check(close(normal, new Vector3f(expected.nx, expected.ny, expected.nz)), "VBO normal matches stream");
                    check(rgb == expected.rgb && light == expected.light, "VBO retains rotor tint and hot-section light");
                }
            }, pose, front, ANGLE, .75F, LIGHT, ROTOR_RGB);
            check(pose.last().pose().equals(new PoseStack().last().pose()), "Turbine draw plan restores pose");
        }
    }

    private static void checkReactorVbo() {
        var groups = ObjMesh.load(IrradiationReactorRenderer.MODEL).groups();
        var batches = ObjVboMesh.partition(IrradiationReactorRenderer.Batch.class, groups,
                IrradiationReactorRenderer::batch);
        check(batches.size() == 7, "Reactor separates moving geometry and state lights");
        check(batches.get(IrradiationReactorRenderer.Batch.SPIN).stream().map(ObjMesh.Group::name)
                .collect(java.util.stream.Collectors.toSet()).equals(Set.of("sample_carousel", "target_capsules", "drive_shaft")),
                "Reactor spins only the carousel, targets and drive shaft");
        for (var state : List.of(IrradiationReactorRenderer.Batch.IDLE, IrradiationReactorRenderer.Batch.ACTIVE,
                IrradiationReactorRenderer.Batch.DONE)) {
            boolean running = state == IrradiationReactorRenderer.Batch.ACTIVE;
            float door = state == IrradiationReactorRenderer.Batch.IDLE ? 1F : 0F;
            int[] totals = new int[2];
            var pose = new PoseStack();
            IrradiationReactorRenderer.drawVbo((batch, stack, light, rgb) -> {
                totals[0]++;
                totals[1] += batches.get(batch).stream().mapToInt(group -> group.triangles().length / 24).sum();
                if (batch.ordinal() >= IrradiationReactorRenderer.Batch.ACTIVE.ordinal())
                    check(batch == state, "Only the expected status lamp batch is drawn");
                if (batch == IrradiationReactorRenderer.Batch.DOOR)
                    check(Math.abs(stack.last().pose().m31() - 1.24F * door) < EPSILON, "Door follows its lift");
                if (batch == IrradiationReactorRenderer.Batch.SPIN)
                    check((Math.abs(stack.last().pose().m02()) > EPSILON) == running, "Carousel only turns during work");
                check(light == LIGHT && rgb == WHITE, "Reactor keeps its existing light and color");
            }, pose, 20D, running, state == IrradiationReactorRenderer.Batch.DONE, door, LIGHT);
            check(totals[0] == 5 && totals[1] == (running ? 6476 : 6452), "Reactor draw plan counts visible triangles");
            check(pose.last().pose().equals(new PoseStack().last().pose()), "Reactor draw plan restores pose");
        }
    }

    private static void checkFacing(LargeGasTurbineRenderer renderer, Capture capture,
                                    List<ObjMesh.Group> groups, Direction front) {
        BlockPos controller = new BlockPos(11, 7, -4);
        AABB bounds = LargeGasTurbineRenderer.bounds(controller, front).inflate(EPSILON);
        PoseStack pose = new PoseStack();
        LargeGasTurbineRenderer.transform(pose, front);
        check(close(pose.last().pose().transformPosition(new Vector3f(0, 1.5F, 0)),
                worldPosition(front, 0, 1.5F, 0)), "Controller face anchor follows host orientation " + front);
        check(close(pose.last().pose().transformPosition(new Vector3f(0, 1.5F, 4)),
                worldPosition(front, 0, 1.5F, 4)), "Rear coupling aligns with host RU cell " + front);

        List<Vertex> idle = capture.draw(renderer, front, 0, 0);
        List<Vertex> rotating = capture.draw(renderer, front, ANGLE, 0);
        float cos = (float) Math.cos(Math.toRadians(ANGLE)), sin = (float) Math.sin(Math.toRadians(ANGLE));
        int offset = 0;
        for (ObjMesh.Group group : groups) {
            boolean moving = MOVING_GROUPS.contains(group.name());
            int rgb = group.name().equals("rotor") ? ROTOR_RGB : WHITE;
            float[] mesh = group.triangles();
            boolean changed = false;
            for (int i = 0; i < mesh.length; i += 24) {
                int out = offset + i / 6;
                checkQuad(rotating, out);
                for (int corner = 0; corner < 3; corner++) {
                    int in = i + corner * 8;
                    Vertex before = idle.get(out + corner);
                    Vertex after = rotating.get(out + corner);
                    Vector3f rest = worldPosition(front, mesh[in], mesh[in + 1], mesh[in + 2]);
                    check(close(new Vector3f(before.x, before.y, before.z), rest), "Idle vertices follow host orientation");
                    check(before.rgb == rgb && after.rgb == rgb, "Rotor tint affects only rotor vertices");
                    check(before.light == LIGHT && after.light == LIGHT, "Idle lighting preserves the sampled light");
                    check(before.u == mesh[in + 3] && before.v == mesh[in + 4]
                            && after.u == before.u && after.v == before.v, "Rotation preserves atlas coordinates");
                    check(bounds.contains(controller.getX() + after.x, controller.getY() + after.y,
                            controller.getZ() + after.z), "Culling box includes the rotated mesh");
                    if (moving) {
                        float x = mesh[in], y = mesh[in + 1] - 1.5F;
                        Vector3f expected = worldPosition(front, x * cos - y * sin, 1.5F + x * sin + y * cos, mesh[in + 2]);
                        check(close(new Vector3f(after.x, after.y, after.z), expected), "Rotor turns around the local shaft");
                    }
                    changed |= !before.equals(after);
                }
            }
            check(changed == moving, "Only moving groups change when the shaft turns: " + group.name());
            offset += mesh.length / 6;
        }
    }

    private static Vector3f worldPosition(Direction front, float x, float y, float z) {
        Direction right = front.getAxis().isVertical() ? Direction.EAST : front.getClockWise();
        Direction up = switch (front) {
            case UP -> Direction.SOUTH;
            case DOWN -> Direction.NORTH;
            default -> Direction.UP;
        };
        Direction back = front.getOpposite();
        return new Vector3f(.5F, .5F, .5F)
                .fma(x, new Vector3f(right.getStepX(), right.getStepY(), right.getStepZ()))
                .fma(y - 1.5F, new Vector3f(up.getStepX(), up.getStepY(), up.getStepZ()))
                .fma(z - .5F, new Vector3f(back.getStepX(), back.getStepY(), back.getStepZ()));
    }

    private static void checkQuad(List<Vertex> vertices, int offset) {
        Vertex a = vertices.get(offset), b = vertices.get(offset + 1), c = vertices.get(offset + 2);
        check(c.equals(vertices.get(offset + 3)), "The fourth QUADS vertex duplicates the third");
        Vector3f cross = new Vector3f(b.x - a.x, b.y - a.y, b.z - a.z)
                .cross(c.x - a.x, c.y - a.y, c.z - a.z);
        check(cross.dot(a.nx, a.ny, a.nz) > 0, "Triangle winding follows the rotated normal");
        check(Math.abs(a.nx * a.nx + a.ny * a.ny + a.nz * a.nz - 1) < EPSILON,
                "Rotated normals remain unit length");
    }

    private static void checkHeat(LargeGasTurbineRenderer renderer, Capture capture, List<ObjMesh.Group> groups) {
        List<Vertex> cold = capture.draw(renderer, Direction.NORTH, 0, 0);
        List<Vertex> hot = capture.draw(renderer, Direction.NORTH, 0, 1);
        int offset = 0;
        boolean warmed = false;
        for (ObjMesh.Group group : groups) {
            int end = offset + group.triangles().length / 6;
            for (int i = offset; i < end; i++) {
                Vertex before = cold.get(i), after = hot.get(i);
                if (before.equals(after)) continue;
                check(HOT_GROUPS.contains(group.name()), "Heat is confined to the combustion and exhaust sections");
                check(before.x == after.x && before.y == after.y && before.z == after.z
                        && before.u == after.u && before.v == after.v, "Heating preserves geometry and UVs");
                check((after.light >>> 16) == (LIGHT >>> 16), "Hot sections preserve sky light");
                if (after.rgb != WHITE && (after.light & 0xFFFF) > (LIGHT & 0xFFFF)) {
                    check((after.rgb >>> 16 & 255) > (after.rgb & 255), "Heat tint is warmer than the cold palette");
                    warmed = true;
                }
            }
            offset = end;
        }
        check(warmed, "A running turbine visibly warms and brightens the hot section");
    }

    private static void checkTextures() {
        Map.of("StainlessSteel", "stainless_steel", "Titanium", "titanium",
                "TungstenSteel", "tungsten_steel", "Adamantium", "adamantium").forEach((grade, name) -> {
            var texture = LargeGasTurbineRenderer.texture(grade);
            check(texture.getNamespace().equals("ssite")
                    && texture.getPath().equals("textures/machine/large_gas_turbine/" + name + ".png"),
                    "Host casing grade selects its baked atlas");
            check(LargeGasTurbineRenderTests.class.getResource("/assets/ssite/" + texture.getPath()) != null,
                    "Baked atlas is packaged: " + grade);
        });
    }

    private static void checkMotion() {
        LargeGasTurbineRenderer.Motion motion = new LargeGasTurbineRenderer.Motion();
        motion.advance(0, false);
        motion.advance(1, false);
        check(motion.angle() == 0 && motion.heat() == 0, "Idle turbine remains cold and still");
        motion.advance(1, true);
        check(motion.angle() == 0, "Starting at the same instant keeps the current phase");
        motion.advance(2, true);
        float runningAngle = motion.angle(), runningHeat = motion.heat();
        check(runningAngle > 0 && runningHeat > 0 && runningHeat < 1, "Running turbine spins up and heats gradually");
        motion.advance(2, false);
        check(motion.angle() == runningAngle, "Stopping at the same instant keeps the current phase");
        motion.advance(3, false);
        check(motion.angle() != runningAngle && motion.heat() > 0 && motion.heat() < runningHeat,
                "Turbine coasts and cools after shutdown");
        float coastingAngle = motion.angle();
        motion.advance(300, false);
        check(motion.angle() == coastingAngle && motion.heat() == 0, "An idle turbine cools during a long render gap");
    }

    private static boolean close(Vector3f a, Vector3f b) {
        return a.distanceSquared(b) < EPSILON * EPSILON;
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
