#!/usr/bin/env python3
"""一次性：从 Blockbench java_block bbmodel 导出 bed_plate6 的 geo / png / animation.json。

新增其它床板款式时：复制资源 basename（如 bed_plate3）、在 FurnitureAnimatedBlocks 中增加
BedPlateAnimatedSpecs.spec(modid, id, 属性供应, 实体工厂)（reverie_core）与薄 BlockEntity 子类，
并在 ClientModEvents 中注册 BedPlateGeoBlockRenderer(modid, basename)。
"""
from __future__ import annotations

import base64
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BBMODEL = Path(r"d:\warehouse\MoonStarfish素材\床板6\床板6.bbmodel")
ASSETS = ROOT / "src/main/resources/assets/fantasy_furniture"


def face_uv(uv4: list[float]) -> dict:
    u1, v1, u2, v2 = uv4
    u = min(u1, u2)
    v = min(v1, v2)
    du = abs(u2 - u1)
    dv = abs(v2 - v1)

    def num(x: float) -> float | int:
        r = round(x, 4)
        return int(r) if r == int(r) else r

    return {"uv": [num(u), num(v)], "uv_size": [num(du), num(dv)]}


def main() -> None:
    data = json.loads(BBMODEL.read_text(encoding="utf-8"))
    tex = data["textures"][0]
    tw, th = int(tex["width"]), int(tex["height"])
    b64 = tex["source"].split(",", 1)[1]
    png_path = ASSETS / "textures/block/bed_plate6.png"
    png_path.parent.mkdir(parents=True, exist_ok=True)
    png_path.write_bytes(base64.b64decode(b64))

    cubes: list[dict] = []
    for el in data["elements"]:
        if el.get("type") != "cube":
            continue
        frm, to = el["from"], el["to"]
        ox, oy, oz = frm[0] - 8.0, frm[1], frm[2] - 8.0
        sx, sy, sz = to[0] - frm[0], to[1] - frm[1], to[2] - frm[2]
        uv_obj: dict[str, dict] = {}
        for fk in ("north", "south", "east", "west", "up", "down"):
            face = el.get("faces", {}).get(fk)
            if face and "uv" in face:
                uv_obj[fk] = face_uv(face["uv"])
            else:
                uv_obj[fk] = {"uv": [0, 0], "uv_size": [1, 1]}
        cubes.append(
            {
                "origin": [round(ox, 4), round(oy, 4), round(oz, 4)],
                "size": [round(sx, 4), round(sy, 4), round(sz, 4)],
                "uv": uv_obj,
            }
        )

    geo = {
        "format_version": "1.21.110",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": "geometry.bed_plate6",
                    "texture_width": tw,
                    "texture_height": th,
                    "visible_bounds_width": 4,
                    "visible_bounds_height": 2.5,
                    "visible_bounds_offset": [0, 0.75, 0],
                },
                "bones": [
                    {"name": "bed_plate6", "pivot": [0, 8, 0]},
                    {
                        "name": "main",
                        "parent": "bed_plate6",
                        "pivot": [0, 8, 0],
                        "cubes": cubes,
                    },
                ],
            }
        ],
    }

    geo_path = ASSETS / "geo/block/bed_plate6.geo.json"
    geo_path.parent.mkdir(parents=True, exist_ok=True)
    geo_path.write_text(json.dumps(geo, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")

    anim = {
        "format_version": "1.8.0",
        "animations": {
            "animation.bed_plate6.idle": {
                "loop": True,
                "animation_length": 1,
                "bones": {},
            }
        },
    }
    anim_path = ASSETS / "animations/block/bed_plate6.animation.json"
    anim_path.parent.mkdir(parents=True, exist_ok=True)
    anim_path.write_text(json.dumps(anim, indent="\t") + "\n", encoding="utf-8")

    print("Wrote:", png_path, geo_path, anim_path)


if __name__ == "__main__":
    main()
