"""Build the original SSITE large AOD art prototype. Requires Python 3 + Pillow.

One OBJ unit = one Minecraft block; +Y up, front = -Z. No HBM data is copied.
Run from any directory. Generated art lives outside the runtime resource tree.
"""
from __future__ import annotations

import argparse
import base64
import json
import math
import random
from collections import defaultdict
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "art" / "large_aod"
TILE = 32
PALETTE = {
    "plate": (79, 83, 82), "shell": (91, 94, 89),
    "dark": (39, 45, 46), "edge": (116, 121, 117),
    "refractory": (143, 99, 68), "hazard": (194, 151, 38),
    "yellow": (189, 146, 43), "grate": (67, 74, 73),
    "pipe": (102, 113, 111), "copper": (140, 89, 57),
    "soot": (40, 37, 34), "molten": (244, 124, 34),
    "panel": (48, 61, 59), "white": (172, 172, 148),
    "coolant": (58, 109, 120), "red": (142, 65, 44),
    "port_oxygen": (110, 154, 161), "port_argon": (145, 128, 161),
    "heat": (83, 77, 65), "nameplate": (62, 67, 61), "port_casting": (194, 129, 66),
}
MATERIALS = list(PALETTE)
LID = dict(group="lid", pivot=[.5, 5.1, 1.84], axis=[1, 0, 0], open_angle_degrees=105)
PORTS = [
    dict(id="heat", label="底部 HU 受热面", normal=[0,-1,0], face="DOWN / −Y", kind="HU 输入", code="HU · 3×3", surface="solid",
         bounds=[[-1,0,-1],[2,0,2]], area=9, groups=["heat_contact"], plane="Y = 0", size="3 × 3",
         connection_cells=[[x,-1,z] for x in range(-1,2) for z in range(-1,2)], media="九格连续平面，下方直接衔接燃烧室"),
    dict(id="oxygen", label="炉身氧气接口", normal=[1,0,0], face="EAST / +X", kind="气体输入", code="O2", surface="solid",
         bounds=[[3,1,-1],[3,2,0]], area=1, groups=["port_oxygen"], plane="X = 3", size="1 × 1",
         connection_cells=[[3,1,-1]], media="圆管颈 + 薄方形法兰；整格平端面接气管"),
    dict(id="argon", label="炉身氩气接口", normal=[1,0,0], face="EAST / +X", kind="气体输入", code="Ar", surface="solid",
         bounds=[[3,1,1],[3,2,2]], area=1, groups=["port_argon"], plane="X = 3", size="1 × 1",
         connection_cells=[[3,1,1]], media="圆管颈 + 薄方形法兰；整格平端面接气管"),
    dict(id="casting", label="侧壁浇铸接口", normal=[0,0,-1], face="NORTH / −Z", kind="浇铸输出", code="浇铸", surface="solid",
         bounds=[[0,1,-2],[1,2,-2]], area=1, groups=["port_casting"], plane="Z = −2", size="1 × 1",
         connection_cells=[[0,1,-3]], mold_cells=[[0,0,-3]], media="外接浇铸口，下方接模具；按大型坩埚的侧壁浇铸布局"),
    dict(id="feed", label="顶部投料开口", normal=[0,1,0], face="UP / +Y", kind="开盖投料", code="投料", surface="opening", active_when="lid_open",
         bounds=[[0,5,0],[1,5,1]], area=1, groups=[], plane="Y = 5", size="1 × 1 通道",
         connection_cells=[[0,5,0]], media="开盖后中央一格畅通；周围保留圆形耐火炉口"),
]


