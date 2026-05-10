#!/usr/bin/env python3
"""
根据主纹理 PNG 的主色（与 plain_glass_window_texture_naming.dominant_rgb 一致）
生成普通玻璃窗 9 个物品的中英文显示名，并在名称中追加「主色」描述以便区分奶油/混色等变体。

用法：
  python tools/plain_glass_window_lang_display_colors.py           # 仅打印表格
  python tools/plain_glass_window_lang_display_colors.py --write   # 写回 zh_cn.json / en_us.json

若某 stem 的 PNG 不存在，使用脚本内 FALLBACK_DOMINANT_RGB（占位，有贴图后请重跑）。
"""

from __future__ import annotations

import argparse
import colorsys
import json
import sys
from pathlib import Path

from plain_glass_window_texture_naming import (
    PLAIN_GLASS_WINDOW_TEXTURE_STEMS,
    dominant_rgb,
)

REPO_ROOT = Path(__file__).resolve().parents[1]
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

# 括号内第一段：与材质 id 对应（玩家已熟悉的简称）
MATERIAL_LABEL_ZH: dict[str, str] = {
    "white": "白",
    "black": "黑",
    "tan": "棕褐",
    "ice_blue": "冰蓝",
    "cream": "奶油",
    "pale_green": "淡绿",
    "cream_b": "奶油 B",
    "mixed": "混色",
    "cream_c": "奶油 C",
}
MATERIAL_LABEL_EN: dict[str, str] = {
    "white": "White",
    "black": "Black",
    "tan": "Tan",
    "ice_blue": "Ice Blue",
    "cream": "Cream",
    "pale_green": "Pale Green",
    "cream_b": "Cream B",
    "mixed": "Mixed",
    "cream_c": "Cream C",
}

# PNG 缺失时的占位主色（有贴图后应删除或保持与 dominant_rgb 一致）
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


def visual_tone_zh_en(r: int, g: int, b: int) -> tuple[str, str]:
    """由主色 RGB 得到简短、玩家可读的中英文主色描述。"""
    rf, gf, bf = r / 255.0, g / 255.0, b / 255.0
    h, s, v = colorsys.rgb_to_hsv(rf, gf, bf)
    deg = (h * 360.0) % 360.0
    mx = max(r, g, b)
    mn = min(r, g, b)

    is_light = v >= 0.62
    is_dark = v <= 0.38
    zh_light = "浅" if is_light else ("深" if is_dark else "")
    en_light = "Light " if is_light else ("Deep " if is_dark else "")

    # 须在灰阶判定之前：极低饱和的粉 / 紫会被误判成「近白」
    if b == mx and (b - r) <= 28 and (b - g) <= 40 and s >= 0.03 and v >= 0.72:
        return zh_light + "粉紫", en_light + "lavender"
    # 品红侧 hue≈330° 仍属粉米，勿仅用 deg<28（否则会落到「近白」）
    # 偏黄奶油（与粉米区分：混色窗常见）
    if r == mx and (g - b) >= 14 and (r - g) <= 28 and v >= 0.84:
        return zh_light + "黄油米", en_light + "butter white"

    pink_hue = deg < 38 or deg > 318
    warm_tint = (r - mn) >= 8
    if (
        r == mx
        and warm_tint
        and (r - b) <= 40
        and (r - g) <= 45
        and pink_hue
        and v >= 0.78
    ):
        if b > g + 4:
            return zh_light + "玫瑰米", en_light + "rose cream"
        return zh_light + "桃米", en_light + "peach cream"

    if g == mx and (g - r) >= 6 and (g - b) >= 6 and v >= 0.70:
        return zh_light + "淡绿", en_light + "greenish"

    # 低饱和：灰阶（略提高阈值，避免把淡奶油判成「纯灰」）
    if s < 0.11:
        if v >= 0.88:
            return "近白", "Near white"
        if v <= 0.14:
            return "近黑", "Near black"
        if v >= 0.62:
            return "浅灰", "Light gray"
        if v <= 0.38:
            return "深灰", "Dark gray"
        return "中灰", "Gray"

    # 奶油 / 米色 / 沙色（暖色相 8°–95°，中低饱和；覆盖橙红～黄）
    if 0.06 <= s < 0.42 and 8 <= deg <= 95 and v > 0.45:
        if v >= 0.88 and s < 0.18:
            return zh_light + "米白", en_light + "off-white"
        if deg < 58 and s >= 0.10 and r >= g and r >= b:
            return zh_light + "土褐色", en_light + "earth brown"
        if deg < 55 and s < 0.24:
            return zh_light + "米色", en_light + "beige"
        return zh_light + "沙色", en_light + "sand"

    # 冰蓝 / 天蓝
    if 165 <= deg <= 230 and b >= r and b >= g:
        if s >= 0.12 and v >= 0.55:
            return zh_light + "冰蓝", en_light + "ice blue"
        return zh_light + "灰蓝", en_light + "blue gray"

    # 绿调
    if 75 <= deg <= 165 and g >= r and g >= b:
        if s >= 0.10:
            return zh_light + "淡绿", en_light + "greenish"
        return zh_light + "灰绿", en_light + "sage"

    # 棕褐（略扩下界，避免 ~13° 土色漏网）
    if 8 <= deg <= 58 and r >= g and r >= b and s >= 0.10 and mx - mn >= 18:
        return zh_light + "土褐色", en_light + "earth brown"

    # 混色 / 杂色（饱和偏高且不易归入上类）
    if s >= 0.35:
        return "杂色主调", "Multitone"

    return "暖灰主调", "Warm gray"


def rgb_for_stem(stem: str) -> tuple[int, int, int, str]:
    path = TEXTURES_BLOCK / f"{stem}.png"
    if path.is_file():
        return (*dominant_rgb(path), "png")
    fb = FALLBACK_DOMINANT_RGB.get(stem)
    if fb:
        return (*fb, "fallback")
    return (128, 128, 128, "missing")


def format_name_zh(material_key: str, tone_zh: str) -> str:
    mz = MATERIAL_LABEL_ZH[material_key]
    return f"普通玻璃窗（{mz}｜主色：{tone_zh}）"


def format_name_en(material_key: str, tone_en: str) -> str:
    me = MATERIAL_LABEL_EN[material_key]
    return f"Plain Glass Window ({me} · {tone_en})"


def run(write: bool) -> int:
    rows: list[tuple[str, str, int, int, int, str, str, str]] = []
    lang_keys_zh: dict[str, str] = {}
    lang_keys_en: dict[str, str] = {}

    for stem, suffix in zip(PLAIN_GLASS_WINDOW_TEXTURE_STEMS, VARIANT_SUFFIXES):
        r, g, b, src = rgb_for_stem(stem)
        tz, te = visual_tone_zh_en(r, g, b)
        zh = format_name_zh(suffix, tz)
        en = format_name_en(suffix, te)
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
        print("\n错误：以下 stem 无 PNG 且无 fallback，未生成可靠主色。", file=sys.stderr)
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
    ap = argparse.ArgumentParser(description="普通玻璃窗物品名追加主色描述")
    ap.add_argument("--write", action="store_true", help="写回 zh_cn.json / en_us.json")
    args = ap.parse_args()
    sys.exit(run(write=args.write))


if __name__ == "__main__":
    main()
