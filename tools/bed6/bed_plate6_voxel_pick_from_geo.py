#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
床板 6 选取用 ``VoxelShape`` 片段生成（设计见 ``docs/幻想家具-文档/开发流程/02设计/方案/斜向模型与子组件选取-VoxelShape与工具链方案.md``）。

- 复用 ``tools/collision/geo_collision_box.compute_north_pick_boxes_axis_aligned``：默认裁切盒为 ``[0,16]×[0,16]×[0,32]``（床板 6 两格身长：枕头 geo 常在 z≈16..32，仅用 ``[0,16]³`` 会空）；被单/被套仍在 x/y/z 内与盒求交即可。
- 裁后按文档 §3.6 保证三轴边长 **≥ min_extent**（默认 0.5）；在各轴裁切区间内对称扩张。
- 可选按 §3.6.1 将 **min/max 量化到 0.5 网格**（四舍五入到最近半格，再裁回同一裁切盒）。
- 输出 Java 片段时写入 **溯源注释**（geo 短哈希、生成时间、路径），见 §2.3。

用法（在 ``fantasy_furniture`` 仓库根目录）::

    python tools/bed6/bed_plate6_voxel_pick_from_geo.py src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_medium_solo.geo.json
    python tools/bed6/bed_plate6_voxel_pick_from_geo.py path/to/model.geo.json --no-snap --precision 4

- 若 ``compute_north_pick_boxes_axis_aligned`` 仍抛出无相交：检查 geo 是否与默认裁切盒相交，或传入 ``--zmax`` / ``--bounds``（待 CLI 扩展）。

单测::

    python tools/bed6/test_bed_plate6_voxel_pick_from_geo.py
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable

_TOOLS_ROOT = Path(__file__).resolve().parent.parent
_COLL = _TOOLS_ROOT / "collision"
for _p in (_TOOLS_ROOT, _COLL):
    if str(_p) not in sys.path:
        sys.path.insert(0, str(_p))
from paths import FF_ROOT  # noqa: E402

import geo_collision_box as gcb  # noqa: E402

ROOT = FF_ROOT

Box6 = tuple[float, float, float, float, float, float]  # x0 x1 y0 y1 z0 z1

# xmin, xmax, ymin, ymax, zmin, zmax — 床板 6 床尾方块局部坐标（北向基准 geo）
BED_PLATE6_PICK_CLIP_DEFAULT: tuple[float, float, float, float, float, float] = (
    0.0,
    16.0,
    0.0,
    16.0,
    0.0,
    32.0,
)


def clip_aabb_to_bounds(
    x0: float,
    x1: float,
    y0: float,
    y1: float,
    z0: float,
    z1: float,
    bounds: tuple[float, float, float, float, float, float],
    eps: float = 1e-9,
) -> Box6 | None:
    """与轴对齐裁切盒 ``[xmin,xmax]×[ymin,ymax]×[zmin,zmax]`` 求交；退化则 ``None``。"""
    xmin, xmax, ymin, ymax, zmin, zmax = bounds
    ax0, ax1 = max(x0, xmin), min(x1, xmax)
    ay0, ay1 = max(y0, ymin), min(y1, ymax)
    az0, az1 = max(z0, zmin), min(z1, zmax)
    if ax1 - ax0 <= eps or ay1 - ay0 <= eps or az1 - az0 <= eps:
        return None
    return (ax0, ax1, ay0, ay1, az0, az1)


def clip_aabb_to_cell_full(
    x0: float,
    x1: float,
    y0: float,
    y1: float,
    z0: float,
    z1: float,
    lo: float = 0.0,
    hi: float = 16.0,
    eps: float = 1e-9,
) -> Box6 | None:
    """与闭立方 ``[lo, hi]³`` 求交；单测与旧接口保留。"""
    return clip_aabb_to_bounds(x0, x1, y0, y1, z0, z1, (lo, hi, lo, hi, lo, hi), eps=eps)


