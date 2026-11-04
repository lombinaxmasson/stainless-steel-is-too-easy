package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Packaged OBJ/tessellation checks for both chemical-looping beds. */
public final class ChemicalLoopingRenderTests {
    private static final int LIGHT = 0x00D00090;
    private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {}

    public static void main(String[] args) {
        model("ssite.obj.fuel", ChemicalLoopingRenderer.FUEL_MODEL, 1.5F,
                "windbox", "bed", "particles", "ku_input", "heat_out", "feed_ignition", "fluid_out");
        model("ssite.obj.air", ChemicalLoopingRenderer.AIR_MODEL, 3.0F,
                "windbox", "bed", "particles", "ku_input", "heat_out", "feed_input", "air_input", "exhaust");
        resources();
        System.out.println("ChemicalLoopingRenderTests: both OBJ vessels, atlas resources and four facings passed.");
    }

    private static void model(String label, String path, float height, String... requiredGroups) {
        var mesh = ObjMesh.load(path);
        check(!mesh.groups().isEmpty(), label + " OBJ must be packaged");
        var names = mesh.groups().stream().map(ObjMesh.Group::name).toList();
        for (String required : requiredGroups) {
            check(names.contains(required), label + " OBJ needs group " + required);
        }
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            PoseStack pose = new PoseStack();
            ChemicalLoopingRenderer.transform(pose, facing);
            List<Vertex> vertices = new ArrayList<>();
            var renderer = new ChemicalLoopingRenderer((consumer, x, y, z, u, v, nx, ny, nz, light) ->
                    vertices.add(new Vertex(x, y, z, u, v, nx, ny, nz, light)));
            for (var group : mesh.groups()) {
                vertices.clear();
                renderer.draw(pose, null, group, LIGHT);
                check(vertices.size() == group.triangles().length / 24 * 4, label + " complete QUADS stream");
                verifyQuads(vertices);
                for (var v : vertices) {
                    check(v.y >= -0.001F && v.y <= height + 0.001F, label + " stays inside its height envelope");
                    check(v.x >= -0.011F && v.x <= 1.011F && v.z >= -0.011F && v.z <= 1.011F,
                            label + " stays inside its 1x1 footprint");
                }
            }
        }
    }

    private static void resources() {
        for (String path : new String[]{
                "/assets/ssite/textures/machine/fuel_reactor.png",
                "/assets/ssite/textures/machine/fuel_reactor.png.mcmeta",
                "/assets/ssite/textures/machine/air_reactor.png",
                "/assets/ssite/textures/machine/air_reactor.png.mcmeta"}) {
            check(ChemicalLoopingRenderer.class.getResourceAsStream(path) != null, "missing runtime resource " + path);
        }
    }

    private static void verifyQuads(List<Vertex> vertices) {
        check(vertices.size() % 4 == 0, "complete QUADS stream");
        for (int i = 0; i < vertices.size(); i += 4) {
            var a = vertices.get(i);
            var b = vertices.get(i + 1);
            var c = vertices.get(i + 2);
            check(c.equals(vertices.get(i + 3)), "every OBJ triangle ends C,C");
            for (var v : vertices.subList(i, i + 4)) {
                check(Float.isFinite(v.x + v.y + v.z + v.nx + v.ny + v.nz), "finite renderer output");
                check(v.u >= 0 && v.u <= 1 && v.v >= 0 && v.v <= 1, "atlas UV bounds");
                check(v.light == LIGHT, "packed light reaches every vertex");
            }
            check(new Vector3f(b.x - a.x, b.y - a.y, b.z - a.z)
                    .cross(new Vector3f(c.x - a.x, c.y - a.y, c.z - a.z))
                    .dot(a.nx, a.ny, a.nz) > 0, "winding agrees with normals");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
