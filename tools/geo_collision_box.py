"""
从 Bedrock / GeckoLib 的 geo.json（minecraft:geometry）估算「单格方块」用的轴对齐碰撞箱。

算法概要
--------
1. 遍历 geometry 下所有 bone 的 cubes（与 Blockbench 导出一致：cube 坐标已在模型空间）。
2. 每个 cube：无 rotation 时用 origin/size 直接得 AABB；有 rotation 时对 8 个角点绕 pivot 做 XYZ 欧拉旋转（度），
   再取轴对齐包围盒（与开发时用于抽奖机的脚本一致）。
3. 将模型坐标映射到方块内 0～16：x' = x + 8，z' = z + 8，y' = y（与模组内 Pestle/果酱锅等约定一致）。
4. **默认输出**：各 cube 裁切后与 [0,16]³ 求交，再对所有交盒取 **全局 min/max**，得到包住全部几何的 **外接轴对齐盒**（不是布尔并集的体积，中间空洞仍会算进「外接盒」里）。
5. 真·并集（保留中间空隙）需用 ``--emit-java`` 生成多盒 ``Shapes.or``。

注意
----
- 与游戏内骨骼层级动画无关，仅静态几何；旋转顺序若与引擎/GeckoLib 不一致，结果会有误差。
- **外接盒**若几何几乎占满单格，结果会接近 ``Block.box(0,0,0,16,16,16)``，游戏中体感与整格碰撞相似，**不一定是脚本算错**。
- 不对称时需配合 ``VoxelShapeRotation.rotateYFromNorth(shape, state.getValue(FACING))`` 使用。

用法（在 fantasy_furniture 仓库根目录）::

    python tools/geo_collision_box.py path/to/model.geo.json

    python tools/geo_collision_box.py src/main/resources/assets/fantasy_furniture/geo/block/lottery_machine.geo.json

可选：``--raw`` 仅打印模型空间并集（映射到方块坐标但未与单格求交），用于排查模型是否超出格子。
"""

from __future__ import annotations

import argparse
import json
import math
from pathlib import Path


def _rot_x(p: tuple[float, float, float], ang: float) -> tuple[float, float, float]:
    c, s = math.cos(ang), math.sin(ang)
    x, y, z = p
    return (x, y * c - z * s, y * s + z * c)


def _rot_y(p: tuple[float, float, float], ang: float) -> tuple[float, float, float]:
    c, s = math.cos(ang), math.sin(ang)
    x, y, z = p
    return (x * c + z * s, y, -x * s + z * c)


def _rot_z(p: tuple[float, float, float], ang: float) -> tuple[float, float, float]:
    c, s = math.cos(ang), math.sin(ang)
    x, y, z = p
    return (x * c - y * s, x * s + y * c, z)


def _apply_rot_euler_xyz_deg(
    v: tuple[float, float, float], rot_deg: list[float] | tuple[float, ...]
) -> tuple[float, float, float]:
    """与历史脚本一致：按 X → Y → Z 顺序施加旋转（度）。"""
    rx, ry, rz = (math.radians(rot_deg[0]), math.radians(rot_deg[1]), math.radians(rot_deg[2]))
    p = v
    p = _rot_x(p, rx)
    p = _rot_y(p, ry)
    p = _rot_z(p, rz)
    return p


def _cube_aabb_model(
    origin: list[float],
    size: list[float],
    rotation: list[float] | tuple[float, ...] | None,
    pivot: tuple[float, float, float],
) -> tuple[float, float, float, float, float, float]:
    ox, oy, oz = origin
    sx, sy, sz = size
    corners: list[tuple[float, float, float]] = []
    for dx in (0.0, sx):
        for dy in (0.0, sy):
            for dz in (0.0, sz):
                corners.append((ox + dx, oy + dy, oz + dz))
    if rotation is None:
        pts = corners
    else:
        px, py, pz = pivot
        pts = []
        for c in corners:
            rel = (c[0] - px, c[1] - py, c[2] - pz)
            rr = _apply_rot_euler_xyz_deg(rel, rotation)
            pts.append((rr[0] + px, rr[1] + py, rr[2] + pz))
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    zs = [p[2] for p in pts]
    return (min(xs), max(xs), min(ys), max(ys), min(zs), max(zs))


