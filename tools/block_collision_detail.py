"""
从 geo.json（minecraft:geometry）计算**实体 / 方块模型在单格内的具体碰撞**：按每个 cube 得到一个轴对齐矩形，
整体是**多个 ``Block.box`` 经 ``Shapes.or`` / ``orParts`` 拼成的复杂几何**（可保留镂空与凹凸，不是一整块实心长方体）。

与 ``geo_collision_box.py`` 的分工
--------------------------------
- **本脚本（``block_collision_detail.py``）**：输出**多矩形并集**的素材——逐条列出裁切后的盒、骨骼名、体积、
  JSON/Markdown，并可生成与游戏代码一致的 ``orParts(...)`` / ``Shapes.or`` 链。用于需要**贴合模型轮廓**的碰撞
  （如卡座多段、带缝家具）。
- **``geo_collision_box.py``（默认模式）**：把裁切后的所有部分再取**全局 min/max**，得到**唯一一个**能包住
  整个模型的**最小外接轴对齐长方体**（单盒 ``Block.box``）。适合抽奖机等「一个粗外接盒就够」或先看占位范围；
  该脚本也有 ``--emit-java`` 可吐多盒，但**不做**本脚本这样的逐 cube 报表与多格式导出。

几何约定（两脚本一致）
----------------------
- Blockbench / GeckoLib 的 cube：origin、size、rotation、pivot；
- 映射到方块坐标：``x' = x + 8``, ``z' = z + 8``, ``y' = y``；
- 与 **水平单格 [0,16]×[0,16]、竖直不裁顶** 求交（与模组内已有方块一致）。

用法（在仓库根目录）::

    python tools/block_collision_detail.py src/main/resources/assets/fantasy_furniture/geo/block/banquette_corner.geo.json

    python tools/block_collision_detail.py path/to/model.geo.json --format json

    python tools/block_collision_detail.py path/to/model.geo.json --java-or
"""

from __future__ import annotations

import argparse
import json
import math
import sys
from pathlib import Path
from typing import Any, Iterator

# 与同目录 geo_collision_box 共用几何实现，避免两套公式漂移
_TOOLS_DIR = Path(__file__).resolve().parent
if str(_TOOLS_DIR) not in sys.path:
    sys.path.insert(0, str(_TOOLS_DIR))

from geo_collision_box import _cube_block_aabb_clipped  # noqa: E402


def _volume(
    box: tuple[float, float, float, float, float, float] | None,
) -> float:
    if box is None:
        return 0.0
    # 与 geo_collision_box 中单盒一致：(xmin, xmax, ymin, ymax, zmin, zmax)
    xmin, xmax, ymin, ymax, zmin, zmax = box
    return max(0.0, xmax - xmin) * max(0.0, ymax - ymin) * max(0.0, zmax - zmin)


def _box_to_min_max_xyz(
    box: tuple[float, float, float, float, float, float],
) -> tuple[tuple[float, float, float], tuple[float, float, float]]:
    xmin, xmax, ymin, ymax, zmin, zmax = box
    return (xmin, ymin, zmin), (xmax, ymax, zmax)


def _fmt_box_java(
    box: tuple[float, float, float, float, float, float],
    precision: int,
) -> str:
    """box 为 (xmin, xmax, ymin, ymax, zmin, zmax) → ``Block.box(minX,minY,minZ,maxX,maxY,maxZ)``。"""
    xmin, xmax, ymin, ymax, zmin, zmax = box
    f = f"{{:.{precision}f}}"
    return f"Block.box({f.format(xmin)}, {f.format(ymin)}, {f.format(zmin)}, {f.format(xmax)}, {f.format(ymax)}, {f.format(zmax)})"


def iter_clipped_cubes(
    data: dict[str, Any],
) -> Iterator[
    tuple[
        str,
        int,
        list[float],
        list[float],
        list[float] | tuple[float, ...] | None,
        tuple[float, float, float],
        tuple[float, float, float, float, float, float] | None,
    ]
]:
    """Yield (bone_name, cube_index, origin, size, rotation, pivot_used, clipped_block_aabb_or_None)."""
    geoms = data.get("minecraft:geometry")
    if not geoms:
        raise ValueError("缺少 minecraft:geometry")
    bones = geoms[0].get("bones", [])
    for bi, bone in enumerate(bones):
        bone_name = bone.get("name", f"bone_{bi}")
        pivot_bone = tuple(bone.get("pivot", [0.0, 8.0, 0.0]))
        for ci, cube in enumerate(bone.get("cubes", [])):
            o = cube["origin"]
            s = cube["size"]
            rot = cube.get("rotation")
            piv = tuple(cube.get("pivot", pivot_bone))
            clipped = _cube_block_aabb_clipped(o, s, rot, piv)
            yield bone_name, ci, o, s, rot, piv, clipped


