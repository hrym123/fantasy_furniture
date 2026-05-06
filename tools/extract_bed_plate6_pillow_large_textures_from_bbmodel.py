#!/usr/bin/env python3
"""从 MoonStarfish 床板 6 大号枕头 Blockbench 工程提取 PNG 到游戏资源目录。

条纹款含 7 张材质；纯色 / 格子各 6 张时，将第 7 色复用最后一张贴图（与 Java 侧
``BedPlate6PillowPalette`` 一致）。
"""
from __future__ import annotations

import base64
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DST = ROOT / "src/main/resources/assets/fantasy_furniture/textures/block"
MOON = Path(r"d:\warehouse\MoonStarfish素材\床板6-Geo")

# 与 BedPlate6PillowPalette 顺序一致（材质 id 1..7）
COLORS = ("cream", "rose", "butter", "mint", "denim", "lilac", "cocoa")

STRIPED_FILE = "床板6枕头（大号 条纹）.bbmodel"
PLAIN_FILE = "枕头6枕头（大号 纯色）.bbmodel"
PLAID_FILE = "枕头6枕头（大号 格子）.bbmodel"


def _decode_data_url(data: str) -> bytes:
    m = re.match(r"^data:image/png;base64,(.+)$", data.strip())
    if not m:
        raise ValueError("expected data URL png")
    return base64.b64decode(m.group(1))


def _textures_sorted(data: dict) -> list[dict]:
    tex = [t for t in data.get("textures", []) if isinstance(t, dict) and t.get("source")]
    def sort_key(t: dict):
        k = t.get("id")
        if isinstance(k, str) and k.isdigit():
            k = int(k)
        return (0, k) if isinstance(k, int) else (1, str(k))

    return sorted(tex, key=sort_key)


def extract_for_style(bb_name: str, style_slug: str, repeat_last_if_short: bool) -> None:
    src = MOON / bb_name
    data = json.loads(src.read_text(encoding="utf-8"))
    textures = _textures_sorted(data)
    if not textures:
        raise SystemExit(f"no textures in {src}")
    n = len(COLORS)
    if len(textures) < n and not repeat_last_if_short:
        raise SystemExit(f"{style_slug}: need {n} textures, got {len(textures)}")
    for i, color in enumerate(COLORS):
        t = textures[i] if i < len(textures) else textures[-1]
        png = _decode_data_url(t["source"])
        out = DST / f"bed_plate6_pillow_large_{style_slug}_{color}.png"
        out.write_bytes(png)
        print("Wrote", out.name, "<-", bb_name, "tex id", t.get("id"))


def main() -> None:
    DST.mkdir(parents=True, exist_ok=True)
    extract_for_style(STRIPED_FILE, "striped", repeat_last_if_short=False)
    extract_for_style(PLAIN_FILE, "plain", repeat_last_if_short=True)
    extract_for_style(PLAID_FILE, "plaid", repeat_last_if_short=True)


if __name__ == "__main__":
    main()
