"""Original pixel atlas for the fluidized-bed combustor prototype.

All marks are drawn here on a 32-pixel grid, then doubled with nearest-neighbor
sampling. No reference texture pixels or external fonts are used. Atlas rows
run top to bottom; OBJ exporters should flip V when mapping these tiles.
"""
from __future__ import annotations

import math
import random

from PIL import Image, ImageDraw

TILE = 64
WIDTH, HEIGHT = 512, 256
MATERIALS = [
    'shell', 'plate', 'dark', 'edge', 'ribbed', 'refractory', 'hazard', 'yellow',
    'pipe', 'soot', 'bed', 'grate', 'panel', 'nameplate', 'rust', 'concrete',
    'port_air', 'port_fuel', 'port_ash', 'port_flue', 'gauge', 'fan_grille', 'door', 'hot',
    'ceramic', 'bolt', 'red', 'green', 'black', 'insulation', 'seam', 'motor',
]

# A small original bitmap alphabet keeps equipment markings crisp at game scale.
GLYPHS = {
    'A': ('010', '101', '111', '101', '101'),
    'B': ('110', '101', '110', '101', '110'),
    'C': ('011', '100', '100', '100', '011'),
    'E': ('111', '100', '110', '100', '111'),
    'F': ('111', '100', '110', '100', '100'),
    'G': ('011', '100', '101', '101', '011'),
    'H': ('101', '101', '111', '101', '101'),
    'I': ('111', '010', '010', '010', '111'),
    'L': ('100', '100', '100', '100', '111'),
    'R': ('110', '101', '110', '101', '101'),
    'S': ('011', '100', '010', '001', '110'),
    'U': ('101', '101', '101', '101', '111'),
    '0': ('111', '101', '101', '101', '111'),
    '1': ('010', '110', '010', '010', '111'),
    '2': ('110', '001', '010', '100', '111'),
    '/': ('001', '001', '010', '100', '100'),
    '-': ('000', '000', '111', '000', '000'),
    ' ': ('000', '000', '000', '000', '000'),
}

COLORS = [
    (83, 79, 73), (102, 100, 94), (43, 42, 40), (139, 139, 132),
    (86, 84, 79), (117, 101, 83), (57, 54, 45), (172, 137, 52),
    (103, 103, 97), (37, 33, 30), (54, 40, 34), (46, 47, 43),
    (71, 74, 68), (80, 77, 69), (91, 62, 44), (95, 93, 84),
    (73, 89, 89), (100, 83, 54), (91, 84, 74), (92, 78, 71),
    (64, 67, 62), (56, 59, 54), (69, 67, 61), (84, 34, 16),
    (167, 156, 128), (104, 106, 99), (126, 43, 33), (65, 111, 68),
    (20, 22, 20), (122, 117, 104), (92, 90, 82), (73, 78, 72),
]


def _shade(color, amount):
    return tuple(max(0, min(255, c + amount)) for c in color)


def _text(draw, text, x, y, color=(207, 200, 175), scale=1):
    for character in text:
        for row, bits in enumerate(GLYPHS[character]):
            for column, bit in enumerate(bits):
                if bit == '1':
                    draw.rectangle((x + column * scale, y + row * scale,
                                    x + (column + 1) * scale - 1,
                                    y + (row + 1) * scale - 1), fill=color)
        x += 4 * scale


def _rivet(draw, x, y, light=(164, 162, 148)):
    draw.rectangle((x - 1, y - 1, x + 1, y + 1), fill=(35, 36, 32))
    draw.point((x - 1, y - 1), fill=light)
    draw.point((x, y), fill=(121, 121, 110))


def _frame(draw, color, bolts=True, inset=0):
    low, high = inset, 31 - inset
    draw.rectangle((low, low, high, high), outline=_shade(color, -33))
    draw.line((low + 1, high - 1, low + 1, low + 1, high - 1, low + 1),
              fill=_shade(color, 17))
    if bolts:
        for x in (low + 3, high - 3):
            for y in (low + 3, high - 3):
                _rivet(draw, x, y)


