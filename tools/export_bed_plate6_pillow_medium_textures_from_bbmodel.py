#!/usr/bin/env python3
"""从「床板6枕头（中 1个放置的样子）.bbmodel」内嵌纹理写出 ``bed_plate6_pillow_medium_{1..6}.png``。

bbmodel 中立方体面引用 ``"texture": 1`` 等与 ``textures[].id``（字符串 \"1\"…\"6\"）对应；
按 **id** 命名输出，与 ``BedPlate6MediumPillowMaterials`` 一致。仅写 PNG，不写 geo。"""
from __future__ import annotations

import base64
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DST = ROOT / "src/main/resources/assets/fantasy_furniture/textures/block"
BBMODEL = Path(r"d:\warehouse\MoonStarfish素材\床板6-Geo\床板6枕头（中 1个放置的样子）.bbmodel")


def _decode_data_url(data: str) -> bytes:
    m = re.match(r"^data:image/png;base64,(.+)$", data.strip())
    if not m:
        raise ValueError("expected data:image/png;base64,...")
    return base64.b64decode(m.group(1))


def main() -> None:
    data = json.loads(BBMODEL.read_text(encoding="utf-8"))
    by_id: dict[int, bytes] = {}
    for t in data.get("textures", []):
        if not isinstance(t, dict) or not t.get("source"):
            continue
        tid = t.get("id")
        if tid is None:
            continue
        if isinstance(tid, str) and tid.isdigit():
            tid_i = int(tid)
        elif isinstance(tid, int):
            tid_i = tid
        else:
            continue
        by_id[tid_i] = _decode_data_url(t["source"])

    missing = [i for i in range(1, 7) if i not in by_id]
    if missing:
        raise SystemExit(f"bbmodel 缺少 id 为 {missing} 的纹理，当前有 id: {sorted(by_id)}")

    DST.mkdir(parents=True, exist_ok=True)
    for i in range(1, 7):
        out = DST / f"bed_plate6_pillow_medium_{i}.png"
        out.write_bytes(by_id[i])
        print("Wrote", out)


if __name__ == "__main__":
    main()
