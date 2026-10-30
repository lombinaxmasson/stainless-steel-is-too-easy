"""Build/install the tower and independent HU transport models, atlas and block/item resources.

Recipes, translations and Java registration are maintained outside this script.
Requires Python 3 and Pillow; may be run from any working directory.
"""
from __future__ import annotations

import json
import shutil
from pathlib import Path

from PIL import Image

import build_fluidized_bed as art
import build_heat_transport as transport
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


def baked_model(boxes, particle):
    textures = {t: "ssite:block/machine/fluidized_bed/" + t for _, _, t in boxes}
    textures["particle"] = "ssite:block/machine/fluidized_bed/" + particle
    elements = [{"from": low, "to": high, "faces": {face: {"texture": "#" + texture}
                for face in ("down", "up", "north", "south", "west", "east")}}
                for low, high, texture in boxes]
    return {"parent": "minecraft:block/block", "textures": textures, "elements": elements}


def duct_core_boxes():
    boxes = [([3.52, 3.52, 3.52], [12.48, 12.48, 12.48], "insulation")]
    for axis in range(3):
        other = [i for i in range(3) if i != axis]
        for u in (3.2, 11.84):
            for v in (3.2, 11.84):
                low, high = [3.2]*3, [12.8]*3
                for dim, start in zip(other, (u, v)):
                    low[dim], high[dim] = start, start + .96
                boxes.append((low, high, "edge"))
    return boxes


def main():
    art.main()
    transport.main()
    obj = ASSETS / "obj" / "fluidized_bed_combustor.obj"
    atlas = ASSETS / "textures" / "machine" / "fluidized_bed_combustor.png"
    obj.parent.mkdir(parents=True, exist_ok=True)
    atlas.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(art.OUT / (art.STEM + ".obj"), obj)
    for name in ("heat_duct", "heating_plate"):
        shutil.copyfile(transport.OUT / (name + ".obj"), ASSETS / "obj" / (name + ".obj"))
    shutil.copyfile(art.OUT / (art.STEM + "_atlas.png"), atlas)
    write(atlas.with_suffix(".png.mcmeta"), {"texture": {"blur": False, "clamp": False}})
    TEXTURES.mkdir(parents=True, exist_ok=True)
    with Image.open(atlas) as image:
        for tile in sorted({tile for surfaces in PARTS.values() for tile in surfaces.values()} | {"edge", "ribbed"}):
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
    for name, tile in (("heat_duct", "insulation"), ("heating_plate", "ceramic")):
        model = "ssite:block/machine/parts/" + name
        write(ASSETS / "blockstates" / (name + ".json"), {"variants": {"": {"model": model}}})
        boxes = (duct_core_boxes() + [([4,4,0],[12,12,16],"insulation"),
                  ([3.2,3.2,.24],[12.8,12.8,1.52],"edge"), ([3.2,3.2,14.48],[12.8,12.8,15.76],"edge")]
                 if name == "heat_duct" else
                 [([0,0,0],[16,2,16],"dark"), ([.32,2,.32],[15.68,14,15.68],"refractory"),
                  ([0,3.52,0],[16,11.52,.56],"ribbed"), ([0,3.52,15.44],[16,11.52,16],"ribbed"),
                  ([0,3.52,0],[.56,11.52,16],"ribbed"), ([15.44,3.52,0],[16,11.52,16],"ribbed"),
                  ([0,12.16,0],[16,13.6,16],"edge"), ([0,13.92,0],[16,16,16],"ceramic")])
        # The native baked-model wrapper hides this only once a valid behavior BE is available.
        # Without a BE, a closed insulated core or complete plate remains visible.
        fallback = duct_core_boxes() if name == "heat_duct" else boxes
        write(ASSETS / "models" / "block" / "machine" / "parts" / (name + ".json"), baked_model(fallback, tile))
        write(ASSETS / "models" / "item" / (name + ".json"), baked_model(boxes, tile))
    assert obj.read_bytes() == (art.OUT / (art.STEM + ".obj")).read_bytes()
    assert atlas.read_bytes() == (art.OUT / (art.STEM + "_atlas.png")).read_bytes()
    print("Installed tower, independent six-way heat duct and heating plate; legacy part resources retained.")


if __name__ == "__main__":
    main()