def _expand_axis(a0: float, a1: float, lo: float, hi: float, min_e: float) -> tuple[float, float]:
    span = a1 - a0
    if span >= min_e - 1e-12:
        return a0, a1
    mid = 0.5 * (a0 + a1)
    half = 0.5 * min_e
    n0, n1 = mid - half, mid + half
    if n0 < lo:
        n0, n1 = lo, lo + min_e
    if n1 > hi:
        n1, n0 = hi, hi - min_e
    if n1 - n0 < min_e - 1e-12:
        n0, n1 = lo, min(lo + min_e, hi)
        if n1 - n0 < min_e - 1e-12:
            n0, n1 = max(lo, hi - min_e), hi
    return n0, n1


def ensure_min_extents_bounds(
    box: Box6,
    bounds: tuple[float, float, float, float, float, float],
    min_extent: float = 0.5,
) -> Box6:
    """§3.6：三轴边长均 ≥ min_extent；在裁切盒各轴区间内扩张。"""
    xmin, xmax, ymin, ymax, zmin, zmax = bounds
    x0, x1, y0, y1, z0, z1 = box
    x0, x1 = _expand_axis(x0, x1, xmin, xmax, min_extent)
    y0, y1 = _expand_axis(y0, y1, ymin, ymax, min_extent)
    z0, z1 = _expand_axis(z0, z1, zmin, zmax, min_extent)
    return (x0, x1, y0, y1, z0, z1)


def ensure_min_extents_cell(
    box: Box6,
    lo: float = 0.0,
    hi: float = 16.0,
    min_extent: float = 0.5,
) -> Box6:
    """§3.6：立方 ``[lo,hi]³`` 内扩张（单测保留）。"""
    return ensure_min_extents_bounds(box, (lo, hi, lo, hi, lo, hi), min_extent=min_extent)


def snap_half_grid(
    box: Box6,
    bounds: tuple[float, float, float, float, float, float] = BED_PLATE6_PICK_CLIP_DEFAULT,
) -> Box6:
    """§3.6.1：各 min/max 四舍五入到最近 0.5，再裁回 ``bounds``。"""
    x0, x1, y0, y1, z0, z1 = box

    def snap(v: float) -> float:
        return round(v * 2.0) / 2.0

    x0, x1 = snap(x0), snap(x1)
    y0, y1 = snap(y0), snap(y1)
    z0, z1 = snap(z0), snap(z1)
    if x0 > x1:
        x0, x1 = x1, x0
    if y0 > y1:
        y0, y1 = y1, y0
    if z0 > z1:
        z0, z1 = z1, z0
    c = clip_aabb_to_bounds(x0, x1, y0, y1, z0, z1, bounds)
    return c if c is not None else box


def file_sha256_short(path: Path, n: int = 12) -> str:
    h = hashlib.sha256()
    h.update(path.read_bytes())
    return h.hexdigest()[:n]


def provenance_comment_lines(geo_path: Path, extra: str | None = None) -> list[str]:
    """§2.3 溯源：短哈希、时间、路径。"""
    short = file_sha256_short(geo_path)
    ts = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    try:
        rel = geo_path.resolve().relative_to(ROOT)
        path_s = str(rel).replace("\\", "/")
    except ValueError:
        path_s = str(geo_path.resolve()).replace("\\", "/")
    lines = [
        f"// bed_plate6_voxel_pick_from_geo: sha256[:12]={short} generated_utc={ts}",
        f"// source_geo={path_s}",
    ]
    if extra:
        lines.append(f"// {extra}")
    return lines


