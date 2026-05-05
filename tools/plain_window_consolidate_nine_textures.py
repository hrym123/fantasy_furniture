#!/usr/bin/env python3
"""
普通窗户贴图只保留 9 张：``plain_window_1.png`` … ``plain_window_9.png``。

- 六个几何母版中每个纹理槽映射到上述之一（与槽位键稳定对应，避免 53 张散图）；
- 若缺少 ``plain_window_{i}.png``，则从 ``plain_window_cocoa_diagonal_{i}.png`` 复制；
- 删除 ``textures/block`` 下其余 ``plain_window*.png``。

在 fantasy_furniture 根目录执行：python tools/plain_window_consolidate_nine_textures.py
"""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "src/main/resources/assets/fantasy_furniture/models/block"
TEX = ROOT / "src/main/resources/assets/fantasy_furniture/textures/block"
MODID = "fantasy_furniture"
P = f"{MODID}:block"

STEMS = (
    "plain_window",
    "plain_window_y180",
    "plain_window_y22_5",
    "plain_window_y45",
    "plain_window_y67_5",
    "plain_window_diagonal",
)


def layer_index_for_key(key: str) -> int:
    if key == "particle":
        return 2
    if key.isdigit():
        k = int(key)
        if 1 <= k <= 9:
            return k
        return (k % 9) + 1
    return 1


def patch_geometry_textures() -> None:
    for stem in STEMS:
        path = MODELS / f"{stem}.json"
        data = json.loads(path.read_text(encoding="utf-8"))
        tex = data.get("textures")
        if not isinstance(tex, dict):
            continue
        data["textures"] = {k: f"{P}/plain_window_{layer_index_for_key(k)}" for k in tex}
        path.write_text(json.dumps(data, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
        print("OK model", stem)


def ensure_nine_png() -> None:
    for i in range(1, 10):
        dst = TEX / f"plain_window_{i}.png"
        if dst.is_file():
            continue
        src = TEX / f"plain_window_cocoa_diagonal_{i}.png"
        if not src.is_file():
            raise SystemExit(f"Missing source for plain_window_{i}.png (need {src.name} or existing plain_window_{i}.png)")
        shutil.copy2(src, dst)
        print("COPY", src.name, "->", dst.name)


def delete_extra_png() -> int:
    pat = re.compile(r"^plain_window_([1-9])\.png$")
    n = 0
    for p in TEX.glob("plain_window*.png"):
        if pat.match(p.name):
            continue
        p.unlink()
        n += 1
    return n


def main() -> None:
    ensure_nine_png()
    patch_geometry_textures()
    removed = delete_extra_png()
    print(f"Removed {removed} extra plain_window*.png; kept plain_window_1..9 only.")


if __name__ == "__main__":
    main()
