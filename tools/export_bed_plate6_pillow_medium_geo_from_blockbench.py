#!/usr/bin/env python3
"""从 Blockbench 侧车 ``.geo.json`` 或同目录 bbmodel 导出结果写入模组内中号枕头三种布局 Geo。

MoonStarfish 源目录（与 ``export_bed_plate6_pillow_large_geo_from_blockbench.py`` 相同根）::

    d:\\warehouse\\MoonStarfish素材\\床板6-Geo

期望侧车文件名（若 Blockbench 导出名称略有不同，请改下表 ``SOURCES``）::

    solo:      床板6枕头（中 1个放置的样子）.geo.json
    pair_rear: 自「床板6枕头（中 2个放置的样子 后）」导出；或从 bbmodel 在 BB 中导出 geo 后指向该文件
    pair_front: 自「床板6枕头（中 三个堆叠+两个堆叠样子 前）」导出

输出（identifier / 根骨骼名已规范化）::

    bed_plate6_pillow_medium_solo.geo.json
    bed_plate6_pillow_medium_pair_rear.geo.json
    bed_plate6_pillow_medium_pair_front.geo.json

``format_version`` 与被套等资源对齐为 1.21.110。侧车多为 16×16 贴图坐标，游戏内 ``bed_plate6_pillow_medium_*.png``
为 64×64：将 ``description`` 固定为 64×64，并把各面 UV 按 ``64 / 侧车 texture_width`` 缩放。
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/fantasy_furniture/geo/block"
MOON = Path(r"d:\warehouse\MoonStarfish素材\床板6-Geo")

SOURCES: list[tuple[str, str]] = [
    ("solo", "床板6枕头（中 1个放置的样子）.geo.json"),
    ("pair_rear", "床板6枕头（中 2个放置的样子 后）.geo.json"),
    ("pair_front", "床板6枕头（中 三个堆叠+两个堆叠样子 前）.geo.json"),
]

BBMODEL_PAIR_FRONT = "床板6枕头（中 三个堆叠+两个堆叠样子 前）.bbmodel"

TEXTURE_GAME = 64
# pair_front 自 bbmodel：工程 resolution 常为 16，与 64 PNG 对齐
UV_FROM_16 = TEXTURE_GAME // 16  # 4


def collect_ordered_cubes_from_bbmodel(bb: dict) -> list[dict]:
    """按 outliner 顺序收集 ``type==cube`` 的 elements。"""
    el_by = {e["uuid"]: e for e in bb.get("elements", []) if e.get("type") == "cube"}
    out: list[dict] = []
    for root in bb.get("outliner", []):
        for cid in root.get("children", []):
            if cid in el_by:
                out.append(el_by[cid])
    return out


def bbmodel_element_to_cube(el: dict, uv_scale: float) -> dict:
    f, t = el["from"], el["to"]
    cube: dict = {
        "origin": [f[0], f[1], f[2]],
        "size": [t[0] - f[0], t[1] - f[1], t[2] - f[2]],
        "uv": {},
    }
    rot = el.get("rotation", [0, 0, 0])
    if any(rot):
        cube["rotation"] = list(rot)
        cube["pivot"] = list(el.get("origin", [(f[i] + t[i]) / 2 for i in range(3)]))
    for fname, face in el.get("faces", {}).items():
        q = face.get("uv")
        if not q or len(q) < 4:
            continue
        u1, v1, u2, v2 = float(q[0]), float(q[1]), float(q[2]), float(q[3])
        cube["uv"][fname] = {
            "uv": [min(u1, u2) * uv_scale, min(v1, v2) * uv_scale],
            "uv_size": [abs(u2 - u1) * uv_scale, abs(v2 - v1) * uv_scale],
        }
    return cube


def write_pair_front_from_bbmodel() -> None:
    slug = "pair_front"
    bb_path = MOON / BBMODEL_PAIR_FRONT
    if not bb_path.is_file():
        print("Skip bbmodel pair_front (missing):", bb_path)
        return
    bb = json.loads(bb_path.read_text(encoding="utf-8"))
    cubes = [bbmodel_element_to_cube(el, UV_FROM_16) for el in collect_ordered_cubes_from_bbmodel(bb)]
    bone_name = f"bed_plate6_pillow_medium_{slug}"
    geom = {
        "description": {
            "identifier": f"geometry.{bone_name}",
            "texture_width": TEXTURE_GAME,
            "texture_height": TEXTURE_GAME,
            "visible_bounds_width": 5,
            "visible_bounds_height": 2.5,
            "visible_bounds_offset": [0, 0.75, 0],
        },
        "bones": [{"name": bone_name, "pivot": [0, 8, 0], "cubes": cubes}],
    }
    out = {"format_version": "1.21.110", "minecraft:geometry": [geom]}
    dst = ASSETS / f"bed_plate6_pillow_medium_{slug}.geo.json"
    dst.write_text(json.dumps(out, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
    print("Wrote", dst, "(from bbmodel)")


def scale_uv_cube_by(cube: dict, mult: float) -> None:
    uv = cube.get("uv")
    if not isinstance(uv, dict):
        return
    for face in uv.values():
        if not isinstance(face, dict):
            continue
        if "uv" in face:
            u, v = face["uv"]
            face["uv"] = [u * mult, v * mult]
        if "uv_size" in face:
            du, dv = face["uv_size"]
            face["uv_size"] = [du * mult, dv * mult]


def main() -> None:
    pair_front_from_sidecar = False
    for slug, name in SOURCES:
        src = MOON / name
        if slug == "pair_front" and not src.is_file():
            continue
        if not src.is_file():
            print("Skip (missing):", src)
            continue
        if slug == "pair_front":
            pair_front_from_sidecar = True
        data = json.loads(src.read_text(encoding="utf-8"))
        geom = data["minecraft:geometry"][0]
        desc = geom["description"]
        bone_name = f"bed_plate6_pillow_medium_{slug}"
        desc["identifier"] = f"geometry.{bone_name}"
        orig_tw = max(1, int(desc["texture_width"]))
        orig_th = max(1, int(desc["texture_height"]))
        orig = min(orig_tw, orig_th)
        mult = float(TEXTURE_GAME) / float(orig)
        desc["texture_width"] = TEXTURE_GAME
        desc["texture_height"] = TEXTURE_GAME
        bone = geom["bones"][0]
        bone["name"] = bone_name
        for cube in bone.get("cubes", []):
            scale_uv_cube_by(cube, mult)
        out = {
            "format_version": "1.21.110",
            "minecraft:geometry": [geom],
        }
        dst = ASSETS / f"bed_plate6_pillow_medium_{slug}.geo.json"
        dst.write_text(json.dumps(out, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
        print("Wrote", dst)
    if not pair_front_from_sidecar:
        write_pair_front_from_bbmodel()


if __name__ == "__main__":
    main()
