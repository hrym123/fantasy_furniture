#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从 Bedrock geo 生成北向基准 ``VoxelShape`` / ``Block.box`` Java 片段（通用管线）。

- 复用 ``geo_collision_box.compute_north_pick_boxes_axis_aligned``：默认裁切盒为单格 ``[0,16]³``（1/16 格坐标）。
- 裁后保证三轴边长 **≥ min_extent**（默认 0.5）；半格量化默认关闭（``--snap-half`` 可选）。
- 输出 Java 时写入溯源注释（geo 短哈希、时间、路径）。
- 床板 6 等专用裁切/镜像/合并规则见 ``--preset bed-plate6`` 或 ``tools/bed6/bed_plate6_voxel_pick_from_geo.py``。

用法（在 ``fantasy_furniture`` 仓库根目录）::

    python tools/collision/voxel_pick_from_geo.py path/to/model.geo.json
    python tools/collision/voxel_pick_from_geo.py path/to/model.geo.json --clip 0 16 0 16 0 32
    python tools/collision/voxel_pick_from_geo.py path/to/model.geo.json --export-md
    python tools/collision/voxel_pick_from_geo.py path/to/model.geo.json --preset bed-plate6 --export-md

单测::

    python tools/collision/test_voxel_pick_from_geo.py
    python tools/bed6/test_bed_plate6_voxel_pick_from_geo.py
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import re
import sys
from dataclasses import dataclass, field
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
ClipBounds = tuple[float, float, float, float, float, float]

_MODEL_MARKER_RE = re.compile(r"^<!-- model: (.+?) -->\s*$", re.MULTILINE)
_MODEL_END_RE = re.compile(r"^<!-- /model: (.+?) -->\s*$", re.MULTILINE)

# 单格方块局部坐标（北向基准 geo，1/16 格）
DEFAULT_PICK_CLIP: ClipBounds = (0.0, 16.0, 0.0, 16.0, 0.0, 16.0)

DEFAULT_EXPORT_MD = Path(__file__).resolve().parent / "voxel_pick_export.md"


@dataclass(frozen=True)
class PickPipelineConfig:
    """按模型/系列可覆盖的选取管线参数（默认适用于单格方块 geo）。"""

    clip_default: ClipBounds = DEFAULT_PICK_CLIP
    unrotated_models: frozenset[str] = frozenset()
    swap_yz_models: frozenset[str] = frozenset()
    mirror_x_north_models: frozenset[str] = frozenset()
    merge_union_aabb_bones: dict[str, frozenset[str]] = field(default_factory=dict)
    default_export_md: Path = DEFAULT_EXPORT_MD
    provenance_tag: str = "voxel_pick_from_geo"
    export_md_title: str = "Geo 选取体素导出"
    export_md_script_ref: str = "tools/collision/voxel_pick_from_geo.py"
    export_md_coords_note: str = (
        "坐标为北向基准局部 **1/16 格**；默认裁切为单格 `x,y,z ∈ [0,16]`。"
    )
    export_md_stage_a_clip_note: str = "裁到配置裁切盒"
    export_md_stage_b_java_ref: str = "写入游戏的最终盒"


DEFAULT_PICK_CONFIG = PickPipelineConfig()


def resolve_preset_config(name: str) -> PickPipelineConfig:
    if name == "bed-plate6":
        import importlib.util

        path = _TOOLS_ROOT / "bed6" / "bed_plate6_pick_config.py"
        spec = importlib.util.spec_from_file_location("bed_plate6_pick_config", path)
        if spec is None or spec.loader is None:
            raise ImportError(f"无法加载 preset 配置: {path}")
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        return mod.BED_PLATE6_PICK_CONFIG
    raise ValueError(f"未知 preset: {name!r}（可选: bed-plate6）")


