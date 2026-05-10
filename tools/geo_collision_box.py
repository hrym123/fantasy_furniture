"""
从 Bedrock / GeckoLib 的 geo.json（minecraft:geometry）估算「单格方块」用的轴对齐碰撞箱。

**默认要算什么**
----------------
在**不**加 ``--emit-java`` 时，本脚本输出的是**一个**轴对齐长方体：先把每个 cube 裁到单格内，再对所有裁切盒取
**全局 min/max**，得到**包住整段模型的最小外接盒**（单条 ``Block.box``）。中间镂空、分体式结构会被「糊成」一整块，
体积大于真实并集——这是外接盒的定义，不是 bug。

若需要**多个矩形拼合、保留镂空**的具体碰撞，请用 ``tools/block_collision_detail.py``（逐 cube 列表、JSON、
``orParts`` 等）。本脚本的 ``--emit-java`` 也能生成多盒 ``Shapes.or``，但不做明细报表。

算法概要
--------
**geo（Bedrock / GeckoLib）**

1. 遍历 geometry 下所有 bone 的 cubes（与 Blockbench 导出一致：cube 坐标已在模型空间）。
2. 每个 cube：无 rotation 时用 origin/size 直接得 AABB；有 rotation 时对 8 个角点绕 pivot 做 XYZ 欧拉旋转（度），
   再取轴对齐包围盒（与开发时用于抽奖机的脚本一致）。
3. 再对八个角点施加**所属骨骼及其父链**上的 ``rotation``（绕各自 ``pivot``），**自叶向根**顺序与 GeckoLib /
   Blockbench 对子骨顶点的复合变换一致（旧版若按根→叶施加会与预览不符，并可能导致部分倾角窗裁切交为空）。
   对 ``geometry.plain_glass_window_*`` 且骨骼为**纯 X 倾角**（仅 ``rx`` 非零）时，骨骼链欧拉再取 ``(-rx,-ry,rz)``
   以与 Gecko 读入取反后的顶点一致；斜角 45°（纯 ``ry``）仍按 JSON 字面欧拉计算。
4. 将模型坐标映射到方块内 0～16：x' = x + 8，z' = z + 8，y' = y（与模组内 Pestle/果酱锅等约定一致）。
5. **默认输出**：各 cube 与 **水平单格 [0,16]×[0,16]、竖直不裁顶** 求交（y 可 >16，以包含超高模型），再对所有交盒取 **全局 min/max**，得到外接轴对齐盒（非布尔并集体积）。
6. **``--emit-java``**：对每个裁切盒输出 ``Shapes.or`` 链（真并集），与 ``block_collision_detail.py`` 的 Java 片段类似，但无逐条说明。

**``--mc-block-model``（Java 方块模型 JSON）**

1. 遍历顶层 ``elements`` 的 ``from``/``to``（已为方块 0～16 刻度），无 ``+8`` 映射。
2. 有 ``rotation`` 时对 8 角点绕 ``origin`` 做单轴旋转（与脚本内 ``_rot_x``/``_rot_y``/``_rot_z`` 一致）。
3. 与 geo 相同：各 element 与水平单格裁切、竖直可超高，再外接或 ``--emit-java`` 多盒并集。

注意
----
- 与游戏内骨骼层级动画无关，仅静态几何；旋转顺序若与引擎/GeckoLib 不一致，结果会有误差。
- **外接盒**若几何几乎占满单格，结果会接近 ``Block.box(0,0,0,16,16,16)``，游戏中体感与整格碰撞相似，**不一定是脚本算错**。
- 不对称时需配合 ``org.lanye.fantasy_furniture.block.util.VoxelShapeRotation.rotateYFromNorth(shape, state.getValue(FACING))`` 使用。
- 骨骼层级若与 Blockbench 导出约定不一致（例如非欧拉 XYZ），结果会有误差。

用法（在 fantasy_furniture 仓库根目录）::

    python tools/geo_collision_box.py path/to/model.geo.json

    python tools/geo_collision_box.py src/main/resources/assets/fantasy_furniture/geo/block/lottery_machine.geo.json

    # Java 方块模型（Blockbench 导出的 models/block/*.json，from/to 为 0～16 刻度）
    python tools/geo_collision_box.py src/main/resources/assets/fantasy_furniture/models/block/decorative_screen.json --mc-block-model

    # 实体 GeckoLib geo：估算 EntityType.Builder.sized(width, height)（水平取 xz 外包正方形边长）
    python tools/geo_collision_box.py src/main/resources/assets/fantasy_furniture/geo/entity/sweeper_robot.geo.json --entity-hitbox

可选：``--raw`` 仅打印模型空间并集（映射到方块坐标但未与单格求交），用于排查模型是否超出格子。

逐 cube 明细、骨骼名与 ``orParts`` 片段见 ``tools/block_collision_detail.py``。
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


def _geometry_identifier(data: dict) -> str | None:
    geoms = data.get("minecraft:geometry")
    if not geoms or not isinstance(geoms[0], dict):
        return None
    desc = geoms[0].get("description")
    if not isinstance(desc, dict):
        return None
    ident = desc.get("identifier")
    return ident if isinstance(ident, str) else None


def _plain_glass_window_geo(ident: str | None) -> bool:
    """此类 geo 中「纯 X 倾角」骨骼已按 Blockbench Bedrock 的 rx 符号书写，骨骼链需与 Gecko 读入取反一致。"""
    return ident is not None and ident.startswith("geometry.plain_glass_window_")


def _bone_rotation_deg_for_collision(
    rot_deg: list[float] | tuple[float, ...], geometry_identifier: str | None
) -> tuple[float, float, float]:
    rx, ry, rz = (float(rot_deg[0]), float(rot_deg[1]), float(rot_deg[2]))
    # 仅纯 X 倾角与 Java→Bedrock 的 rx 取反成对；斜角 45° 为纯 Y，仓库 geo 与 Gecko 一致，勿再对 ry 取反（否则线框又偏）。
    if _plain_glass_window_geo(geometry_identifier):
        if abs(ry) < 1e-6 and abs(rz) < 1e-6 and abs(rx) > 1e-6:
            return (-rx, -ry, rz)
    return (rx, ry, rz)


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
    """仅 cube 级旋转、**不含**骨骼链；与 ``_cube_eight_corners_model`` 的 AABB 一致。"""
    pts = _cube_eight_corners_model(origin, size, rotation, pivot)
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    zs = [p[2] for p in pts]
    return (min(xs), max(xs), min(ys), max(ys), min(zs), max(zs))


def _cube_eight_corners_model(
    origin: list[float],
    size: list[float],
    rotation: list[float] | tuple[float, ...] | None,
    pivot: tuple[float, float, float],
) -> list[tuple[float, float, float]]:
    """cube 八个角点（模型空间），先施加 cube 自身 rotation（与 ``_cube_aabb_model`` 一致）。"""
    ox, oy, oz = origin
    sx, sy, sz = size
    corners: list[tuple[float, float, float]] = []
    for dx in (0.0, sx):
        for dy in (0.0, sy):
            for dz in (0.0, sz):
                corners.append((ox + dx, oy + dy, oz + dz))
    if rotation is None:
        return corners
    px, py, pz = pivot
    out: list[tuple[float, float, float]] = []
    for c in corners:
        rel = (c[0] - px, c[1] - py, c[2] - pz)
        rr = _apply_rot_euler_xyz_deg(rel, rotation)
        out.append((rr[0] + px, rr[1] + py, rr[2] + pz))
    return out


def _build_bone_by_name(bones: list) -> dict[str, dict]:
    out: dict[str, dict] = {}
    for bone in bones:
        if isinstance(bone, dict) and isinstance(bone.get("name"), str):
            out[bone["name"]] = bone
    return out


def _bone_chain_root_first(bone_name: str, bone_by_name: dict[str, dict]) -> list[str]:
    """自根到叶（含 ``bone_name``），用于遍历层级；对点的旋转施加须用 ``reversed(...)`` 即叶→根。"""
    up: list[str] = []
    cur: str | None = bone_name
    seen: set[str] = set()
    while cur and cur not in seen:
        seen.add(cur)
        up.append(cur)
        b = bone_by_name.get(cur)
        parent = b.get("parent") if isinstance(b, dict) else None
        cur = parent if isinstance(parent, str) and parent else None
    up.reverse()
    return up


def _rotate_model_point_by_bone_chain(
    p: tuple[float, float, float],
    bone_name: str,
    bone_by_name: dict[str, dict],
    geometry_identifier: str | None,
) -> tuple[float, float, float]:
    """对模型空间点施加 ``bone_name`` 及其祖先骨骼上的 ``rotation``（绕各自 ``pivot``，欧拉顺序同 cube）。

    复合顺序为**叶→根**（与 Gecko / Blockbench：子空间点先经子骨、再经父骨……一致）。
    """
    q = p
    for bn in reversed(_bone_chain_root_first(bone_name, bone_by_name)):
        b = bone_by_name.get(bn)
        if not isinstance(b, dict):
            continue
        rot = b.get("rotation")
        if not isinstance(rot, (list, tuple)) or len(rot) != 3:
            continue
        if all(abs(float(x)) < 1e-9 for x in rot):
            continue
        pivot = tuple(float(x) for x in b.get("pivot", (0.0, 0.0, 0.0)))
        rel = (q[0] - pivot[0], q[1] - pivot[1], q[2] - pivot[2])
        rr = _apply_rot_euler_xyz_deg(rel, _bone_rotation_deg_for_collision(rot, geometry_identifier))
        q = (rr[0] + pivot[0], rr[1] + pivot[1], rr[2] + pivot[2])
    return q


def _cube_aabb_model_after_bones(
    bone_by_name: dict[str, dict],
    bone_name: str,
    origin: list[float],
    size: list[float],
    rotation: list[float] | tuple[float, ...] | None,
    pivot: tuple[float, float, float],
    geometry_identifier: str | None,
) -> tuple[float, float, float, float, float, float]:
    """cube 角点经 cube 旋转后再经骨骼链旋转，取模型空间 AABB。"""
    corners = _cube_eight_corners_model(origin, size, rotation, pivot)
    transformed = [
        _rotate_model_point_by_bone_chain(c, bone_name, bone_by_name, geometry_identifier) for c in corners
    ]
    xs = [p[0] for p in transformed]
    ys = [p[1] for p in transformed]
    zs = [p[2] for p in transformed]
    return (min(xs), max(xs), min(ys), max(ys), min(zs), max(zs))


def _iter_cubes_from_geo(data: dict) -> list[tuple[str, list[float], list[float], list[float] | None, tuple[float, float, float]]]:
    """解析 geometry[0].bones，收集 (bone_name, origin, size, rotation, pivot)。"""
    geoms = data.get("minecraft:geometry")
    if not geoms:
        raise ValueError("缺少 minecraft:geometry")
    bones = geoms[0].get("bones", [])
    out: list[tuple[str, list[float], list[float], list[float] | None, tuple[float, float, float]]] = []
    for bone in bones:
        if not isinstance(bone, dict):
            continue
        bone_name = bone.get("name")
        if not isinstance(bone_name, str):
            continue
        pivot = tuple(bone.get("pivot", [0.0, 8.0, 0.0]))
        for cube in bone.get("cubes", []):
            o = cube["origin"]
            s = cube["size"]
            rot = cube.get("rotation")
            piv = tuple(cube.get("pivot", pivot))
            out.append((bone_name, o, s, rot, piv))
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


def _apply_mc_element_rotation(
    p: tuple[float, float, float],
    origin: tuple[float, float, float],
    axis: str,
    angle_deg: float,
) -> tuple[float, float, float]:
    """
    Java 版方块模型 element.rotation：绕 origin 的单轴旋转（度），
    与 geo 脚本中 _rot_x/_rot_y/_rot_z 的右手系约定一致。
    """
    ox, oy, oz = origin
    rel = (p[0] - ox, p[1] - oy, p[2] - oz)
    rad = math.radians(angle_deg)
    ax = axis.lower()
    if ax == "x":
        rr = _rot_x(rel, rad)
    elif ax == "y":
        rr = _rot_y(rel, rad)
    elif ax == "z":
        rr = _rot_z(rel, rad)
    else:
        raise ValueError(f"不支持的 rotation.axis: {axis!r}")
    return (rr[0] + ox, rr[1] + oy, rr[2] + oz)


def _mc_element_aabb_block_pixels(
    from_: list[float],
    to: list[float],
    rotation: dict | None,
) -> tuple[float, float, float, float, float, float]:
    """单个 element：from/to（方块 0～16 刻度）→ 轴对齐包围盒（同坐标系）。"""
    fx, fy, fz = from_
    tx, ty, tz = to
    x0, x1 = min(fx, tx), max(fx, tx)
    y0, y1 = min(fy, ty), max(fy, ty)
    z0, z1 = min(fz, tz), max(fz, tz)
    corners: list[tuple[float, float, float]] = []
    for cx in (x0, x1):
        for cy in (y0, y1):
            for cz in (z0, z1):
                corners.append((cx, cy, cz))
    if rotation is None:
        pts = corners
    else:
        origin = tuple(float(x) for x in rotation["origin"])
        axis = str(rotation["axis"])
        angle = float(rotation["angle"])
        pts = [_apply_mc_element_rotation(c, origin, axis, angle) for c in corners]
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    zs = [p[2] for p in pts]
    return (min(xs), max(xs), min(ys), max(ys), min(zs), max(zs))


def _iter_elements_mc_block_model(data: dict) -> list[tuple[list[float], list[float], dict | None]]:
    """解析 Blockbench/Java block model 顶层 elements。"""
    elements = data.get("elements")
    if not isinstance(elements, list):
        raise ValueError("不是有效的 Java 方块模型 JSON：缺少 elements 数组")
    out: list[tuple[list[float], list[float], dict | None]] = []
    for el in elements:
        if not isinstance(el, dict):
            continue
        if "from" not in el or "to" not in el:
            continue
        from_ = el["from"]
        to = el["to"]
        rot = el.get("rotation")
        if rot is not None and not isinstance(rot, dict):
            rot = None
        out.append((from_, to, rot))
    if not out:
        raise ValueError("elements 中无可用几何")
    return out


def _mc_element_clipped_box(
    from_: list[float],
    to: list[float],
    rotation: dict | None,
) -> tuple[float, float, float, float, float, float] | None:
    m = _mc_element_aabb_block_pixels(from_, to, rotation)
    bx0, bx1, by0, by1, bz0, bz1 = m[0], m[1], m[2], m[3], m[4], m[5]
    return _intersect_block_xz_single_cell_unbounded_y(bx0, bx1, by0, by1, bz0, bz1)


def compute_north_clipped_boxes_mc_block_model(model_path: Path) -> list[tuple[float, float, float, float, float, float]]:
    data = json.loads(model_path.read_text(encoding="utf-8"))
    boxes: list[tuple[float, float, float, float, float, float]] = []
    for from_, to, rot in _iter_elements_mc_block_model(data):
        clipped = _mc_element_clipped_box(from_, to, rot)
        if clipped is not None:
            boxes.append(clipped)
    if not boxes:
        raise ValueError("没有与单格相交的几何，请检查模型或坐标")
    return boxes


def compute_shape_north_union_mc_block_model(model_path: Path) -> tuple[float, float, float, float, float, float]:
    """Java block model：各 element 裁切后取全局 min/max（外接盒）。"""
    data = json.loads(model_path.read_text(encoding="utf-8"))
    min_x = min_y = min_z = float("inf")
    max_x = max_y = max_z = float("-inf")
    for from_, to, rot in _iter_elements_mc_block_model(data):
        clipped = _mc_element_clipped_box(from_, to, rot)
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
        raise ValueError("没有与单格相交的几何，请检查模型或坐标")
    return (min_x, min_y, min_z, max_x, max_y, max_z)


def compute_raw_mapped_box_mc_block_model(model_path: Path) -> tuple[float, float, float, float, float, float]:
    """全模型各 element 的 AABB 并集，不与单格求交（调试用）。"""
    data = json.loads(model_path.read_text(encoding="utf-8"))
    min_x = min_y = min_z = float("inf")
    max_x = max_y = max_z = float("-inf")
    for from_, to, rot in _iter_elements_mc_block_model(data):
        m = _mc_element_aabb_block_pixels(from_, to, rot)
        bx0, bx1, by0, by1, bz0, bz1 = m[0], m[1], m[2], m[3], m[4], m[5]
        min_x = min(min_x, bx0)
        max_x = max(max_x, bx1)
        min_y = min(min_y, by0)
        max_y = max(max_y, by1)
        min_z = min(min_z, bz0)
        max_z = max(max_z, bz1)
    if min_x is float("inf"):
        raise ValueError("模型中无几何")
    return (min_x, min_y, min_z, max_x, max_y, max_z)


def _intersect_block_xz_single_cell_unbounded_y(
    bx0: float, bx1: float, by0: float, by1: float, bz0: float, bz1: float
) -> tuple[float, float, float, float, float, float] | None:
    """
    与「水平投影落在单格 [0,16]×[0,16] 内」的竖直棱柱求交：x/z 裁到单格，y 允许 >16（模型可高于一格）。
    Minecraft 中 Block.box 的 y 可大于 16，碰撞会延伸到上方相邻空气格。
    """
    x0, x1 = max(0.0, bx0), min(16.0, bx1)
    z0, z1 = max(0.0, bz0), min(16.0, bz1)
    y0, y1 = max(0.0, by0), by1
    if x0 >= x1 or z0 >= z1 or y0 >= y1:
        return None
    return (x0, x1, y0, y1, z0, z1)


def _cube_block_aabb_clipped(
    bone_by_name: dict[str, dict],
    bone_name: str,
    origin: list[float],
    size: list[float],
    rotation: list[float] | None,
    pivot: tuple[float, float, float],
    geometry_identifier: str | None,
) -> tuple[float, float, float, float, float, float] | None:
    """单个 cube：骨骼链 + cube 旋转 → 模型空间 AABB → 方块坐标 → 水平单格 + 竖直可超高。"""
    m = _cube_aabb_model_after_bones(
        bone_by_name, bone_name, origin, size, rotation, pivot, geometry_identifier
    )
    b = _model_aabb_to_block_space(m)
    return _intersect_block_xz_single_cell_unbounded_y(b[0], b[1], b[2], b[3], b[4], b[5])


def compute_north_clipped_boxes(geo_path: Path) -> list[tuple[float, float, float, float, float, float]]:
    """
    各 cube 与单格 [0,16]³ 求交后的轴对齐盒列表（真·并集素材，用于 Shapes.or 组合）。
    旧版 compute_shape_north_union_clipped 仅取全局 min/max，是外接盒而非几何并集，中间空洞会被填满。
    """
    data = json.loads(geo_path.read_text(encoding="utf-8"))
    geoms = data.get("minecraft:geometry")
    ident = _geometry_identifier(data)
    bone_by_name = _build_bone_by_name(geoms[0].get("bones", [])) if geoms else {}
    boxes: list[tuple[float, float, float, float, float, float]] = []
    for bn, o, s, rot, piv in _iter_cubes_from_geo(data):
        clipped = _cube_block_aabb_clipped(bone_by_name, bn, o, s, rot, piv, ident)
        if clipped is not None:
            boxes.append(clipped)
    if not boxes:
        raise ValueError("没有与单格相交的几何，请检查 geo 或坐标约定")
    return boxes


def compute_shape_north_union_clipped(geo_path: Path) -> tuple[float, float, float, float, float, float]:
    """
    返回北向基准外接盒：每个 cube 先与「水平单格 + 竖直无限延伸」棱柱求交（见
    ``_intersect_block_xz_single_cell_unbounded_y``），再对结果取全局 min/max。

    y 方向可大于 16，以匹配高出单格的模型；x/z 仍限制在放置格内。
    """
    data = json.loads(geo_path.read_text(encoding="utf-8"))
    geoms = data.get("minecraft:geometry")
    ident = _geometry_identifier(data)
    bone_by_name = _build_bone_by_name(geoms[0].get("bones", [])) if geoms else {}
    cubes = _iter_cubes_from_geo(data)
    min_x = min_y = min_z = float("inf")
    max_x = max_y = max_z = float("-inf")
    for bn, o, s, rot, piv in cubes:
        clipped = _cube_block_aabb_clipped(bone_by_name, bn, o, s, rot, piv, ident)
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
    geoms = data.get("minecraft:geometry")
    ident = _geometry_identifier(data)
    bone_by_name = _build_bone_by_name(geoms[0].get("bones", [])) if geoms else {}
    cubes = _iter_cubes_from_geo(data)
    min_x = min_y = min_z = float("inf")
    max_x = max_y = max_z = float("-inf")
    for bn, o, s, rot, piv in cubes:
        m = _cube_aabb_model_after_bones(bone_by_name, bn, o, s, rot, piv, ident)
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


def compute_model_space_union(geo_path: Path) -> tuple[float, float, float, float, float, float]:
    """
    全模型各 cube 在 **Bedrock/geo 模型空间** 下的轴对齐并集（不做 x/z+8、不做单格裁切）。
    坐标与 Blockbench 导出一致；通常 16 单位 = 1 方块（与方块碰撞脚本中 Block.box 的 0～16 刻度一致）。
    """
    data = json.loads(geo_path.read_text(encoding="utf-8"))
    geoms = data.get("minecraft:geometry")
    ident = _geometry_identifier(data)
    bone_by_name = _build_bone_by_name(geoms[0].get("bones", [])) if geoms else {}
    cubes = _iter_cubes_from_geo(data)
    min_x = min_y = min_z = float("inf")
    max_x = max_y = max_z = float("-inf")
    for bn, o, s, rot, piv in cubes:
        m = _cube_aabb_model_after_bones(bone_by_name, bn, o, s, rot, piv, ident)
        minx, maxx, miny, maxy, minz, maxz = m
        min_x = min(min_x, minx)
        max_x = max(max_x, maxx)
        min_y = min(min_y, miny)
        max_y = max(max_y, maxy)
        min_z = min(min_z, minz)
        max_z = max(max_z, maxz)
    if min_x is float("inf"):
        raise ValueError("geo 中无 cube")
    return (min_x, min_y, min_z, max_x, max_y, max_z)


def compute_entity_sized_width_height(geo_path: Path) -> tuple[float, float, tuple[float, float, float, float, float, float]]:
    """
    估算 Forge ``EntityType.Builder.sized(width, height)``：
    - ``width``：水平面包住模型 xz 投影所需的**正方形边长**（方块）= max(dx, dz) / 16；
    - ``height``：竖直跨度（方块）= dy / 16。

    实体碰撞在 xz 上为对称正方形，故取 xz 外包较大的边作为边长。
    返回 (width_blocks, height_blocks, model_aabb)。
    """
    min_x, min_y, min_z, max_x, max_y, max_z = compute_model_space_union(geo_path)
    dx = max_x - min_x
    dy = max_y - min_y
    dz = max_z - min_z
    w = max(dx, dz) / 16.0
    h = dy / 16.0
    return (w, h, (min_x, min_y, min_z, max_x, max_y, max_z))


def _fmt_java_box(
    mn: tuple[float, float, float, float, float, float], precision: int = 2
) -> str:
    x0, y0, z0, x1, y1, z1 = mn
    fmt = f"{{:.{precision}f}}"
    parts = [fmt.format(v) for v in (x0, y0, z0, x1, y1, z1)]
    return f"Block.box({', '.join(parts)})"


def main() -> None:
    parser = argparse.ArgumentParser(
        description=(
            "从 geo.json 或 Java 方块模型 JSON 估算单格碰撞：默认输出最小外接单盒（北向基准）；"
            " 多盒明细见 block_collision_detail.py。"
        )
    )
    parser.add_argument("geo", type=Path, help="geo 路径，或配合 --mc-block-model 时传入 models/block/*.json")
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
    parser.add_argument(
        "--entity-hitbox",
        action="store_true",
        help=(
            "按实体 geo 计算 Forge EntityType.Builder.sized(width, height)："
            "模型空间 cube 并集，16 单位=1 方块；width=max(xz跨度)/16，height=y跨度/16"
        ),
    )
    parser.add_argument(
        "--mc-block-model",
        action="store_true",
        help="输入为 Java/Blockbench 方块模型（顶层 elements、from/to 为 0～16），不做 geo 的 x/z+8",
    )
    args = parser.parse_args()
    path = args.geo
    if not path.is_file():
        raise SystemExit(f"文件不存在: {path}")

    if args.entity_hitbox and args.mc_block_model:
        raise SystemExit("--entity-hitbox 仅适用于 geo，不能与 --mc-block-model 同时使用")

    if args.entity_hitbox:
        w, h, aabb = compute_entity_sized_width_height(path)
        min_x, min_y, min_z, max_x, max_y, max_z = aabb
        dx = max_x - min_x
        dy = max_y - min_y
        dz = max_z - min_z
        print("实体碰撞（模型空间 AABB，geo 单位；与方块脚本相同 16 单位 = 1 方块）：")
        print(
            f"  min ({min_x:.{args.precision}f}, {min_y:.{args.precision}f}, {min_z:.{args.precision}f})"
        )
        print(
            f"  max ({max_x:.{args.precision}f}, {max_y:.{args.precision}f}, {max_z:.{args.precision}f})"
        )
        print(f"  跨度 dx={dx:.{args.precision}f}, dy={dy:.{args.precision}f}, dz={dz:.{args.precision}f}")
        print()
        print(
            f"EntityType.Builder...sized({w:.{args.precision}f}f, {h:.{args.precision}f}f)  "
            f"// width=max(dx,dz)/16, height=dy/16"
        )
        print()
        print(
            "说明：与 GeckoLib 渲染原点、动画无关，仅静态几何；若游戏内模型整体有额外平移，可微调 Java 中的数值。"
        )
        return

    if args.emit_java:
        if args.mc_block_model:
            boxes = compute_north_clipped_boxes_mc_block_model(path)
            src = "Java block model"
        else:
            boxes = compute_north_clipped_boxes(path)
            src = "geo"
        print(f"    // 由 tools/geo_collision_box.py --emit-java 自 {src} 生成（每 element/cube 裁切后 Shapes.or）")
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
        if args.mc_block_model:
            box = compute_raw_mapped_box_mc_block_model(path)
        else:
            box = compute_raw_mapped_box(path)
        print("映射后未裁切单格（调试用）：")
    else:
        if args.mc_block_model:
            box = compute_shape_north_union_mc_block_model(path)
        else:
            box = compute_shape_north_union_clipped(path)
        print(
            "北向基准（各 element/cube 水平裁在单格、竖直可超高；外接 AABB，非几何并集体积；镂空请用 --emit-java）："
        )

    x0, y0, z0, x1, y1, z1 = box
    print(f"  min ({x0:.{args.precision}f}, {y0:.{args.precision}f}, {z0:.{args.precision}f})")
    print(f"  max ({x1:.{args.precision}f}, {y1:.{args.precision}f}, {z1:.{args.precision}f})")
    print()
    print(_fmt_java_box((x0, y0, z0, x1, y1, z1), precision=args.precision))
    full = 16.0**3
    vol = max(0.0, x1 - x0) * max(0.0, y1 - y0) * max(0.0, z1 - z0)
    print()
    if y1 > 16.0 + 1e-6:
        print(
            f"说明：max y={y1:.{args.precision}f} > 16，碰撞会延伸到放置格**上方**相邻格（与模型超高一致）。"
        )
    else:
        vol_pct = 100.0 * vol / full
        print(
            f"说明：外接盒体积约占单格立方 {vol_pct:.1f}%（整格立方为 100%）；"
            f"min z={z0:.2f} 表示一侧有空带。"
        )
    print("需要保留模型内部镂空请用 --emit-java。")
    print()
    print("若方块需四向旋转且 xz 不对称，可：")
    print("  block.util.VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, state.getValue(FACING));")


if __name__ == "__main__":
    main()
