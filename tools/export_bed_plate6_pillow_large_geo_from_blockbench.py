#!/usr/bin/env python3
"""从 Blockbench 导出的侧车 ``.geo.json`` 写入 ``bed_plate6_pillow_large_{striped|plain|plaid}.geo.json``。

大号枕头在 BB 中为 GeckoLib 工程；源文件（与 bbmodel 同目录）：
  - 条纹：``床板6-Geo/床板6枕头（大号 条纹）.geo.json`` → ``striped``
  - 纯色：``床板6-Geo/枕头6枕头（大号 纯色）.geo.json`` → ``plain``
  - 格子：``床板6-Geo/枕头6枕头（大号 格子）.geo.json`` → ``plaid``

游戏内贴图为 64×64，侧车为 32×32 时对 UV / uv_size 做 ×2。骨骼名与 identifier 使用
``bed_plate6_pillow_large_{slug}``；``format_version`` 与被套等资源对齐为 1.21.110。
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/fantasy_furniture/geo/block"
MOON = Path(r"d:\warehouse\MoonStarfish素材\床板6-Geo")

SOURCES: list[tuple[str, str]] = [
    ("striped", "床板6枕头（大号 条纹）.geo.json"),
    ("plain", "枕头6枕头（大号 纯色）.geo.json"),
    ("plaid", "枕头6枕头（大号 格子）.geo.json"),
]

UV_SCALE = 2  # 侧车 32 → 游戏 64


def scale_uv_cube(cube: dict) -> None:
    uv = cube.get("uv")
    if not isinstance(uv, dict):
        return
    for face in uv.values():
        if not isinstance(face, dict):
            continue
        if "uv" in face:
            u, v = face["uv"]
            face["uv"] = [u * UV_SCALE, v * UV_SCALE]
        if "uv_size" in face:
            du, dv = face["uv_size"]
            face["uv_size"] = [du * UV_SCALE, dv * UV_SCALE]


def main() -> None:
    for slug, name in SOURCES:
        src = MOON / name
        data = json.loads(src.read_text(encoding="utf-8"))
        geom = data["minecraft:geometry"][0]
        desc = geom["description"]
        desc["identifier"] = f"geometry.bed_plate6_pillow_large_{slug}"
        desc["texture_width"] = int(desc["texture_width"]) * UV_SCALE
        desc["texture_height"] = int(desc["texture_height"]) * UV_SCALE
        bone = geom["bones"][0]
        bone["name"] = f"bed_plate6_pillow_large_{slug}"
        for cube in bone.get("cubes", []):
            scale_uv_cube(cube)
        out = {
            "format_version": "1.21.110",
            "minecraft:geometry": [geom],
        }
        dst = ASSETS / f"bed_plate6_pillow_large_{slug}.geo.json"
        dst.write_text(json.dumps(out, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
        print("Wrote", dst)


if __name__ == "__main__":
    main()
