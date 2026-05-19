#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
床板 6 选取用 ``VoxelShape`` 片段（``--preset bed-plate6`` 的便捷入口）。

实现位于 ``tools/collision/voxel_pick_from_geo.py``；本文件仅绑定床板 6 默认裁切与后处理规则。

用法（在 ``fantasy_furniture`` 仓库根目录）::

    python tools/bed6/bed_plate6_voxel_pick_from_geo.py path/to/bed_plate6_*.geo.json
    python tools/bed6/bed_plate6_voxel_pick_from_geo.py path/to/model.geo.json --export-md

单测::

    python tools/bed6/test_bed_plate6_voxel_pick_from_geo.py
"""

from __future__ import annotations

import sys
from pathlib import Path

_TOOLS_ROOT = Path(__file__).resolve().parent.parent
_COLL = _TOOLS_ROOT / "collision"
for _p in (_TOOLS_ROOT, _COLL):
    if str(_p) not in sys.path:
        sys.path.insert(0, str(_p))

from bed_plate6_pick_config import BED_PLATE6_PICK_CONFIG  # noqa: E402
import voxel_pick_from_geo as _vp  # noqa: E402

# 向后兼容：测试与 Java 溯源仍从此模块导入
BED_PLATE6_PICK_CLIP_DEFAULT = BED_PLATE6_PICK_CONFIG.clip_default
PICK_MIRROR_X_NORTH_MODELS = BED_PLATE6_PICK_CONFIG.mirror_x_north_models
PICK_MERGE_UNION_AABB_BONES = BED_PLATE6_PICK_CONFIG.merge_union_aabb_bones
PICK_UNROTATED_CUBE_MODELS = BED_PLATE6_PICK_CONFIG.unrotated_models
PICK_SWAP_YZ_CUBE_MODELS = BED_PLATE6_PICK_CONFIG.swap_yz_models
DEFAULT_EXPORT_MD = BED_PLATE6_PICK_CONFIG.default_export_md

_CFG = BED_PLATE6_PICK_CONFIG


def _with_config(**kwargs):
    if "config" not in kwargs:
        kwargs["config"] = _CFG
    return kwargs


def north_pick_boxes_unrotated_cubes_from_geo(geo_path, **kwargs):
    return _vp.north_pick_boxes_unrotated_cubes_from_geo(geo_path, **_with_config(**kwargs))


def north_pick_boxes_raw_from_geo(geo_path, **kwargs):
    return _vp.north_pick_boxes_raw_from_geo(geo_path, **_with_config(**kwargs))


def north_pick_boxes_from_geo(geo_path, **kwargs):
    return _vp.north_pick_boxes_from_geo(geo_path, **_with_config(**kwargs))


def export_geo_to_markdown(geo_path, md_path, **kwargs):
    return _vp.export_geo_to_markdown(geo_path, md_path, **_with_config(**kwargs))


def build_markdown_section(*args, **kwargs):
    return _vp.build_markdown_section(*args, **_with_config(**kwargs))


def emit_java_shapes_or(*args, **kwargs):
    return _vp.emit_java_shapes_or(*args, **_with_config(**kwargs))


def provenance_comment_lines(geo_path, extra=None, **kwargs):
    return _vp.provenance_comment_lines(geo_path, extra, **_with_config(**kwargs))


def upsert_export_markdown(md_path, model_key, section_body, **kwargs):
    return _vp.upsert_export_markdown(md_path, model_key, section_body, **_with_config(**kwargs))


# 其余工具函数直接再导出
apply_pick_mirror_x_north = _vp.apply_pick_mirror_x_north
apply_pick_merge_union_aabb = _vp.apply_pick_merge_union_aabb
apply_pick_swap_yz = _vp.apply_pick_swap_yz
clip_aabb_to_bounds = _vp.clip_aabb_to_bounds
clip_aabb_to_cell_full = _vp.clip_aabb_to_cell_full
ensure_min_extents_bounds = _vp.ensure_min_extents_bounds
ensure_min_extents_cell = _vp.ensure_min_extents_cell
mirror_x_box = _vp.mirror_x_box
snap_half_grid = _vp.snap_half_grid
model_key_from_geo = _vp.model_key_from_geo
geometry_identifier_from_geo = _vp.geometry_identifier_from_geo
geo_source_path_for_display = _vp.geo_source_path_for_display
box_size_xyz = _vp.box_size_xyz
format_boxes_markdown_table_origin_size = _vp.format_boxes_markdown_table_origin_size
format_box_origin_size_line = _vp.format_box_origin_size_line
boxes_equal = _vp.boxes_equal
pipeline_changed_raw_vs_final = _vp.pipeline_changed_raw_vs_final
file_sha256_short = _vp.file_sha256_short
merge_boxes_union_aabb = _vp.merge_boxes_union_aabb
swap_yz_extents_box = _vp.swap_yz_extents_box

Box6 = _vp.Box6


def main() -> None:
    _vp.main(default_config=_CFG)


if __name__ == "__main__":
    main()
