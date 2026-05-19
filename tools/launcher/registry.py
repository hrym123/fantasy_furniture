# -*- coding: utf-8 -*-
"""工具分类、表单 schema、命令行拼装（``tools_gui`` / Web 共用）。"""
from __future__ import annotations

import shlex
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Literal

from paths import FF_ROOT, TOOLS_ROOT

T_BLOCKBENCH = TOOLS_ROOT / "blockbench"
T_COLLISION = TOOLS_ROOT / "collision"
T_BED6 = TOOLS_ROOT / "bed6"
T_GLASS = TOOLS_ROOT / "glass"
T_BLOCK_MODEL = TOOLS_ROOT / "block_model"

FieldType = Literal["file", "directory", "text", "number", "bool", "select"]


@dataclass(frozen=True)
class FieldSpec:
    key: str
    type: FieldType
    label: str
    required: bool = False
    default: str | int | bool | None = None
    options: tuple[str, ...] = ()
    min_value: int = 0
    max_value: int = 8
    hint: str = ""


@dataclass(frozen=True)
class ToolSpec:
    id: str
    label: str
    description: str
    fields: tuple[FieldSpec, ...] = ()
    supports_extra_args: bool = False


@dataclass(frozen=True)
class CategorySpec:
    id: str
    label: str
    tools: tuple[ToolSpec, ...]


class CommandBuildError(Exception):
    def __init__(self, message: str, *, field: str | None = None) -> None:
        super().__init__(message)
        self.field = field


def _extra_argv(raw: str) -> list[str]:
    s = raw.strip()
    if not s:
        return []
    try:
        return shlex.split(s, posix=(sys.platform != "win32"))
    except ValueError as e:
        raise CommandBuildError(f"附加参数无法解析: {e}", field="extra_args") from e


def _param_str(params: dict[str, Any], key: str) -> str:
    v = params.get(key)
    if v is None:
        return ""
    return str(v).strip()


def _param_bool(params: dict[str, Any], key: str) -> bool:
    v = params.get(key)
    if isinstance(v, bool):
        return v
    if isinstance(v, str):
        return v.lower() in ("1", "true", "yes", "on")
    return bool(v)


def _param_int(params: dict[str, Any], key: str, default: int) -> int:
    v = params.get(key)
    if v is None or v == "":
        return default
    try:
        return int(v)
    except (TypeError, ValueError) as e:
        raise CommandBuildError(f"{key} 须为整数", field=key) from e


def _path_required(params: dict[str, Any], key: str = "path") -> Path:
    s = _param_str(params, key)
    if not s:
        raise CommandBuildError("请填写或选择输入文件路径。", field=key)
    p = Path(s)
    if not p.is_file():
        raise CommandBuildError(f"文件不存在: {p}", field=key)
    return p


def _path_optional(params: dict[str, Any], key: str) -> Path | None:
    s = _param_str(params, key)
    if not s:
        return None
    return Path(s)


_EXTRA = FieldSpec(
    key="extra_args",
    type="text",
    label="附加参数（shell 规则，可选）",
    hint="例如: --help",
)

_EXPORT_BBMODEL_FIELDS = (
    FieldSpec("path", "file", ".bbmodel", required=True),
    FieldSpec("asset_id", "text", "asset-id", default=""),
    FieldSpec("dry_run", "bool", "仅预览（--dry-run）"),
    FieldSpec(
        "shared",
        "select",
        "共享贴图键",
        default="",
        options=("", "plain_glass_window"),
    ),
    FieldSpec("only_primary", "bool", "仅主贴图槽（--only-primary-texture）"),
    FieldSpec("skip_geo", "bool", "跳过 geo（--skip-geo）"),
    FieldSpec("skip_textures", "bool", "跳过贴图（--skip-textures）"),
    FieldSpec("del_anim", "bool", "无动画时删旧 animation（--delete-stale-animation）"),
    _EXTRA,
)

_GEO_COLLISION_FIELDS = (
    FieldSpec("path", "file", "输入文件", required=True),
    FieldSpec("raw", "bool", "原始并集（--raw）"),
    FieldSpec("emit_java", "bool", "输出 Java（--emit-java）"),
    FieldSpec("entity_hit", "bool", "实体碰撞盒（--entity-hitbox）"),
    FieldSpec("mc_block", "bool", "方块模型 JSON（--mc-block-model）"),
    FieldSpec("precision", "number", "小数位", default=2, min_value=0, max_value=8),
    _EXTRA,
)

