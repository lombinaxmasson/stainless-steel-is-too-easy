"""Verify the small-furnace preview. Requires Python Playwright + Chromium."""
import argparse
import json
import re
from pathlib import Path
from PIL import Image
from playwright.sync_api import sync_playwright

ROOT=Path(__file__).resolve().parents[2]


def inspect_assets():
    folder=ROOT/'art/small_furnaces/aod'
    meta=json.loads((folder/'interfaces.json').read_text(encoding='utf-8'))
    enum=(ROOT/'common/src/main/java/com/lombinaxmasson/ssite/machine/AodTier.java').read_text(encoding='utf-8')
    tiers=re.findall(r'\b[A-Z][A-Z0-9_]*\("([a-z0-9_]+)",\s*"([A-Za-z0-9]+)"\)',enum)
    assert [(v['id'],v['material']) for v in meta['variants']]==tiers
    mask=list(Image.open(folder/'small_aod_tint_mask.png').getdata())
    fixed=None;hulls=set();geometry=None
    for variant in meta['variants']:
        vf=folder/variant['path'];stem=Path(variant['model']).stem
        albedo=Image.open(vf/(stem+'_atlas.png'))
        assert albedo.size==(512,256)
        pixels=list(albedo.getdata())
        overlay=[p for p,m in zip(pixels,mask) if not m]
        if fixed is None:fixed=overlay
        assert overlay==fixed,'Recoloring changed fixed logo / lettering / ceramic details'
        hulls.add(bytes(c for p,m in zip(pixels,mask) if m for c in p))
        vertices=[line for line in (vf/variant['model']).read_text().splitlines() if line.startswith('v ')]
        if geometry is None:geometry=vertices
        assert vertices==geometry,'A material variant changed the geometry'
    assert len(hulls)==len(tiers)
    for path in (ROOT/'art/small_furnaces').rglob('*.obj'):
        counts={'v':0,'vt':0,'vn':0};faces=[];used=set();mtl=None
        for line in path.read_text().splitlines():
            parts=line.split()
            if not parts:continue
            if parts[0] in counts:counts[parts[0]]+=1
            if parts[0]=='f':
                assert len(parts)==4
                faces.extend(tuple(map(int,t.split('/'))) for t in parts[1:])
            if parts[0]=='mtllib':mtl=path.parent/parts[1]
            if parts[0]=='usemtl':used.add(parts[1])
        assert all(1<=v<=counts['v'] and 1<=t<=counts['vt'] and 1<=n<=counts['vn'] for v,t,n in faces)
        text=mtl.read_text();assert used<=set(re.findall(r'^newmtl (.+)$',text,re.M))
        for texture in re.findall(r'^map_\w+ (.+)$',text,re.M):assert (path.parent/texture).is_file()
    return meta['variants']


