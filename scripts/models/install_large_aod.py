"""Generate runtime resources for the approved large AOD, including six material palettes."""
import json
import shutil
from pathlib import Path
import build_large_aod as art

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'common/src/main/resources/assets/ssite'
RECIPES = ROOT / 'common/src/main/resources/data/ssite/recipes'

def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def block(name, texture, item_parent=None):
    write(ASSETS / f'blockstates/{name}.json', {'variants': {'': {'model': f'ssite:block/machine/hidden/{name}'}}})
    write(ASSETS / f'models/block/machine/hidden/{name}.json', {
        'parent':'minecraft:block/block', 'textures':{'particle':texture}, 'elements':[]})
    write(ASSETS / f'models/item/{name}.json', {'parent': item_parent or f'ssite:block/machine/parts/{name}'})
    if not item_parent:
        write(ASSETS / f'models/block/machine/parts/{name}.json', {
            'parent':'minecraft:block/cube_all', 'textures':{'all':texture}})

def recipe(name, pattern, keys, count=1):
    write(RECIPES / f'{name}.json', {'type':'minecraft:crafting_shaped', 'pattern':pattern,
          'key':{k:{'item':v} for k,v in keys.items()}, 'result':{'item':'ssite:'+name, 'count':count}})

def main():
    materials = json.loads((ROOT/'scripts/models/aod_materials.json').read_text(encoding='utf-8'))['materials']
    palette = dict(art.PALETTE)
    metallic = {'plate','shell','dark','edge','grate','pipe'}
    out = ASSETS/'textures/machine/large_aod'
    out.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(ROOT/'art/large_aod/large_aod.obj', ASSETS/'obj/large_aod.obj')
    for mat in materials:
        name = mat['id']
        color = tuple(int(mat['color'][i:i+2],16) for i in (1,3,5))
        tint = tuple(56 + .70*c for c in color)
        art.PALETTE.update({k:tuple(round(c*t/147) for c,t in zip(v,tint)) if k in metallic else v
                            for k,v in palette.items()})
        art.atlas().save(out/f'{name}.png')
        write(out/f'{name}.png.mcmeta', {'texture':{'blur':False,'clamp':False}})
        texture = f'ssite:block/machine/aod_converter/{name}_side'
        controller, casing = 'large_aod_'+name, 'large_aod_casing_'+name
        block(controller,texture,f'ssite:block/machine/basic/aod_converter_{name}')
        block(casing,texture)
        small = json.loads((RECIPES/f'aod_converter_{name}.json').read_text())
        recipe(casing,['PFP','FMF','PFP'],{'P':small['key']['P']['item'],'M':small['key']['M']['item'],
                'F':'gt6ce:fire_bricks'},4)
        recipe(controller,['CMC','OHA','CLC'],{'C':'ssite:'+casing,'M':'ssite:aod_converter_'+name,
                'O':'ssite:large_aod_oxygen','A':'ssite:large_aod_argon','H':'ssite:large_aod_heat','L':'ssite:aod_lid'})
    for part in ('heat','oxygen','argon','lid'):
        block('large_aod_'+part,'ssite:block/aod_lid', 'ssite:block/machine/basic/aod_lid_item')
    recipe('large_aod_heat',['FFF','PPP','FFF'],{'F':'gt6ce:fire_bricks','P':'gt6ce:plate_double_steel'},3)
    for gas in ('oxygen','argon'):
        recipe('large_aod_'+gas,['PPP','PSP','PPP'],{'P':'gt6ce:plate_stainlesssteel',
                'S':'gt6ce:pipe_small_'+('steel' if gas == 'oxygen' else 'copper')})
    recipe('large_aod_lid',['LLL','LFL','LLL'],{'L':'ssite:aod_lid','F':'gt6ce:fire_bricks'})
    for language in ('en_us','zh_cn'):
        path=ASSETS/f'lang/{language}.json'
        strings=json.loads(path.read_text(encoding='utf-8'))
        cn=language=='zh_cn'
        for mat in materials:
            label=mat['label'] if cn else mat['material']
            strings['block.ssite.large_aod_'+mat['id']]=f'{label}大型 AOD 浇铸控制器' if cn else f'{label} Large AOD Casting Controller'
            strings['block.ssite.large_aod_casing_'+mat['id']]=f'{label}大型 AOD 炉壁' if cn else f'{label} Large AOD Wall'
        for name,zh,en in [('heat','HU 受热块','HU Heat Receiver'),('oxygen','氧气口','Oxygen Inlet'),
                           ('argon','氩气口','Argon Inlet'),('lid','投料炉盖','Charging Lid')]:
            strings['block.ssite.large_aod_'+name]=('大型 AOD '+zh) if cn else ('Large AOD '+en)
        for mode,zh,en in [(0,'手动','Manual'),(1,'有信号开盖','Signal opens lid'),(2,'无信号开盖','Signal closes lid')]:
            strings[f'ssite.aod.lid.mode.{mode}']=f'炉盖：{zh}（潜行右键切换模式）' if cn else f'Lid: {en} (sneak-use to change mode)'
        strings['ssite.recipe.large_aod']='大型 AOD 精炼' if cn else 'Large AOD Refining'
        write(path,strings)
    print('Installed large AOD OBJ, six palettes, 16 blocks and crafting recipes.')

if __name__=='__main__': main()