def outer_bounds_from_clipped(
    boxes: list[tuple[float, float, float, float, float, float]],
) -> tuple[float, float, float, float, float, float]:
    """输入各盒 (xmin,xmax,ymin,ymax,zmin,zmax)，返回全局外接 (minX,minY,minZ,maxX,maxY,maxZ)。"""
    min_x = min_y = min_z = float("inf")
    max_x = max_y = max_z = float("-inf")
    for xmin, xmax, ymin, ymax, zmin, zmax in boxes:
        min_x = min(min_x, xmin)
        max_x = max(max_x, xmax)
        min_y = min(min_y, ymin)
        max_y = max(max_y, ymax)
        min_z = min(min_z, zmin)
        max_z = max(max_z, zmax)
    if min_x is float("inf"):
        raise ValueError("没有与单格相交的 cube")
    return (min_x, min_y, min_z, max_x, max_y, max_z)


def run_report(
    geo_path: Path,
    fmt: str,
    precision: int,
    skip_empty: bool,
    java_or: bool,
    java_or_parts: bool,
) -> None:
    data = json.loads(geo_path.read_text(encoding="utf-8"))
    rows: list[dict[str, Any]] = []
    clipped_list: list[tuple[float, float, float, float, float, float]] = []

    for bone_name, ci, o, s, rot, piv, clipped in iter_clipped_cubes(data):
        if skip_empty and clipped is None:
            continue
        vol = _volume(clipped)
        row: dict[str, Any] = {
            "bone": bone_name,
            "cube_index": ci,
            "origin": o,
            "size": s,
            "rotation": list(rot) if rot is not None else None,
            "pivot": list(piv),
            # 与 geo_collision_box 裁切盒一致：[xmin,xmax,ymin,ymax,zmin,zmax]
            "clipped_block": (
                list(clipped) if clipped is not None else None
            ),
            "volume": round(vol, 6) if clipped is not None else 0.0,
            "block_box_java": (
                _fmt_box_java(clipped, precision) if clipped is not None else None
            ),
        }
        if clipped is not None:
            mn, mx = _box_to_min_max_xyz(clipped)
            row["clipped_min_xyz"] = [round(mn[0], precision), round(mn[1], precision), round(mn[2], precision)]
            row["clipped_max_xyz"] = [round(mx[0], precision), round(mx[1], precision), round(mx[2], precision)]
        rows.append(row)
        if clipped is not None:
            clipped_list.append(clipped)

    if fmt == "json":
        out = {
            "geo": str(geo_path.as_posix()),
            "cube_count": len(rows),
            "cubes": rows,
        }
        if clipped_list:
            ob = outer_bounds_from_clipped(clipped_list)
            out["outer_aabb_block"] = {
                "min_xyz": [round(ob[0], precision), round(ob[1], precision), round(ob[2], precision)],
                "max_xyz": [round(ob[3], precision), round(ob[4], precision), round(ob[5], precision)],
            }
        print(json.dumps(out, ensure_ascii=False, indent=2))
        return

    if fmt == "markdown":
        print(f"# 碰撞明细：`{geo_path.name}`\n")
        print("| 骨骼 | # | 裁切后方块 AABB (min→max) | 体积 | Java |")
        print("|------|---|---------------------------|------|------|")
        for row in rows:
            cb = row["clipped_block"]
            if cb is None:
                aabb = "∅（与单格无交）"
            else:
                mn, mx = _box_to_min_max_xyz(
                    tuple(cb)  # type: ignore[arg-type]
                )
                aabb = (
                    f"({mn[0]:.{precision}f},{mn[1]:.{precision}f},{mn[2]:.{precision}f})"
                    f"→({mx[0]:.{precision}f},{mx[1]:.{precision}f},{mx[2]:.{precision}f})"
                )
            java = row["block_box_java"] or "—"
            print(
                f"| {row['bone']} | {row['cube_index']} | {aabb} | {row['volume']:.4f} | `{java}` |"
            )
        if clipped_list:
            ob = outer_bounds_from_clipped(clipped_list)
            print("\n**外接 AABB（非几何并集）**")
            print(
                f"- min_xyz ({ob[0]:.{precision}f}, {ob[1]:.{precision}f}, {ob[2]:.{precision}f})\n"
                f"- max_xyz ({ob[3]:.{precision}f}, {ob[4]:.{precision}f}, {ob[5]:.{precision}f})"
            )
        return

    # text (default)
    print(f"文件: {geo_path}")
    print(f"cube 条数（含与单格无交）: {len(rows)}")
    if skip_empty:
        print("（已跳过与单格无交的 cube）")
    print()
    for row in rows:
        print(f"--- {row['bone']}  cube[{row['cube_index']}] ---")
        print(f"  origin: {row['origin']}  size: {row['size']}")
        if row["rotation"] is not None:
            print(f"  rotation: {row['rotation']}  pivot: {row['pivot']}")
        if row["clipped_block"] is None:
            print("  裁切后: （与水平单格无交，已丢弃）")
        else:
            cb = tuple(row["clipped_block"])  # type: ignore[arg-type]
            mn, mx = _box_to_min_max_xyz(cb)
            print(
                f"  裁切后方块空间: min ({mn[0]:.{precision}f}, {mn[1]:.{precision}f}, {mn[2]:.{precision}f})  "
                f"max ({mx[0]:.{precision}f}, {mx[1]:.{precision}f}, {mx[2]:.{precision}f})"
            )
            print(f"  体积: {row['volume']:.4f}")
            print(f"  {row['block_box_java']}")
        print()

    if clipped_list:
        ob = outer_bounds_from_clipped(clipped_list)
        print("=== 外接 AABB（各盒 min/max 包络，非 Shapes 布尔并集体积）===")
        print(
            f"  min_xyz ({ob[0]:.{precision}f}, {ob[1]:.{precision}f}, {ob[2]:.{precision}f})\n"
            f"  max_xyz ({ob[3]:.{precision}f}, {ob[4]:.{precision}f}, {ob[5]:.{precision}f})"
        )
        print(f"  {_fmt_box_java((ob[0], ob[3], ob[1], ob[4], ob[2], ob[5]), precision)}")
        print()

    total_v = sum(
        _volume(tuple(r["clipped_block"]))  # type: ignore[arg-type]
        for r in rows
        if r["clipped_block"] is not None
    )
    union_note = (
        "几何并集体积 ≤ 上列体积之和；有重叠时应用 Shapes.or 而非相加。"
    )
    print(f"有交 cube 体积之和（近似上界）: {total_v:.4f}  — {union_note}")

    if java_or and clipped_list:
        print()
        print("// --- Shapes.or 链（Forge/Vanilla）---")
        print("VoxelShape s = Shapes.empty();")
        for b in clipped_list:
            xmin, xmax, ymin, ymax, zmin, zmax = b
            print(
                f"s = Shapes.or(s, Block.box({xmin:.{precision}f}f, {ymin:.{precision}f}f, {zmin:.{precision}f}f, "
                f"{xmax:.{precision}f}f, {ymax:.{precision}f}f, {zmax:.{precision}f}f));"
            )

    if java_or_parts and clipped_list:
        print()
        print("// --- orParts 风格（首盒 + varargs）---")
        lines = [_fmt_box_java(b, precision) for b in clipped_list]
        if len(lines) == 1:
            print(f"orParts({lines[0]})")
        else:
            print(f"orParts(\n        {lines[0]},")
            for line in lines[1:-1]:
                print(f"        {line},")
            print(f"        {lines[-1]});")


