#!/usr/bin/env python3
"""从 Blockbench「床板6（被单）」Gecko bbmodel 导出 7 张床单贴图。

bbmodel 的 ``textures`` 数组里通常有 7 份内嵌 base64 PNG（七种被单配色/纹样）。
游戏内通过 ``bed_plate6_duvet_1.png`` … ``bed_plate6_duvet_7.png`` 切换材质。

常见错误（会导致七种看起来是同一张图）：
  - 沿用 ``export_bed_plate6_from_bbmodel.py`` 的逻辑只解码 ``textures[0]``；
  - 把同一张 PNG 复制 7 份命名成 _1…_7；
  - 只导出模型里当前立方体正在使用的那一个槽位，却复制 7 次。

本脚本按 ``textures`` 数组顺序依次写出 ``bed_plate6_duvet_{1+index}.png``。
若你在 Blockbench 里纹理顺序与游戏期望不一致，可在导出后手动对调文件或改数组顺序。

主体床板单张贴图仍请使用 ``export_bed_plate6_from_bbmodel.py``。
"""
from __future__ import annotations

import argparse
import base64
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_BBMODEL = Path(r"d:\warehouse\MoonStarfish素材\床板6-Geo\床板6（被单）.bbmodel")
ASSETS_BLOCK = ROOT / "src/main/resources/assets/fantasy_furniture/textures/block"


def decode_texture_source(source: str) -> bytes:
    if "," in source and source.strip().lower().startswith("data:"):
        b64 = source.split(",", 1)[1]
    else:
        b64 = source
    return base64.b64decode(b64)


def main() -> int:
    p = argparse.ArgumentParser(description="Export bed_plate6_duvet_1..7.png from bbmodel textures[]")
    p.add_argument(
        "--bbmodel",
        type=Path,
        default=DEFAULT_BBMODEL,
        help="Path to 床板6（被单）.bbmodel",
    )
    p.add_argument(
        "--out-dir",
        type=Path,
        default=ASSETS_BLOCK,
        help="Output directory for PNGs",
    )
    args = p.parse_args()

    path = args.bbmodel
    if not path.is_file():
        print("ERROR: bbmodel not found:", path, file=sys.stderr)
        return 1

    data = json.loads(path.read_text(encoding="utf-8"))
    textures = data.get("textures") or []
    if not textures:
        print("ERROR: bbmodel has no textures[]", file=sys.stderr)
        return 1

    out_dir: Path = args.out_dir
    out_dir.mkdir(parents=True, exist_ok=True)

    written: list[Path] = []
    for i, tex in enumerate(textures):
        src = tex.get("source")
        if not src:
            print(f"WARN: textures[{i}] has no source, skip", file=sys.stderr)
            continue
        png_bytes = decode_texture_source(src)
        out_path = out_dir / f"bed_plate6_duvet_{i + 1}.png"
        out_path.write_bytes(png_bytes)
        written.append(out_path)

    if len(written) != 7:
        print(
            f"WARN: expected 7 textures, wrote {len(written)} (check Blockbench project)",
            file=sys.stderr,
        )

    for w in written:
        print("Wrote:", w)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