def main():
    ap=argparse.ArgumentParser();ap.add_argument('--output',type=Path,default=ROOT/'build'/'small-furnaces-qa');args=ap.parse_args()
    args.output.mkdir(parents=True,exist_ok=True)
    variants=inspect_assets()
    errors=[]
    with sync_playwright() as p:
        browser=p.chromium.launch(headless=True)
        page=browser.new_page(viewport={'width':1024,'height':850},color_scheme='light')
        page.on('pageerror',lambda e:errors.append(str(e)))
        page.goto((ROOT/'art'/'small_furnaces'/'preview.html').as_uri())
        root=page.locator('#ssite-small-furnaces')
        page.locator('#ssite-small-furnaces[data-ready=true][data-texture-ready=true]').wait_for()
        def frame():page.evaluate('new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)))')
        def state(key,value):page.wait_for_function('([key,value])=>document.getElementById("ssite-small-furnaces").dataset[key]===value',arg=[key,value])
        images=set()
        for variant in variants:
            page.locator('select[data-finish]').select_option(variant['id']);state('finish',variant['id']);frame()
            images.add(page.locator('canvas').screenshot())
            page.locator('.aod-stage').screenshot(path=str(args.output/('aod-'+variant['id']+'.png')))
        assert len(images)==len(variants),'Finish picker did not visibly update the material'
        page.locator('select[data-finish]').select_option('steel');state('finish','steel')
        for machine in ('aod','var'):
            page.locator('button[data-machine='+machine+']').click();state('machine',machine);frame()
            assert page.locator('[data-finish-field]').is_visible()==(machine=='aod')
            page.screenshot(path=str(args.output/(machine+'-closed.png')))
            page.locator('[data-ports]').uncheck();frame()
            page.locator('.aod-stage').screenshot(path=str(args.output/(machine+'-beauty.png')))
            page.locator('[data-ports]').check()
            page.locator('[data-view=bottom]').click();state('selected','bottom');frame()
            assert '1 × 1' in page.locator('.aod-detail').inner_text()
            assert ('EU' if machine=='var' else 'HU / KU') in page.locator('.aod-detail').inner_text()
            page.screenshot(path=str(args.output/(machine+'-bottom.png')))
            for side in ('north','east','south','west'):
                page.locator('[data-port]').select_option(side);state('selected',side);frame()
                assert '宽 0.75 × 高 1' in page.locator('.aod-detail').inner_text()
            page.locator('[data-view=sides]').click();state('selected','north');frame()
            page.screenshot(path=str(args.output/(machine+'-flat-side.png')))
            page.locator('[data-ports]').uncheck();frame()
            page.locator('.aod-stage').screenshot(path=str(args.output/(machine+'-lettering.png')))
            page.locator('[data-ports]').check();frame()
            page.locator('[data-port]').select_option('none');state('selected','none');frame()
            label=page.locator('.aod-port-label').filter(has_text='北面 75%');box=label.bounding_box()
            assert box
            page.mouse.click(box['x']+box['width']/2,box['y']+box['height']/2+9);state('selected','north')
            page.locator('[data-port]').select_option('feed');state('lid','1.000');frame()
            page.screenshot(path=str(args.output/(machine+'-open.png')))
            assert page.locator('.aod-port-label').filter(has_text='投料').is_visible()
            page.locator('button[data-lid]').click();state('lid','0.000')
            assert not page.locator('.aod-port-label').filter(has_text='投料').is_visible()
            page.locator('[data-view=iso]').click();frame()
            before=root.get_attribute('data-view');box=page.locator('canvas').bounding_box();x=box['x']+box['width']/2;y=box['y']+box['height']/2
            page.mouse.move(x,y);page.mouse.down();page.mouse.move(x+120,y+20,steps=10);page.mouse.up();frame()
            assert root.get_attribute('data-view')!=before
            before=root.get_attribute('data-view');page.mouse.wheel(0,-150);frame();assert root.get_attribute('data-view')!=before
            page.locator('[data-view=iso]').click()
            for width in (736,360):
                page.set_viewport_size({'width':width,'height':940})
                for theme in ('light','dark'):
                    page.emulate_media(color_scheme=theme);frame()
                    assert page.evaluate('document.documentElement.scrollWidth<=innerWidth')
                    page.screenshot(path=str(args.output/f'{machine}-{width}-{theme}.png'))
            page.set_viewport_size({'width':1024,'height':850});page.emulate_media(color_scheme='light')
        page.emulate_media(reduced_motion='reduce')
        page.locator('button[data-lid]').click();state('lid','1.000')
        page.locator('button[data-machine=aod]').click();state('machine','aod');state('lid','0.000')
        assert not errors,errors
        page.locator('select[data-finish]').select_option('tungsten_steel');state('finish','tungsten_steel')
        page.locator('button[data-machine=var]').click();state('finish','copper')
        page.locator('button[data-machine=aod]').click();state('finish','tungsten_steel')
        print(json.dumps({'errors':errors,'checks':['all six AodTier materials','masked recolor / fixed graphics','all OBJ / MTL references','512x256 textures','finish switch and persistence','5 flat connection faces per model','face ray picking','lid states / VAR staged motion','open feed','orbit and zoom','switch while open','reduced motion','736 / 360px','light / dark']},ensure_ascii=False))
        browser.close()


if __name__=='__main__':main()
