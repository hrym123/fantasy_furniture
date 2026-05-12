#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""根据「床板6（被单）」bbmodel 内嵌贴图的主色，为每条 texture 生成中文显示名并重写 ``textures[].name``。

命名格式与项目习惯一致：``被单（奶油色）``、``被单（樱花粉）`` 等（可通过 ``--prefix`` 修改前缀）。

- 主色：与 ``plain_glass_window_texture_naming.dominant_rgb`` 相同，对 RGBA 按 alpha 加权平均。
- 色名：在较大调色板中选 **互不重复** 且总色差最小的组合（``scipy.optimize.linear_sum_assignment``；
  未安装 SciPy 时用贪心回退）。

常见用法（先看再写）::

  python tools/bed6/bed_plate6_duvet_bbmodel_rename_textures_by_color.py --bbmodel "…/床板6（被单）.bbmodel"
  python tools/bed6/bed_plate6_duvet_bbmodel_rename_textures_by_color.py --bbmodel … --write

Windows 终端若中文乱码，可先 ``chcp 65001`` 或 ``set PYTHONIOENCODING=utf-8``，或使用 ``--report`` 输出 UTF-8 文本文件查看。

可选从已导出的 PNG 目录读取（顺序 ``bed_plate6_duvet_1.png`` …）::

  python tools/bed6/bed_plate6_duvet_bbmodel_rename_textures_by_color.py --png-dir src/main/resources/assets/fantasy_furniture/textures/block
