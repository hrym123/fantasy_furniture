#!/usr/bin/env python3
"""仅从「床板6」bbmodel 解码 ``textures[0]`` 写入 ``bed_plate6.png``。

Geo / animation 请在 Blockbench 侧车导出 json 后，手动放入 ``assets/.../geo`` 与 ``animations``。
本仓库不再通过脚本生成这些 json。"""
from __future__ import annotations

import base64
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BBMODEL = Path(r"d:\warehouse\MoonStarfish素材\床板6\床板6.bbmodel")
ASSETS = ROOT / "src/main/resources/assets/fantasy_furniture"


def main() -> None:
    data = json.loads(BBMODEL.read_text(encoding="utf-8"))
    tex = data["textures"][0]
    b64 = tex["source"].split(",", 1)[1]
    png_path = ASSETS / "textures/block/bed_plate6.png"
    png_path.parent.mkdir(parents=True, exist_ok=True)
    png_path.write_bytes(base64.b64decode(b64))
    print("Wrote:", png_path)


if __name__ == "__main__":
    main()