def atlas():
    rng = random.Random(1127)
    result = Image.new("RGB", (256, 128), (29, 34, 34))
    for index, (name, color) in enumerate(PALETTE.items()):
        tile = Image.new("RGB", (TILE, TILE))
        for y in range(TILE):
            for x in range(TILE):
                noise = rng.choice([-4, -2, 0, 0, 0, 2, 4])
                scorch = -int(max(0, 8-y) * 1.5) if name == "shell" else 0
                tile.putpixel((x, y), tuple(max(0, min(255, c + noise + scorch)) for c in color))
        d = ImageDraw.Draw(tile)
        if name in ("plate", "shell", "panel"):
            d.rectangle((0, 0, 31, 31), outline=(31, 35, 34))
            d.line((1, 30, 1, 1, 30, 1), fill=tuple(min(255, c+20) for c in color))
            for x in (3, 28):
                for y in (3, 28):
                    d.rectangle((x-1, y-1, x+1, y+1), fill=(33, 37, 36))
                    d.point((x-1, y-1), fill=(150, 151, 137))
        if name == "shell":
            for x in (7, 20, 25):
                d.line((x, 3, x, rng.randrange(8, 18)), fill=(65, 64, 55))
        if name == "refractory":
            for y in range(0, 32, 8):
                d.line((0, y, 31, y), fill=(71, 58, 45))
                for x in range(0 if y%16 else 8, 32, 16):
                    d.line((x, y, x, y+7), fill=(71, 58, 45))
                d.line((0, y+1, 31, y+1), fill=(169, 118, 77))
        if name == "hazard":
            for y in range(32):
                for x in range(32):
                    if (x+y)//7%2: d.point((x, y), fill=(35, 40, 38))
            d.line((0, 0, 31, 0), fill=(211, 181, 74))
            d.line((0, 31, 31, 31), fill=(62, 57, 34))
        if name == "grate":
            for y in range(1, 32, 4):
                d.line((0, y, 31, y), fill=(26, 31, 31))
                d.line((0, y+1, 31, y+1), fill=(108, 115, 110))
        if name == "molten":
            for _ in range(75):
                x, y = rng.randrange(32), rng.randrange(32)
                d.rectangle((x, y, x+2, y+1), fill=rng.choice([(255, 181, 55), (229, 91, 23), (255, 204, 86)]))
        if name == "heat":
            d.rectangle((0,0,31,31),outline=(44,42,37))
            for y in (7,15,23): d.line((3,y,28,y),fill=(107,90,66))
        if name.startswith("port_"):
            d.rectangle((0, 0, 31, 31), fill=(50, 57, 55), outline=(20, 25, 25))
            d.rectangle((1, 1, 30, 30), outline=(127,132,119))
            for x in (4, 27):
                for y in (4, 27): d.rectangle((x, y, x+1, y+1), fill=(184, 182, 156))
            d.ellipse((7,6,25,24),fill=(122,127,119))
            d.ellipse((9,8,23,22),fill=color)
            d.ellipse((11,10,21,20),fill=(24,29,29))
            d.line((12,11,20,11),fill=(54,62,61))
            d.rectangle((12,27,20,28),fill=color)
        if name == "nameplate":
            d.rectangle((0, 0, 31, 31), outline=(154, 157, 135))
            d.text((5, 3), "AOD", fill=(216, 214, 182), font_size=10)
            d.text((7, 16), "02", fill=(204, 165, 61), font_size=10)
        result.paste(tile, ((index % 8)*32, (index//8)*32))
    return result


def add(a, b): return tuple(x+y for x, y in zip(a, b))
def sub(a, b): return tuple(x-y for x, y in zip(a, b))
def mul(a, s): return tuple(x*s for x in a)
def dot(a, b): return sum(x*y for x, y in zip(a, b))
def cross(a, b): return (a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0])
def unit(a): return mul(a, 1 / math.sqrt(dot(a, a)))


class Mesh:
    def __init__(self): self.faces = []

    def face(self, points, mat, group):
        normal = unit(cross(sub(points[1], points[0]), sub(points[2], points[0])))
        index = MATERIALS.index(mat)
        x, y = index%8*32, index//8*32
        uv = [(x+.5, y+31.5), (x+31.5, y+31.5), (x+31.5, y+.5), (x+.5, y+.5)]
        uv = [(u/256, 1-v/128) for u, v in uv[:len(points)]]
        self.faces.append((group, points, normal, uv, mat))

    def box(self, a, b, mat="plate", group="structure", front=None):
        x, y, z = a; X, Y, Z = b
        quads = [([(x,y,z),(x,y,Z),(x,Y,Z),(x,Y,z)], "-x"),
                 ([(X,y,Z),(X,y,z),(X,Y,z),(X,Y,Z)], "+x"),
                 ([(x,y,Z),(x,y,z),(X,y,z),(X,y,Z)], "-y"),
                 ([(x,Y,z),(x,Y,Z),(X,Y,Z),(X,Y,z)], "+y"),
                 ([(X,y,z),(x,y,z),(x,Y,z),(X,Y,z)], "-z"),
                 ([(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)], "+z")]
        for points, side in quads:
            self.face(points, (front or {}).get(side, mat), group)

    def tube(self, a, b, radius, mat="pipe", group="structure", sides=8, radius_b=None, caps=True):
        axis = unit(sub(b, a)); r2 = radius if radius_b is None else radius_b
        tangent = unit(cross((0, 1, 0) if abs(axis[1])<.95 else (0, 0, 1), axis))
        bitangent = cross(axis, tangent)
        rings=[]
        for center, r in ((a, radius), (b, r2)):
            rings.append([add(center, add(mul(tangent, math.cos(2*math.pi*i/sides)*r), mul(bitangent, math.sin(2*math.pi*i/sides)*r))) for i in range(sides)])
        for i in range(sides):
            j=(i+1)%sides
            self.face([rings[0][i],rings[0][j],rings[1][j],rings[1][i]], mat, group)
            if caps:
                self.face([a,rings[0][j],rings[0][i]], mat, group)
                self.face([b,rings[1][i],rings[1][j]], mat, group)

    def profile(self, profile, mat, group, sides=12, inward=False, center=(0,0)):
        for (y1,r1),(y2,r2) in zip(profile,profile[1:]):
            for i in range(sides):
                angles=[(i+.5)*2*math.pi/sides,(i+1.5)*2*math.pi/sides]
                points=[(center[0]+r*math.cos(a), y, center[1]+r*math.sin(a)) for y,r,a in
                        [(y1,r1,angles[0]),(y2,r2,angles[0]),(y2,r2,angles[1]),(y1,r1,angles[1])]]
                self.face(list(reversed(points)) if inward else points,mat,group)

    def ring(self,y,r1,r2,height,mat,group,center=(0,0),sides=12):
        self.profile([(y,r2),(y+height,r2),(y+height,r1),(y,r1),(y,r2)],mat,group,sides,center=center)

    def pipe(self, points, radius=.12, mat="pipe", group="pipework"):
        for a,b in zip(points,points[1:]):
            self.tube(a,b,radius,mat,group,caps=True)
        # Low-poly collars at junctions deliberately retain the industrial silhouette.
        for i in range(1,len(points)-1):
            direction=unit(sub(points[i+1],points[i]))
            self.tube(add(points[i],mul(direction,-.08)),add(points[i],mul(direction,.08)),min(radius*1.32,radius+.06),"edge",group)

    def export(self, folder, stem="large_aod"):
        # Keep each named object contiguous so Blender imports one object per group.
        group_order=list(dict.fromkeys(face[0] for face in self.faces))
        self.faces.sort(key=lambda face: group_order.index(face[0]))
        lines=["# Original SSITE large AOD prototype; units = blocks; +Y up; front = -Z", "mtllib large_aod.mtl"]
        vertices=[]; uvs=[]; normals=[]; faces=[]
        for group, points, normal, uv, mat in self.faces:
            start=len(vertices)+1
            vertices.extend(points);uvs.extend(uv);normals.extend([normal]*len(points))
            faces.append((group,start,len(points)))
        lines.extend("v "+" ".join(f"{v:.5f}" for v in p) for p in vertices)
        lines.extend("vt "+" ".join(f"{v:.6f}" for v in p) for p in uvs)
        lines.extend("vn "+" ".join(f"{v:.5f}" for v in p) for p in normals)
        lines.extend(["usemtl aod_atlas", "s off"])
        group_last=None
        for group,start,count in faces:
            if group!=group_last:
                lines.extend(["o "+group,"g "+group]);group_last=group
            for i in range(1,count-1):
                lines.append("f "+" ".join(f"{j}/{j}/{j}" for j in (start,start+i,start+i+1)))
        (folder/(stem+".obj")).write_text("\n".join(lines)+"\n",encoding="utf-8")
        (folder/"large_aod.mtl").write_text("newmtl aod_atlas\nKa 0.25 0.25 0.25\nKd 1 1 1\nKs 0 0 0\nd 1\nillum 1\nmap_Kd large_aod_atlas.png\n",encoding="utf-8")
        compact=defaultdict(lambda:dict(v=[],n=[],uv=[],i=[]))
        for group,points,normal,uv,mat in self.faces:
            data=compact[group];start=len(data["v"])//3
            data["v"].extend(round(x,4) for p in points for x in p)
            data["n"].extend(round(x,4) for _ in points for x in normal)
            data["uv"].extend(round(x,6) for p in uv for x in p)
            for i in range(1,len(points)-1): data["i"].extend((start,start+i,start+i+1))
        return compact


def machine():
    from aod_vessel import build
    return build(Mesh())


def opened(mesh, degrees=105):
    result=Mesh();angle=math.radians(degrees);c,s=math.cos(angle),math.sin(angle)
    def rotate(v): return (v[0],v[1]*c-v[2]*s,v[1]*s+v[2]*c)
    for group,points,normal,uv,mat in mesh.faces:
        if group==LID["group"]:
            points=[add(rotate(sub(p,LID["pivot"])),LID["pivot"]) for p in points]
            normal=rotate(normal)
        result.faces.append((group,points,normal,uv,mat))
    return result


def bounds_of(mesh):
    vertices=[p for _,points,_,_,_ in mesh.faces for p in points]
    return [[round(fn(p[i] for p in vertices),5) for i in range(3)] for fn in (min,max)]


def validate(mesh, compact):
    assert len({p["id"] for p in PORTS})==len(PORTS)
    vertices=[p for _,points,_,_,_ in mesh.faces for p in points]
    bounds=[[round(min(p[i] for p in vertices),4) for i in range(3)], [round(max(p[i] for p in vertices),4) for i in range(3)]]
    # Validate actual coplanar full faces, rather than the bounding boxes of whole port blocks.
    contact_areas={}
    for port in PORTS:
        low,high=port["bounds"];axis=next(i for i,v in enumerate(port["normal"]) if v)
        assert low[axis]==high[axis]
        assert all(float(v).is_integer() for p in (low,high) for v in p)
        if port["surface"]=="solid":
            area=0
            for group,points,normal,_,_ in mesh.faces:
                if group not in port["groups"] or dot(normal,port["normal"])<.999:continue
                if not all(abs(p[axis]-low[axis])<1e-6 for p in points):continue
                assert all(low[i]-1e-6<=p[i]<=high[i]+1e-6 for p in points for i in range(3))
                for j in range(1,len(points)-1):
                    v=cross(sub(points[j],points[0]),sub(points[j+1],points[0]));area+=math.sqrt(dot(v,v))/2
            assert abs(area-port["area"])<1e-6,(port["id"],area)
            contact_areas[port["id"]]=area
        active_mesh=opened(mesh) if port["surface"]=="opening" else mesh
        for cell in port["connection_cells"]+port.get("mold_cells",[]):
            for group,points,_,_,_ in active_mesh.faces:
                overlaps=all(max(p[i] for p in points)>cell[i]+1e-5 and min(p[i] for p in points)<cell[i]+1-1e-5 for i in range(3))
                assert not overlaps, (port["id"], "connection or mold cell obstructed by", group)
    for group,points,normal,uv,_ in mesh.faces:
        assert .999 < dot(normal,normal) < 1.001, group
        assert all(math.isfinite(v) for p in points for v in p)
        assert all(0<=v<=1 for p in uv for v in p)
    # At Y=1,2,3 the vessel has the same 12 vertex positions in X/Z.
    sections=[]
    for y in (1,2,3):
        sections.append({(round(p[0],6),round(p[2],6)) for g,ps,_,_,_ in mesh.faces if g=="vessel" for p in ps if abs(p[1]-y)<1e-6})
    assert len(sections[0])==12 and sections[0]==sections[1]==sections[2]
    assert min(p[1] for p in vertices)==0
    return dict(bounds=bounds,open_bounds=bounds_of(opened(mesh)),groups=len(compact),triangles=sum(len(g["i"])//3 for g in compact.values()),faces=len(mesh.faces),ports=len(PORTS),contact_areas=contact_areas,column_layers=[1,2],connection_cells_clear=True,casting_mold_space_clear=True,feed_open_when_lid_open=True)


def main():
    ap=argparse.ArgumentParser();ap.add_argument("--inline-dir",type=Path);args=ap.parse_args()
    OUT.mkdir(parents=True,exist_ok=True)
    atlas().save(OUT/"large_aod_atlas.png")
    mesh=machine();compact=mesh.export(OUT);stats=validate(mesh,compact)
    opened(mesh).export(OUT,"large_aod_open")
    ports=[dict(p,owner_cells=[sub(cell,p["normal"]) for cell in p["connection_cells"]]) for p in PORTS]
    manifest=dict(schema_version=2,name="SSITE large AOD / art prototype 02",units="blocks",up="+Y",front="-Z",origin="(0,0,0) is on the heat-contact plane; furnace center X/Z=(0.5,0.5)",heat_footprint=[3,3],ports=ports,stats=stats,groups=list(compact),texture_filter="nearest",lid=LID,regular_column=dict(y_range=[1,3],layers=2,section="12-sided constant cross-section"),runtime_integrated=False)
    (OUT/"ports.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    data=dict(meshes=compact,ports=PORTS,stats=stats,lid=LID,texture=base64.b64encode((OUT/"large_aod_atlas.png").read_bytes()).decode())
    template=(Path(__file__).with_name("large_aod_viewer.html")).read_text(encoding="utf-8")
    fragment=template.replace("/*__MODEL_DATA__*/",json.dumps(data,ensure_ascii=False,separators=(",",":")))
    (OUT/"preview.html").write_text("<!doctype html>\n<html lang=\"zh-CN\"><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>SSITE · Large AOD</title><style>"+(Path(__file__).with_name("viewer_shell.css")).read_text(encoding="utf-8")+"</style></head><body>\n"+fragment+"\n</body></html>\n",encoding="utf-8")
    if args.inline_dir:
        args.inline_dir.mkdir(parents=True,exist_ok=True)
        assert len(fragment.encode("utf-8"))<1_000_000
        (args.inline_dir/"large-aod-v2.html").write_text(fragment,encoding="utf-8")
    print(json.dumps(stats,ensure_ascii=False))


if __name__ == "__main__": main()
