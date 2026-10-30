"""Original block-local HU pipe and modular heating plate, using the existing FBC pixel atlas."""
from __future__ import annotations

import base64
import json
import math
from pathlib import Path

from build_fluidized_bed import Mesh, add, sub, cross, dot, MATERIALS
from fluidized_bed_materials import build_atlas

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "art" / "heat_transport"
# Minecraft Direction.get3DDataValue order. Geometry is in absolute block-local axes.
DIRECTIONS = {"down": (1, -1, 0), "up": (1, 1, 1), "north": (2, -1, 2),
              "south": (2, 1, 3), "west": (0, -1, 4), "east": (0, 1, 5)}


def axis_box(mesh, axis, lo, hi, outer, material, group):
    a = [.5-outer, .5-outer, .5-outer]
    b = [.5+outer, .5+outer, .5+outer]
    a[axis], b[axis] = lo, hi
    mesh.box(a, b, material, group)


def ring(mesh, axis, lo, hi, outer, inner, material, group):
    other = [i for i in range(3) if i != axis]
    for fixed, span in (other, other[::-1]):
        for low, high in ((.5-outer, .5-inner), (.5+inner, .5+outer)):
            a = [0., 0., 0.]; b = [0., 0., 0.]
            a[axis], b[axis] = lo, hi
            a[fixed], b[fixed] = low, high
            a[span], b[span] = .5-inner, .5+inner
            mesh.box(a, b, material, group)
    # Fill the four corners as separate square bolt pads.
    for u in (-1, 1):
        for v in (-1, 1):
            a = [0., 0., 0.]; b = [0., 0., 0.]
            a[axis], b[axis] = lo, hi
            for dim, sign in zip(other, (u, v)):
                a[dim], b[dim] = (.5-outer, .5-inner) if sign < 0 else (.5+inner, .5+outer)
            mesh.box(a, b, "bolt", group)


def build_duct():
    mesh = Mesh()
    mesh.box((.22,.22,.22),(.78,.78,.78),"insulation","core_caps")
    for i, (_, ps, normal, uv, material) in enumerate(mesh.faces):
        direction = next(name for name, (axis, sign, _) in DIRECTIONS.items() if normal[axis] * sign > .99)
        mesh.faces[i] = ("cap_" + direction, ps, normal, uv, material)
    # A rigid central cage makes bends and T junctions read as one insulated assembly.
    for axis in range(3):
        other = [i for i in range(3) if i != axis]
        for u in (.20, .74):
            for v in (.20, .74):
                a = [.20, .20, .20]; b = [.80, .80, .80]
                for dim, low in zip(other, (u, v)):
                    a[dim], b[dim] = low, low+.06
                mesh.box(a, b, "edge", "core")
    for name, (axis, sign, _) in DIRECTIONS.items():
        group = "arm_" + name
        lo, hi = (0, .5) if sign < 0 else (.5, 1)
        axis_box(mesh, axis, lo, hi, .25, "insulation", group)
        lo, hi = (.015, .095) if sign < 0 else (.905, .985)
        ring(mesh, axis, lo, hi, .30, .25, "edge", group)
    return mesh


def build_plate():
    mesh = Mesh()
    mesh.box((0,0,0),(1,.12,1),"dark","plate_base")
    mesh.box((.02,.12,.02),(.98,.87,.98),"refractory","plate_core")
    # Four armored side panels; horizontal HU connections meet the block center.
    for axis in (0,2):
        for low, high in ((0,.035),(.965,1)):
            a = [.06,.22,.06]; b = [.94,.72,.94]
            a[axis], b[axis] = low, high
            mesh.box(a,b,"ribbed","plate_jacket")
        for low, high in ((0,.055),(.945,1)):
            a = [0,.76,0]; b = [1,.85,1]
            a[axis], b[axis] = low, high
            mesh.box(a,b,"edge","plate_frame")
    for x in (0,.91):
        for z in (0,.91):
            mesh.box((x,.12,z),(x+.09,.76,z+.09),"plate","plate_frame")
    mesh.box((0,.87,0),(1,1,1),"ceramic","heat_contact")
    for index,(group,ps,n,uv,material) in enumerate(mesh.faces):
        if group=="heat_contact" and n[1]<.999:
            mesh.faces[index]=("plate_roof_edges",ps,n,uv,material)
    return mesh


def visible(group, mask):
    if group.startswith("arm_"):
        return bool(mask & (1 << DIRECTIONS[group[4:]][2]))
    if group.startswith("cap_"):
        return not mask & (1 << DIRECTIONS[group[4:]][2])
    return True