def _iter_cubes_from_geo(data: dict) -> list[tuple[list[float], list[float], list[float] | None, tuple[float, float, float]]]:
    """解析 geometry[0].bones，收集 (origin, size, rotation, pivot)。"""
    geoms = data.get("minecraft:geometry")
    if not geoms:
        raise ValueError("缺少 minecraft:geometry")
    bones = geoms[0].get("bones", [])
    out: list[tuple[list[float], list[float], list[float] | None, tuple[float, float, float]]] = []
    for bone in bones:
        pivot = tuple(bone.get("pivot", [0.0, 8.0, 0.0]))
        for cube in bone.get("cubes", []):
            o = cube["origin"]
            s = cube["size"]
            rot = cube.get("rotation")
            piv = tuple(cube.get("pivot", pivot))
            out.append((o, s, rot, piv))
    return out


def _model_aabb_to_block_space(
    mn: tuple[float, float, float, float, float, float],
) -> tuple[float, float, float, float, float, float]:
    """模型空间 AABB → 方块 0～16 坐标（x/z +8，y 不变）。"""
    minx, maxx, miny, maxy, minz, maxz = mn
    return (
        minx + 8.0,
        maxx + 8.0,
        miny,
        maxy,
        minz + 8.0,
        maxz + 8.0,
    )


def _intersect_block(
    bx0: float, bx1: float, by0: float, by1: float, bz0: float, bz1: float
) -> tuple[float, float, float, float, float, float] | None:
    """与单格 [0,16]³ 求交。"""
    x0, x1 = max(0.0, bx0), min(16.0, bx1)
    y0, y1 = max(0.0, by0), min(16.0, by1)
    z0, z1 = max(0.0, bz0), min(16.0, bz1)
    if x0 >= x1 or y0 >= y1 or z0 >= z1:
        return None
    return (x0, x1, y0, y1, z0, z1)


def _cube_block_aabb_clipped(
    origin: list[float],
    size: list[float],
    rotation: list[float] | None,
    pivot: tuple[float, float, float],
) -> tuple[float, float, float, float, float, float] | None:
    """单个 cube：模型空间 AABB → 方块坐标 → 与 [0,16]³ 求交。"""
    m = _cube_aabb_model(origin, size, rotation, pivot)
    b = _model_aabb_to_block_space(m)
    return _intersect_block(b[0], b[1], b[2], b[3], b[4], b[5])


def compute_north_clipped_boxes(geo_path: Path) -> list[tuple[float, float, float, float, float, float]]:
    """
    各 cube 与单格 [0,16]³ 求交后的轴对齐盒列表（真·并集素材，用于 Shapes.or 组合）。
    旧版 compute_shape_north_union_clipped 仅取全局 min/max，是外接盒而非几何并集，中间空洞会被填满。
    """
    data = json.loads(geo_path.read_text(encoding="utf-8"))
    boxes: list[tuple[float, float, float, float, float, float]] = []
    for o, s, rot, piv in _iter_cubes_from_geo(data):
        clipped = _cube_block_aabb_clipped(o, s, rot, piv)
        if clipped is not None:
            boxes.append(clipped)
    if not boxes:
        raise ValueError("没有与单格相交的几何，请检查 geo 或坐标约定")
    return boxes


def compute_shape_north_union_clipped(geo_path: Path) -> tuple[float, float, float, float, float, float]:
    """
    返回北向基准：包住「各 cube 与单格求交后的所有小盒」的最小轴对齐外接盒（与 Block.box 参数一致）。

    实现为对每个裁切盒取 min/max 的全局极值，**等价于几何并集的外接 AABB**，不是并集本身的体积表示。
    模型几乎贴满单格时，外接盒会极度接近整格。
    """
    data = json.loads(geo_path.read_text(encoding="utf-8"))
    cubes = _iter_cubes_from_geo(data)
    min_x = min_y = min_z = float("inf")
    max_x = max_y = max_z = float("-inf")
    for o, s, rot, piv in cubes:
        clipped = _cube_block_aabb_clipped(o, s, rot, piv)
        if clipped is None:
            continue
        x0, x1, y0, y1, z0, z1 = clipped
        min_x = min(min_x, x0)
        max_x = max(max_x, x1)
        min_y = min(min_y, y0)
        max_y = max(max_y, y1)
        min_z = min(min_z, z0)
        max_z = max(max_z, z1)
    if min_x is float("inf"):
        raise ValueError("没有与单格相交的几何，请检查 geo 或坐标约定")
    return (min_x, min_y, min_z, max_x, max_y, max_z)


