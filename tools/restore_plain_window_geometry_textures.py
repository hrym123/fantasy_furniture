#!/usr/bin/env python3
"""已合并至 ``plain_window_consolidate_nine_textures.py``（仅保留 9 张贴图）。请直接运行该脚本。"""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main() -> None:
    script = ROOT / "tools/plain_window_consolidate_nine_textures.py"
    raise SystemExit(subprocess.call([sys.executable, str(script)]))


if __name__ == "__main__":
    main()
