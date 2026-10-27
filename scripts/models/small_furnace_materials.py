"""64px furnace tiles, hand-drawn pixel lettering and masked AOD material finishes.

This is the source of the textures, not an upscale of the former 32px bitmap.
Surface map (linear data): R = metalness, G = roughness, B = emission.
"""
from __future__ import annotations
import json
import random
import re
from pathlib import Path
from PIL import Image, ImageDraw

TILE=64
WIDTH,HEIGHT=512,256
GLYPHS={
    'A':['01110','11011','11011','11111','11011','11011','11011'],
    'O':['01110','11011','11011','11011','11011','11011','01110'],
    'D':['11110','11011','11011','11011','11011','11011','11110'],
    'V':['11011','11011','11011','11011','11011','01110','00100'],
    'R':['11110','11011','11011','11110','11100','11010','11011'],
}


def finishes(root,gt6_root=None):
    config=json.loads(Path(__file__).with_name('aod_materials.json').read_text(encoding='utf-8'))
    enum=(root/'common/src/main/java/com/lombinaxmasson/ssite/machine/AodTier.java').read_text(encoding='utf-8')
    tiers=re.findall(r'\b[A-Z][A-Z0-9_]*\("([a-z0-9_]+)",\s*"([A-Za-z0-9]+)"\)',enum)
    entries={m['id']:m for m in config['materials']}
    assert set(entries)=={t[0] for t in tiers},'Update aod_materials.json to match AodTier.java'
    result=[]
    for suffix,material in tiers:
        entry=dict(entries[suffix]);assert entry['material']==material
        if gt6_root:
            source=gt6_root/'core/src/main/java/gregtech/content/material/generated'/entry['source_file']
            line=next(s for s in source.read_text(encoding='utf-8').splitlines()
                      if re.search(r'\bGTMaterial\s+'+re.escape(entry['source_field'])+r'\s*=',s))
            entry['color']='#'+re.search(r'0x([0-9A-Fa-f]{6})\b',line).group(1).upper()
        rgb=tuple(int(entry['color'][i:i+2],16) for i in (1,3,5))
        entry['tint_rgb']=[round(56+.70*c) for c in rgb]
        entry['machine_type']='aod_converter_'+suffix
        result.append(entry)
    return result


def material_parameters(name,kind):
    if name in ('brick','molten','yellow','red','graphite','seal'):
        return (0,.94,0) if name=='brick' else ((0,1,1) if name=='molten' else (0,.72,0))
    if name in ('ceramic','insulator'):return (0,.34,0)
    if name in ('copper','bus','warm') or (kind=='var' and name in ('north','east','south','west')):return (.90,.27,0)
    if name in ('edge','pipe','gas'):return (.86,.29,0)
    return (.80,.46,0)


def parameters_rgb(params):return tuple(round(c*255) for c in params)


def pixel_text(draw,text,x,y,scale=2,color=(242,235,205)):
    for ch in text:
        for dy,row in enumerate(GLYPHS[ch]):
            for dx,bit in enumerate(row):
                if bit=='1':draw.rectangle((x+dx*scale,y+dy*scale,x+(dx+1)*scale-1,y+(dy+1)*scale-1),fill=color)
        x+=6*scale