def _dial(draw, center=(16, 16), radius=11):
    cx, cy = center
    draw.ellipse((cx - radius, cy - radius, cx + radius, cy + radius), fill=(25, 28, 25))
    draw.ellipse((cx - radius + 1, cy - radius + 1, cx + radius - 1, cy + radius - 1),
                 fill=(148, 148, 132))
    inner = radius - 3
    draw.ellipse((cx - inner, cy - inner, cx + inner, cy + inner), fill=(193, 188, 156))
    for angle in (-220, -180, -140, -100, -60, -20, 20):
        radians = math.radians(angle)
        a = (round(cx + (inner - 2) * math.cos(radians)),
             round(cy + (inner - 2) * math.sin(radians)))
        b = (round(cx + inner * math.cos(radians)),
             round(cy + inner * math.sin(radians)))
        draw.line((a, b), fill=(58, 55, 45))
    draw.line((cx, cy, cx + inner - 2, cy - inner + 2), fill=(137, 48, 31))
    draw.point((cx, cy), fill=(44, 43, 34))


def _tile(name, color, rng):
    tile = Image.new('RGB', (32, 32))
    # Restrained two-pixel grain, with just enough broad shading to read as steel.
    for y in range(32):
        band = rng.choice((-1, 0, 0, 0, 1))
        for x in range(32):
            noise = rng.choice((-2, -1, 0, 0, 0, 1, 2))
            slope = round((15.5 - y) / 12) if name not in ('bed', 'hot', 'black') else 0
            tile.putpixel((x, y), _shade(color, noise + band + slope))
    d = ImageDraw.Draw(tile)

    if name in ('shell', 'plate'):
        _frame(d, color)
        if name == 'plate':
            # The weld lies at the boundary so broad faces retain a calm center.
            d.line((6, 30, 25, 30), fill=(54, 53, 48))
            for x in range(7, 26, 3):
                d.point((x, 29), fill=(125, 122, 111))
        else:
            for x, y in ((10, 9), (22, 24), (14, 26)):
                d.line((x, y, x + 2, y), fill=_shade(color, -8))
    elif name == 'dark':
        _frame(d, color, bolts=False)
        d.line((2, 29, 29, 29), fill=(30, 30, 28))
    elif name == 'edge':
        d.line((0, 0, 31, 0), fill=(183, 182, 170))
        d.line((0, 2, 31, 2), fill=(155, 154, 143))
        d.rectangle((0, 28, 31, 31), fill=(81, 82, 76))
        d.line((0, 27, 31, 27), fill=(119, 120, 111))
        for x in (4, 27):
            _rivet(d, x, 16, (196, 193, 174))
    elif name in ('ribbed', 'motor'):
        for x in range(2, 31, 4):
            d.rectangle((x, 2, x + 1, 29), fill=_shade(color, 25))
            d.line((x + 2, 2, x + 2, 29), fill=_shade(color, -27))
        for y in (0, 29):
            d.rectangle((0, y, 31, y + 2), fill=_shade(color, -9))
            d.line((0, y, 31, y), fill=_shade(color, 19))
        if name == 'motor':
            d.rectangle((9, 12, 22, 20), fill=(43, 47, 41), outline=(110, 113, 103))
            d.rectangle((12, 15, 19, 17), fill=(173, 165, 131))
    elif name in ('refractory', 'ceramic'):
        mortar = (65, 58, 48) if name == 'refractory' else (106, 101, 82)
        for y in range(0, 32, 8):
            d.line((0, y, 31, y), fill=mortar)
            d.line((0, y + 1, 31, y + 1), fill=_shade(color, 13))
            offset = 0 if y % 16 else 8
            for x in range(offset, 32, 16):
                d.line((x, y, x, y + 7), fill=mortar)
        if name == 'refractory':
            d.line((24, 10, 22, 13, 25, 15), fill=(78, 68, 55))
    elif name == 'hazard':
        d.rectangle((1, 6, 30, 25), fill=(171, 137, 47), outline=(34, 33, 28))
        for y in range(7, 25):
            for x in range(2, 30):
                if (x + y) % 12 < 6:
                    d.point((x, y), fill=(36, 37, 32))
        d.line((2, 7, 29, 7), fill=(197, 163, 66))
        for x in (3, 28):
            _rivet(d, x, 3)
            _rivet(d, x, 28)
    elif name == 'yellow':
        d.line((0, 1, 31, 1), fill=(199, 166, 76))
        d.rectangle((0, 28, 31, 31), fill=(113, 91, 39))
        for x, y in ((4, 27), (24, 3), (12, 29)):
            d.rectangle((x, y, x + 2, y), fill=(80, 75, 56))
    elif name == 'pipe':
        for x in range(32):
            value = round(21 * math.cos((x - 10) / 32 * math.pi))
            d.line((x, 0, x, 31), fill=_shade(color, value))
        d.line((29, 0, 29, 31), fill=(65, 68, 61))
        d.line((30, 0, 30, 31), fill=(117, 117, 106))
    elif name == 'soot':
        for _ in range(100):
            x, y = rng.randrange(32), rng.randrange(32)
            d.rectangle((x, y, x + 1, y + 1), fill=rng.choice(((27, 26, 24), (43, 36, 29), (48, 41, 32))))
    elif name == 'bed':
        for _ in range(125):
            x, y = rng.randrange(32), rng.randrange(32)
            slag = rng.choice(((33, 32, 29), (74, 55, 38), (80, 43, 28), (91, 53, 29), (54, 52, 43)))
            d.rectangle((x, y, x + rng.randrange(1, 3), y + 1), fill=slag)
        for _ in range(12):
            x, y = rng.randrange(32), rng.randrange(32)
            d.point((x, y), fill=rng.choice(((161, 66, 23), (198, 91, 29), (132, 48, 20))))
    elif name == 'grate':
        d.rectangle((0, 0, 31, 31), fill=(27, 30, 26))
        for y in range(2, 32, 5):
            d.line((0, y, 31, y), fill=(103, 106, 95))
            d.line((0, y + 1, 31, y + 1), fill=(64, 69, 59))
        for x in range(2, 32, 7):
            d.line((x, 0, x, 31), fill=(78, 83, 73))
    elif name == 'panel':
        _frame(d, color)
        _dial(d, (11, 11), 6)
        d.rectangle((20, 6, 26, 17), fill=(23, 30, 24), outline=(108, 115, 99))
        d.rectangle((22, 10, 24, 15), fill=(106, 133, 70))
        for x, lamp in ((8, (127, 48, 34)), (16, (74, 123, 71))):
            d.rectangle((x - 2, 23, x + 2, 27), fill=(31, 34, 29))
            d.rectangle((x - 1, 24, x + 1, 26), fill=lamp)
        d.line((23, 23, 23, 27), fill=(163, 161, 142))
        d.line((22, 23, 24, 23), fill=(198, 192, 168))
    elif name == 'nameplate':
        d.rectangle((1, 5, 30, 26), fill=(29, 31, 27))
        d.rectangle((2, 6, 29, 25), fill=(177, 169, 138), outline=(219, 211, 176))
        _text(d, 'FBC', 5, 9, (45, 47, 38), scale=2)
        _text(d, '/01', 10, 20, (65, 65, 51))
        _rivet(d, 3, 7)
        _rivet(d, 28, 24)
    elif name == 'rust':
        for _ in range(80):
            x, y = rng.randrange(32), rng.randrange(32)
            d.rectangle((x, y, x + rng.randrange(1, 4), y + rng.randrange(1, 3)),
                        fill=rng.choice(((77, 59, 44), (110, 72, 43), (119, 81, 50), (61, 55, 44))))
    elif name == 'concrete':
        for _ in range(80):
            x, y = rng.randrange(32), rng.randrange(32)
            d.point((x, y), fill=rng.choice(((78, 78, 70), (116, 113, 101))))
        d.line((0, 31, 31, 31), fill=(61, 63, 56))
        d.line((0, 1, 31, 1), fill=(122, 119, 105))
    elif name.startswith('port_'):
        _frame(d, color)
        label = {'port_air': 'AIR', 'port_fuel': 'FUEL', 'port_ash': 'ASH', 'port_flue': 'GAS'}[name]
        d.rectangle((4, 5, 27, 15), fill=(38, 41, 35), outline=_shade(color, 26))
        _text(d, label, (32 - (len(label) * 4 - 1)) // 2, 8)
        incoming = name in ('port_air', 'port_fuel')
        if incoming:
            d.polygon(((11, 19), (20, 19), (20, 23), (24, 23), (16, 28), (8, 23), (11, 23)), fill=(172, 165, 130))
        else:
            d.polygon(((11, 27), (20, 27), (20, 23), (24, 23), (16, 18), (8, 23), (11, 23)), fill=(172, 165, 130))
    elif name == 'gauge':
        _frame(d, color)
        _dial(d)
    elif name == 'fan_grille':
        _frame(d, color)
        d.ellipse((3, 3, 28, 28), fill=(23, 28, 24), outline=(123, 129, 115))
        for angle in range(0, 360, 45):
            rad = math.radians(angle)
            d.line((16, 16, round(16 + 12 * math.cos(rad)), round(16 + 12 * math.sin(rad))),
                   fill=(83, 91, 78))
        for radius in (5, 9):
            d.ellipse((16 - radius, 16 - radius, 16 + radius, 16 + radius), outline=(114, 121, 105))
        d.ellipse((14, 14, 18, 18), fill=(119, 126, 110), outline=(39, 45, 36))
    elif name == 'door':
        _frame(d, color)
        d.rectangle((5, 5, 26, 26), outline=(25, 29, 23), width=2)
        d.line((7, 24, 7, 7, 24, 7), fill=(109, 111, 97))
        for y in (9, 21):
            d.rectangle((2, y - 1, 7, y + 1), fill=(115, 118, 104), outline=(43, 47, 38))
        d.rectangle((22, 13, 24, 20), fill=(22, 28, 21))
        d.line((23, 13, 25, 13, 25, 18), fill=(157, 157, 133))
    elif name == 'hot':
        # A confined observation-glass glow, with a darker perimeter and clinker.
        for y in range(32):
            for x in range(32):
                distance = math.hypot((x - 15.5) / 17, (y - 19) / 20)
                heat = max(0, 1 - distance)
                noise = rng.randrange(-7, 8)
                d.point((x, y), fill=(min(255, round(88 + heat * 165 + noise)),
                                     max(0, min(255, round(28 + heat * 128 + noise))),
                                     max(0, round(10 + heat * 22))))
        for _ in range(25):
            x, y = rng.randrange(32), rng.randrange(20, 32)
            d.rectangle((x, y, x + 2, y + 1), fill=(81, 41, 20))
        d.rectangle((0, 0, 31, 31), outline=(42, 32, 23), width=2)
    elif name == 'bolt':
        d.rectangle((1, 1, 30, 30), fill=(58, 63, 53), outline=(151, 155, 136))
        d.polygon(((10, 5), (22, 5), (28, 16), (22, 27), (10, 27), (4, 16)),
                  fill=(131, 137, 119), outline=(33, 40, 31))
        d.line((10, 6, 21, 6, 26, 15), fill=(188, 189, 165), width=2)
        d.line((10, 18, 21, 18), fill=(55, 62, 48), width=2)
    elif name in ('red', 'green'):
        _frame(d, color, bolts=False)
        d.line((4, 4, 27, 4), fill=_shade(color, 36))
        d.rectangle((3, 26, 28, 29), fill=_shade(color, -36))
    elif name == 'black':
        d.line((0, 31, 31, 31), fill=(12, 15, 12))
    elif name == 'insulation':
        for y in range(0, 32, 8):
            d.line((0, y, 31, y), fill=(84, 82, 71))
            d.line((0, y + 1, 31, y + 1), fill=(148, 141, 123))
            for x in range(4, 32, 8):
                d.point((x, y + 4), fill=(156, 149, 132))
        for x in (1, 29):
            d.line((x, 0, x, 31), fill=(80, 81, 70))
            d.line((x + 1, 0, x + 1, 31), fill=(174, 171, 148))
    elif name == 'seam':
        d.rectangle((13, 0, 18, 31), fill=(51, 54, 44))
        for y in range(0, 32, 3):
            d.line((13, y + 2, 16, y, 18, y + 1), fill=(141, 142, 121))
            d.point((17, y + 2), fill=(104, 107, 88))

    return tile.resize((TILE, TILE), Image.Resampling.NEAREST)


def build_atlas() -> Image.Image:
    """Return the deterministic 8-column, 4-row RGB atlas (512 x 256)."""
    atlas = Image.new('RGB', (WIDTH, HEIGHT))
    for index, (name, color) in enumerate(zip(MATERIALS, COLORS)):
        tile = _tile(name, color, random.Random(271828 + index * 173))
        atlas.paste(tile, (index % 8 * TILE, index // 8 * TILE))
    return atlas
