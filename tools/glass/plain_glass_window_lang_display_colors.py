#!/usr/bin/env python3
"""
普通玻璃窗 9 个物品的语言条目：中文与床品枕头一致——单括号、纯色名，如「（白色）」「（奶油色）」，
不用「近白色」「白·××」等。英文与 bed_plate6 枕头词条风格一致（Cream / Rose / Butter / Mint / Lilac 等）。

仍可选读主纹理 PNG 的 dominant_rgb，仅在终端打印供对照；写回语言文件时使用下方固定表。

用法：
  python tools/glass/plain_glass_window_lang_display_colors.py           # 打印表（含 RGB）
  python tools/glass/plain_glass_window_lang_display_colors.py --write   # 写回 zh_cn.json / en_us.json

若某 stem 的 PNG 不存在，使用脚本内 FALLBACK_DOMINANT_RGB（仅影响打印列，不影响译名）。
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

_TOOLS_ROOT = Path(__file__).resolve().parent.parent
if str(_TOOLS_ROOT) not in sys.path:
    sys.path.insert(0, str(_TOOLS_ROOT))
from paths import FF_ROOT as REPO_ROOT  # noqa: E402

from plain_glass_window_texture_naming import (
    PLAIN_GLASS_WINDOW_TEXTURE_STEMS,
    dominant_rgb,
)

TEXTURES_BLOCK = REPO_ROOT / "src/main/resources/assets/fantasy_furniture/textures/block"
LANG_ZH = REPO_ROOT / "src/main/resources/assets/fantasy_furniture/lang/zh_cn.json"
LANG_EN = REPO_ROOT / "src/main/resources/assets/fantasy_furniture/lang/en_us.json"

# 物品注册名后缀顺序与 ModItems / 语言键一致
VARIANT_SUFFIXES: tuple[str, ...] = (
    "white",
    "black",
    "tan",
    "ice_blue",
    "cream",
    "pale_green",
    "cream_b",
    "mixed",
    "cream_c",
)

# 与 bed_plate6 枕头（奶油色/蔷薇粉/黄油黄/薄荷绿/丁香紫）同一套叫法；棕/冰蓝为窗框常用纯色名
VARIANT_DISPLAY_ZH: dict[str, str] = {
    "white": "白色",
    "black": "黑色",
    "tan": "棕褐色",
    "ice_blue": "冰蓝色",
    "cream": "奶油色",
    "pale_green": "薄荷绿",
    "cream_b": "蔷薇粉",
    "mixed": "黄油黄",
    "cream_c": "丁香紫",
}
VARIANT_DISPLAY_EN: dict[str, str] = {
    "white": "White",
    "black": "Black",
    "tan": "Tan",
    "ice_blue": "Ice Blue",
    "cream": "Cream",
    "pale_green": "Mint",
    "cream_b": "Rose",
    "mixed": "Butter",
    "cream_c": "Lilac",
}

# PNG 缺失时仅用于打印列
FALLBACK_DOMINANT_RGB: dict[str, tuple[int, int, int]] = {
    "plain_glass_window_0_white": (248, 248, 252),
    "plain_glass_window_1_black": (28, 28, 32),
    "plain_glass_window_2_tan": (188, 148, 112),
    "plain_glass_window_3_ice_blue": (198, 228, 248),
    "plain_glass_window_4_cream": (242, 232, 210),
    "plain_glass_window_5_pale_green": (206, 222, 196),
    "plain_glass_window_6_cream": (236, 226, 200),
    "plain_glass_window_7_mixed": (168, 158, 148),
    "plain_glass_window_8_cream": (248, 238, 218),
}


def rgb_for_stem(stem: str) -> tuple[int, int, int, str]:
    path = TEXTURES_BLOCK / f"{stem}.png"
    if path.is_file():
        return (*dominant_rgb(path), "png")
    fb = FALLBACK_DOMINANT_RGB.get(stem)
    if fb:
        return (*fb, "fallback")
    return (128, 128, 128, "missing")


def format_name_zh(suffix: str) -> str:
    return f"普通玻璃窗（{VARIANT_DISPLAY_ZH[suffix]}）"


def format_name_en(suffix: str) -> str:
    return f"Plain Glass Window ({VARIANT_DISPLAY_EN[suffix]})"


def run(write: bool) -> int:
    rows: list[tuple[str, str, int, int, int, str, str, str]] = []
    lang_keys_zh: dict[str, str] = {}
    lang_keys_en: dict[str, str] = {}

    for stem, suffix in zip(PLAIN_GLASS_WINDOW_TEXTURE_STEMS, VARIANT_SUFFIXES):
        r, g, b, src = rgb_for_stem(stem)
        zh = format_name_zh(suffix)
        en = format_name_en(suffix)
        key = f"item.fantasy_furniture.plain_glass_window_{suffix}"
        lang_keys_zh[key] = zh
        lang_keys_en[key] = en
        rows.append((stem, suffix, r, g, b, src, zh, en))

    print(f"{'stem':<34} {'id':<12} {'RGB':<14} {'src':<8} zh")
    print("-" * 120)
    for stem, suffix, r, g, b, src, zh, en in rows:
        print(f"{stem:<34} {suffix:<12} ({r:3},{g:3},{b:3}) {src:<8} {zh}")
        print(f"{'':34} {'':12} {'':14} {'':8} {en}")

    missing = [r for r in rows if r[5] == "missing"]
    if missing:
        print("\n错误：以下 stem 无 PNG 且无 fallback。", file=sys.stderr)
        for stem, suffix, *_ in missing:
            print(f"  {stem} ({suffix})", file=sys.stderr)
        return 1

    if not write:
        return 0

    def merge_lang(path: Path, updates: dict[str, str]) -> None:
        data = json.loads(path.read_text(encoding="utf-8"))
        for k, v in updates.items():
            data[k] = v
        path.write_text(
            json.dumps(data, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )

    merge_lang(LANG_ZH, lang_keys_zh)
    merge_lang(LANG_EN, lang_keys_en)
    print(f"\n已写入 {LANG_ZH.relative_to(REPO_ROOT)} 与 {LANG_EN.relative_to(REPO_ROOT)}")
    return 0


def main() -> None:
    ap = argparse.ArgumentParser(description="普通玻璃窗物品译名（枕头同款纯色括号风格）")
    ap.add_argument("--write", action="store_true", help="写回 zh_cn.json / en_us.json")
    args = ap.parse_args()
    sys.exit(run(write=args.write))


if __name__ == "__main__":
    main()
