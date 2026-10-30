"""Inspect the offline model viewer in isolated Chromium; requires Playwright.

Default: check controls and responsive layouts, saving captures under build/.
To refresh the three art previews after rebuilding geometry:
    python scripts/models/inspect_fluidized_bed.py --smoke --output art/fluidized_bed
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path

from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[2]
VIEWER = "#ssite-fluidized-bed-preview"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=ROOT / "build" / "fluidized-bed-qa")
    parser.add_argument("--input", type=Path, default=ROOT / "art" / "fluidized_bed" / "preview.html")
    parser.add_argument("--smoke", action="store_true", help="Check loading and cutaway; capture three views only.")
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    errors = []
    checks = ["WebGL2 render", "texture loaded", "cutaway hides groups", "three model captures"]

    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True, timeout=15000)
        try:
            page = browser.new_page(
                viewport={"width": 1200, "height": 900},
                device_scale_factor=1,
                color_scheme="light",
            )
            page.on("pageerror", lambda error: errors.append(str(error)))
            page.goto(args.input.resolve().as_uri())
            page.locator(VIEWER + '[data-ready="true"][data-texture-ready="true"]').wait_for()

            def frame():
                page.evaluate("new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)))")

            def state():
                return page.evaluate("window.__fluidizedBedViewer.getState()")

            def capture(name):
                frame()
                page.locator(".fb-stage").screenshot(path=str(args.output / name))

            def view(name):
                page.locator(f'button[data-view="{name}"]').click()
                frame()
                assert state()["currentView"] == name

            frame()
            assert page.locator(".fb-error").inner_text() == ""
            whole = state()["visibleGroups"]
            assert whole, "The viewer has no model components"
            capture("preview.png")
            page.locator("input[data-cutaway]").check()
            page.wait_for_function(
                'document.getElementById("ssite-fluidized-bed-preview").dataset.cutaway==="true"'
            )
            inside = state()["visibleGroups"]
            assert inside and set(inside) < set(whole), "Cutaway did not hide the outer shell"
            capture("preview_cutaway.png")
            page.locator("input[data-cutaway]").uncheck()
            view("back")
            assert state()["visibleGroups"] == whole, "Closing cutaway did not restore the shell"
            capture("preview_back.png")
            view("iso")

            if not args.smoke:
                before = page.locator(VIEWER).get_attribute("data-view")
                box = page.locator("canvas").bounding_box()
                x, y = box["x"] + box["width"] / 2, box["y"] + box["height"] / 2
                page.mouse.move(x, y)
                page.mouse.down()
                page.mouse.move(x + 100, y + 20, steps=5)
                page.mouse.up()
                page.wait_for_function(
                    '(v)=>document.getElementById("ssite-fluidized-bed-preview").dataset.view!==v',
                    arg=before,
                )
                old_zoom = state()["zoom"]
                page.mouse.wheel(0, 100)
                page.wait_for_function(
                    "(z)=>window.__fluidizedBedViewer.getState().zoom>z", arg=old_zoom
                )
                choice = "bed" if "bed" in whole else inside[0]
                page.locator("[data-part]").select_option(choice)
                page.wait_for_function(
                    '(v)=>document.getElementById("ssite-fluidized-bed-preview").dataset.selected===v',
                    arg=choice,
                )
                page.locator("[data-part]").select_option("")
                for name in ("front", "back", "top", "iso"):
                    view(name)
                page.locator("input[data-grid]").check()
                frame()
                assert state()["showGrid"]
                page.locator("input[data-grid]").uncheck()
                frame()
                assert not state()["showGrid"]
                for width in (736, 360):
                    page.set_viewport_size({"width": width, "height": 900})
                    for theme in ("light", "dark"):
                        page.emulate_media(color_scheme=theme)
                        frame()
                        assert page.evaluate("document.documentElement.scrollWidth<=innerWidth"), (
                            width, theme, "horizontal overflow"
                        )
                        page.screenshot(path=str(args.output / f"{width}-{theme}.png"))
                checks += [
                    "drag rotation", "wheel zoom", "component selection", "four view buttons",
                    "grid toggle", "736/360 light/dark no overflow",
                ]
            assert not errors, errors
            print(json.dumps({
                "browser": "isolated Chromium", "mode": "smoke" if args.smoke else "full",
                "checks": checks, "errors": errors, "output": str(args.output.resolve()),
            }, ensure_ascii=False))
        finally:
            browser.close()


if __name__ == "__main__":
    main()