def north_pick_boxes_unrotated_cubes_from_geo(
    geo_path: Path,
    *,
    clip_bounds: ClipBounds | None = None,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> list[Box6]:
    if clip_bounds is None:
        clip_bounds = config.clip_default
    """未旋转 cube + 骨骼链（跳过骨 rotation），与床尾 Geo 静态绘制一致。"""
    data = json.loads(geo_path.read_text(encoding="utf-8"))
    ident = gcb._geometry_identifier(data)
    bone_by_name = gcb._build_bone_by_name(data["minecraft:geometry"][0]["bones"])
    xmin, xmax, ymin, ymax, zmin, zmax = clip_bounds
    out: list[Box6] = []
    for bn, origin, size, rot, pivot in gcb._iter_cubes_from_geo(data):
        m_aabb = gcb._cube_aabb_model_after_bones(
            bone_by_name,
            bn,
            origin,
            size,
            rot,
            pivot,
            ident,
            apply_bone_rotation=False,
        )
        b = gcb._model_aabb_to_block_space(m_aabb)
        clipped = clip_aabb_to_bounds(b[0], b[1], b[2], b[3], b[4], b[5], clip_bounds)
        if clipped is not None:
            out.append(clipped)
    if not out:
        raise ValueError(f"无有效选取盒（unrotated）：{geo_path}")
    return out


def apply_pick_mirror_x_north(
    model_key: str,
    boxes: list[Box6],
    clip_bounds: ClipBounds,
    *,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> list[Box6]:
    if model_key not in config.mirror_x_north_models:
        return boxes
    xmin, xmax, _, _, _, _ = clip_bounds
    return [mirror_x_box(b, xmin, xmax) for b in boxes]


def merge_boxes_union_aabb(boxes: list[Box6]) -> list[Box6]:
    """多盒并成单轴对齐外包盒；空列表原样返回。"""
    if not boxes:
        return []
    x0 = min(b[0] for b in boxes)
    x1 = max(b[1] for b in boxes)
    y0 = min(b[2] for b in boxes)
    y1 = max(b[3] for b in boxes)
    z0 = min(b[4] for b in boxes)
    z1 = max(b[5] for b in boxes)
    return [(x0, x1, y0, y1, z0, z1)]


def apply_pick_merge_union_aabb(
    model_key: str,
    boxes: list[Box6],
    *,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> list[Box6]:
    if model_key not in config.merge_union_aabb_bones:
        return boxes
    return merge_boxes_union_aabb(boxes)


def swap_yz_extents_box(box: Box6) -> Box6:
    """geo 竖放 10×7×2 → 与 solo 一致的平躺 10×2×7 跨度（绕盒心交换 y/z 边长）。"""
    x0, x1, y0, y1, z0, z1 = box
    dy, dz = y1 - y0, z1 - z0
    if dy <= dz + 1e-6:
        return box
    cy = 0.5 * (y0 + y1)
    cz = 0.5 * (z0 + z1)
    hy, hz = 0.5 * dz, 0.5 * dy
    return (x0, x1, cy - hy, cy + hy, cz - hz, cz + hz)


def apply_pick_swap_yz(
    model_key: str,
    boxes: list[Box6],
    clip_bounds: ClipBounds,
    *,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> list[Box6]:
    if model_key not in config.swap_yz_models:
        return boxes
    out: list[Box6] = []
    for b in boxes:
        swapped = swap_yz_extents_box(b)
        clipped = clip_aabb_to_bounds(*swapped, clip_bounds)
        if clipped is not None:
            out.append(clipped)
    return out if out else boxes


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


def mirror_x_box(box: Box6, xmin: float, xmax: float) -> Box6:
    """关于局部坐标竖直面 ``x=(xmin+xmax)/2`` 镜像。"""
    x0, x1, y0, y1, z0, z1 = box
    nx0 = xmin + xmax - x1
    nx1 = xmin + xmax - x0
    if nx0 > nx1:
        nx0, nx1 = nx1, nx0
    return (nx0, nx1, y0, y1, z0, z1)


def snap_half_grid(
    box: Box6,
    bounds: ClipBounds = DEFAULT_PICK_CLIP,
) -> Box6:
    """§3.6.1：各 min/max 四舍五入到最近 0.5，再裁回 ``bounds``（当前管线未调用，保留供日后恢复）。"""
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


def model_key_from_geo(geo_path: Path) -> str:
    """导出文档分节键：geo 文件名（不含 ``.geo.json``）。"""
    name = geo_path.name
    if name.endswith(".geo.json"):
        return name[: -len(".geo.json")]
    return geo_path.stem


def geometry_identifier_from_geo(geo_path: Path) -> str | None:
    try:
        data = json.loads(geo_path.read_text(encoding="utf-8"))
    except OSError:
        return None
    geoms = data.get("minecraft:geometry")
    if not geoms or not isinstance(geoms[0], dict):
        return None
    desc = geoms[0].get("description")
    if not isinstance(desc, dict):
        return None
    ident = desc.get("identifier")
    return ident if isinstance(ident, str) else None


def geo_source_path_for_display(geo_path: Path) -> str:
    try:
        return str(geo_path.resolve().relative_to(ROOT)).replace("\\", "/")
    except ValueError:
        return str(geo_path.resolve()).replace("\\", "/")


def north_pick_boxes_raw_from_geo(
    geo_path: Path,
    *,
    clip_bounds: ClipBounds | None = None,
    bone_names: frozenset[str] | None = None,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> list[Box6]:
    """裁切后、后处理前的选取盒（与 ``north_pick_boxes_from_geo`` 同源）。"""
    if clip_bounds is None:
        clip_bounds = config.clip_default
    model_key = model_key_from_geo(geo_path)
    if model_key in config.unrotated_models:
        raw = north_pick_boxes_unrotated_cubes_from_geo(geo_path, clip_bounds=clip_bounds)
        if bone_names is None:
            return raw
        raise ValueError(f"骨骼过滤与 unrotated 模式不兼容：{geo_path}")
    xmin, xmax, ymin, ymax, zmin, zmax = clip_bounds
    if bone_names is None:
        return list(
            gcb.compute_north_pick_boxes_axis_aligned(
                geo_path,
                xmin=xmin,
                xmax=xmax,
                ymin=ymin,
                ymax=ymax,
                zmin=zmin,
                zmax=zmax,
            )
        )
    data = json.loads(geo_path.read_text(encoding="utf-8"))
    geoms = data.get("minecraft:geometry")
    if not geoms or not isinstance(geoms[0], dict):
        raise ValueError(f"无效 geo：{geo_path}")
    ident = gcb._geometry_identifier(data)
    bone_by_name = gcb._build_bone_by_name(geoms[0].get("bones", []))
    boxes: list[Box6] = []
    for bn, o, s, rot, piv in gcb._iter_cubes_from_geo(data):
        if bn not in bone_names:
            continue
        clipped = gcb._cube_block_aabb_clipped_axis_aligned(
            bone_by_name,
            bn,
            o,
            s,
            rot,
            piv,
            ident,
            xmin=xmin,
            xmax=xmax,
            ymin=ymin,
            ymax=ymax,
            zmin=zmin,
            zmax=zmax,
        )
        if clipped is not None:
            boxes.append(clipped)
    if not boxes:
        raise ValueError(f"骨骼 {sorted(bone_names)} 与裁切盒无相交：{geo_path}")
    return boxes


def _postprocess_pick_boxes(
    model_key: str,
    raw_boxes: list[Box6],
    *,
    clip_bounds: ClipBounds,
    min_extent: float,
    mirror_x: bool,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> list[Box6]:
    xmin, xmax, _, _, _, _ = clip_bounds
    raw_boxes = apply_pick_swap_yz(model_key, raw_boxes, clip_bounds, config=config)
    out: list[Box6] = []
    for b in raw_boxes:
        fixed = ensure_min_extents_bounds(b, clip_bounds, min_extent=min_extent)
        if mirror_x:
            fixed = mirror_x_box(fixed, xmin, xmax)
        out.append(fixed)
    return apply_pick_mirror_x_north(model_key, out, clip_bounds, config=config)


def box_size_xyz(box: Box6) -> tuple[float, float, float]:
    x0, x1, y0, y1, z0, z1 = box
    return (x1 - x0, y1 - y0, z1 - z0)


def format_boxes_markdown_table_origin_size(boxes: list[Box6], *, precision: int = 4) -> str:
    """原点 (x,y,z) + 尺寸 (宽,高,深)，单位 1/16 格；原点为 ``Block.box`` 的 min 角。"""
    if not boxes:
        return "_（无盒）_\n"
    fmt = f"{{:.{precision}f}}"
    lines = [
        "| # | x | y | z | 宽 | 高 | 深 |",
        "|---:|---:|---:|---:|---:|---:|---:|",
    ]
    for i, b in enumerate(boxes, start=1):
        x0, x1, y0, y1, z0, z1 = b
        dx, dy, dz = box_size_xyz(b)
        lines.append(
            "| "
            + " | ".join(
                [
                    str(i),
                    fmt.format(x0),
                    fmt.format(y0),
                    fmt.format(z0),
                    fmt.format(dx),
                    fmt.format(dy),
                    fmt.format(dz),
                ]
            )
            + " |"
        )
    return "\n".join(lines) + "\n"


def format_box_origin_size_line(box: Box6, *, precision: int = 4) -> str:
    x0, x1, y0, y1, z0, z1 = box
    dx, dy, dz = box_size_xyz(box)
    fmt = f"{{:.{precision}f}}"
    return (
        f"原点 ({fmt.format(x0)}, {fmt.format(y0)}, {fmt.format(z0)})，"
        f"尺寸 ({fmt.format(dx)} × {fmt.format(dy)} × {fmt.format(dz)})"
    )


def boxes_equal(a: Box6, b: Box6, eps: float = 1e-4) -> bool:
    return all(abs(x - y) <= eps for x, y in zip(a, b))


def pipeline_changed_raw_vs_final(raw: list[Box6], final: list[Box6]) -> bool:
    if len(raw) != len(final):
        return True
    return any(not boxes_equal(r, f) for r, f in zip(raw, final))


def _stage_a_heading(model_key: str, *, config: PickPipelineConfig = DEFAULT_PICK_CONFIG) -> str:
    if model_key in config.unrotated_models:
        return (
            "### 阶段 A — 未旋转 cube + 骨骼链（`north_pick_boxes_unrotated_cubes_from_geo`）\n\n"
            "> **含义**：仅把 geo cube 变到床尾北向局部坐标并裁切；**未**做最小边长、半格量化、Y/Z 交换、北向 X 镜像。"
        )
    return (
        "### 阶段 A — geo 轴对齐外包盒（`compute_north_pick_boxes_axis_aligned`）\n\n"
        "> **含义**：cube 经骨骼/自身旋转后的**轴对齐外包盒**，再裁到配置范围；**未**做最小边长、半格量化、"
        "Y/Z 交换、北向 X 镜像。斜摆时「高/深」常与视觉厚度不一致，属正常现象。"
    )


def _stage_b_heading() -> str:
    return (
        "### 阶段 B — 最终盒（写入 Java）\n\n"
        "> **含义**：在阶段 A 基础上依次应用：**Y/Z 跨度交换**（若配置）→ **最小边长** → **半格量化** → "
        "CLI `--mirror-x`（若指定）→ **北向 X 镜像**（若配置）→ **单外包盒合并**（若配置）。"
        "下表为**最终结果**。"
    )


def build_markdown_section(
    geo_path: Path,
    *,
    raw_boxes: list[Box6],
    final_boxes: list[Box6],
    clip_bounds: ClipBounds,
    min_extent: float,
    snap_half: bool,
    mirror_x: bool,
    generated_utc: str,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> str:
    model_key = model_key_from_geo(geo_path)
    ident = geometry_identifier_from_geo(geo_path) or "_unknown_"
    short = file_sha256_short(geo_path)
    rel = geo_source_path_for_display(geo_path)
    xmin, xmax, ymin, ymax, zmin, zmax = clip_bounds
    changed = pipeline_changed_raw_vs_final(raw_boxes, final_boxes)
    pick_mode = (
        "未旋转 cube（对齐渲染）"
        if model_key in config.unrotated_models
        else "轴对齐（含骨骼/cube 旋转）"
    )

    lines: list[str] = [
        f"<!-- model: {model_key} -->",
        f"## `{model_key}`",
        "",
        f"- **几何标识符**：`{ident}`",
        f"- **源 geo 路径**：`{rel}`",
        f"- **sha256（前 12 位）**：`{short}`",
        f"- **生成时间（UTC）**：`{generated_utc}`",
        f"- **裁切盒**：x∈[{xmin},{xmax}] y∈[{ymin},{ymax}] z∈[{zmin},{zmax}]（1/16 格）",
        f"- **处理管线**：最小边长 {min_extent}；半格量化 {'是' if snap_half else '否'}；"
        f"CLI `--mirror-x` {'是' if mirror_x else '否'}",
        f"- **后处理是否改变 原始→最终**：{'是' if changed else '否'}",
        f"- **选取模式**：{pick_mode}",
        f"- **Y/Z 跨度交换**：{'是' if model_key in config.swap_yz_models else '否'}",
        f"- **北向 X 镜像（与渲染 +180°Y 对齐）**：{'是' if model_key in config.mirror_x_north_models else '否'}",
        f"- **合并为单外包盒**："
        f"{'是（骨骼 ' + ', '.join(sorted(config.merge_union_aabb_bones[model_key])) + '）' if model_key in config.merge_union_aabb_bones else '否'}",
        "",
        _stage_a_heading(model_key, config=config),
        "",
        format_boxes_markdown_table_origin_size(raw_boxes),
        "",
        _stage_b_heading(),
        "",
        format_boxes_markdown_table_origin_size(final_boxes),
        "",
        "### 最终结果（坐标 + 尺寸 → Java）",
        "",
        "> 原点为 `Block.box` 的 **min 角** (x0,y0,z0)；尺寸为宽×高×深 (Δx,Δy,Δz)。单位：1/16 格。",
        "",
    ]
    for i, b in enumerate(final_boxes, start=1):
        x0, x1, y0, y1, z0, z1 = b
        lines.append(f"{i}. {format_box_origin_size_line(b)}")
        lines.append(
            f"   → `Block.box({x0:.4f}, {y0:.4f}, {z0:.4f}, {x1:.4f}, {y1:.4f}, {z1:.4f})`"
        )
    lines.extend(
        [
            "",
            "> **说明**：斜摆 cube 经欧拉旋转后取轴对齐外包盒，Δy/Δz 常明显大于视觉厚度；"
            "若与游戏中已粘贴片段的溯源 sha 一致，则 Java **未**额外补偿，问题在脚本/旋转链。",
            "",
            f"<!-- /model: {model_key} -->",
            "",
        ]
    )
    return "\n".join(lines)


def _parse_model_sections(text: str) -> dict[str, str]:
    """按 ``<!-- model: key -->`` … ``<!-- /model: key -->`` 解析各节正文（含标记行）。"""
    sections: dict[str, str] = {}
    for m in _MODEL_MARKER_RE.finditer(text):
        key = m.group(1)
        end = _MODEL_END_RE.search(text, m.end())
        if end is None or end.group(1) != key:
            continue
        end_pos = end.end()
        sections[key] = text[m.start() : end_pos].rstrip() + "\n"
    return sections


def _export_md_header(config: PickPipelineConfig = DEFAULT_PICK_CONFIG) -> str:
    md_name = config.default_export_md.name
    return (
        f"# {config.export_md_title}\n\n"
        f"> 由 `{config.export_md_script_ref} --export-md` 维护"
        f"（默认写入 `{md_name}`）。"
        "同一 geo **仅保留最新一节**（按文件名 `model_key` 覆盖）。\n"
        f"{config.export_md_coords_note}\n\n"
        "## 阶段 A / B 区别\n\n"
        "| 阶段 | 内容 | 用途 |\n"
        "|------|------|------|\n"
        f"| **A** | geo → 轴对齐外包盒，再{config.export_md_stage_a_clip_note} | 对照 geo 与旋转后外包盒是否合理；**不是**写入游戏的盒 |\n"
        f"| **B** | A + 最小边长、半格量化、Y/Z 交换、北向镜像等 | **{config.export_md_stage_b_java_ref}** |\n\n"
        "表中 **x,y,z** 为盒原点（min 角），**宽/高/深** 为三轴跨度。\n\n"
    )


def _rebuild_index(sections: dict[str, str]) -> str:
    rows = ["<!-- EXPORT_INDEX -->", "| 模型 | 最后更新 (UTC) |", "|------|----------------|"]
    meta_ts: dict[str, str] = {}
    for key, body in sections.items():
        m = re.search(r"\*\*(?:generated_utc|生成时间（UTC）)\*\*：`([^`]+)`", body)
        meta_ts[key] = m.group(1) if m else "—"
    for key in sorted(sections.keys()):
        rows.append(f"| `{key}` | {meta_ts[key]} |")
    rows.append("<!-- /EXPORT_INDEX -->")
    return "\n".join(rows) + "\n\n"


def upsert_export_markdown(
    md_path: Path,
    model_key: str,
    section_body: str,
    *,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> None:
    """写入或覆盖 ``model_key`` 对应节，并刷新索引表。"""
    if md_path.is_file():
        existing = md_path.read_text(encoding="utf-8")
        sections = _parse_model_sections(existing)
    else:
        sections = {}
    # 移除旧版误用 Path.stem 产生的 ``*.geo`` 重复节
    legacy = f"{model_key}.geo"
    if legacy in sections:
        del sections[legacy]
    sections[model_key] = section_body.rstrip() + "\n"
    index = _rebuild_index(sections)
    body_parts = [sections[k] for k in sorted(sections.keys())]
    md_path.write_text(_export_md_header(config) + index + "\n".join(body_parts), encoding="utf-8")


def export_geo_to_markdown(
    geo_path: Path,
    md_path: Path,
    *,
    min_extent: float = 0.5,
    snap_half: bool = True,
    clip_bounds: ClipBounds | None = None,
    mirror_x: bool = False,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> str:
    """生成并 upsert 单模型 Markdown 节；返回 model_key。"""
    if clip_bounds is None:
        clip_bounds = config.clip_default
    generated_utc = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    raw_boxes = north_pick_boxes_raw_from_geo(geo_path, clip_bounds=clip_bounds, config=config)
    final_boxes = north_pick_boxes_from_geo(
        geo_path,
        min_extent=min_extent,
        snap_half=snap_half,
        clip_bounds=clip_bounds,
        mirror_x=mirror_x,
        config=config,
    )
    model_key = model_key_from_geo(geo_path)
    section = build_markdown_section(
        geo_path,
        raw_boxes=raw_boxes,
        final_boxes=final_boxes,
        clip_bounds=clip_bounds,
        min_extent=min_extent,
        snap_half=snap_half,
        mirror_x=mirror_x,
        generated_utc=generated_utc,
        config=config,
    )
    upsert_export_markdown(md_path, model_key, section, config=config)
    return model_key


def file_sha256_short(path: Path, n: int = 12) -> str:
    h = hashlib.sha256()
    h.update(path.read_bytes())
    return h.hexdigest()[:n]


def provenance_comment_lines(
    geo_path: Path,
    extra: str | None = None,
    *,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> list[str]:
    """溯源：短哈希、时间、路径。"""
    short = file_sha256_short(geo_path)
    ts = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    try:
        rel = geo_path.resolve().relative_to(ROOT)
        path_s = str(rel).replace("\\", "/")
    except ValueError:
        path_s = str(geo_path.resolve()).replace("\\", "/")
    lines = [
        f"// {config.provenance_tag}: sha256[:12]={short} generated_utc={ts}",
        f"// source_geo={path_s}",
    ]
    if extra:
        lines.append(f"// {extra}")
    return lines


def north_pick_boxes_from_geo(
    geo_path: Path,
    *,
    min_extent: float = 0.5,
    snap_half: bool = False,
    clip_bounds: ClipBounds | None = None,
    mirror_x: bool = False,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> list[Box6]:
    """
    读取 geo，返回北向基准、裁切盒内选取盒列表（经最小边长处理；半格量化默认关闭）。

    默认 ``clip_bounds`` 来自 ``config.clip_default``（通用为单格 ``[0,16]³``）。
    """
    if clip_bounds is None:
        clip_bounds = config.clip_default
    model_key = model_key_from_geo(geo_path)
    merge_bones = config.merge_union_aabb_bones.get(model_key)
    if merge_bones is not None:
        raw_boxes = north_pick_boxes_raw_from_geo(
            geo_path, clip_bounds=clip_bounds, bone_names=merge_bones, config=config
        )
    else:
        raw_boxes = north_pick_boxes_raw_from_geo(geo_path, clip_bounds=clip_bounds, config=config)
    out = _postprocess_pick_boxes(
        model_key,
        raw_boxes,
        clip_bounds=clip_bounds,
        min_extent=min_extent,
        mirror_x=mirror_x,
        config=config,
    )
    return apply_pick_merge_union_aabb(model_key, out, config=config)


def emit_java_shapes_or(
    boxes: Iterable[Box6],
    geo_path: Path,
    *,
    precision: int = 4,
    method_name: str = "buildPickShapeNorthUnionGenerated",
    extra_note: str | None = None,
    config: PickPipelineConfig = DEFAULT_PICK_CONFIG,
) -> str:
    """生成 ``Shapes.or`` + ``Block.box`` Java 片段（不含类包装）。"""
    lines: list[str] = []
    lines.extend(provenance_comment_lines(geo_path, extra=extra_note, config=config))
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


def main(*, default_config: PickPipelineConfig | None = None) -> None:
    p = argparse.ArgumentParser(
        description="从 bedrock geo 生成北向选取用 VoxelShape Java 片段（默认裁切单格 [0,16]³）"
    )
    p.add_argument("geo", type=Path, help="geo.json 路径")
    p.add_argument(
        "--preset",
        choices=("bed-plate6",),
        default=None,
        help="应用系列专用裁切/后处理（bed-plate6：床板6 两格深度 z∈[0,32] 等）",
    )
    p.add_argument(
        "--clip",
        type=float,
        nargs=6,
        metavar=("XMIN", "XMAX", "YMIN", "YMAX", "ZMIN", "ZMAX"),
        default=None,
        help="裁切盒 xmin xmax ymin ymax zmin zmax（像素=1/16 格；默认取自 config/preset）",
    )
    p.add_argument(
        "--snap-half",
        action="store_true",
        help="启用 §3.6.1 半格量化（默认关闭）",
    )
    p.add_argument("--min-extent", type=float, default=0.5, help="单盒三轴最小边长（默认 0.5）")
    p.add_argument("--precision", type=int, default=4, help="Java 小数位（默认 4）")
    p.add_argument("--method-name", type=str, default="buildPickShapeNorthUnionGenerated", help="生成的 Java 方法名")
    p.add_argument("--mirror-x", action="store_true", help="输出前按裁切盒 x 中线镜像（用于与渲染手系对齐）")
    p.add_argument(
        "--export-md",
        nargs="?",
        const="",
        default=None,
        metavar="PATH",
        help="将本 geo 写入共用 Markdown；省略 PATH 时用 config 默认路径；同文件名覆盖旧节",
    )
    args = p.parse_args()
    config = default_config or DEFAULT_PICK_CONFIG
    if args.preset:
        config = resolve_preset_config(args.preset)
    geo = args.geo
    if not geo.is_file():
        raise SystemExit(f"文件不存在: {geo}")
    clip_bounds: ClipBounds = tuple(args.clip) if args.clip is not None else config.clip_default
    xmin, xmax, ymin, ymax, zmin, zmax = clip_bounds
    clip_note = f"clip=[{xmin},{xmax}]x[{ymin},{ymax}]x[{zmin},{zmax}] pick-shape"
    boxes = north_pick_boxes_from_geo(
        geo,
        min_extent=args.min_extent,
        snap_half=args.snap_half,
        clip_bounds=clip_bounds,
        mirror_x=args.mirror_x,
        config=config,
    )
    if not boxes:
        raise SystemExit(
            "无有效选取盒（检查 geo 是否与裁切盒相交；可试 --clip 0 16 0 16 0 16 或 --preset bed-plate6）"
        )
    if args.export_md is not None:
        md_path = config.default_export_md if args.export_md == "" else Path(args.export_md)
        key = export_geo_to_markdown(
            geo,
            md_path,
            min_extent=args.min_extent,
            snap_half=args.snap_half,
            clip_bounds=clip_bounds,
            mirror_x=args.mirror_x,
            config=config,
        )
        print(f"// exported markdown section: {key} -> {md_path}", file=sys.stderr)

    text = emit_java_shapes_or(
        boxes,
        geo,
        precision=args.precision,
        method_name=args.method_name,
        extra_note=clip_note,
        config=config,
    )
    print(text, end="")


if __name__ == "__main__":
    main()
