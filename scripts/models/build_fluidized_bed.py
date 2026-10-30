"""Build an original HBM-inspired circulating fluidized-bed combustor art asset.

Python 3 + Pillow. One unit = one block, +Y up, front = -Z.
No HBM model or texture data is read or copied by this generator.
"""
from __future__ import annotations

import base64
import json
import math
from collections import defaultdict
from pathlib import Path

from fluidized_bed_materials import MATERIALS, build_atlas

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "art" / "fluidized_bed"
STEM = "fluidized_bed_combustor"
CENTER = (-.85, .15)
GAME_TRANSLATION = (.5, 0, 2.5)
HEAT_BOUNDS = [(-.25, .25, 2.5), (.25, .75, 2.5)]
CUTAWAY_HIDE = ["front_shell", "front_lining", "front_bands", "front_ribs", "inspection_hatch", "front_instruments", "bed_front"]
GROUP_LABELS = {
    "skid":"钢制底架", "windbox":"底部风箱", "front_shell":"前侧炉壳", "riser_shell":"主燃烧筒",
    "crown":"炉顶封盖", "refractory_lining":"耐火内衬", "front_lining":"前侧内衬",
    "shell_bands":"炉壳带箍", "front_bands":"前侧带箍", "shell_ribs":"纵向加强筋", "front_ribs":"前侧加强筋",
    "distributor":"布风板", "bed":"床料", "bed_front":"前侧床料", "bed_particles":"炽热颗粒",
    "inspection_hatch":"检修门与观察孔", "ash_outlet":"底灰出口", "front_instruments":"仪表与铭牌",
    "fuel_hopper":"燃料料斗", "fuel_screw":"螺旋给料器", "hopper_support":"料斗支架",
    "cyclone":"旋风分离器", "cyclone_bands":"分离器法兰", "crossover_flue":"上部烟气管",
    "solids_return":"回料管", "loop_seal":"回料封", "aeration_line":"回料曝气管",
    "flue_outlet":"净烟气出口", "cyclone_frame":"分离器支架", "service_platform":"检修平台",
    "guardrails":"黄色护栏", "ladder":"检修爬梯", "blower_base":"风机底座", "blower":"鼓风机",
    "blower_motor":"风机电机", "primary_air":"一次风管", "secondary_air":"二次风管", "lifting_lugs":"炉顶吊耳",
    "controller":"燃烧室控制面板", "controller_mount":"控制面板支架",
    "heat_outlet":"后侧 HU 输热接口", "heat_outlet_flange":"输热接口法兰",
}


def add(a, b): return tuple(x + y for x, y in zip(a, b))
def sub(a, b): return tuple(x - y for x, y in zip(a, b))
def mul(a, s): return tuple(x * s for x in a)
def dot(a, b): return sum(x * y for x, y in zip(a, b))
def cross(a, b): return (a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0])
def unit(a): return mul(a, 1 / math.sqrt(dot(a, a)))


