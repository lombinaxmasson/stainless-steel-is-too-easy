package com.lombinaxmasson.ssite.client;

import com.lombinaxmasson.ssite.machine.FluidizedBedStructure;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Actual QUADS output, tower outlet placement, all 64 pipe masks and independent heating faces. */
public final class FluidizedBedRenderTests {
    private static final int LIGHT = 0x00D00090;
    private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {}

    public static void main(String[] args) {
        tower(); pipes(); heatingPlates();
        System.out.println("FluidizedBedRenderTests: four tower facings, rear outlet, 64 pipe masks and modular heating faces passed.");
    }

    private static MeshSink recorder(List<Vertex> vertices) {
        return (consumer,x,y,z,u,v,nx,ny,nz,light) -> vertices.add(new Vertex(x,y,z,u,v,nx,ny,nz,light));
    }

    private static void tower() {
        var mesh = ObjMesh.load(FluidizedBedRenderer.MODEL);
        check(!mesh.groups().isEmpty(), "Runtime tower OBJ must be packaged");
        check(mesh.groups().stream().noneMatch(g -> g.name().startsWith("heat_bank")
                || g.name().equals("heat_contact") || g.name().equals("hot_gas_duct")), "Tower has no fixed external duct or heating table");
        check(mesh.groups().stream().anyMatch(g -> g.name().equals("crossover_flue")), "Internal cyclone duct remains visible");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            List<Vertex> vertices = new ArrayList<>();
            var renderer = new FluidizedBedRenderer(recorder(vertices));
            PoseStack pose = new PoseStack();
            FluidizedBedRenderer.transform(pose, facing);
            BlockPos target = FluidizedBedStructure.heatOutletTarget(BlockPos.ZERO,facing);
            Direction back = facing.getOpposite();
            Vector3f outlet = pose.last().pose().transformPosition(new Vector3f(0,.5F,2.5F));
            check(outlet.distance(new Vector3f(target.getX()+.5F-back.getStepX()*.5F,
                    target.getY()+.5F,target.getZ()+.5F-back.getStepZ()*.5F))<1E-5,
                    "Rear outlet center meets the adjacent pipe face in all four facings");
            boolean outletFace = false;
            for (var group : mesh.groups()) {
                vertices.clear(); renderer.draw(pose,null,group,LIGHT);
                check(vertices.size()==group.triangles().length/24*4, "Complete tower QUADS stream");
                verifyQuads(vertices);
                if (group.name().equals("skid"))
                    for (var v : vertices) check(v.y<=.28001F, "Skid matches the existing 0.28-high foundation shape");
                if (group.name().equals("heat_outlet")) {
                    for (var v : vertices) {
                        float plane = (v.x-outlet.x)*back.getStepX()+(v.z-outlet.z)*back.getStepZ();
                        if (Math.abs(plane)<1E-5 && v.nx*back.getStepX()+v.nz*back.getStepZ()>.99F) outletFace=true;
                    }
                }
            }
            check(outletFace,"Visible rear HU face reaches the external pipe boundary");
        }
    }

    private static void pipes() {
        var mesh=ObjMesh.load(HeatTransportRenderer.DUCT_MODEL);
        check(mesh.groups().size()==13,"Duct has core, six end caps and six arms");
        for (int mask=0;mask<64;mask++) {
            List<Vertex> vertices=new ArrayList<>();
            new HeatTransportRenderer(recorder(vertices)).draw(new PoseStack(),null,mesh,mask,LIGHT);
            int expected=0;
            for (var group:mesh.groups()) if (HeatTransportRenderer.groupVisible(group.name(),mask))
                expected+=group.triangles().length/24*4;
            check(vertices.size()==expected,"Only connected arms and unconnected caps enter the stream");
            verifyQuads(vertices);
            for (var v:vertices) check(v.x>=0 && v.x<=1 && v.y>=0 && v.y<=1 && v.z>=0 && v.z<=1,
                    "Pipe model stays within its own block");
            for (Direction direction:Direction.values()) {
                boolean connected=(mask & 1<<direction.get3DDataValue())!=0;
                check(HeatTransportRenderer.groupVisible("arm_"+direction.getName(),mask)==connected,"Direction bit selects the correct arm");
                check(HeatTransportRenderer.groupVisible("cap_"+direction.getName(),mask)!=connected,"Connected core face is uncapped");
                if (connected) {
                    float boundary=direction.getAxisDirection()==Direction.AxisDirection.POSITIVE?1:0;
                    boolean reaches=vertices.stream().anyMatch(v -> Math.abs((switch(direction.getAxis()) {
                        case X -> v.x; case Y -> v.y; case Z -> v.z;
                    })-boundary)<1E-6);
                    check(reaches,"Connected arm reaches the adjacent block face");
                }
            }
        }
    }

    private static void heatingPlates() {
        var mesh=ObjMesh.load(HeatTransportRenderer.PLATE_MODEL);
        var contact=mesh.groups().stream().filter(g->g.name().equals("heat_contact")).findFirst().orElseThrow();
        check(contact.triangles().length==48,"One block has one two-triangle heating face");
        List<Vertex> vertices=new ArrayList<>();
        var renderer=new FluidizedBedRenderer(recorder(vertices));
        float totalArea=0;
        for (int x=0;x<3;x++) for(int z=0;z<3;z++) {
            PoseStack pose=new PoseStack(); pose.translate(x,0,z);
            for(var group:mesh.groups()) {
                vertices.clear(); renderer.draw(pose,null,group,LIGHT); verifyQuads(vertices);
                for(var v:vertices) check(v.y>=0 && v.y<=1,"No plate geometry enters the furnace space above");
            }
            vertices.clear(); renderer.draw(pose,null,contact,LIGHT);
            for(int i=0;i<vertices.size();i+=4) {
                var a=vertices.get(i); var b=vertices.get(i+1); var c=vertices.get(i+2);
                totalArea+=new Vector3f(b.x-a.x,b.y-a.y,b.z-a.z).cross(new Vector3f(c.x-a.x,c.y-a.y,c.z-a.z)).length()*.5F;
                for(var v:vertices.subList(i,i+3)) check(v.y==1 && v.ny==1
                        && Math.abs(v.x-Math.round(v.x))<1E-5 && Math.abs(v.z-Math.round(v.z))<1E-5,
                        "Independent heating face is up-facing and aligned to exact block boundaries");
            }
        }
        check(Math.abs(totalArea-9)<1E-5,"Nine independently placed plates supply a complete 3x3 contact surface");
    }

    private static void verifyQuads(List<Vertex> vertices) {
        check(vertices.size()%4==0,"Complete QUADS stream");
        for(int i=0;i<vertices.size();i+=4) {
            var a=vertices.get(i); var b=vertices.get(i+1); var c=vertices.get(i+2);
            check(c.equals(vertices.get(i+3)),"Every OBJ triangle ends C,C");
            for(var v:vertices.subList(i,i+4)) {
                check(Float.isFinite(v.x+v.y+v.z+v.nx+v.ny+v.nz),"Finite renderer output");
                check(v.u>=0 && v.u<=1 && v.v>=0 && v.v<=1,"Atlas UV bounds");
                check(v.light==LIGHT,"Packed light reaches every vertex");
            }
            check(new Vector3f(b.x-a.x,b.y-a.y,b.z-a.z).cross(new Vector3f(c.x-a.x,c.y-a.y,c.z-a.z))
                    .dot(a.nx,a.ny,a.nz)>0,"Winding agrees with normals");
        }
    }

    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}
