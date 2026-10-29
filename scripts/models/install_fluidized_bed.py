"""Build and install the fluidized-bed OBJ, atlas and five block/item resource sets.

Recipes, translations and Java registration are maintained outside this script.
Requires Python 3 and Pillow; may be run from any working directory.
"""
from __future__ import annotations

import json
import shutil
from pathlib import Path

from PIL import Image

import build_fluidized_bed as art
from fluidized_bed_materials import MATERIALS, TILE

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "common" / "src" / "main" / "resources" / "assets" / "ssite"
TEXTURES = ASSETS / "textures" / "block" / "machine" / "fluidized_bed"
PARTS = {
    "fluidized_bed_combustor": {"side": "plate", "north": "panel", "up": "shell", "down": "dark"},
    "fluidized_bed_casing": {"side": "shell", "up": "plate", "down": "dark"},
    "fluidized_bed_duct": {"side": "insulation"},
    "fluidized_bed_heat_plate": {"side": "refractory", "up": "ceramic", "down": "dark"},
    "fluidized_bed_foundation": {"side": "dark", "up": "plate"},
}


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    art.main()
    obj = ASSETS / "obj" / "fluidized_bed_combustor.obj"
    atlas = ASSETS / "textures" / "machine" / "fluidized_bed_combustor.png"
    obj.parent.mkdir(parents=True, exist_ok=True)
    atlas.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(art.OUT / (art.STEM + ".obj"), obj)
    shutil.copyfile(art.OUT / (art.STEM + "_atlas.png"), atlas)
    write(atlas.with_suffix(".png.mcmeta"), {"texture": {"blur": False, "clamp": False}})
    TEXTURES.mkdir(parents=True, exist_ok=True)
    with Image.open(atlas) as image:
        for tile in sorted({tile for surfaces in PARTS.values() for tile in surfaces.values()}):
            index = MATERIALS.index(tile)
            x, y = index % 8 * TILE, index // 8 * TILE
            path = TEXTURES / (tile + ".png")
            image.crop((x, y, x + TILE, y + TILE)).save(path)
            write(path.with_suffix(".png.mcmeta"), {"texture": {"blur": False, "clamp": False}})
    for name, surfaces in PARTS.items():
        model = "ssite:block/machine/parts/" + name
        textures = {
            face: "ssite:block/machine/fluidized_bed/" + surfaces.get(face, surfaces["side"])
            for face in ("down", "up", "north", "south", "west", "east")
        }
        textures["particle"] = "ssite:block/machine/fluidized_bed/" + surfaces["side"]
        variants = {"": {"model": model}}
        if name == "fluidized_bed_combustor":
            variants = {"facing=" + face: {"model": model, "y": rotation}
                        for face, rotation in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}
        write(ASSETS / "blockstates" / (name + ".json"), {"variants": variants})
        write(ASSETS / "models" / "block" / "machine" / "parts" / (name + ".json"), {
            "parent": "minecraft:block/cube", "textures": textures,
        })
        write(ASSETS / "models" / "item" / (name + ".json"), {"parent": model})
    assert obj.read_bytes() == (art.OUT / (art.STEM + ".obj")).read_bytes()
    assert atlas.read_bytes() == (art.OUT / (art.STEM + "_atlas.png")).read_bytes()
    print("Installed fluidized-bed OBJ, nearest-filtered atlas, and five block/item resource sets.")


if __name__ == "__main__":
    main()