_BLOCK_COLLISION_FIELDS = (
    FieldSpec("path", "file", "geo.json", required=True),
    FieldSpec(
        "fmt",
        "select",
        "格式",
        default="text",
        options=("text", "json", "markdown"),
    ),
    FieldSpec("precision", "number", "小数位", default=4, min_value=0, max_value=8),
    FieldSpec("skip_empty", "bool", "跳过空交（--skip-empty）"),
    FieldSpec("java_or", "bool", "Java Shapes.or（--java-or）"),
    FieldSpec("java_parts", "bool", "Java orParts（--java-or-parts）"),
    _EXTRA,
)

_VOXEL_PICK_FIELDS = (
    FieldSpec("path", "file", "geo.json", required=True),
    FieldSpec(
        "preset",
        "select",
        "系列预设",
        default="",
        options=("", "bed-plate6"),
    ),
    FieldSpec("snap_half", "bool", "半格量化（--snap-half）"),
    FieldSpec("min_extent", "text", "min-extent", default="0.5"),
    FieldSpec("precision", "number", "Java 小数位", default=4, min_value=0, max_value=8),
    FieldSpec(
        "method",
        "text",
        "方法名",
        default="buildPickShapeNorthUnionGenerated",
    ),
    _EXTRA,
)

_BBMODEL_OPTIONAL = FieldSpec("bbmodel", "file", ".bbmodel（可选）")
_BBMODEL_OUT_DIR = FieldSpec("out_dir", "directory", "输出目录（可选）")

TOOL_CATALOG: tuple[CategorySpec, ...] = (
    CategorySpec(
        "cat_bb_export",
        "① Blockbench 导出资源",
        (
            ToolSpec(
                "export_bbmodel",
                "bbmodel → geo / 贴图 / 动画",
                "将 .bbmodel 导出到 assets（geo、贴图、动画）。玻璃窗共享贴图需 Pillow/NumPy。",
                _EXPORT_BBMODEL_FIELDS,
                supports_extra_args=True,
            ),
            ToolSpec(
                "export_bed_png",
                "床板6 · 主贴图 PNG",
                "从「床板6」.bbmodel 解码 textures[0] → bed_plate6.png（脚本内默认 MoonStarfish 路径）。",
            ),
            ToolSpec(
                "export_duvet",
                "床板6 · 被单 7 张贴图",
                "从「床板6（被单）」.bbmodel 按 textures[] 顺序导出 bed_plate6_duvet_1..7.png。",
                (_BBMODEL_OPTIONAL, _BBMODEL_OUT_DIR, _EXTRA),
                supports_extra_args=True,
            ),
            ToolSpec(
                "export_duvet_cover",
                "床板6 · 被套贴图",
                "从「床板6（被套）」.bbmodel 导出 bed_plate6_duvet_cover_1..6.png。",
                (_BBMODEL_OPTIONAL, _BBMODEL_OUT_DIR, _EXTRA),
                supports_extra_args=True,
            ),
            ToolSpec(
                "export_pillow_medium",
                "床板6 · 中号枕头贴图",
                "从「床板6枕头（中）」.bbmodel 导出 bed_plate6_pillow_medium_{1..6}.png。",
                (_BBMODEL_OPTIONAL, _EXTRA),
                supports_extra_args=True,
            ),
            ToolSpec(
                "extract_pillow_large",
                "床板6 · 大号枕头贴图",
                "从 MoonStarfish 大号枕头 .bbmodel 批量写出 PNG（脚本内默认路径）。",
            ),
            ToolSpec(
                "duvet_rename",
                "床板6 · 被单纹理中文名",
                "按内嵌贴图主色生成显示名，可选 --write 写回 .bbmodel。",
                (
                    _BBMODEL_OPTIONAL,
                    FieldSpec("write", "bool", "写回 .bbmodel（--write）"),
                    _EXTRA,
                ),
                supports_extra_args=True,
            ),
        ),
    ),
    CategorySpec(
        "cat_blockjson",
        "② 方块模型 JSON",
        (
            ToolSpec(
                "split_screen",
                "屏风 full → lower/upper",
                "读取 decorative_screen_full.json，写出 lower / upper。无参数。",
            ),
        ),
    ),
    CategorySpec(
        "cat_collision",
        "③ Geo / 碰撞",
        (
            ToolSpec(
                "geo_collision",
                "geo / 方块模型 → 外接盒",
                "从 geo.json 或方块模型 JSON 计算外接碰撞盒。",
                _GEO_COLLISION_FIELDS,
                supports_extra_args=True,
            ),
            ToolSpec(
                "block_collision",
                "geo → 多盒明细",
                "逐 cube 多盒碰撞明细。",
                _BLOCK_COLLISION_FIELDS,
                supports_extra_args=True,
            ),
        ),
    ),
    CategorySpec(
        "cat_voxel",
        "④ Geo 选取",
        (
            ToolSpec(
                "voxel_pick",
                "geo → VoxelShape Java",
                "从 geo 生成北向选取用 VoxelShape Java 片段（默认单格裁切 [0,16]³）。",
                _VOXEL_PICK_FIELDS,
                supports_extra_args=True,
            ),
            ToolSpec(
                "bed_voxel",
                "床板6 geo → VoxelShape",
                "等同 voxel_pick + --preset bed-plate6（床尾 z∈[0,32] 等规则）。",
                _VOXEL_PICK_FIELDS,
                supports_extra_args=True,
            ),
            ToolSpec(
                "test_voxel",
                "单元测试 voxel_pick",
                "运行通用与床板6 voxel_pick 单测。",
            ),
        ),
    ),
    CategorySpec(
        "cat_lang",
        "⑤ 语言 / 贴图对照",
        (
            ToolSpec(
                "glass_lang",
                "玻璃窗 · 译名 / 主色",
                "对照玻璃窗贴图主色与译名；可选写回语言文件。",
                (FieldSpec("write", "bool", "写回 zh_cn / en_us（--write）"),),
            ),
            ToolSpec(
                "pillow_lang",
                "床板6 枕头 · 主色对照",
                "按已导出 PNG 打印主色，供核对译名（不写文件）。",
            ),
        ),
    ),
)

