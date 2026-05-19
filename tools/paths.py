# -*- coding: utf-8 -*-
"""``tools`` 根目录与模组仓库根路径（子目录内脚本通过 ``sys.path`` 引入本模块）。"""
from __future__ import annotations

from pathlib import Path

TOOLS_ROOT = Path(__file__).resolve().parent
FF_ROOT = TOOLS_ROOT.parent
DEFAULT_ASSETS = FF_ROOT / "src/main/resources/assets/fantasy_furniture"
