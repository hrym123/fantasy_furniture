# -*- coding: utf-8 -*-
"""床板 6 选取管线专用配置（供 ``voxel_pick_from_geo --preset bed-plate6`` 与床板封装脚本使用）。"""
from __future__ import annotations

import sys
from pathlib import Path

_TOOLS_ROOT = Path(__file__).resolve().parent.parent
_COLL = _TOOLS_ROOT / "collision"
if str(_COLL) not in sys.path:
    sys.path.insert(0, str(_COLL))

from voxel_pick_from_geo import ClipBounds, PickPipelineConfig  # noqa: E402

BED6_DIR = Path(__file__).resolve().parent

# 床尾方块局部坐标（北向基准 geo）；枕头 geo 常在 z≈16..32
BED_PLATE6_PICK_CLIP: ClipBounds = (0.0, 16.0, 0.0, 16.0, 0.0, 32.0)

BED_PLATE6_PICK_CONFIG = PickPipelineConfig(
    clip_default=BED_PLATE6_PICK_CLIP,
    mirror_x_north_models=frozenset(
        {
            "bed_plate6_pillow_medium_pair_front",
            "bed_plate6_pillow_medium_pair_rear",
            "bed_plate6_pillow_small_stack",
        }
    ),
    merge_union_aabb_bones={
        "bed_plate6_duvet_cover": frozenset({"group4"}),
    },
    default_export_md=BED6_DIR / "床板6-选取体素导出.md",
    provenance_tag="bed_plate6_voxel_pick_from_geo",
    export_md_title="床板 6 选取体素导出",
    export_md_script_ref="tools/bed6/bed_plate6_voxel_pick_from_geo.py",
    export_md_coords_note="坐标为床尾北向局部 **1/16 格**；默认裁切 z∈[0,32]。",
    export_md_stage_a_clip_note="裁到床体范围",
    export_md_stage_b_java_ref="**写入 `BedPlate6PickShapesNorth` 的最终盒**",
)