def compute_raw_mapped_box(geo_path: Path) -> tuple[float, float, float, float, float, float]:
    """全模型并集（模型空间）映射到方块坐标，不与单格求交（用于调试）。"""
    data = json.loads(geo_path.read_text(encoding="utf-8"))
    cubes = _iter_cubes_from_geo(data)
    min_x = min_y = min_z = float("inf")
    max_x = max_y = max_z = float("-inf")
    for o, s, rot, piv in cubes:
        m = _cube_aabb_model(o, s, rot, piv)
        bx0, bx1, by0, by1, bz0, bz1 = _model_aabb_to_block_space(m)
        min_x = min(min_x, bx0)
        max_x = max(max_x, bx1)
        min_y = min(min_y, by0)
        max_y = max(max_y, by1)
        min_z = min(min_z, bz0)
        max_z = max(max_z, bz1)
    if min_x is float("inf"):
        raise ValueError("geo 中无 cube")
    return (min_x, min_y, min_z, max_x, max_y, max_z)


def _fmt_java_box(
    mn: tuple[float, float, float, float, float, float], precision: int = 2
) -> str:
    x0, y0, z0, x1, y1, z1 = mn
    fmt = f"{{:.{precision}f}}"
    parts = [fmt.format(v) for v in (x0, y0, z0, x1, y1, z1)]
    return f"Block.box({', '.join(parts)})"


def main() -> None:
    parser = argparse.ArgumentParser(description="从 geo.json 估算单格方块碰撞箱（北向基准）")
    parser.add_argument("geo", type=Path, help="geo 文件路径，例如 geo/block/foo.geo.json")
    parser.add_argument(
        "--raw",
        action="store_true",
        help="仅打印全模型并集映射到方块坐标后的 AABB（不与单格 [0,16]³ 求交）",
    )
    parser.add_argument(
        "--precision",
        type=int,
        default=2,
        help="输出小数位数（默认 2）",
    )
    parser.add_argument(
        "--emit-java",
        action="store_true",
        help="输出用于 LotteryMachineBlock 的 Java：对每 cube 裁切盒做 Shapes.or（真并集，非外接盒）",
    )
    args = parser.parse_args()
    path = args.geo
    if not path.is_file():
        raise SystemExit(f"文件不存在: {path}")

    if args.emit_java:
        boxes = compute_north_clipped_boxes(path)
        print("    // 由 tools/geo_collision_box.py --emit-java 自 geo 生成（每 cube 裁切后 Shapes.or）")
        print("    private static VoxelShape buildShapeNorthUnion() {")
        print("        VoxelShape s = Shapes.empty();")
        for b in boxes:
            x0, x1, y0, y1, z0, z1 = b
            print(
                f"        s = Shapes.or(s, Block.box({x0:.4f}, {y0:.4f}, {z0:.4f}, {x1:.4f}, {y1:.4f}, {z1:.4f}));"
            )
        print("        return s;")
        print("    }")
        return

    if args.raw:
        box = compute_raw_mapped_box(path)
        print("映射后未裁切单格（调试用）：")
    else:
        box = compute_shape_north_union_clipped(path)
        print("北向基准（各 cube 与单格求交后的外接 AABB，非几何并集；碰撞请用 --emit-java）：")

    x0, y0, z0, x1, y1, z1 = box
    print(f"  min ({x0:.{args.precision}f}, {y0:.{args.precision}f}, {z0:.{args.precision}f})")
    print(f"  max ({x1:.{args.precision}f}, {y1:.{args.precision}f}, {z1:.{args.precision}f})")
    print()
    print(_fmt_java_box((x0, y0, z0, x1, y1, z1), precision=args.precision))
    full = 16.0**3
    vol = max(0.0, x1 - x0) * max(0.0, y1 - y0) * max(0.0, z1 - z0)
    print()
    print(
        f"说明：外接盒体积约占单格的 {100.0 * vol / full:.1f}% "
        f"（整格为 100%；本模型当前 min z={z0:.2f}，即一侧约有 {z0:.2f}/16 厚度的空带，其余方向仍可能满格）。"
    )
    print("若体感「和普通方块一样」，多因外接盒几乎占满一格，而非脚本错误；需要明显镂空碰撞请用 --emit-java。")
    print()
    print("若方块需四向旋转且 xz 不对称，可：")
    print("  VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, state.getValue(FACING));")


if __name__ == "__main__":
    main()