def texture_set(kind,tiles,logo):
    rng=random.Random(2701 if kind=='aod' else 2702)
    shell=(210,210,210) if kind=='aod' else (161,105,65)
    palette=[(211,211,211) if kind=='aod' else (90,96,93),(39,45,45),(240,240,240) if kind=='aod' else (155,163,150),
             (154,101,62),(174,112,69),(202,195,166),(43,46,47),(198,153,46),
             (72,81,80),(111,133,131),(153,66,43),(243,115,33)]
    palette += [shell]*4+[(151,151,151) if kind=='aod' else (68,70,62),(63,71,68),(45,48,42),(184,180,154),
                         (108,153,159),(180,119,67),(53,66,65),(135,80,42),
                         (132,132,132) if kind=='aod' else (99,66,45)]
    atlas=Image.new('RGB',(WIDTH,HEIGHT),(30,34,33))
    mask=Image.new('L',(WIDTH,HEIGHT),0)
    surface=Image.new('RGB',(WIDTH,HEIGHT),(0,255,0))
    tint_tiles={'plate','edge','north','east','south','west','bottom','chamfer'}
    for index,(name,color) in enumerate(zip(tiles,palette)):
        tile=Image.new('RGB',(TILE,TILE));tint=255 if kind=='aod' and name in tint_tiles else 0
        tm=Image.new('L',(TILE,TILE),tint)
        sm=Image.new('RGB',(TILE,TILE),parameters_rgb(material_parameters(name,kind)))
        # Pixel grain and restrained horizontal brushing are drawn at native 64px.
        for y in range(TILE):
            brush=rng.choice([-2,-1,0,0,1,2]) if material_parameters(name,kind)[0]>.5 else 0
            for x in range(TILE):
                n=rng.choice([-1,0,0,0,1])+brush
                tile.putpixel((x,y),tuple(max(0,min(255,c+n)) for c in color))
        d,md,sd=ImageDraw.Draw(tile),ImageDraw.Draw(tm),ImageDraw.Draw(sm)
        def rect(box,fill,outline=None,masked=False,params=None):
            d.rectangle(box,fill=fill,outline=outline)
            md.rectangle(box,fill=255 if masked and kind=='aod' else 0)
            if params is not None:sd.rectangle(box,fill=parameters_rgb(params))
        def border(box,fill,width=1):
            d.rectangle(box,outline=fill,width=width);md.rectangle(box,outline=0,width=width)
        if name in ('plate','bottom','name'):
            border((0,0,63,63),(32,38,36),2)
            d.line((2,60,2,2,60,2),fill=tuple(min(c+20,255) for c in color))
            for x in (6,57):
                for y in (6,57):
                    rect((x-2,y-2,x+2,y+2),(43,49,46),params=(.75,.4,0));d.point((x-1,y-1),fill=(203,205,180))
        if name=='brick':
            for y in range(0,64,16):
                d.line((0,y,63,y),fill=(70,57,42),width=2);d.line((0,y+2,63,y+2),fill=(184,129,80))
                for x in range(0 if y%32 else 16,64,32):d.line((x,y,x,y+15),fill=(78,61,44),width=2)
        if name in ('grate','vent','bus'):
            for y in range(5,64,10):
                d.rectangle((4,y,59,y+2),fill=(29,36,34));d.line((4,y+3,59,y+3),fill=tuple(min(c+25,255) for c in color))
        if name in ('ceramic','insulator'):
            for y in range(0,64,16):d.line((0,y,63,y),fill=(107,111,103),width=2);d.line((0,y+4,63,y+4),fill=(226,220,193))
        if name=='molten':
            for _ in range(100):
                x,y=rng.randrange(64),rng.randrange(64);d.rectangle((x,y,x+3,y+1),fill=rng.choice([(255,175,48),(233,94,26),(255,211,89)]))
        if name in ('north','east','south','west'):
            # 48 x 64 texels = the exact width/height ratio of a 0.75 x 1 face.
            border((8,0,55,63),(28,35,33),2)
            d.line((10,60,10,2,53,2),fill=tuple(min(c+24,255) for c in color))
            for x in (12,51):
                for y in (6,57):rect((x,y,x+1,y+1),(193,197,170),params=(.65,.4,0))
            if name=='north':
                rect((13,3,50,21),(32,41,40),params=(0,.78,0))
                pixel_text(d,'AOD' if kind=='aod' else 'VAR',15,6)
                # A precise 2px pixel-grid logo; the face lettering is redrawn, not filtered.
                for (x,y),c in logo.items():rect((15+x*2,25+y*2,16+x*2,26+y*2),c,params=(0,.72,0))
                rect((20,60,43,61),(203,161,58) if kind=='aod' else (206,207,179),params=(0,.7,0))
            elif name=='east':
                rect((16,14,47,50),(43,50,48),outline=(139,146,128),params=(.35,.55,0))
                d.ellipse((20,20,44,44),fill=(130,173,163));d.ellipse((26,26,38,38),fill=(24,32,31))
                sd.ellipse((20,20,44,44),fill=parameters_rgb((.25,.48,0)))
                rect((24,51,40,53),(146,130,170),params=(0,.72,0))
            elif name=='west':
                rect((16,14,47,50),(43,50,48),outline=(139,146,128),params=(.3,.6,0))
                rect((20,20,42,34),(47,104,84),params=(0,.38,0));d.line((24,24,36,24),fill=(197,224,161),width=2)
                for x,c in [(22,(181,64,44)),(32,(181,144,52)),(42,(99,158,100))]:rect((x,42,x+2,44),c,params=(0,.48,0))
            else:
                rect((16,14,47,50),(43,50,48),outline=(139,146,128),params=(.3,.6,0))
                if kind=='var':d.polygon([(34,16),(22,34),(32,34),(26,46),(46,26),(34,26),(42,16)],fill=(222,178,63))
                else:
                    for x in (22,32,42):d.line((x,42,x-2,36,x+2,30,x,22),fill=(208,140,74),width=3)
        if name=='chamfer':
            for x in (4,59):d.line((x,0,x,63),fill=tuple(min(c+35,255) for c in color),width=2)
            for y in (4,59):d.line((6,y,57,y),fill=(30,36,34),width=2);md.line((6,y,57,y),fill=0,width=2)
            for y in (10,52):rect((26,y,37,y+2),(158,164,143),params=(.8,.35,0))
        if name=='bottom':
            border((14,14,49,49),(112,107,82),2)
            for y in (20,30,40):d.line((18,y,44,y),fill=(128,109,74),width=2);md.line((18,y,44,y),fill=0,width=2)
        if name=='name':pixel_text(d,'AOD' if kind=='aod' else 'VAR',15,25)
        pos=(index%8*TILE,index//8*TILE)
        atlas.paste(tile,pos);mask.paste(tm,pos);surface.paste(sm,pos)
    return atlas,mask,surface


def bake_finish(neutral,mask,surface,finish):
    albedo=neutral.copy();params=surface.copy()
    if finish:
        tint=finish['tint_rgb'];p=parameters_rgb((finish['metalness'],finish['roughness'],0))
        for i,m in enumerate(mask.getdata()):
            if not m:continue
            xy=(i%WIDTH,i//WIDTH)
            albedo.putpixel(xy,tuple(round(c*t/255) for c,t in zip(neutral.getpixel(xy),tint)))
            params.putpixel(xy,p)
    return albedo,params


def save_maps(folder,stem,albedo,surface):
    albedo.save(folder/f'{stem}_atlas.png',optimize=True)
    surface.save(folder/f'{stem}_surface.png',optimize=True)
    surface.getchannel('R').save(folder/f'{stem}_metallic.png',optimize=True)
    surface.getchannel('G').save(folder/f'{stem}_roughness.png',optimize=True)
    # Classic OBJ/MTL specular approximation; the preview uses the surface map directly.
    spec=Image.new('RGB',albedo.size)
    spec.putdata([tuple(round((.04*(1-m/255)+(c/255)**2.2*m/255)*255) for c in rgb)
                  for rgb,(m,_,_) in zip(albedo.getdata(),surface.getdata())])
    spec.save(folder/f'{stem}_specular.png',optimize=True)
