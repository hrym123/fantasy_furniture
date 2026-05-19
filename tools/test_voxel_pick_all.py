#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""运行通用与床板 6 的 voxel_pick 单测。"""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TESTS = (
    Path(__file__).resolve().parent / "collision" / "test_voxel_pick_from_geo.py",
    Path(__file__).resolve().parent / "bed6" / "test_bed_plate6_voxel_pick_from_geo.py",
)


def main() -> int:
    for script in TESTS:
        print(f"\n=== {script.relative_to(ROOT)} ===\n", flush=True)
        code = subprocess.call([sys.executable, str(script)], cwd=str(ROOT))
        if code != 0:
            return code
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
