#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""床板 6 枕头系列：按 ``textures/block`` 内 PNG 主色（与 ``plain_glass_window_texture_naming.dominant_rgb`` 相同）对照译名。

与「被单」一致：中文为 ``床板 6 …（××色）`` 风格；英文保持 ``Bed Plate 6 … (Color)``。

用法：
  python tools/bed_plate6_pillow_lang_display_colors.py
  # 仅打印 RGB 与当前脚本内嵌表，不写文件。改译名时：先跑脚本对照贴图，再改本文件 *_DISPLAY_ZH / *_DISPLAY_EN 与 zh_cn.json / en_us.json。
"""
from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TOOLS = ROOT / "tools"
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

from plain_glass_window_texture_naming import dominant_rgb  # noqa: E402

BLOCK_TEX = ROOT / "src/main/resources/assets/fantasy_furniture/textures/block"

# 键 = 资源 stem（不含 .png）；与 lang 键 item.fantasy_furniture.<stem> 对应
MEDIUM_STEMS = tuple(f"bed_plate6_pillow_medium_{i}" for i in range(1, 7))
SMALL_STEMS = tuple(f"bed_plate6_pillow_small_{i}" for i in range(1, 7))

LARGE_STEMS: tuple[str, ...] = tuple(
    f"bed_plate6_pillow_large_{style}_{color}"
    for style in ("striped", "plain", "plaid")
    for color in ("cream", "rose", "butter", "mint", "denim", "lilac", "cocoa")
    if not (style == "striped" and color == "cream")
    and not (style in ("plain", "plaid") and color == "cocoa")
)


def _sample(stem: str) -> tuple[int, int, int] | str:
    p = BLOCK_TEX / f"{stem}.png"
    if not p.is_file():
        return "MISSING"
    return dominant_rgb(p)


def main() -> int:
    print("# bed_plate6 pillow textures — dominant_rgb (RGBA weighted)")
    for group, stems in (
        ("medium", MEDIUM_STEMS),
        ("small", SMALL_STEMS),
        ("large", LARGE_STEMS),
    ):
        print(f"\n## {group}")
        for s in stems:
            rgb = _sample(s)
            print(f"  {s}: {rgb}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
