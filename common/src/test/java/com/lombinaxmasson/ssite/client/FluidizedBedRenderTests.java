package com.lombinaxmasson.ssite.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Exercises the actual renderer's QUADS output and all four world-aligned heating faces. */
public final class FluidizedBedRenderTests {
    private static final int LIGHT = 0x00D00090;
    private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {}

    public static void main(String[] args) {
        var mesh = ObjMesh.load(FluidizedBedRenderer.MODEL);
        check(!mesh.groups().isEmpty(), "Runtime OBJ must be packaged");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            List<Vertex> vertices = new ArrayList<>();
            MeshSink sink = (consumer, x, y, z, u, v, nx, ny, nz, light) ->
                    vertices.add(new Vertex(x,y,z,u,v,nx,ny,nz,light));
            var renderer = new FluidizedBedRenderer(sink);
            PoseStack pose = new PoseStack();
            FluidizedBedRenderer.transform(pose, facing);
            float heatArea = 0;
            int heatFaces = 0;
            for (var group : mesh.groups()) {
                vertices.clear();
                renderer.draw(pose, null, group, LIGHT);
                check(vertices.size() == group.triangles().length / 24 * 4, "Complete QUADS stream");
                for (int i=0; i<vertices.size(); i+=4) {
                    var a=vertices.get(i); var b=vertices.get(i+1); var c=vertices.get(i+2);
                    check(c.equals(vertices.get(i+3)), "Every triangle ends C,C");
                    for (var v : vertices.subList(i,i+4)) {
                        check(Float.isFinite(v.x+v.y+v.z+v.nx+v.ny+v.nz), "Finite renderer output");
                        check(v.u>=0 && v.u<=1 && v.v>=0 && v.v<=1, "Atlas UV bounds");
                        check(v.light == LIGHT, "Light reaches every vertex");
                    }
                    Vector3f cross = new Vector3f(b.x-a.x,b.y-a.y,b.z-a.z)
                            .cross(new Vector3f(c.x-a.x,c.y-a.y,c.z-a.z));
                    check(cross.dot(a.nx,a.ny,a.nz)>0, "Winding agrees with normals");
                    if (group.name().equals("heat_contact")) {
                        heatFaces++;
                        heatArea += cross.length()*.5F;
                        for (var v : vertices.subList(i,i+3)) {
                            check(Math.abs(v.y-2)<1E-5, "Heat surface is at Y=2");
                            check(Math.abs(v.ny-1)<1E-5, "Heat surface faces up");
                            check(Math.abs(v.x-Math.round(v.x))<1E-4 && Math.abs(v.z-Math.round(v.z))<1E-4,
                                    "Heating tiles stay aligned to world block boundaries");
                        }
                    }
                }
            }
            check(heatFaces==18, "Nine distinct two-triangle heat tiles");
            check(Math.abs(heatArea-9)<1E-4, "Full 3x3 heating surface in every orientation");
        }
        System.out.println("FluidizedBedRenderTests: four facings, QUADS, light, winding and nine heating tiles passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
