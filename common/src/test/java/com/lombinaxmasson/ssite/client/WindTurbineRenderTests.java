package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Checks the actual rotor vertex stream and animation without a window or a world. */
public final class WindTurbineRenderTests {
    private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {}

    public static void main(String[] args) {
        var rotor = ObjMesh.load(WindTurbineRenderer.MODEL);
        int triangles = rotor.groups().stream().mapToInt(group -> group.triangles().length / 24).sum();
        check(rotor.groups().size() == 4 && triangles > 0 && triangles <= 330, "Packaged rotor keeps its triangle budget");
        var head = ObjMesh.load("/assets/ssite/obj/wind_turbine_head.obj");
        check(!head.groups().isEmpty(), "Static head OBJ is packaged for the loader model");
        for (var group : head.groups()) {
            float[] data = group.triangles();
            for (int i = 0; i < data.length; i += 8)
                for (int axis = 0; axis < 3; axis++)
                    check(data[i + axis] >= 0 && data[i + axis] <= 1, "Head stays inside its one-block collision shape");
        }
        List<Vertex> vertices = new ArrayList<>();
        WindTurbineRenderer renderer = new WindTurbineRenderer((buffer, x, y, z, u, v, nx, ny, nz, light) ->
                vertices.add(new Vertex(x, y, z, u, v, nx, ny, nz)));
        check(!renderer.shouldRenderOffScreen(null), "Rotor keeps normal frustum culling");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            var bounds = WindTurbineRenderer.bounds(BlockPos.ZERO, facing).inflate(1E-5);
            for (int angle = 0; angle < 360; angle += 15) {
                vertices.clear();
                renderer.draw(new PoseStack(), null, facing, angle, 0x00D00090);
                check(vertices.size() == triangles * 4, "Only rotor triangles enter the dynamic QUADS stream");
                for (int i = 0; i < vertices.size(); i += 4) {
                    Vertex a = vertices.get(i), b = vertices.get(i + 1), c = vertices.get(i + 2);
                    check(c.equals(vertices.get(i + 3)), "Triangle closes with a duplicate fourth vertex");
                    Vector3f cross = new Vector3f(b.x-a.x, b.y-a.y, b.z-a.z)
                            .cross(new Vector3f(c.x-a.x, c.y-a.y, c.z-a.z));
                    check(cross.dot(a.nx, a.ny, a.nz) > 0, "Visible winding follows the rotated normal");
                }
                for (Vertex v : vertices) {
                    check(bounds.contains(v.x, v.y, v.z), "Full swept AABB includes every rotated vertex");
                    check(Math.abs(v.nx*v.nx + v.ny*v.ny + v.nz*v.nz - 1) < 1E-4, "Normals remain unit length");
                    check(v.u >= 0 && v.u <= 1 && v.v >= 0 && v.v <= 1, "UV remains in the atlas");
                }
            }
        }
        var rotation = new WindTurbineRenderer.Rotation();
        check(rotation.advance(0, 0) == 0 && rotation.advance(20, 0) == 0, "Stopped wind does not rotate");
        check(rotation.advance(20, 1000) == 0, "Changing wind speed does not jump the phase");
        double spinning = rotation.advance(30, 1000);
        check(spinning > 0 && spinning < 30, "Spin-up interpolates toward the target");
        check(rotation.advance(30, 0) == spinning, "Stopping preserves the current phase");
        double stopped = rotation.advance(230, 0);
        check(rotation.advance(240, 0) == stopped, "Returning after a long gap respects stopped wind");
        reload();
        System.out.println("WindTurbineRenderTests: four facings, 24 rotor angles, swept bounds, QUADS, interpolation and mesh reload passed.");
    }

    private static void reload() {
        String model = "/ssite-test/wind-reload.obj";
        String triangle = "v 0 0 0\nv 1 0 0\nv 0 1 0\no first\nf 1 2 3\n";
        ObjMesh.reload(model, new ByteArrayInputStream(triangle.getBytes(StandardCharsets.UTF_8)));
        check(ObjMesh.load(model).groups().get(0).name().equals("first"), "Resource stream populates the mesh cache");
        ObjMesh.reload(model, new ByteArrayInputStream(triangle.replace("first", "second").getBytes(StandardCharsets.UTF_8)));
        check(ObjMesh.load(model).groups().get(0).name().equals("second"), "Reload replaces the old cached mesh");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
