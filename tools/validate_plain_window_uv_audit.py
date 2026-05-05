#!/usr/bin/env python3
"""
将脚本导出的 ``models/block/{prefix}.json`` 与 Moon 目录下的 ``{prefix}_对比用.json`` 对比。

因模组 ``textures`` 为 ``fantasy_furniture:block/...``，对比版为 ``block/texture`` 等占位，比较前仅把
导出 JSON 的 ``textures`` 与各面 ``texture``（# 引用）换成对比版中的写法，其余须与对比版**完全一致**。
不一致则打印 NG，退出码 1；无对比文件则 SKIP；全部通过则 OK，退出码 0。
"""
from __future__ import annotations

import copy
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MOON = Path(r"d:\warehouse\MoonStarfish素材\普通窗户")
MODEL_OUT = ROOT / "src/main/resources/assets/fantasy_furniture/models/block"

# 与 export_moonstarfish_plain_windows.PAIRS 保持一致
PAIRS = [
    ("plain_window.json", "plain_window"),
    ("plain_window_y180.json", "plain_window_y180"),
    ("plain_window_y22_5.json", "plain_window_y22_5"),
    ("plain_window_y45.json", "plain_window_y45"),
    ("plain_window_y67_5.json", "plain_window_y67_5"),
    ("plain_window_diagonal.json", "plain_window_diagonal"),
]


def _with_golden_texture_layer(gen: dict, golden: dict) -> dict:
    """导出模型 + 对比版的 textures / 各面 # 引用 → 可与 golden 做 dict 相等。"""
    out: dict = {}
    for k in golden:
        if k not in gen:
            raise KeyError(k)
        if k == "textures":
            out[k] = copy.deepcopy(golden[k])
        elif k == "elements":
            ge = gen["elements"]
            gg = golden["elements"]
            if not isinstance(ge, list) or not isinstance(gg, list) or len(ge) != len(gg):
                raise ValueError("elements length mismatch")
            merged = copy.deepcopy(ge)
            for el_g, el_m in zip(gg, merged):
                fg = el_g.get("faces") or {}
                fm = el_m.get("faces") or {}
                for fname, face_g in fg.items():
                    if fname not in fm:
                        raise KeyError(fname)
                    fm[fname]["texture"] = face_g["texture"]
            out[k] = merged
        else:
            out[k] = copy.deepcopy(gen[k])
    return out


def main() -> int:
    any_checked = False
    all_ok = True

    for _bb_name, prefix in PAIRS:
        golden_path = MOON / f"{prefix}_对比用.json"
        mod_path = MODEL_OUT / f"{prefix}.json"
        if not golden_path.is_file():
            print("SKIP", prefix, "(no 对比用.json)")
            continue
        if not mod_path.is_file():
            print("NG", prefix, "missing", mod_path)
            all_ok = False
            continue

        any_checked = True
        golden = json.loads(golden_path.read_text(encoding="utf-8"))
        gen = json.loads(mod_path.read_text(encoding="utf-8"))

        if set(golden.keys()) != set(gen.keys()):
            print("NG", prefix, "top-level keys differ")
            print("  golden:", sorted(golden.keys()))
            print("  gen:   ", sorted(gen.keys()))
            all_ok = False
            continue

        try:
            candidate = _with_golden_texture_layer(gen, golden)
        except (KeyError, ValueError) as e:
            print("NG", prefix, str(e))
            all_ok = False
            continue

        if candidate != golden:
            print("NG", prefix, "(differs from 对比用 after texture layer)")
            all_ok = False
            continue

        print("OK", prefix)

    if not any_checked:
        print("SKIP (no 对比用.json for any variant)", file=sys.stderr)
    return 0 if all_ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