def north_pick_boxes_from_geo(
    geo_path: Path,
    *,
    min_extent: float = 0.5,
    snap_half: bool = True,
    clip_bounds: tuple[float, float, float, float, float, float] = BED_PLATE6_PICK_CLIP_DEFAULT,
) -> list[Box6]:
    """
    读取 geo，返回北向基准、裁切盒内选取盒列表（经最小边长处理；可选半格量化）。

    默认 ``clip_bounds`` 为床板 6 床尾局部 ``[0,16]×[0,16]×[0,32]``；若只要单格可传入
    ``(0,16,0,16,0,16)`` 并调用 ``geo_collision_box.compute_north_pick_boxes_full_cell`` 等价裁切。
    """
    xmin, xmax, ymin, ymax, zmin, zmax = clip_bounds
    raw_boxes = gcb.compute_north_pick_boxes_axis_aligned(
        geo_path,
        xmin=xmin,
        xmax=xmax,
        ymin=ymin,
        ymax=ymax,
        zmin=zmin,
        zmax=zmax,
    )
    out: list[Box6] = []
    for b in raw_boxes:
        fixed = ensure_min_extents_bounds(b, clip_bounds, min_extent=min_extent)
        if snap_half:
            fixed = snap_half_grid(fixed, clip_bounds)
            fixed = ensure_min_extents_bounds(fixed, clip_bounds, min_extent=min_extent)
        out.append(fixed)
    return out


def emit_java_shapes_or(
    boxes: Iterable[Box6],
    geo_path: Path,
    *,
    precision: int = 4,
    method_name: str = "buildPickShapeNorthUnionGenerated",
    extra_note: str | None = None,
) -> str:
    """生成 ``Shapes.or`` + ``Block.box`` Java 片段（不含类包装）。"""
    lines: list[str] = []
    lines.extend(provenance_comment_lines(geo_path, extra=extra_note))
    ident = ""
    try:
        data = json.loads(geo_path.read_text(encoding="utf-8"))
        geoms = data.get("minecraft:geometry")
        if geoms and isinstance(geoms[0], dict):
            desc = geoms[0].get("description")
            if isinstance(desc, dict) and isinstance(desc.get("identifier"), str):
                ident = desc["identifier"]
    except OSError:
        pass
    if ident:
        lines.append(f"// geometry_identifier={ident}")
    lines.append(f"private static VoxelShape {method_name}() {{")
    lines.append("    VoxelShape s = Shapes.empty();")
    fmt = f"{{:.{precision}f}}"
    for x0, x1, y0, y1, z0, z1 in boxes:
        parts = ", ".join(
            fmt.format(v)
            for v in (
                x0,
                y0,
                z0,
                x1,
                y1,
                z1,
            )
        )
        lines.append(f"    s = Shapes.or(s, Block.box({parts}));")
    lines.append("    return s;")
    lines.append("}")
    return "\n".join(lines) + "\n"


def main() -> None:
    p = argparse.ArgumentParser(description="从 bedrock geo 生成床板6选取用 VoxelShape Java 片段（北向、[0,16]³）")
    p.add_argument("geo", type=Path, help="geo.json 路径")
    p.add_argument("--no-snap", action="store_true", help="不做 0.5 网格量化（§3.6.1）")
    p.add_argument("--min-extent", type=float, default=0.5, help="单盒三轴最小边长（默认 0.5）")
    p.add_argument("--precision", type=int, default=4, help="Java 小数位（默认 4）")
    p.add_argument("--method-name", type=str, default="buildPickShapeNorthUnionGenerated", help="生成的 Java 方法名")
    args = p.parse_args()
    geo = args.geo
    if not geo.is_file():
        raise SystemExit(f"文件不存在: {geo}")
    boxes = north_pick_boxes_from_geo(
        geo,
        min_extent=args.min_extent,
        snap_half=not args.no_snap,
    )
    if not boxes:
        raise SystemExit("无有效选取盒（检查 geo 是否与单格相交）")
    text = emit_java_shapes_or(
        boxes,
        geo,
        precision=args.precision,
        method_name=args.method_name,
        extra_note="clip=[0,16]^3 pick-shape",
    )
    print(text, end="")


if __name__ == "__main__":
    main()
