#!/usr/bin/env python3
"""从「床板6（被套）」.bbmodel 导出被套贴图到 bed_plate6_duvet_cover_1..6.png（与游戏内被套六种材质一致）。"""
from __future__ import annotations

import argparse
import base64
import json
import sys
from pathlib import Path

_TOOLS_ROOT = Path(__file__).resolve().parent.parent
if str(_TOOLS_ROOT) not in sys.path:
    sys.path.insert(0, str(_TOOLS_ROOT))
from paths import FF_ROOT as ROOT  # noqa: E402

DEFAULT_BBMODEL = Path(r"d:\warehouse\MoonStarfish素材\床板6-Geo\床板6（被套）.bbmodel")
ASSETS_BLOCK = ROOT / "src/main/resources/assets/fantasy_furniture/textures/block"


def decode_texture_source(source: str) -> bytes:
    if "," in source and source.strip().lower().startswith("data:"):
        b64 = source.split(",", 1)[1]
    else:
        b64 = source
    return base64.b64decode(b64)


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--bbmodel", type=Path, default=DEFAULT_BBMODEL)
    p.add_argument("--out-dir", type=Path, default=ASSETS_BLOCK)
    args = p.parse_args()
    if not args.bbmodel.is_file():
        print("ERROR: bbmodel not found:", args.bbmodel, file=sys.stderr)
        return 1
    data = json.loads(args.bbmodel.read_text(encoding="utf-8"))
    textures = data.get("textures") or []
    if not textures:
        print("ERROR: no textures[]", file=sys.stderr)
        return 1
    args.out_dir.mkdir(parents=True, exist_ok=True)
    written: list[Path] = []
    for i, tex in enumerate(textures):
        src = tex.get("source")
        if not src:
            continue
        p = args.out_dir / f"bed_plate6_duvet_cover_{len(written) + 1}.png"
        p.write_bytes(decode_texture_source(src))
        written.append(p)
        print("Wrote:", p)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
