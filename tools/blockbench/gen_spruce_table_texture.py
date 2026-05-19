# -*- coding: utf-8 -*-
"""用原版云杉木板贴图平铺生成 spruce_table 64×64 贴图。"""
from __future__ import annotations

import shutil
import urllib.request
from pathlib import Path

from PIL import Image

FF = Path(__file__).resolve().parents[2]
TOOLS = Path(__file__).resolve().parent
OUT = TOOLS / "spruce_table_texture_64.png"
ASSETS = FF / "src/main/resources/assets/fantasy_furniture/textures/block/spruce_table.png"
VANILLA_CACHE = TOOLS / "spruce_planks_vanilla_16.png"

# 1.20.1 与 1.20.4 云杉板纹理一致（16×16）
VANILLA_URLS = (
    "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/"
    "1.20.4/assets/minecraft/textures/block/spruce_planks.png",
    "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/"
    "1.20.1/assets/minecraft/textures/block/spruce_planks.png",
)


def fetch_vanilla_planks(dest: Path) -> Path:
    if dest.is_file() and dest.stat().st_size > 100:
        return dest
    last_err: Exception | None = None
    for url in VANILLA_URLS:
        try:
            urllib.request.urlretrieve(url, dest)
            with Image.open(dest) as probe:
                probe.verify()
            return dest
        except Exception as exc:  # noqa: BLE001
            last_err = exc
    raise RuntimeError("无法下载原版 spruce_planks.png") from last_err


def tile_to_64(src: Path, dest: Path, *, tile: int = 64) -> None:
    with Image.open(src) as planks:
        planks = planks.convert("RGBA")
        w, h = planks.size
        if w != h:
            raise ValueError(f"期望正方形贴图，实际 {w}x{h}")
        atlas = Image.new("RGBA", (tile, tile))
        reps = tile // w
        if reps * w != tile:
            raise ValueError(f"tile 尺寸 {tile} 须为 {w} 的整数倍")
        for ty in range(reps):
            for tx in range(reps):
                atlas.paste(planks, (tx * w, ty * h))
        dest.parent.mkdir(parents=True, exist_ok=True)
        atlas.save(dest)


def main() -> None:
    vanilla = fetch_vanilla_planks(VANILLA_CACHE)
    tile_to_64(vanilla, OUT)
    ASSETS.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy(OUT, ASSETS)
    print(f"vanilla 16x16: {vanilla}")
    print(f"atlas 64x64:   {OUT}")
    print(f"assets:        {ASSETS}")


if __name__ == "__main__":
    main()
