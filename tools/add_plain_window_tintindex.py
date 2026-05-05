#!/usr/bin/env python3
"""为几何母版 plain_window*.json 的每个可见面添加 tintindex: 0，供客户端 BlockColors/ItemColors 染色。"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "src/main/resources/assets/fantasy_furniture/models/block"

STEMS = (
    "plain_window",
    "plain_window_y180",
    "plain_window_y22_5",
    "plain_window_y45",
    "plain_window_y67_5",
    "plain_window_diagonal",
)


def main() -> None:
    for stem in STEMS:
        p = MODELS / f"{stem}.json"
        if not p.is_file():
            print("SKIP", p)
            continue
        data = json.loads(p.read_text(encoding="utf-8"))
        elements = data.get("elements")
        if not isinstance(elements, list):
            continue
        n = 0
        for el in elements:
            faces = el.get("faces")
            if not isinstance(faces, dict):
                continue
            for face in faces.values():
                if isinstance(face, dict):
                    face["tintindex"] = 0
                    n += 1
        p.write_text(json.dumps(data, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
        print("OK", stem, "faces", n)


if __name__ == "__main__":
    main()