def main() -> None:
    parser = argparse.ArgumentParser(
        description=(
            "实体/方块模型碰撞明细：多矩形并集（逐 cube），非单盒外接。"
            " 单盒外包见 geo_collision_box.py。"
        )
    )
    parser.add_argument("geo", type=Path, help="geo 文件路径")
    parser.add_argument(
        "--format",
        "-f",
        choices=("text", "json", "markdown"),
        default="text",
        help="输出格式（默认 text）",
    )
    parser.add_argument(
        "--precision",
        type=int,
        default=4,
        help="小数位数（默认 4）",
    )
    parser.add_argument(
        "--skip-empty",
        action="store_true",
        help="不输出与单格水平无交的 cube",
    )
    parser.add_argument(
        "--java-or",
        action="store_true",
        help="在 text/markdown 之后额外打印 Shapes.or 的 Java 片段",
    )
    parser.add_argument(
        "--java-or-parts",
        action="store_true",
        help="额外打印 orParts(Block.box(...), ...) 风格（与 BanquetteBlock 等一致）",
    )
    args = parser.parse_args()
    path = args.geo
    if not path.is_file():
        raise SystemExit(f"文件不存在: {path}")

    run_report(
        path,
        fmt=args.format,
        precision=args.precision,
        skip_empty=args.skip_empty,
        java_or=args.java_or,
        java_or_parts=args.java_or_parts,
    )


if __name__ == "__main__":
    main()