class Mesh:
    def __init__(self):
        self.faces = []

    def face(self, points, material, group, uv=None):
        normal = unit(cross(sub(points[1], points[0]), sub(points[2], points[0])))
        if len(points)==4 and abs(dot(normal, sub(points[3],points[0])))>1e-7:
            local_uv=uv or [(0,1),(1,1),(1,0),(0,0)]
            for indices in ((0,1,2),(0,2,3)):
                self.face([points[i] for i in indices],material,group,[local_uv[i] for i in indices])
            return
        idx = MATERIALS.index(material)
        x, y = idx % 8 * 64, idx // 8 * 64
        if uv is None:
            uv = [(0, 1), (1, 1), (1, 0), (0, 0)][:len(points)]
        uv = [((x + 1 + u*62)/512, 1-(y + 1 + v*62)/256) for u, v in uv]
        self.faces.append((group, points, normal, uv, material))

    def box(self, a, b, material="plate", group="structure", surfaces=None):
        x, y, z = a; X, Y, Z = b
        assert X > x and Y > y and Z > z
        faces = [
            ([(x,y,z),(x,y,Z),(x,Y,Z),(x,Y,z)], "-x"),
            ([(X,y,Z),(X,y,z),(X,Y,z),(X,Y,Z)], "+x"),
            ([(x,y,Z),(x,y,z),(X,y,z),(X,y,Z)], "-y"),
            ([(x,Y,z),(x,Y,Z),(X,Y,Z),(X,Y,z)], "+y"),
            ([(X,y,z),(x,y,z),(x,Y,z),(X,Y,z)], "-z"),
            ([(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)], "+z")]
        for ps, side in faces:
            self.face(ps, (surfaces or {}).get(side, material), group)

    def tube(self, a, b, r, material="pipe", group="pipework", sides=8, r2=None, caps=True):
        axis = unit(sub(b, a)); r2 = r if r2 is None else r2
        tangent = unit(cross((0,1,0) if abs(axis[1]) < .95 else (0,0,1), axis))
        bitangent = cross(axis, tangent)
        rings = [[add(c, add(mul(tangent, math.cos(2*math.pi*i/sides)*rr), mul(bitangent, math.sin(2*math.pi*i/sides)*rr))) for i in range(sides)] for c, rr in ((a,r),(b,r2))]
        for i in range(sides):
            j = (i+1) % sides
            self.face([rings[0][i], rings[0][j], rings[1][j], rings[1][i]], material, group)
            if caps:
                for center, ring, indices, radius in ((a,rings[0],(j,i),r),(b,rings[1],(i,j),r2)):
                    uv = [(.5,.5)] + [(.5+.48*math.cos(2*math.pi*k/sides), .5-.48*math.sin(2*math.pi*k/sides)) for k in indices]
                    self.face([center, ring[indices[0]], ring[indices[1]]], material, group, uv)

    def profile(self, levels, material, group, center=CENTER, sides=12, split=None, inward=False):
        for (y1,r1),(y2,r2) in zip(levels,levels[1:]):
            for i in range(sides):
                a, b = (i+.5)*2*math.pi/sides, (i+1.5)*2*math.pi/sides
                ps = [(center[0]+r*math.cos(t), y, center[1]+r*math.sin(t)) for y,r,t in ((y1,r1,a),(y2,r2,a),(y2,r2,b),(y1,r1,b))]
                selected = split if split and math.sin((a+b)/2) < -.24 else group
                uv=[(0,1),(0,0),(1,0),(1,1)]
                self.face(list(reversed(ps)) if inward else ps, material, selected, list(reversed(uv)) if inward else uv)

    def ring(self, y, inner, outer, h, material, group, center=CENTER, sides=12, split=None):
        self.profile([(y,outer),(y+h,outer),(y+h,inner),(y,inner),(y,outer)], material, group, center, sides, split)

    def pipe(self, points, radius, material="pipe", group="pipework", sides=8, phase=0):
        # Shared rings bisect corners, producing a continuous closed elbow without overlapping caps.
        tangents = [unit(sub(b,a)) for a,b in zip(points,points[1:])]
        reference = unit(cross(tangents[0], tangents[1])) if len(tangents)>1 and abs(dot(tangents[0],tangents[1])) < .999 else (0,0,1)
        if abs(dot(reference,tangents[0]))>.95: reference=(0,1,0)
        rings=[]
        for i, p in enumerate(points):
            axis = tangents[0] if i==0 else tangents[-1] if i==len(points)-1 else unit(add(tangents[i-1],tangents[i]))
            u=unit(cross(reference,axis)); v=cross(axis,u)
            scale=1 if i in (0,len(points)-1) else 1/dot(axis,tangents[i-1])
            rings.append([add(p,add(mul(u,radius*math.cos(j*2*math.pi/sides+phase)*scale),mul(v,radius*math.sin(j*2*math.pi/sides+phase)))) for j in range(sides)])
        for a,b in zip(rings,rings[1:]):
            for i in range(sides):
                j=(i+1)%sides
                self.face([a[i],a[j],b[j],b[i]],material,group)
        for i in range(sides):
            j=(i+1)%sides
            self.face([points[0],rings[0][j],rings[0][i]],material,group)
            self.face([points[-1],rings[-1][i],rings[-1][j]],material,group)

    def export(self, stem, hidden=(), out=OUT):
        faces=[f for f in self.faces if f[0] not in hidden]
        order=list(dict.fromkeys(f[0] for f in faces))
        faces.sort(key=lambda f:order.index(f[0]))
        vertices=[]; uvs=[]; normals=[]; indexed=[]
        compact=defaultdict(lambda:dict(v=[],n=[],uv=[],i=[]))
        for group,ps,n,uv,_ in faces:
            start=len(vertices)+1
            vertices.extend(ps); uvs.extend(uv); normals.extend([n]*len(ps))
            indexed.append((group,start,len(ps)))
            d=compact[group]; local=len(d["v"])//3
            d["v"].extend(round(x,5) for p in ps for x in p)
            d["n"].extend(round(x,5) for _ in ps for x in n)
            d["uv"].extend(round(x,6) for p in uv for x in p)
            for i in range(1,len(ps)-1): d["i"].extend((local,local+i,local+i+1))
        lines=["# SSITE original circulating fluidized-bed combustor", "# +Y up; front -Z; 1 unit = 1 block; base y=0", f"mtllib {STEM}.mtl"]
        lines.extend("v "+" ".join(f"{x:.5f}" for x in p) for p in vertices)
        lines.extend("vt "+" ".join(f"{x:.6f}" for x in p) for p in uvs)
        lines.extend("vn "+" ".join(f"{x:.6f}" for x in p) for p in normals)
        lines.extend(["usemtl fluidized_bed_atlas","s off"])
        last=None
        for group,start,count in indexed:
            if group!=last:
                lines.extend(["o "+group,"g "+group]); last=group
            for i in range(1,count-1):
                lines.append("f "+" ".join(f"{v}/{v}/{v}" for v in (start,start+i,start+i+1)))
        (out/(stem+".obj")).write_text("\n".join(lines)+"\n",encoding="utf-8")
        return compact