def validate(mesh, compact, plate=False):
    points=[]; triangles=0; contact_area=0
    for group,ps,n,uv,material in mesh.faces:
        assert material in MATERIALS and abs(dot(n,n)-1)<1e-6
        assert all(math.isfinite(x) and -1e-6<=x<=1+1e-6 for p in ps for x in p)
        assert all(0<=x<=1 for p in uv for x in p)
        for i in range(1,len(ps)-1):
            area_vector=cross(sub(ps[i],ps[0]),sub(ps[i+1],ps[0]))
            assert dot(area_vector,n)>1e-9,group
            triangles+=1
            if group=="heat_contact":
                assert n==(0.,1.,0.) and all(p[1]==1 for p in ps)
                contact_area+=math.sqrt(dot(area_vector,area_vector))/2
        points.extend(ps)
    if plate: assert contact_area==1
    else:
        assert set(compact)=={"core"}|{"arm_"+d for d in DIRECTIONS}|{"cap_"+d for d in DIRECTIONS}
        for mask in range(64):
            for name in DIRECTIONS:
                assert visible("arm_"+name,mask)!=visible("cap_"+name,mask)
    return dict(bounds=[[min(p[i] for p in points) for i in range(3)], [max(p[i] for p in points) for i in range(3)]],
                triangles=triangles,groups=len(compact),heat_contact_area=contact_area)


def append_instance(target, source, offset, prefix, mask=63):
    for group,ps,n,uv,material in source.faces:
        if visible(group,mask):
            target.faces.append((prefix+"_"+group,[add(p,offset) for p in ps],n,uv,material))


def write_preview(mesh, name, title, subtitle, hidden=()):
    compact=mesh.export(name,out=OUT)
    points=[p for _,ps,_,_,_ in mesh.faces for p in ps]
    stats=dict(bounds=[[min(p[i] for p in points) for i in range(3)],[max(p[i] for p in points) for i in range(3)]],
               groups=len(compact),triangles=sum(len(d["i"])//3 for d in compact.values()))
    data=dict(meshes=compact,texture=base64.b64encode((OUT/"heat_transport_atlas.png").read_bytes()).decode(),
              stats=stats,cutaway_hide=hidden,ports=[],title=title,subtitle=subtitle)
    template=Path(__file__).with_name("fluidized_bed_viewer.html").read_text(encoding="utf-8")
    fragment=template.replace("/*__MODEL_DATA__*/",json.dumps(data,ensure_ascii=False,separators=(",",":")))
    css=Path(__file__).with_name("viewer_shell.css").read_text(encoding="utf-8")
    (OUT/(name+".html")).write_text('<!doctype html><html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>'+title+'</title><style>'+css+'</style></head><body>'+fragment+'</body></html>',encoding="utf-8")


def main():
    OUT.mkdir(parents=True,exist_ok=True)
    build_atlas().save(OUT/"heat_transport_atlas.png")
    (OUT/"fluidized_bed_combustor.mtl").write_text("newmtl fluidized_bed_atlas\nKd 1 1 1\nmap_Kd heat_transport_atlas.png\n",encoding="utf-8")
    duct,plate=build_duct(),build_plate()
    stats={name:validate(mesh,mesh.export(name,out=OUT),name=="heating_plate")
           for name,mesh in (("heat_duct",duct),("heating_plate",plate))}
    # A routed example: side-fed 3x3 independent heating blocks with a rising pipe branch.
    demo=Mesh()
    for x in range(3):
        for z in range(3): append_instance(demo,plate,(x,0,z),f"plate_{x}_{z}")
    nodes={(-3,0,1),(-2,0,1),(-1,0,1),(-2,0,0),(-2,0,2),(-2,1,2)}
    plate_cells={(x,0,z) for x in range(3) for z in range(3)}
    for p in sorted(nodes):
        mask=0
        for _,(axis,sign,bit) in DIRECTIONS.items():
            q=list(p); q[axis]+=sign
            if tuple(q) in nodes or axis!=1 and tuple(q) in plate_cells: mask|=1<<bit
        append_instance(demo,duct,p,"duct_"+"_".join(map(str,p)),mask)
    hidden=["plate_0_0_heat_contact","plate_1_0_heat_contact","plate_2_0_heat_contact"]
    write_preview(demo,"preview","HU 输热管与独立加热板","可自由布线的六向保温管 / 独立板可拼成 3×3 供热面",hidden)
    # Close-up of representative masks and a full-height individual heating block.
    detail=Mesh()
    for index,mask in enumerate((12,40,42,63)):
        append_instance(detail,duct,(index*1.4,0,0),f"sample_{index}",mask)
    append_instance(detail,plate,(2.1,0,1.8),"heating_plate")
    write_preview(detail,"components","HU 输热组件 · 近景","直通 / 弯头 / 三通 / 六向接头 / 独立上供热板",["heating_plate_heat_contact"])
    (OUT/"model.json").write_text(json.dumps(dict(schema_version=1,units="blocks",models=stats,direction_bits={k:v[2] for k,v in DIRECTIONS.items()},
        runtime_texture="ssite:textures/machine/fluidized_bed_combustor.png"),ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(stats,ensure_ascii=False))


if __name__=="__main__": main()
