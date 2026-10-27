"""Original small AOD / VAR model prototypes. Python 3 + Pillow; no game edits.

Run from any directory. Writes art/small_furnaces and optional inline previews.
All geometry uses block units. Bottom area = 1; each flat side is 0.75 wide x 1 high.
"""
from __future__ import annotations

import argparse
import base64
import json
import math
from collections import defaultdict
from pathlib import Path
from build_large_aod import Mesh as Primitives, add, sub, mul, cross, dot, unit
from small_furnace_materials import TILE, WIDTH, HEIGHT, finishes, texture_set, bake_finish, save_maps, material_parameters

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'art'/'small_furnaces'
TILES=['plate','dark','edge','brick','copper','ceramic','graphite','yellow',
       'grate','pipe','red','molten','north','east','south','west',
       'bottom','name','seal','insulator','gas','bus','vent','warm','chamfer']
LID_PIVOT=[.5,1.16,.86]
CUT=.125
SOLE=1/32
# Pixel-vector interpretation of the user-supplied GT6CE mark, not an HBM asset.
LOGO_OUTLINE=[(6,0),(10,0),(10,2),(14,2),(14,4),(4,4),(4,7),(16,7),
              (16,10),(14,10),(14,14),(10,14),(10,16),(6,16),(6,14),
              (2,14),(2,10),(0,10),(0,6),(2,6),(2,2),(6,2)]
LOGO_HOLE=[(4,9),(12,9),(12,12),(4,12)]


def logo_pixels(kind):
    def inside(x,y,points):
        result=False
        for a,b in zip(points,points[1:]+points[:1]):
            if (a[1]>y)!=(b[1]>y) and x<(b[0]-a[0])*(y-a[1])/(b[1]-a[1])+a[0]:result=not result
        return result
    mark={(x,y) for x in range(16) for y in range(16)
          if inside(x+.5,y+.5,LOGO_OUTLINE) and not inside(x+.5,y+.5,LOGO_HOLE)}
    pixels={(x+1,y+1):(31,43,48) for x,y in mark}
    pixels.update({p:(33,165,142) for p in mark})
    accent=[(5,10),(7,9),(7,10),(9,10)] if kind=='aod' else [(8,9),(7,10),(8,10),(7,11)]
    pixels.update({p:(216,145,63) if kind=='aod' else (223,222,183) for p in accent})
    return pixels


def export_logo(folder,kind):
    # SVG is the editable source asset; the atlas samples this same integer grid.
    pixels=logo_pixels(kind)
    rects=''.join(f'<rect x="{x}" y="{y}" width="1" height="1" fill="#{r:02x}{g:02x}{b:02x}"/>'
                  for (x,y),(r,g,b) in sorted(pixels.items()))
    (folder/f'gt6ce_{kind}_mark.svg').write_text(
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 17 17" shape-rendering="crispEdges">'
        f'<title>GT6CE reference mark - {kind.upper()} furnace variant</title>{rects}</svg>\n',encoding='utf-8')


class Mesh(Primitives):
    def face(self,points,mat,group):
        normal=unit(cross(sub(points[1],points[0]),sub(points[2],points[0])))
        index=TILES.index(mat);x,y=index%8*TILE,index//8*TILE
        left,right=(8.5,55.5) if mat in ('north','east','south','west') else (0.5,63.5)
        uv=[((x+u)/WIDTH,1-(y+v)/HEIGHT) for u,v in [(left,63.5),(right,63.5),(right,.5),(left,.5)][:len(points)]]
        self.faces.append((group,points,normal,uv,mat))

    def export(self,folder,stem,material_stem,finish=None):
        groups=list(dict.fromkeys(f[0] for f in self.faces));self.faces.sort(key=lambda f:groups.index(f[0]))
        lines=['# SSITE original small furnace; one unit = one block; +Y up',f'mtllib {material_stem}.mtl']
        vs=[];uvs=[];ns=[];refs=[];compact=defaultdict(lambda:dict(v=[],n=[],uv=[],i=[]))
        for group,points,normal,uv,mat in self.faces:
            start=len(vs)+1;vs.extend(points);uvs.extend(uv);ns.extend([normal]*len(points));refs.append((group,start,len(points),mat))
            d=compact[group];offset=len(d['v'])//3
            d['v'].extend(round(v,5) for p in points for v in p);d['n'].extend(round(v,5) for _ in points for v in normal);d['uv'].extend(round(v,6) for p in uv for v in p)
            for j in range(1,len(points)-1):d['i'].extend((offset,offset+j,offset+j+1))
        for prefix,values in [('v',vs),('vt',uvs),('vn',ns)]:lines.extend(prefix+' '+' '.join(f'{v:.6f}' for v in p) for p in values)
        lines.append('s off');last=None;last_mat=None
        for group,start,count,mat in refs:
            if group!=last:lines.extend(['o '+group,'g '+group]);last=group
            if mat!=last_mat:lines.append('usemtl furnace_'+mat);last_mat=mat
            for j in range(1,count-1):lines.append('f '+' '.join(f'{i}/{i}/{i}' for i in (start,start+j,start+j+1)))
        (folder/(stem+'.obj')).write_text('\n'.join(lines)+'\n',encoding='utf-8')
        materials=[]
        for mat in dict.fromkeys(f[4] for f in self.faces):
            _,rough,_=material_parameters(mat,'aod' if material_stem.startswith('small_aod') else 'var')
            if finish and mat in ('plate','edge','north','east','south','west','bottom','chamfer'):rough=finish['roughness']
            ns=min(1000,2/max(.04,rough)**4-2)
            materials.append(f'newmtl furnace_{mat}\nKa 0.25 0.25 0.25\nKd 1 1 1\nKs 1 1 1\nNs {ns:.2f}\nd 1\nillum 2\nmap_Kd {material_stem}_atlas.png\nmap_Ks {material_stem}_specular.png\n')
        (folder/(material_stem+'.mtl')).write_text('\n'.join(materials),encoding='utf-8')
        return dict(compact)