_TOOLS_BY_ID: dict[str, ToolSpec] = {
    t.id: t for cat in TOOL_CATALOG for t in cat.tools
}


def get_tool(tool_id: str) -> ToolSpec | None:
    return _TOOLS_BY_ID.get(tool_id)


def catalog_for_api() -> list[dict[str, Any]]:
    out: list[dict[str, Any]] = []
    for cat in TOOL_CATALOG:
        tools = []
        for t in cat.tools:
            tools.append(
                {
                    "id": t.id,
                    "label": t.label,
                    "description": t.description,
                    "supports_extra_args": t.supports_extra_args,
                    "fields": [
                        {
                            "key": f.key,
                            "type": f.type,
                            "label": f.label,
                            "required": f.required,
                            "default": f.default,
                            "options": list(f.options),
                            "min": f.min_value,
                            "max": f.max_value,
                            "hint": f.hint,
                        }
                        for f in t.fields
                    ],
                }
            )
        out.append({"id": cat.id, "label": cat.label, "tools": tools})
    return out


def build_command(tool_id: str, params: dict[str, Any]) -> list[str]:
    """返回传给 ``python -u`` 的脚本路径与参数列表（不含解释器）。"""
    extra = _extra_argv(_param_str(params, "extra_args"))

    if tool_id == "export_bbmodel":
        p = _path_required(params)
        cmd = [str(T_BLOCKBENCH / "export_bbmodel_to_fantasy_furniture_assets.py"), str(p)]
        aid = _param_str(params, "asset_id")
        if aid:
            cmd += ["--asset-id", aid]
        if _param_bool(params, "dry_run"):
            cmd.append("--dry-run")
        sh = _param_str(params, "shared")
        if sh:
            cmd += ["--shared-textures", sh]
        if _param_bool(params, "only_primary"):
            cmd.append("--only-primary-texture")
        if _param_bool(params, "skip_geo"):
            cmd.append("--skip-geo")
        if _param_bool(params, "skip_textures"):
            cmd.append("--skip-textures")
        if _param_bool(params, "del_anim"):
            cmd.append("--delete-stale-animation")
        return cmd + extra

    if tool_id == "split_screen":
        return [str(T_BLOCK_MODEL / "split_screen_model.py")] + extra

    if tool_id == "geo_collision":
        p = _path_required(params)
        cmd = [str(T_COLLISION / "geo_collision_box.py"), str(p)]
        if _param_bool(params, "raw"):
            cmd.append("--raw")
        if _param_bool(params, "emit_java"):
            cmd.append("--emit-java")
        if _param_bool(params, "entity_hit"):
            cmd.append("--entity-hitbox")
        if _param_bool(params, "mc_block"):
            cmd.append("--mc-block-model")
        cmd += ["--precision", str(_param_int(params, "precision", 2))]
        return cmd + extra

    if tool_id == "block_collision":
        p = _path_required(params)
        cmd = [
            str(T_COLLISION / "block_collision_detail.py"),
            str(p),
            "--format",
            _param_str(params, "fmt") or "text",
            "--precision",
            str(_param_int(params, "precision", 4)),
        ]
        if _param_bool(params, "skip_empty"):
            cmd.append("--skip-empty")
        if _param_bool(params, "java_or"):
            cmd.append("--java-or")
        if _param_bool(params, "java_parts"):
            cmd.append("--java-or-parts")
        return cmd + extra

    if tool_id in ("voxel_pick", "bed_voxel"):
        p = _path_required(params)
        cmd = [str(T_COLLISION / "voxel_pick_from_geo.py"), str(p)]
        preset = _param_str(params, "preset")
        if tool_id == "bed_voxel":
            cmd.append("--preset")
            cmd.append("bed-plate6")
        elif preset:
            cmd += ["--preset", preset]
        if _param_bool(params, "snap_half"):
            cmd.append("--snap-half")
        me = _param_str(params, "min_extent")
        if me:
            cmd += ["--min-extent", me]
        cmd += ["--precision", str(_param_int(params, "precision", 4))]
        mn = _param_str(params, "method")
        if mn:
            cmd += ["--method-name", mn]
        return cmd + extra

    if tool_id == "export_duvet":
        cmd = [str(T_BED6 / "export_bed_plate6_duvet_textures_from_bbmodel.py")]
        b = _path_optional(params, "bbmodel")
        o = _path_optional(params, "out_dir")
        if b:
            cmd += ["--bbmodel", str(b)]
        if o:
            cmd += ["--out-dir", str(o)]
        return cmd + extra

    if tool_id == "export_duvet_cover":
        cmd = [str(T_BED6 / "export_bed_plate6_duvet_cover_textures_from_bbmodel.py")]
        b = _path_optional(params, "bbmodel")
        o = _path_optional(params, "out_dir")
        if b:
            cmd += ["--bbmodel", str(b)]
        if o:
            cmd += ["--out-dir", str(o)]
        return cmd + extra

    if tool_id == "export_pillow_medium":
        cmd = [str(T_BED6 / "export_bed_plate6_pillow_medium_textures_from_bbmodel.py")]
        b = _path_optional(params, "bbmodel")
        if b:
            cmd += ["--bbmodel", str(b)]
        return cmd + extra

    if tool_id == "export_bed_png":
        return [str(T_BED6 / "export_bed_plate6_texture_from_bbmodel.py")] + extra

    if tool_id == "extract_pillow_large":
        return [str(T_BED6 / "extract_bed_plate6_pillow_large_textures_from_bbmodel.py")] + extra

    if tool_id == "duvet_rename":
        cmd = [str(T_BED6 / "bed_plate6_duvet_bbmodel_rename_textures_by_color.py")]
        b = _path_optional(params, "bbmodel")
        if b:
            cmd += ["--bbmodel", str(b)]
        if _param_bool(params, "write"):
            cmd.append("--write")
        return cmd + extra

    if tool_id == "glass_lang":
        cmd = [str(T_GLASS / "plain_glass_window_lang_display_colors.py")]
        if _param_bool(params, "write"):
            cmd.append("--write")
        return cmd + extra

    if tool_id == "pillow_lang":
        return [str(T_BED6 / "bed_plate6_pillow_lang_display_colors.py")] + extra

    if tool_id == "test_voxel":
        return [str(TOOLS_ROOT / "test_voxel_pick_all.py")] + extra

    raise CommandBuildError(f"未知工具: {tool_id}")


def full_argv(tool_id: str, params: dict[str, Any]) -> list[str]:
    """含 ``python -u`` 的完整 argv。"""
    return [sys.executable, "-u", *build_command(tool_id, params)]