def build_machine():
    m=Mesh(); cx,cz=CENTER
    # Open steel skid; the separate service modules share a visibly continuous foundation.
    for z in (-1.95,1.8):
        m.box((-3.45,0,z-.12),(3.45,.22,z+.12),"dark","skid")
        m.box((-3.45,.19,z-.18),(3.45,.28,z+.18),"edge","skid")
    for x in (-3.2,-1.7,.1,1.8,3.2):
        m.box((x-.1,.06,-1.9),(x+.1,.2,1.85),"dark","skid")
    for x in (-3.12,3.12):
        for z in (-1.85,1.72):
            m.box((x-.22,0,z-.22),(x+.22,.12,z+.22),"plate","skid")
            m.tube((x,.12,z),(x,.18,z),.065,"bolt","skid",6)
    # Main pressure envelope: windbox, expanded dense bed, tall combustion riser.
    m.profile([(.3,1.2),(.57,1.44),(.99,1.44)],"ribbed","windbox",split="front_shell")
    m.profile([(.99,1.44),(1.21,1.48),(2.42,1.48),(2.85,1.25),(3.9,1.25),(4.95,1.25),(6.0,1.25),(6.68,1.25),(7.12,.94)],"shell","riser_shell",split="front_shell")
    m.profile([(7.12,.94),(7.25,.94)],"dark","crown",split="front_shell")
    m.tube((cx,7.24,cz),(cx,7.31,cz),.94,"plate","crown",12)
    m.ring(7.1,.87,1.01,.10,"edge","crown")
    # The back liner remains visible in the cutaway, with real thickness at the cut edges.
    m.profile([(1.03,1.27),(2.37,1.27),(2.82,1.10),(3.9,1.10),(4.95,1.10),(6,1.10),(6.68,1.10),(7.12,.79)],"refractory","refractory_lining",split="front_lining",inward=True)
    for y,r in ((1.03,1.48),(2.42,1.48),(2.85,1.25),(4.92,1.25),(6.65,1.25)):
        m.ring(y-.06,r-.06,r+.065,.12,"dark","shell_bands",split="front_bands")
    # Structural ribs stay sparse; rivets live in the atlas rather than costly geometry.
    for i in range(12):
        a=(i+.5)*math.pi/6
        front=math.sin(a)<-.24
        for low,high,r in ((1.21,2.4,1.49),(2.89,4.89,1.26),(4.99,6.63,1.26)):
            x,z=cx+r*math.cos(a),cz+r*math.sin(a)
            m.tube((x,low,z),(x,high,z),.035,"edge","front_ribs" if front else "shell_ribs",4,caps=False)
    # Open distributor bars and perimeter ring, plus a raised granular bed.
    m.ring(1.05,.99,1.29,.08,"ceramic","distributor")
    for z in (-.78,-.52,-.26,0,.26,.52,.78):
        half=math.sqrt(1.24**2-z*z)
        m.box((cx-half,1.07,cz+z-.07),(cx+half,1.14,cz+z+.07),"grate","distributor")
    m.tube((cx,1.38,cz),(cx,1.43,cz),1.235,"bed","bed",12)
    # A short cutaway wedge opens a view through the bed to the distributor.
    # Remove the opaque front cap sector from the diagnostic OBJ only.
    for index,face in enumerate(m.faces):
        group,ps,n,uv,mat=face
        if group=="bed" and sum(p[2] for p in ps)/len(ps)<cz-.18:
            m.faces[index]=("bed_front",ps,n,uv,mat)
    for x,z in ((-.6,-.2),(-.2,.5),(.4,.2),(.6,-.4),(-.5,.65)):
        m.tube((cx+x,1.43,cz+z),(cx+x,1.49,cz+z),.095,"hot" if x>.3 else "bed","bed_particles",5)
    # Thick bolted inspection hatch, central orange sight port, latch and hinge.
    m.tube((cx,2.08,-1.13),(cx,2.08,-1.46),.48,"dark","inspection_hatch",12)
    m.tube((cx,2.08,-1.46),(cx,2.08,-1.55),.42,"edge","inspection_hatch",12)
    m.tube((cx,2.08,-1.55),(cx,2.08,-1.575),.33,"door","inspection_hatch",12)
    m.tube((cx,2.08,-1.577),(cx,2.08,-1.61),.16,"black","inspection_hatch",8)
    m.tube((cx,2.08,-1.612),(cx,2.08,-1.62),.115,"hot","inspection_hatch",8)
    for i in range(8):
        a=i*math.pi/4
        x,y=cx+.365*math.cos(a),2.08+.365*math.sin(a)
        m.tube((x,y,-1.55),(x,y,-1.585),.032,"bolt","inspection_hatch",6)
    m.box((cx+.36,1.93,-1.61),(cx+.49,2.24,-1.46),"dark","inspection_hatch")
    m.tube((cx-.3,1.92,-1.65),(cx-.3,2.22,-1.65),.035,"yellow","inspection_hatch",6)
    # Solid ash outlet and gravity bin below the bed; clear of the air inlet.
    m.box((cx-.39,.37,-1.66),(cx+.39,.88,-1.29),"dark","ash_outlet",{"-z":"port_ash"})
    m.box((cx-.44,.85,-1.72),(cx+.44,.96,-1.28),"edge","ash_outlet",{"-z":"hazard"})
    # Raised instrument pod and a restrained engraved identification plate.
    m.box((cx-.53,5.57,-1.13),(cx+.53,5.97,-1.085),"dark","front_instruments",{"-z":"nameplate"})
    m.box((cx+.35,3.15,-1.19),(cx+.82,3.92,-1.01),"dark","front_instruments",{"-z":"panel"})
    m.tube((cx-.38,3.52,-1.04),(cx-.38,3.52,-1.25),.19,"edge","front_instruments",8)
    m.tube((cx-.38,3.52,-1.25),(cx-.38,3.52,-1.265),.16,"gauge","front_instruments",12)
    # Fuel hopper: true open top, double wall, tapered throat, screw-feed barrel.
    hx,hz=-2.72,-.18
    corners=[(-.5,-.48),(.5,-.48),(.5,.48),(-.5,.48)]
    rings=[[(hx+x*r,y,hz+z*r) for x,z in corners] for y,r in ((2.03,.4),(2.72,1),(3.37,1))]
    for low,high in zip(rings,rings[1:]):
        for i in range(4):
            j=(i+1)%4; m.face([low[i],high[i],high[j],low[j]],"plate","fuel_hopper")
    inner=[(hx+x*.86,3.37,hz+z*.86) for x,z in corners]
    for i in range(4):
        j=(i+1)%4
        m.face([rings[-1][i],inner[i],inner[j],rings[-1][j]],"edge","fuel_hopper")
        m.face([inner[i],(hx+corners[i][0]*.4,2.03,hz+corners[i][1]*.4),(hx+corners[j][0]*.4,2.03,hz+corners[j][1]*.4),inner[j]],"soot","fuel_hopper")
    m.box((hx-.17,2.05,hz-.15),(hx+.17,2.11,hz+.15),"black","fuel_hopper")
    m.tube((-3.18,1.99,hz),(-1.95,1.99,hz),.225,"pipe","fuel_screw",8)
    for x in (-3.12,-2.5,-2.03):m.tube((x-.045,1.99,hz),(x+.045,1.99,hz),.29,"edge","fuel_screw",8)
    m.box((-3.42,1.79,hz-.25),(-3.15,2.19,hz+.25),"motor","fuel_screw")
    for x in (-3.08,-2.37):
        for z in (-.56,.20):m.box((x-.04,.26,z-.04),(x+.04,2.7,z+.04),"dark","hopper_support")
    # Side cyclone: smaller diameter than the riser, clear long cone and return leg.
    cc=(2.12,.48)
    m.profile([(3.4,.20),(4.55,.74),(5.52,.74),(6.49,.74),(6.77,.51)],"plate","cyclone",center=cc)
    m.profile([(6.77,.51),(6.77,.31)],"plate","cyclone",center=cc)
    for y,r in ((4.52,.77),(5.51,.77),(6.48,.77)):
        m.ring(y,r-.07,r,.085,"edge","cyclone_bands",center=cc)
    m.ring(3.37,.15,.28,.11,"dark","cyclone_bands",center=cc,sides=8)
    # Tangential crossover flue travels behind the two vessels at the high level.
    m.pipe([(.18,6.28,.62),(.55,6.28,1.2),(1.43,6.28,1.2),(2.12,6.28,.99)],.34,"ribbed","crossover_flue",8)
    m.tube((.88,6.28,1.2),(1.02,6.28,1.2),.43,"dark","crossover_flue",8)
    m.pipe([(2.12,3.41,.48),(2.12,1.64,.48),(1.78,1.35,.48),(1.35,1.35,.48),(.5,1.94,.48)],.19,"pipe","solids_return",8)
    for y in (2.03,3.14):m.tube((2.12,y-.055,.48),(2.12,y+.055,.48),.26,"edge","solids_return",8)
    m.box((1.45,1.17,.19),(1.94,1.61,.77),"plate","loop_seal")
    m.pipe([(1.7,1.2,.26),(1.7,.68,.26),(.79,.68,.26)],.055,"pipe","aeration_line",6)
    # Clean gas outlet terminates in a flat connection flange, with a dark open bore.
    m.tube((2.12,6.7,.48),(2.12,7.86,.48),.31,"pipe","flue_outlet",10,caps=False)
    m.ring(6.76,.25,.42,.13,"dark","flue_outlet",center=cc,sides=10)
    m.ring(7.81,.25,.45,.16,"edge","flue_outlet",center=cc,sides=10)
    m.profile([(7.56,.25),(7.97,.25)],"soot","flue_outlet",center=cc,sides=10,inward=True)
    m.tube((2.12,7.55,.48),(2.12,7.56,.48),.25,"black","flue_outlet",10)
    # Cyclone frame / rear service platform with yellow rail and fixed ladder.
    for x in (1.26,2.96):
        for z in (-.18,1.18):
            m.box((x-.06,.25,z-.06),(x+.06,4.55,z+.06),"dark","cyclone_frame")
            m.box((x-.17,.24,z-.16),(x+.17,.32,z+.16),"plate","cyclone_frame")
    for x in (1.26,2.96):
        m.tube((x,.6,1.18),(x,3.65,-.18),.045,"edge","cyclone_frame",4)
    m.box((-.95,4.46,1.40),(3.29,4.58,2.06),"dark","service_platform",{"+y":"grate"})
    m.box((2.89,4.46,-.46),(3.45,4.58,2.06),"dark","service_platform",{"+y":"grate"})
    for x in (-.88,.12,1.12,2.12,3.37):
        m.tube((x,4.58,2.02),(x,5.42,2.02),.026,"yellow","guardrails",6)
    for y in (4.96,5.42):
        for a,b in ((-.88,.32),(.98,3.37)):
            m.tube((a,y,2.02),(b,y,2.02),.028,"yellow","guardrails",6)
    for z in (-.4,.6):m.tube((3.4,4.58,z),(3.4,5.42,z),.026,"yellow","guardrails",6)
    for y in (4.96,5.42):m.tube((3.4,y,-.4),(3.4,y,2.02),.028,"yellow","guardrails",6)
    # The rail opening at the ladder landing stays clear; rails rise as handholds.
    for x in (.36,.94):m.tube((x,.3,2.16),(x,5.35,2.16),.032,"yellow","ladder",6)
    for i in range(15):
        y=.43+i*.285
        m.tube((.36,y,2.16),(.94,y,2.16),.025,"edge","ladder",6)
    for y in (1.05,2.7,4.36):
        for x in (.36,.94):m.tube((x,y,1.96),(x,y,2.16),.027,"dark","ladder",4)
    # Front centrifugal blower: volute, intake grille, drive motor and rising air duct.
    bx,bz=1.97,-1.37
    m.box((1.31,.24,-1.94),(2.64,.4,-.82),"plate","blower_base")
    m.tube((bx,.99,-1.81),(bx,.99,-1.11),.58,"shell","blower",12)
    m.tube((bx,.99,-1.85),(bx,.99,-1.80),.49,"edge","blower",12)
    m.tube((bx,.99,-1.87),(bx,.99,-1.85),.405,"fan_grille","blower",12)
    m.tube((bx,.99,-1.88),(bx,.99,-1.87),.11,"dark","blower",8)
    m.box((1.67,1.26,-1.73),(2.12,1.63,-1.17),"plate","blower")
    m.tube((2.47,.69,-1.28),(3.05,.69,-1.28),.27,"motor","blower_motor",8)
    m.tube((3.04,.69,-1.28),(3.14,.69,-1.28),.30,"dark","blower_motor",8)
    m.box((2.6,.35,-1.5),(3.13,.43,-1.06),"dark","blower_motor")
    m.pipe([(1.67,1.46,-1.45),(1.1,1.46,-1.45),(.75,1.03,-1.45),(.30,.83,-.62)],.225,"pipe","primary_air",8)
    m.tube((.96,1.46,-1.45),(1.10,1.46,-1.45),.30,"edge","primary_air",8)
    m.box((1.01,1.68,-1.53),(1.08,1.82,-1.37),"dark","primary_air")
    m.tube((.86,1.82,-1.45),(1.23,1.82,-1.45),.028,"red","primary_air",6)
    # Secondary air manifold and two nozzles on the rear riser wall.
    m.pipe([(.26,.85,.61),(.52,1.22,.94),(.52,3.15,.94),(-.05,3.15,1.01)],.105,"pipe","secondary_air",8)
    m.pipe([(.52,2.66,.94),(.1,2.66,1.32),(-.35,2.66,1.35)],.08,"pipe","secondary_air",6)
    # Two crown lifting eyes retain the blocky low-poly silhouette.
    for x in (-1.43,-.27):
        m.box((x-.055,7.27,-.09),(x+.055,7.56,.15),"edge","lifting_lugs")
        m.box((x-.057,7.36,-.04),(x+.057,7.48,.10),"black","lifting_lugs")
    add_heat_outlet(m)
    return m


