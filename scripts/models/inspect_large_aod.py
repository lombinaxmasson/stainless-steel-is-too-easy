"""Isolated Chromium QA for the model preview. Requires Python Playwright."""
import argparse
import json
from pathlib import Path
from playwright.sync_api import sync_playwright

ROOT=Path(__file__).resolve().parents[2]


def main():
    ap=argparse.ArgumentParser();ap.add_argument('--output',type=Path,default=ROOT/'build'/'large-aod-qa');args=ap.parse_args()
    args.output.mkdir(parents=True,exist_ok=True)
    failures=[]
    with sync_playwright() as p:
        browser=p.chromium.launch(headless=True)
        page=browser.new_page(viewport={'width':1050,'height':820},device_scale_factor=1,color_scheme='light')
        page.on('pageerror',lambda e:failures.append(str(e)))
        page.goto((ROOT/'art'/'large_aod'/'preview.html').as_uri())
        page.locator('#ssite-aod-preview[data-ready="true"][data-texture-ready="true"]').wait_for()
        def frame():page.evaluate('new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)))')
        def selected(id):page.wait_for_function('(id)=>document.getElementById("ssite-aod-preview").dataset.selected===id',arg=id)
        frame();page.screenshot(path=str(args.output/'closed.png'))
        original=page.locator('#ssite-aod-preview').get_attribute('data-view')
        canvas=page.locator('canvas').bounding_box();x=canvas['x']+canvas['width']/2;y=canvas['y']+canvas['height']/2
        page.mouse.move(x,y);page.mouse.down();page.mouse.move(x+140,y+30,steps=12);page.mouse.up()
        page.wait_for_function('(before)=>document.getElementById("ssite-aod-preview").dataset.view!==before',arg=original)
        before=page.locator('#ssite-aod-preview').get_attribute('data-view')
        page.mouse.wheel(0,-200)
        page.wait_for_function('(before)=>document.getElementById("ssite-aod-preview").dataset.view!==before',arg=before)
        page.get_by_role('button',name='底部 HU',exact=True).click();selected('heat');frame()
        assert '3 × 3' in page.locator('.aod-detail').inner_text()
        assert 'Y = 0' in page.locator('.aod-detail').inner_text()
        page.screenshot(path=str(args.output/'heat-bottom.png'))
        page.get_by_role('button',name='浇铸面',exact=True).click();selected('casting');frame()
        assert 'Z = −2' in page.locator('.aod-detail').inner_text()
        page.screenshot(path=str(args.output/'casting.png'))
        # Test face-based ray picking rather than only the selector.
        page.locator('[data-port]').select_option('none');selected('none');frame()
        label=page.locator('.aod-port-label').filter(has_text='浇铸');label.wait_for(state='visible');bounds=label.bounding_box()
        page.mouse.click(bounds['x']+bounds['width']/2,bounds['y']+bounds['height']/2+9);selected('casting')
        page.locator('[data-port]').select_option('oxygen');selected('oxygen');frame()
        page.screenshot(path=str(args.output/'gas-flange.png'))
        page.locator('[data-port]').select_option('feed')
        page.wait_for_function('document.getElementById("ssite-aod-preview").dataset.lid==="105.00"');frame()
        page.screenshot(path=str(args.output/'lid-open.png'))
        page.get_by_role('button',name='关闭炉盖',exact=True).click()
        page.wait_for_function('document.getElementById("ssite-aod-preview").dataset.lid==="0.00"')
        assert not page.locator('.aod-port-label').filter(has_text='投料').is_visible()
        page.emulate_media(reduced_motion='reduce')
        page.get_by_role('button',name='开盖投料',exact=True).click()
        page.wait_for_function('document.getElementById("ssite-aod-preview").dataset.lid==="105.00"')
        page.get_by_role('button',name='关闭炉盖',exact=True).click()
        page.wait_for_function('document.getElementById("ssite-aod-preview").dataset.lid==="0.00"')
        page.get_by_role('button',name='全貌',exact=True).click();selected('none')
        page.locator('[data-ports]').uncheck();frame()
        page.locator('.aod-stage').screenshot(path=str(args.output/'beauty.png'))
        page.locator('[data-ports]').check()
        for width in (736,360):
            page.set_viewport_size({'width':width,'height':900})
            for theme in ('light','dark'):
                page.emulate_media(color_scheme=theme);frame()
                page.screenshot(path=str(args.output/f'{width}-{theme}.png'))
                assert page.evaluate('document.documentElement.scrollWidth<=innerWidth'),f'Overflow at {width}'
        assert not failures,failures
        print(json.dumps({'browser':'isolated Chromium','errors':failures,'checks':['WebGL render','orbit drag','wheel zoom','underside view','face selector','casting face ray picking','lid animation open / closed','opening hidden when closed','reduced motion','736px / 360px','light / dark','no horizontal overflow']},ensure_ascii=False))
        browser.close()


if __name__=='__main__':main()
