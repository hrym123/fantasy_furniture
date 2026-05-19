# -*- coding: utf-8 -*-
"""按 lanye-create-model 规范：为 spruce_table 排布 UV 岛并生成贴图。"""
from __future__ import annotations

import json
import shutil
from pathlib import Path

from PIL import Image

FF = Path(__file__).resolve().parents[2]
TOOLS = Path(__file__).resolve().parent
BBMODEL = FF / "models/source/spruce_table.bbmodel"
VANILLA = TOOLS / "spruce_planks_vanilla_16.png"
OUT_TEX = TOOLS / "spruce_table_texture_64.png"
ASSETS_TEX = FF / "src/main/resources/assets/fantasy_furniture/textures/block/spruce_table.png"

TEX = 64
PLANK = 16

# --- UV 岛（Blockbench: [u1, v1, u2, v2]）---
ISLAND = {
    "top_up": (0, 0, 14, 14),
    "top_side": (0, 16, 14, 18),
    "edge_up": (16, 0, 32, 1),
    "edge_side": (16, 2, 32, 3),
    "apron_h": (0, 20, 12, 22),
    "apron_v": (32, 16, 34, 24),
    "leg_side": (40, 0, 42, 11),
    "leg_cap": (44, 0, 46, 2),
    "leg_inner": (48, 0, 50, 11),
}


def uv_rect(u1: float, v1: float, u2: float, v2: float) -> list[float]:
    return [u1, v1, u2, v2]


def face(texture: int = 0, uv: list[float] | None = None) -> dict:
    f: dict = {"texture": texture}
    if uv is not None:
        f["uv"] = uv
    return f


def assign_top(faces: dict) -> None:
    faces["up"] = face(0, uv_rect(*ISLAND["top_up"]))
    side = uv_rect(*ISLAND["top_side"])
    for k in ("north", "south", "east", "west"):
        faces[k] = face(0, side)
    faces["down"] = face(0, uv_rect(*ISLAND["leg_cap"]))


def assign_top_edge(faces: dict) -> None:
    faces["up"] = face(0, uv_rect(*ISLAND["edge_up"]))
    side = uv_rect(*ISLAND["edge_side"])
    for k in ("north", "south", "east", "west"):
        faces[k] = face(0, side)
    faces["down"] = face(0, uv_rect(*ISLAND["apron_h"]))


def assign_apron_ns(faces: dict) -> None:
    h = uv_rect(*ISLAND["apron_h"])
    v = uv_rect(*ISLAND["apron_v"])
    faces["north"] = face(0, h)
    faces["south"] = face(0, h)
    faces["east"] = face(0, v)
    faces["west"] = face(0, v)
    faces["up"] = face(0, uv_rect(*ISLAND["leg_cap"]))
    faces["down"] = face(0, uv_rect(*ISLAND["leg_cap"]))


def assign_apron_ew(faces: dict) -> None:
    v = uv_rect(*ISLAND["apron_v"])
    h = uv_rect(*ISLAND["apron_h"])
    faces["east"] = face(0, v)
    faces["west"] = face(0, v)
    faces["north"] = face(0, h)
    faces["south"] = face(0, h)
    faces["up"] = face(0, uv_rect(*ISLAND["leg_cap"]))
    faces["down"] = face(0, uv_rect(*ISLAND["leg_cap"]))


def assign_leg(faces: dict) -> None:
    side = uv_rect(*ISLAND["leg_side"])
    cap = uv_rect(*ISLAND["leg_cap"])
    inner = uv_rect(*ISLAND["leg_inner"])
    faces["north"] = face(0, side)
    faces["south"] = face(0, side)
    faces["east"] = face(0, inner)
    faces["west"] = face(0, inner)
    faces["up"] = face(0, cap)
    faces["down"] = face(0, cap)


ASSIGNERS = {
    "top": assign_top,
    "top_edge": assign_top_edge,
    "apron_n": assign_apron_ns,
    "apron_s": assign_apron_ns,
    "apron_w": assign_apron_ew,
    "apron_e": assign_apron_ew,
    "leg_fl": assign_leg,
    "leg_fr": assign_leg,
    "leg_bl": assign_leg,
    "leg_br": assign_leg,
}


def darken(img: Image.Image, factor: float = 0.88) -> Image.Image:
    px = img.load()
    out = img.copy()
    opx = out.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            opx[x, y] = (int(r * factor), int(g * factor), int(b * factor), a)
    return out


def build_atlas(planks: Image.Image) -> Image.Image:
    atlas = Image.new("RGBA", (TEX, TEX), (0, 0, 0, 255))
    p = planks.convert("RGBA")

    def blit_rect(island_key: str) -> None:
        u1, v1, u2, v2 = ISLAND[island_key]
        w, h = int(u2 - u1), int(v2 - v1)
        if w <= 0 or h <= 0:
            return
        if island_key in ("leg_side", "leg_inner", "apron_v"):
            patch = p.resize((p.width, h), Image.Resampling.NEAREST).transpose(Image.Transpose.ROTATE_90)
            patch = patch.resize((w, h), Image.Resampling.NEAREST)
        elif island_key in ("top_side", "apron_h", "edge_up", "edge_side"):
            patch = p.resize((w, PLANK), Image.Resampling.NEAREST).crop((0, 0, w, h))
        elif island_key == "top_up":
            patch = p.resize((w, h), Image.Resampling.NEAREST)
        else:
            patch = p.resize((w, h), Image.Resampling.NEAREST)
        if island_key in ("leg_cap",):
            patch = darken(patch, 0.85)
        atlas.paste(patch, (int(u1), int(v1)))

    for key in ISLAND:
        blit_rect(key)

    # 统一 1px 深边（桌面板外圈）
    px = atlas.load()
    edge = darken(p, 0.82)
    u1, v1, u2, v2 = ISLAND["top_up"]
    for x in range(int(u1), int(u2)):
        px[x, int(v1)] = edge.getpixel((x % PLANK, 0))
        px[x, int(v2) - 1] = edge.getpixel((x % PLANK, 0))
    for y in range(int(v1), int(v2)):
        px[int(u1), y] = edge.getpixel((0, y % PLANK))
        px[int(u2) - 1, y] = edge.getpixel((0, y % PLANK))

    return atlas


def update_bbmodel(data: dict) -> None:
    for el in data["elements"]:
        name = el.get("name", "")
        fn = ASSIGNERS.get(name)
        if not fn:
            continue
        faces = {}
        fn(faces)
        el["faces"] = faces

    if data.get("textures"):
        data["textures"][0]["name"] = "spruce_table.png"
        data["textures"] = [data["textures"][0]]


def main() -> None:
    if not VANILLA.is_file():
        from gen_spruce_table_texture import fetch_vanilla_planks

        fetch_vanilla_planks(VANILLA)

    planks = Image.open(VANILLA)
    atlas = build_atlas(planks)
    OUT_TEX.parent.mkdir(parents=True, exist_ok=True)
    atlas.save(OUT_TEX)
    ASSETS_TEX.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy(OUT_TEX, ASSETS_TEX)

    data = json.loads(BBMODEL.read_text(encoding="utf-8"))
    update_bbmodel(data)
    BBMODEL.write_text(json.dumps(data, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")

    print(f"texture: {OUT_TEX}")
    print(f"bbmodel: {BBMODEL}")
    print(f"islands: {len(ISLAND)}")


if __name__ == "__main__":
    main()