def add_heat_outlet(m):
    """Rear local (0,0,4) outlet meets a separately placed pipe at local (0,0,5)."""
    # Controller front maps exactly to the controller's north face in game.
    m.box((-.5,0,-2.5),(.5,1,-2.3),"plate","controller",{"-z":"panel"})
    m.box((-.4,.12,-2.3),(.4,.89,-1.89),"dark","controller_mount")
    # Only the short connection within the original skid remains part of the tower.
    m.pipe([(-.85,.5,1.25),(-.85,.5,1.7),(0,.5,1.7),(0,.5,2.5)],
           math.sqrt(2)*.25,"insulation","heat_outlet",4,phase=math.pi/4)
    for x0,x1 in ((-.32,-.25),(.25,.32)):
        m.box((x0,.18,2.4),(x1,.82,2.5),"edge","heat_outlet_flange")
    for y0,y1 in ((.18,.25),(.75,.82)):
        m.box((-.25,y0,2.4),(.25,y1,2.5),"edge","heat_outlet_flange")


def validate(mesh, compact):
    vertices=[]; triangles=0
    for group,ps,n,uv,material in mesh.faces:
        assert material in MATERIALS
        assert all(math.isfinite(x) for p in ps for x in p),group
        assert abs(dot(n,n)-1)<1e-6,group
        assert all(0<=x<=1 for p in uv for x in p),group
        for i in range(1,len(ps)-1):
            area_vector=cross(sub(ps[i],ps[0]),sub(ps[i+1],ps[0]))
            assert dot(area_vector,area_vector)>1e-12,(group,"degenerate triangle")
            assert dot(unit(area_vector),n)>.99,(group,"nonplanar or reversed triangle")
            triangles+=1
        vertices.extend(ps)
    bounds=[[round(fn(p[i] for p in vertices),5) for i in range(3)] for fn in (min,max)]
    assert bounds[0][1]==0
    assert all(g in compact for g in CUTAWAY_HIDE)
    assert set(compact)==set(GROUP_LABELS)
    assert triangles==sum(len(d["i"])//3 for d in compact.values())
    assert triangles<12000,triangles
    assert bounds[1][0] < 3.5 and bounds[1][2] == 2.5, "No fixed exterior heat bank or duct"
    assert add((0,.5,2.5),GAME_TRANSLATION)==(.5,.5,5), "Rear HU outlet center"
    return dict(bounds=bounds,groups=len(compact),triangles=triangles,faces=len(mesh.faces),texture_size=[512,256],finite_vertices=True,unit_normals=True,uv_in_atlas=True,nondegenerate_triangles=True,consistent_winding=True)


def main():
    OUT.mkdir(parents=True,exist_ok=True)
    build_atlas().save(OUT/(STEM+"_atlas.png"))
    (OUT/(STEM+".mtl")).write_text(f"newmtl fluidized_bed_atlas\nKa 0.25 0.25 0.25\nKd 1 1 1\nKs 0 0 0\nNs 1\nd 1\nillum 1\nmap_Kd {STEM}_atlas.png\n",encoding="utf-8")
    mesh=build_machine(); compact=mesh.export(STEM); stats=validate(mesh,compact)
    cutaway=mesh.export(STEM+"_cutaway",CUTAWAY_HIDE)
    ports=[
        dict(id="fuel",label="燃料料斗 / 螺旋给料",code="FUEL",bounds=[[-3.22,3.37,-.66],[-2.22,3.37,.30]],normal=[0,1,0],groups=["fuel_hopper","fuel_screw"]),
        dict(id="air",label="一次风 / 鼓风机吸入口",code="AIR",bounds=[[1.56,.58,-1.88],[2.38,1.4,-1.88]],normal=[0,0,-1],groups=["blower","blower_motor","primary_air"]),
        dict(id="ash",label="底灰排出口",code="ASH",bounds=[[-1.24,.37,-1.66],[-.46,.88,-1.66]],normal=[0,0,-1],groups=["ash_outlet"]),
        dict(id="flue",label="旋风分离器净烟气出口",code="FLUE",bounds=[[1.67,7.97,.03],[2.57,7.97,.93]],normal=[0,1,0],groups=["flue_outlet"]),
        dict(id="heat",label="后侧 HU 管道接口",code="HU",bounds=HEAT_BOUNDS,normal=[0,0,1],groups=["heat_outlet","heat_outlet_flange"],connection_cells=[[0,0,5]],connection_coordinate_system="controller-local after game_translation"),
        dict(id="controller",label="前侧控制面板",code="FBC",bounds=[[-.5,0,-2.5],[.5,1,-2.5]],normal=[0,0,-1],groups=["controller"]),
    ]
    manifest=dict(schema_version=3,name="SSITE fluidized-bed combustor / independent circulating tower",units="blocks",up="+Y",front="-Z",origin="Skid base at Y=0; main riser X/Z=(-0.85,0.15)",game_translation=GAME_TRANSLATION,nominal_envelope=[7,8,5],heat_outlet_bounds=HEAT_BOUNDS,heat_owner_cell=[0,0,4],stats=stats,groups=list(compact),cutaway_hide=CUTAWAY_HIDE,cutaway_triangles=sum(len(d["i"])//3 for d in cutaway.values()),ports=ports,texture_filter="nearest",runtime_obj="ssite:obj/fluidized_bed_combustor.obj",runtime_texture="ssite:textures/machine/fluidized_bed_combustor.png",engineering_note="Stylized game asset; internal cutaway illustrates a bed, distributor, windbox and circulation loop, not a pressure-vessel design.")
    (OUT/"model.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    data=dict(meshes=compact,texture=base64.b64encode((OUT/(STEM+"_atlas.png")).read_bytes()).decode(),stats=stats,cutaway_hide=CUTAWAY_HIDE,ports=ports,group_labels=GROUP_LABELS,title="流化床燃烧室 · FBC–01",subtitle="独立循环流化床 / 后侧 HU 接口 / 外接管道与加热板")
    template=Path(__file__).with_name("fluidized_bed_viewer.html").read_text(encoding="utf-8")
    assert template.count("/*__MODEL_DATA__*/")==1
    fragment=template.replace("/*__MODEL_DATA__*/",json.dumps(data,ensure_ascii=False,separators=(",",":")))
    css=Path(__file__).with_name("viewer_shell.css").read_text(encoding="utf-8")
    (OUT/"preview.html").write_text('<!doctype html>\n<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>SSITE · 流化床燃烧室</title><style>'+css+'</style></head><body>'+fragment+'</body></html>\n',encoding="utf-8")
    print(json.dumps(stats,ensure_ascii=False))


if __name__=="__main__": main()