def body_ring(y):
    c=CUT
    return [(1-c,y,0),(c,y,0),(0,y,c),(0,y,1-c),
            (c,y,1),(1-c,y,1),(1,y,1-c),(1,y,c)]


def build(kind):
    m=Mesh()
    m.face([(0,0,0),(1,0,0),(1,0,1),(0,0,1)],'bottom','contact_bottom')
    lower,upper=body_ring(0),body_ring(1)
    for i in range(8):
        j=(i+1)%8
        if i%2==0:
            side=['north','west','south','east'][i//2]
            m.face([lower[i],lower[j],upper[j],upper[i]],side,'contact_'+side)
        else:
            a,b=lower[i],lower[j]
            m.face([(a[0],SOLE,a[2]),(b[0],SOLE,b[2]),upper[j],upper[i]],'chamfer','cut_corners')
            # Fill only the four bottom wedges, keeping the full square sole continuous.
            corner=(round((a[0]+b[0])/2),SOLE,round((a[2]+b[2])/2))
            m.face([(a[0],SOLE,a[2]),corner,(b[0],SOLE,b[2])],'dark','sole_edge')
            for p,q in [(a,corner),(corner,b)]:
                m.face([(p[0],0,p[2]),(q[0],0,q[2]),(q[0],SOLE,q[2]),(p[0],SOLE,p[2])],'dark','sole_edge')
    # An annular octagonal shoulder, entirely above the full-height side contacts.
    inner=[]
    for i in range(8):
        a=math.radians(-67.5-45*i)
        inner.append((.5+.39*math.cos(a),1.025,.5+.39*math.sin(a)))
    for i in range(8):
        j=(i+1)%8;m.face([upper[i],upper[j],inner[j],inner[i]],'plate' if kind=='aod' else 'copper','shoulder')
    center=(.5,.5)
    m.profile([(1.02,.28),(.87,.30),(.2,.32)],'brick' if kind=='aod' else 'copper','lining',8,center=center)
    m.tube((.5,.17,.5),(.5,.2,.5),.32,'dark','lining',8)
    m.tube((.5,.43,.5),(.5,.44,.5),.31,'molten','bath',8)
    m.ring(.98,.28,.39,.045,'brick' if kind=='aod' else 'ceramic','rim',center=center,sides=8)
    # Hardware starts above the full-height side faces.
    for x in (.16,.84):
        m.box((x-.035,1.0,.84),(x+.035,1.17,.94),'dark','hinge')
        m.tube((x-.06,1.16,.86),(x+.06,1.16,.86),.043,'edge','hinge',8)
    for x in (.16,.84):
        for z in (.16,.84):m.tube((x,1.005,z),(x,1.04,z),.025,'edge','shoulder',6)
    # The cover and all its details are a single animation group.
    m.ring(1.03,.075 if kind=='var' else .24,.40,.045,'edge','lid',center=center,sides=8)
    m.profile([(1.075,.4),(1.1,.4),(1.16,.28)],'plate','lid',8,center=center)
    m.ring(1.15,.075 if kind=='var' else .02,.28,.02,'dark','lid',center=center,sides=8)
    if kind=='aod':
        m.tube((.5,1.04,.5),(.5,1.055,.5),.275,'brick','lid',8)
        m.tube((.5,1.15,.5),(.5,1.17,.5),.04,'dark','lid',8)
        for x in (.36,.64):m.tube((x,1.16,.5),(x,1.245,.5),.018,'yellow','lid',6)
        m.tube((.36,1.245,.5),(.64,1.245,.5),.02,'yellow','lid',6)
        m.tube((.88,1.02,.18),(.88,1.09,.18),.018,'pipe','gas_trim',8)
        m.tube((.88,1.09,.18),(.84,1.09,.24),.018,'pipe','gas_trim',8)
        m.tube((.85,1.07,.18),(.92,1.07,.18),.026,'red','gas_trim',6)
    else:
        # Compact consumable electrode feed-through, copper collar and ceramic stack.
        m.ring(1.17,.078,.15,.085,'ceramic','lid',center=center,sides=8)
        m.tube((.5,.73,.5),(.5,1.44,.5),.065,'graphite','electrode',8)
        m.tube((.5,1.31,.5),(.5,1.44,.5),.115,'copper','electrode',8)
        m.tube((.5,1.44,.5),(.5,1.56,.5),.125,'dark','electrode',8)
        for y in (1.455,1.50,1.545):m.tube((.5,y,.5),(.5,y+.012,.5),.137,'edge','electrode',8)
        m.box((.39,1.56,.43),(.61,1.61,.57),'bus','electrode')
        # Front-mounted service mast leaves the rear cover sweep unobstructed.
        m.box((.045,1.,.12),(.115,1.98,.22),'dark','mast')
        m.box((.115,1.92,.14),(.94,1.98,.20),'edge','mast')
        m.tube((.08,1.02,.1),(.08,1.86,.1),.012,'copper','mast',6)
        for y in (1.08,1.36,1.64):m.box((.025,y,.11),(.135,y+.025,.23),'plate','mast')
        m.box((.47,1.93,.14),(.53,1.965,.55),'dark','carriage')
        m.box((.485,1.61,.485),(.515,1.94,.515),'edge','suspension')
        for a,b,c,mat in [((.84,1.02,.12),(.84,1.10,.12),(.82,1.10,.2),'copper'),
                          ((.84,1.02,.88),(.84,1.10,.88),(.82,1.10,.8),'pipe')]:
            m.tube(a,b,.018,mat,'buswork',8);m.tube(b,c,.018,mat,'buswork',8)
    for x in (.16,.84):m.box((x-.022,1.105,.72),(x+.022,1.14,.88),'edge','lid')
    return m


def motion(kind,t):
    # VAR withdraws the electrode, parks the head in the front-right upper corner,
    # and then opens the independent lid. All three stages fit inside 1x1x2.
    if kind=='aod':return t*90,[0,0,0]
    lift=min(1,t/.3)*.3
    slide=max(0,min(1,(t-.3)/.3))
    return max(0,min(1,(t-.6)/.4))*90,[.32*slide,lift,-.32*slide]


def posed(mesh,kind,t):
    result=Mesh();angle,shift=motion(kind,t);a=math.radians(angle);c,s=math.cos(a),math.sin(a)
    def rotate(p):return (p[0],p[1]*c-p[2]*s,p[1]*s+p[2]*c)
    for group,points,n,uv,mat in mesh.faces:
        if group=='lid':points=[add(rotate(sub(p,LID_PIVOT)),LID_PIVOT) for p in points];n=rotate(n)
        if group=='electrode':points=[add(p,shift) for p in points]
        if group=='suspension':
            scale=(.33-shift[1])/.33
            points=[(p[0]+shift[0],1.94+(p[1]-1.94)*scale,p[2]+shift[2]) for p in points]
        if group=='carriage':
            scale=(.41+shift[2])/.41
            points=[(p[0]+shift[0],p[1],.14+(p[2]-.14)*scale) for p in points]
        # Axis-aligned telescoping parts retain the same face normals under positive scale.
        result.faces.append((group,points,n,uv,mat))
    return result


def contacts(kind):
    faces=[('bottom',[0,-1,0],[[0,0,0],[1,0,1]],[0,-1,0],'底面','Y = 0'),
           ('north',[0,0,-1],[[CUT,0,0],[1-CUT,1,0]],[0,0,-1],'北面','Z = 0'),
           ('east',[1,0,0],[[1,0,CUT],[1,1,1-CUT]],[1,0,0],'东面','X = 1'),
           ('south',[0,0,1],[[CUT,0,1],[1-CUT,1,1]],[0,0,1],'南面','Z = 1'),
           ('west',[-1,0,0],[[0,0,CUT],[0,1,1-CUT]],[-1,0,0],'西面','X = 0')]
    result=[]
    for id,n,b,cell,label,plane in faces:
        bottom=id=='bottom'
        result.append(dict(id=id,label=label+'完整面' if bottom else label+'平整连接区',normal=n,bounds=b,plane=plane,
                           size='1 × 1' if bottom else '宽 0.75 × 高 1',area=1 if bottom else .75,code='底面 100%' if bottom else label+' 75%',
                           groups=['contact_'+id],surface='solid',connection_cells=[cell],media=('AOD：HU / KU 能源连接' if kind=='aod' else 'VAR：EU 能源连接') if bottom else '气管、浇铸口、仪表可贴合；此处保留几何空间，不限定该面的功能'))
    result.append(dict(id='feed',label='开盖投料通道',normal=[0,1,0],bounds=[[.34,1.025,.34],[.66,1.025,.66]],plane='Y = 1.025',
                       size='0.32 × 0.32',area=.1024,code='投料',groups=[],surface='opening',connection_cells=[],media='开盖后中央通道畅通；VAR 先提起并移开电极'))
    return result


def bounds(mesh):
    vertices=[p for _,ps,_,_,_ in mesh.faces for p in ps]
    return [[round(fn(p[i] for p in vertices),6) for i in range(3)] for fn in (min,max)]


def validate(mesh,kind,ports):
    areas={}
    for p in ports:
        if p['surface']=='opening':continue
        area=0;axis=next(i for i,n in enumerate(p['normal']) if n)
        for g,ps,n,_,_ in mesh.faces:
            if g not in p['groups']:continue
            assert dot(n,p['normal'])>.99999
            assert all(abs(v[axis]-p['bounds'][0][axis])<1e-7 for v in ps)
            assert all(p['bounds'][0][i]-1e-7<=v[i]<=p['bounds'][1][i]+1e-7 for v in ps for i in range(3))
            for j in range(1,len(ps)-1):area+=math.sqrt(dot(v:=cross(sub(ps[j],ps[0]),sub(ps[j+1],ps[0])),v))/2
        assert abs(area-p['area'])<1e-7
        if p['id']!='bottom':
            assert p['bounds'][0][1]==0 and p['bounds'][1][1]==1
            horizontal=2 if axis==0 else 0
            assert p['bounds'][0][horizontal]==CUT and p['bounds'][1][horizontal]==1-CUT
        areas[p['id']]=area
    sweep=[]
    for step in range(41):
        state=posed(mesh,kind,step/40);bb=bounds(state);sweep.append(bb)
        assert all(-1e-6<=bb[0][i] and bb[1][i]<=[1,2,1][i]+1e-6 for i in range(3)),(kind,step,bb)
        for g,ps,n,uv,_ in state.faces:
            assert abs(dot(n,n)-1)<1e-5
            assert all(math.isfinite(v) for point in ps for v in point)
            assert all(0<=v<=1 for pair in uv for v in pair)
            if g not in [p['groups'][0] for p in ports if p['groups']]:
                # All shapes remain on the machine side of the world-block contact planes.
                assert min(v[1] for v in ps)>=0
    # Exposed corner walls are truly cut back above the thin, full-square sole.
    for g,ps,_,_,_ in mesh.faces:
        if g=='cut_corners':
            assert min(v[1] for v in ps)==SOLE and max(v[1] for v in ps)==1
            assert all(abs(abs(v[0]-.5)+abs(v[2]-.5)-(1-CUT))<1e-7 for v in ps)
    # The open central vertical charge path must remain clear above the mouth.
    for g,ps,_,_,_ in posed(mesh,kind,1).faces:
        overlap=all(max(v[i] for v in ps)>a+1e-6 and min(v[i] for v in ps)<b-1e-6 for i,(a,b) in enumerate(zip([.34,1.025,.34],[.66,2,.66])))
        assert not overlap,(kind,'charging path',g)
    return dict(closed_bounds=bounds(mesh),open_bounds=bounds(posed(mesh,kind,1)),triangles=sum(len(ps)-2 for _,ps,_,_,_ in mesh.faces),
                groups=len({g for g,_,_,_,_ in mesh.faces}),contact_areas=areas,side_width=1-2*CUT,side_height=1,
                corner_cut=CUT,square_sole_height=SOLE,animation_samples=41,within_1x1x2=True,open_feed_clear=True)


def main():
    ap=argparse.ArgumentParser();ap.add_argument('--inline-dir',type=Path)
    ap.add_argument('--gt6-root',type=Path,help='Optional GregTech6 source root to refresh material colors')
    args=ap.parse_args();OUT.mkdir(parents=True,exist_ok=True)
    aod_finishes=finishes(ROOT,args.gt6_root)
    models=[]
    for kind,title in [('aod','小型 AOD'),('var','VAR 电弧炉')]:
        folder=OUT/kind;folder.mkdir(exist_ok=True);stem='small_'+kind
        export_logo(folder,kind)
        neutral,mask,surface=texture_set(kind,TILES,logo_pixels(kind))
        finish=aod_finishes[0] if kind=='aod' else None
        albedo,params=bake_finish(neutral,mask,surface,finish)
        save_maps(folder,stem,albedo,params)
        neutral.save(folder/(stem+'_neutral.png'),optimize=True)
        mask.save(folder/(stem+'_tint_mask.png'),optimize=True)
        surface.save(folder/(stem+'_neutral_surface.png'),optimize=True)
        mesh=build(kind);ports=contacts(kind);stats=validate(mesh,kind,ports)
        compact=mesh.export(folder,stem,stem,finish);opened=posed(mesh,kind,1);opened.export(folder,stem+'_open',stem,finish)
        variant_list=[]
        if kind=='aod':
            for variant in aod_finishes:
                vf=folder/'variants'/variant['id'];vf.mkdir(parents=True,exist_ok=True)
                vs='small_aod_'+variant['id'];va,vp=bake_finish(neutral,mask,surface,variant)
                save_maps(vf,vs,va,vp);mesh.export(vf,vs,vs,variant);opened.export(vf,vs+'_open',vs,variant)
                variant_list.append(dict(variant,path=str(vf.relative_to(folder)).replace('\\','/'),model=vs+'.obj',open_model=vs+'_open.obj'))
        texture_meta=dict(size=[WIDTH,HEIGHT],tile_size=TILE,side_texels=[48,64],
                          albedo=stem+'_atlas.png',surface=stem+'_surface.png',metallic=stem+'_metallic.png',roughness=stem+'_roughness.png',
                          specular=stem+'_specular.png',neutral=stem+'_neutral.png',tint_mask=stem+'_tint_mask.png',neutral_surface=stem+'_neutral_surface.png',
                          surface_channels=dict(R='metalness',G='roughness',B='emission'),albedo_color_space='sRGB',parameter_color_space='linear')
        meta=dict(id=kind,title=title,revision=3,units='blocks',up='+Y',front='-Z',base_cell=[0,0,0],lid_cell=[0,1,0],protected_side_y=[0,1],
                  textures=texture_meta,variants=variant_list,
                  side_contact_width=1-2*CUT,corner_cut=CUT,square_sole_height=SOLE,
                  front_mark=dict(source='User-supplied GT6CE reference',variant='heat' if kind=='aod' else 'arc',file=f'gt6ce_{kind}_mark.svg',surface='texture_only'),
                  ports=ports,stats=stats,lid=dict(group='lid',pivot=LID_PIVOT,open_angle_degrees=90),runtime_integrated=False,
                  motion=dict(type='hinge' if kind=='aod' else 'retract_park_hinge',electrode_group='electrode' if kind=='var' else None,
                              suspension_group='suspension' if kind=='var' else None,carriage_group='carriage' if kind=='var' else None,
                              lift=.3 if kind=='var' else 0,park=[.32,0,-.32] if kind=='var' else [0,0,0],stages=[.3,.6,1] if kind=='var' else [1]))
        (folder/'interfaces.json').write_text(json.dumps(meta,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        encoded=lambda name:base64.b64encode((folder/(stem+name+'.png')).read_bytes()).decode()
        models.append(dict(meta,meshes=compact,texture=encoded('_neutral'),surfaceTexture=encoded('_neutral_surface'),tintTexture=encoded('_tint_mask')))
        print(kind,json.dumps(stats))
    template=Path(__file__).with_name('small_furnaces_viewer.html').read_text(encoding='utf-8')
    fragment=template.replace('/*__MODEL_DATA__*/',json.dumps(dict(models=models),ensure_ascii=False,separators=(',',':')))
    shell=Path(__file__).with_name('viewer_shell.css').read_text(encoding='utf-8')
    (OUT/'preview.html').write_text('<!doctype html>\n<html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>SSITE · Small AOD & VAR</title><style>'+shell+'</style></head><body>\n'+fragment+'\n</body></html>',encoding='utf-8')
    if args.inline_dir:
        assert len(fragment.encode('utf-8'))<1_000_000
        args.inline_dir.mkdir(parents=True,exist_ok=True);(args.inline_dir/'small-aod-var.html').write_text(fragment,encoding='utf-8')


if __name__=='__main__':main()