"""
from __future__ import annotations

import argparse
import base64
import json
import sys
from pathlib import Path

import numpy as np

_TOOLS_ROOT = Path(__file__).resolve().parent.parent
_GLASS = _TOOLS_ROOT / "glass"
for _p in (_TOOLS_ROOT, _GLASS):
    if str(_p) not in sys.path:
        sys.path.insert(0, str(_p))
from paths import FF_ROOT as ROOT  # noqa: E402

from plain_glass_window_texture_naming import dominant_rgb_from_bytes, dominant_rgb  # noqa: E402

DEFAULT_BBMODEL = Path(r"d:\warehouse\MoonStarfish素材\※已添加\床板6\被单\床板6（被单）.bbmodel")

# 中文纯色名（含「色」字）+ 参考 RGB；仅作匹配锚点，不必与真实染料一致。
PALETTE_ZH_RGB: tuple[tuple[str, tuple[int, int, int]], ...] = (
    ("奶油色", (248, 242, 217)),
    ("象牙白", (255, 250, 240)),
    ("樱花粉", (255, 192, 206)),
    ("蔷薇粉", (240, 175, 190)),
    ("玫瑰粉", (220, 130, 155)),
    ("珊瑚红", (235, 150, 140)),
    ("樱桃红", (190, 45, 55)),
    ("黄油黄", (255, 226, 137)),
    ("蜂蜜黄", (235, 200, 120)),
    ("芥末黄", (220, 190, 90)),
    ("薄荷绿", (188, 214, 190)),
    ("抹茶绿", (165, 190, 155)),
    ("鼠尾草绿", (175, 185, 165)),
    ("天蓝色", (165, 205, 255)),
    ("冰蓝色", (185, 207, 255)),
    ("丹宁蓝", (95, 125, 185)),
    ("矢车菊蓝", (140, 165, 220)),
    ("薰衣草紫", (200, 185, 230)),
    ("丁香紫", (191, 191, 255)),
    ("葡萄紫", (150, 110, 180)),
    ("可可棕", (125, 85, 65)),
    ("焦糖棕", (165, 115, 80)),
    ("栗棕色", (105, 70, 55)),
    ("炭灰色", (90, 90, 95)),
    ("银灰色", (190, 192, 198)),
)


def decode_texture_source(source: str) -> bytes:
    if "," in source and source.strip().lower().startswith("data:"):
        b64 = source.split(",", 1)[1]
    else:
        b64 = source
    return base64.b64decode(b64)


def _rgb_dist_sq(a: tuple[int, int, int], b: tuple[int, int, int]) -> float:
    r1, g1, b1 = a
    r2, g2, b2 = b
    # 简单感知权重
    return 0.3 * (r1 - r2) ** 2 + 0.59 * (g1 - g2) ** 2 + 0.11 * (b1 - b2) ** 2


def assign_labels(
    rgbs: list[tuple[int, int, int]],
    palette: tuple[tuple[str, tuple[int, int, int]], ...],
) -> tuple[list[str], float]:
    """为每条纹理分配互不重复的 palette 名称，使总平方距离最小。"""
    n = len(rgbs)
    m = len(palette)
    if n > m:
        raise ValueError(f"纹理数 {n} 超过调色板条目 {m}")
    cost = np.zeros((n, m), dtype=np.float64)
    for i, rgb in enumerate(rgbs):
        for j, (_, prgb) in enumerate(palette):
            cost[i, j] = _rgb_dist_sq(rgb, prgb)
    try:
        from scipy.optimize import linear_sum_assignment

        ri, cj = linear_sum_assignment(cost)
        order = sorted(zip(ri, cj), key=lambda t: t[0])
        names = [palette[j][0] for _, j in order]
        total = float(cost[ri, cj].sum())
        return names, total
    except ImportError:
        pass

    # 贪心回退：按行最小边排序，依次占用列
    edges: list[tuple[float, int, int]] = []
    for i in range(n):
        for j in range(m):
            edges.append((cost[i, j], i, j))
    edges.sort(key=lambda t: t[0])
    col_used: set[int] = set()
    row_name: dict[int, str] = {}
    total = 0.0
    for d, i, j in edges:
        if i in row_name or j in col_used:
            continue
        row_name[i] = palette[j][0]
        col_used.add(j)
        total += d
        if len(row_name) == n:
            break
    if len(row_name) != n:
        raise RuntimeError("贪心匹配失败（请安装 scipy 或扩大 PALETTE_ZH_RGB）")
    names = [row_name[i] for i in range(n)]
    return names, total


def collect_rgbs_from_bbmodel(data: dict) -> list[tuple[int, int, int]]:
    textures = data.get("textures") or []
    rgbs: list[tuple[int, int, int]] = []
    for i, tex in enumerate(textures):
        src = tex.get("source")
        if not src:
            raise ValueError(f"textures[{i}] 无 source，无法解码 PNG")
        rgbs.append(dominant_rgb_from_bytes(decode_texture_source(src)))
    return rgbs


def collect_rgbs_from_png_dir(png_dir: Path, count: int) -> list[tuple[int, int, int]]:
    rgbs = []
    for k in range(1, count + 1):
        p = png_dir / f"bed_plate6_duvet_{k}.png"
        if not p.is_file():
            raise FileNotFoundError(p)
        rgbs.append(dominant_rgb(p))
    return rgbs


def main() -> int:
    ap = argparse.ArgumentParser(
        description="按贴图主色为 bbmodel 被单纹理生成「被单（××色）」式名称并可选写回"
    )
    ap.add_argument("--bbmodel", type=Path, default=DEFAULT_BBMODEL, help="床板6（被单）.bbmodel 路径")
    ap.add_argument(
        "--png-dir",
        type=Path,
        default=None,
        help="若指定则从 bed_plate6_duvet_1..N.png 读主色，并仍用 --bbmodel 写回名称（需与纹理数量一致）",
    )
    ap.add_argument("--prefix", type=str, default="被单", help="名称前缀，默认「被单」")
    ap.add_argument("--write", action="store_true", help="写回 bbmodel（UTF-8，保留缩进 min）")
    ap.add_argument(
        "--indent",
        type=int,
        default=2,
        help="写回 JSON 缩进；Blockbench 通常接受 2",
    )
    ap.add_argument(
        "--report",
        type=Path,
        default=None,
        help="将表格写入该 UTF-8 文本（便于 Windows 控制台编码异常时查看）",
    )
    args = ap.parse_args()

    bb_path: Path = args.bbmodel
    if not bb_path.is_file():
        print("ERROR: bbmodel 不存在:", bb_path, file=sys.stderr)
        return 1

    data = json.loads(bb_path.read_text(encoding="utf-8"))
    textures = data.get("textures") or []
    n = len(textures)
    if n == 0:
        print("ERROR: textures 为空", file=sys.stderr)
        return 1

    if args.png_dir is not None:
        rgbs = collect_rgbs_from_png_dir(args.png_dir, n)
    else:
        rgbs = collect_rgbs_from_bbmodel(data)

    labels, total_cost = assign_labels(rgbs, PALETTE_ZH_RGB)
    prefix = args.prefix.strip() or "被单"

    lines: list[str] = [
        f"# 纹理数: {n}  总匹配代价(加权平方和): {total_cost:.1f}",
    ]
    for i, (rgb, lab) in enumerate(zip(rgbs, labels)):
        full = f"{prefix}（{lab}）"
        lines.append(f"  [{i + 1}] RGB{rgb} -> {full}")
    text = "\n".join(lines) + "\n"
    print(text, end="")
    if args.report is not None:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(text, encoding="utf-8")
        print("Report:", args.report.resolve())

    if args.write:
        for i, lab in enumerate(labels):
            textures[i]["name"] = f"{prefix}（{lab}）"
        bb_path.write_text(
            json.dumps(data, ensure_ascii=False, indent=args.indent) + "\n",
            encoding="utf-8",
        )
        print("Wrote:", bb_path)
    else:
        print("# 未使用 --write，未修改文件。确认无误后追加 --write 写回。")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
