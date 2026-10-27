package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Vector3f;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Checks the actual renderer vertex stream without opening a window or initializing OpenGL. */
public final class SmallFurnaceRenderTests {
    private static final int LIGHT = 0x00D00090;
    private static final Method DRAW;
    private static final Method POSE_GROUP;
    private static final Class<?> KIND;

    static {
        try {
            DRAW = SmallFurnaceRenderer.class.getDeclaredMethod("draw", PoseStack.class,
                    VertexConsumer.class, ObjMesh.Group.class, int.class);
            DRAW.setAccessible(true);
            KIND = Class.forName(SmallFurnaceRenderer.class.getName() + "$Kind");
            POSE_GROUP = SmallFurnaceRenderer.class.getDeclaredMethod("poseGroup", PoseStack.class,
                    String.class, KIND, float.class);
            POSE_GROUP.setAccessible(true);
        } catch (ReflectiveOperationException error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    public static void main(String[] args) throws Exception {
        int groups = 0;
        for (String kind : new String[]{"aod", "var", "large_aod"}) {
            ObjMesh mesh = ObjMesh.load("/assets/ssite/obj/" + kind + ".obj");
            check(!mesh.groups().isEmpty(), kind + " model must load");
            Object modelKind = KIND.getEnumConstants()["aod".equals(kind) ? 0 : "var".equals(kind) ? 1 : 2];
            for (float yaw : new float[]{0, 90, 180, 270}) {
                for (float progress : new float[]{0, .15F, .3F, .45F, .6F, .8F, 1}) {
                    RecordingSink sink = new RecordingSink();
                    SmallFurnaceRenderer renderer = new SmallFurnaceRenderer(sink);
                    for (ObjMesh.Group group : mesh.groups()) {
                        PoseStack stack = new PoseStack();
                        stack.translate(.5, 0, .5);
                        stack.mulPose(Axis.YP.rotationDegrees(-yaw));
                        stack.translate(-.5, 0, -.5);
                        if (kind.equals("large_aod")) stack.translate(0, -1, 2);
                        POSE_GROUP.invoke(null, stack, group.name(), modelKind, progress);
                        int start = sink.vertices.size();
                        DRAW.invoke(renderer, stack, null, group, LIGHT);
                        verifyGroup(group, stack, sink.vertices.subList(start, sink.vertices.size()));
                        check(sink.vertices.size() % 4 == 0, "groups must not share an incomplete quad");
                        groups++;
                    }
                }
            }
            if (kind.equals("large_aod")) verifyLargeContacts(mesh);
            else verifyContacts(mesh);
        }
        System.out.println("SmallFurnaceRenderTests: " + groups
                + " group/pose checks passed; three models, four facings, seven animation poses, aligned contact faces.");
    }

    private static void verifyLargeContacts(ObjMesh mesh) {
        for (String name : new String[]{"heat_contact", "port_oxygen", "port_argon", "port_casting"}) {
            var group = mesh.groups().stream().filter(g -> g.name().equals(name)).findFirst().orElseThrow();
            float[] data = group.triangles();
            int axis = name.equals("heat_contact") ? 1 : name.equals("port_casting") ? 2 : 0;
            float plane = axis == 1 ? 0 : axis == 2 ? -2 : 3;
            Vector3f outward = axis == 1 ? new Vector3f(0,-1,0) : axis == 2 ? new Vector3f(0,0,-1) : new Vector3f(1,0,0);
            float total = 0;
            for (int i=0; i<data.length; i+=24) {
                if (Math.abs(data[i+axis]-plane)>1E-5 || Math.abs(data[i+8+axis]-plane)>1E-5 || Math.abs(data[i+16+axis]-plane)>1E-5) continue;
                Vector3f a = new Vector3f(data[i],data[i+1],data[i+2]);
                Vector3f b = new Vector3f(data[i+8],data[i+9],data[i+10]);
                Vector3f c = new Vector3f(data[i+16],data[i+17],data[i+18]);
                Vector3f cross = b.sub(a).cross(c.sub(a));
                check(cross.dot(outward)>0, name+": port faces outward");
                total += cross.length() * .5F;
            }
            near(total, axis == 1 ? 9 : 1, name+": full world-aligned contact plane");
        }
    }

    private static void verifyGroup(ObjMesh.Group group, PoseStack stack, List<Vertex> vertices) {
        float[] data = group.triangles();
        int triangles = data.length / 24;
        check(data.length % 24 == 0, group.name() + ": complete source triangles");
        check(vertices.size() == triangles * 4,
                group.name() + ": QUADS needs four submitted vertices per source triangle; expected "
                        + triangles * 4 + " but got " + vertices.size());
        for (int t = 0; t < triangles; t++) {
            List<Vertex> quad = vertices.subList(t * 4, t * 4 + 4);
            // Blaze3D expands each quad with indices 0,1,2,2,3,0. The second must be degenerate.
            check(area(quad.get(2), quad.get(3), quad.get(0)) < 1E-8F,
                    group.name() + ": no spurious triangle joining unrelated OBJ faces");
            check(quad.get(2).equals(quad.get(3)), group.name() + ": duplicate full last corner, including UV/light/normal");
            for (int corner = 0; corner < 3; corner++) {
                int i = t * 24 + corner * 8;
                Vertex actual = quad.get(corner);
                Vector3f expected = stack.last().pose().transformPosition(new Vector3f(data[i], data[i + 1], data[i + 2]));
                near(actual.x, expected.x, "position x");near(actual.y, expected.y, "position y");near(actual.z, expected.z, "position z");
                near(actual.u, data[i + 3], "texture u");near(actual.v, data[i + 4], "texture v");
                check(actual.light == LIGHT, "packed light is preserved");
                near(actual.nx * actual.nx + actual.ny * actual.ny + actual.nz * actual.nz, 1, "unit normal");
            }
            Vector3f cross = edge(quad.get(0), quad.get(1)).cross(edge(quad.get(0), quad.get(2)));
            check(cross.dot(quad.get(0).nx, quad.get(0).ny, quad.get(0).nz) > 0,
                    group.name() + ": winding must agree with transformed normal for backface culling");
        }
    }

    private static void verifyContacts(ObjMesh mesh) {
        for (String name : new String[]{"bottom", "north", "east", "south", "west"}) {
            ObjMesh.Group group = mesh.groups().stream().filter(g -> g.name().equals("contact_" + name)).findFirst().orElseThrow();
            RecordingSink sink = new RecordingSink();
            try {
                DRAW.invoke(new SmallFurnaceRenderer(sink), new PoseStack(), null, group, LIGHT);
            } catch (ReflectiveOperationException error) {
                throw new AssertionError(error);
            }
            float area = 0;
            Vector3f outward = switch (name) {
                case "bottom" -> new Vector3f(0, -1, 0);
                case "north" -> new Vector3f(0, 0, -1);
                case "east" -> new Vector3f(1, 0, 0);
                case "south" -> new Vector3f(0, 0, 1);
                default -> new Vector3f(-1, 0, 0);
            };
            for (int i = 0; i < sink.vertices.size(); i += 4) {
                Vertex a = sink.vertices.get(i), b = sink.vertices.get(i + 1), c = sink.vertices.get(i + 2);
                Vector3f cross = edge(a, b).cross(edge(a, c));
                check(cross.dot(outward) > 0, name + ": visible from outside the machine");
                area += cross.length() * .5F;
            }
            near(area, name.equals("bottom") ? 1 : .75F, name + ": full contact area survives the QUADS pass");
        }
    }

    private static Vector3f edge(Vertex a, Vertex b) {return new Vector3f(b.x - a.x, b.y - a.y, b.z - a.z);}
    private static float area(Vertex a, Vertex b, Vertex c) {return edge(a, b).cross(edge(a, c)).length() * .5F;}
    private static void near(float actual, float expected, String message) {check(Float.isFinite(actual) && Math.abs(actual - expected) < 1E-5F, message);}
    private static void check(boolean condition, String message) {if (!condition) throw new AssertionError(message);}

    private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {}
    private static final class RecordingSink implements MeshSink {
        final List<Vertex> vertices = new ArrayList<>();
        @Override
        public void vertex(VertexConsumer buffer, float x, float y, float z, float u, float v,
                           float nx, float ny, float nz, int packedLight) {
            vertices.add(new Vertex(x, y, z, u, v, nx, ny, nz, packedLight));
        }
    }
}
