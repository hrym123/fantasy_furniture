"""Generate 32x28 pixel-art horizontal HUD frame for paint brush recolor preview."""
from __future__ import annotations

import json
from pathlib import Path

from PIL import Image

W = 32
H = 28
B = 4
OUT_DIR = Path(__file__).resolve().parents[1] / "src/main/resources/assets/fantasy_furniture/textures/gui"
OUT_PNG = OUT_DIR / "paint_brush_recolor_preview.png"
OUT_META = OUT_PNG.with_suffix(".png.mcmeta")

# palette sampled from paint_brush item texture
OUT = (34, 26, 24, 255)
H_S = (52, 40, 38, 255)
H_D = (68, 51, 51, 255)
H_M = (75, 59, 59, 255)
H_L = (80, 64, 64, 255)
CR_D = (249, 233, 182, 255)
CR_M = (254, 240, 195, 255)
CR_L = (255, 244, 208, 255)
MI_D = (150, 210, 148, 255)
MI_M = (176, 237, 175, 255)
MI_L = (202, 237, 175, 255)
FL_A = (28, 22, 20, 188)
FL_B = (36, 28, 26, 188)
FL_C = (32, 25, 23, 188)
IN_S = (22, 17, 15, 205)

TL = [
    [OUT, H_S, H_D, CR_D],
    [H_S, H_D, H_M, CR_M],
    [H_D, H_M, H_L, CR_L],
    [CR_D, CR_M, CR_L, CR_L],
]

TR = [
    [CR_M, MI_L, MI_M, OUT],
    [H_M, MI_M, MI_D, H_S],
    [H_D, H_M, H_L, CR_L],
    [CR_L, CR_M, CR_L, CR_L],
]

BL = [
    [CR_D, CR_M, CR_L, CR_L],
    [H_D, H_M, H_L, H_M],
    [H_S, H_D, H_M, H_D],
    [OUT, H_S, H_D, H_S],
]

BR = [
    [CR_L, CR_L, CR_M, CR_D],
    [H_M, H_D, H_M, H_D],
    [H_D, H_M, H_D, H_S],
    [H_S, H_D, H_S, OUT],
]


def corner_kind(x: int, y: int) -> str:
    left = x < B
    right = x >= W - B
    top = y < B
    bot = y >= H - B
    if top and left:
        return "TL"
    if top and right:
        return "TR"
    if bot and left:
        return "BL"
    if bot and right:
        return "BR"
    if top:
        return "T"
    if bot:
        return "B"
    if left:
        return "L"
    if right:
        return "R"
    return "C"


def handle_noise(x: int, y: int) -> tuple[int, int, int, int]:
    palette = [H_S, H_D, H_M, H_L]
    return palette[(x * 3 + y * 5) % 4]


def edge_top(x: int) -> tuple[int, int, int, int]:
    cycle = [OUT, H_S, H_D, CR_D, H_M, H_D, H_S, OUT]
    return cycle[x % len(cycle)]


def edge_bot(x: int) -> tuple[int, int, int, int]:
    cycle = [OUT, H_S, H_D, H_M, H_D, H_S, OUT, H_S]
    return cycle[x % len(cycle)]


def edge_left(y: int) -> tuple[int, int, int, int]:
    cycle = [OUT, H_S, H_D, CR_D, H_M, H_D, H_S, OUT]
    return cycle[y % len(cycle)]


def edge_right(y: int) -> tuple[int, int, int, int]:
    cycle = [OUT, H_S, H_D, H_M, H_D, H_S, OUT, H_S]
    return cycle[y % len(cycle)]


def center_fill(x: int, y: int) -> tuple[int, int, int, int]:
    # warm inset panel: fine dither + faint diagonal grain
    base = FL_A if (x + y) % 2 == 0 else FL_B
    if (x - y) % 5 == 0:
        base = FL_C
    # inner shadow along bottom-right
    dist_br = (W - B - 1 - x) + (H - B - 1 - y)
    if dist_br <= 1:
        return IN_S
    return base


def paint_top_edge(x: int, row: int) -> tuple[int, int, int, int]:
    if row == 0:
        return edge_top(x)
    if row == 1:
        return handle_noise(x, row)
    if row == 2:
        return H_M if x % 2 == 0 else H_D
    return CR_M if x % 3 != 2 else CR_L


def paint_bottom_edge(x: int, row: int) -> tuple[int, int, int, int]:
    if row == 3:
        return edge_bot(x)
    if row == 2:
        return H_D if x % 2 == 0 else H_M
    if row == 1:
        return handle_noise(x, H - 1 - row)
    return H_S if x % 2 == 0 else H_D


def paint_left_edge(y: int, col: int) -> tuple[int, int, int, int]:
    if col == 0:
        return edge_left(y)
    if col == 1:
        return handle_noise(col, y)
    if col == 2:
        return H_M if y % 2 == 0 else H_D
    return CR_M if y % 3 != 2 else CR_L


def paint_right_edge(y: int, col: int) -> tuple[int, int, int, int]:
    if col == 3:
        return edge_right(y)
    if col == 2:
        return H_D if y % 2 == 0 else H_M
    if col == 1:
        return handle_noise(W - 1 - col, y)
    return H_S if y % 2 == 0 else H_D


def apply_mint_accent(px, x: int, y: int) -> None:
    # small bristle-paint stroke in top-right, tiles safely when center stretches
    accents = {
        (28, 1): MI_L,
        (29, 1): MI_M,
        (30, 1): MI_L,
        (27, 2): MI_M,
        (28, 2): MI_L,
        (29, 2): MI_D,
    }
    color = accents.get((x, y))
    if color is not None:
        px[x, y] = color


def main() -> None:
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px = img.load()

    for y in range(H):
        for x in range(W):
            k = corner_kind(x, y)
            if k == "C":
                px[x, y] = center_fill(x, y)
            elif k == "TL":
                px[x, y] = TL[y][x]
            elif k == "TR":
                px[x, y] = TR[y][x - (W - B)]
            elif k == "BL":
                px[x, y] = BL[y - (H - B)][x]
            elif k == "BR":
                px[x, y] = BR[y - (H - B)][x - (W - B)]
            elif k == "T":
                px[x, y] = paint_top_edge(x, y)
            elif k == "B":
                px[x, y] = paint_bottom_edge(x, y - (H - B))
            elif k == "L":
                px[x, y] = paint_left_edge(y, x)
            elif k == "R":
                px[x, y] = paint_right_edge(y, x - (W - B))

    # crisp outer outline
    for x in range(W):
        px[x, 0] = OUT
        px[x, H - 1] = OUT
    for y in range(H):
        px[0, y] = OUT
        px[W - 1, y] = OUT

    for y in range(H):
        for x in range(W):
            if corner_kind(x, y) == "TR" or (corner_kind(x, y) == "T" and x >= W - B):
                apply_mint_accent(px, x, y)

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    img.save(OUT_PNG)
    OUT_META.write_text(json.dumps({"blur": False, "clamp": True}, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {OUT_PNG} ({img.size[0]}x{img.size[1]} {img.mode})")
    print(f"wrote {OUT_META}")


if __name__ == "__main__":
    main()
