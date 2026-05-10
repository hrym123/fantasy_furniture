# -*- coding: utf-8 -*-
"""普通玻璃窗共享贴图：按方块 id + 槽位 + 图像主色生成资源 stem（与 Java PlainGlassWindowSharedTextures 对齐）。"""

from __future__ import annotations

from io import BytesIO
from pathlib import Path
from typing import Tuple

import numpy as np
from PIL import Image

BLOCK_ID = "plain_glass_window"

# 与 PlainGlassWindowSharedTextures.TEXTURE_STEMS 顺序一致（材质 ordinal = 槽位索引）
PLAIN_GLASS_WINDOW_TEXTURE_STEMS: tuple[str, ...] = (
    "plain_glass_window_0_white",
    "plain_glass_window_1_black",
    "plain_glass_window_2_tan",
    "plain_glass_window_3_ice_blue",
    "plain_glass_window_4_cream",
    "plain_glass_window_5_pale_green",
    "plain_glass_window_6_cream",
    "plain_glass_window_7_mixed",
    "plain_glass_window_8_cream",
)


def dominant_rgb_from_bytes(raw: bytes) -> tuple[int, int, int]:
    im = Image.open(BytesIO(raw)).convert("RGBA")
    return _dominant_rgb_array(np.array(im))


def dominant_rgb(path: Path) -> tuple[int, int, int]:
    im = Image.open(path).convert("RGBA")
    return _dominant_rgb_array(np.array(im))


def _dominant_rgb_array(arr: np.ndarray) -> tuple[int, int, int]:
    rgb = arr[..., :3].astype(np.float32)
    w = arr[..., 3].astype(np.float32) / 255.0
    if w.sum() < 1:
        w = np.ones_like(w)
    tw = float(w.sum())
    avg = (rgb * w[..., None]).reshape(-1, 3).sum(0) / tw
    return tuple(int(round(x)) for x in avg)


def color_suffix(r: int, g: int, b: int) -> str:
    """英文 snake_case，用于文件名。"""
    mx = max(r, g, b)
    mn = min(r, g, b)
    l = (mx + mn) / 510.0
    if mx - mn < 18:
        if l > 0.92:
            return "white"
        if l < 0.12:
            return "black"
        if l > 0.65:
            return "light_gray"
        if l < 0.35:
            return "dark_gray"
        return "gray"
    if r >= g - 5 and r >= b - 5 and g < b + 25:
        if l > 0.55 and g > 170:
            return "cream"
        if r > g + 25 and r > b + 25:
            return "terracotta" if l < 0.55 else "salmon"
        return "tan"
    if g >= r - 5 and g >= b - 5:
        if b > r + 20:
            return "seafoam"
        return "sage" if l < 0.5 else "pale_green"
    if b >= r - 5 and b >= g - 5:
        if l > 0.75:
            return "ice_blue"
        return "slate_blue"
    return "mixed"


def texture_stem_for_slot(slot: int, image_path: Path) -> str:
    return texture_stem_from_rgb(slot, dominant_rgb(image_path))


def texture_stem_from_rgb(slot: int, rgb: Tuple[int, int, int]) -> str:
    r, g, b = rgb
    return f"{BLOCK_ID}_{slot}_{color_suffix(r, g, b)}"
