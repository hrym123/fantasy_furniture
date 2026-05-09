# -*- coding: utf-8 -*-
"""
将 Blockbench .bbmodel（Java / GeckoLib 项目格式）转为 Bedrock ``minecraft:geometry`` JSON，
供 GeckoLib 读取。

与旧版 MoonStarfish 脚本的主要差异：**会写入分组骨骼的 ``rotation``**。若根组在 Blockbench 里
绕枢轴旋转（如普通窗 90° 的 ``[90,0,0]``、斜角窗的 ``[0,45,0]``），省略该字段会导致与
Blockbench 预览不一致。

限制（与 ``export_bbmodel_to_fantasy_furniture_assets`` 说明一致）：

- 每个面的 UV 仅当 ``texture == 0`` 时导出；其它槽位需合并 atlas 或用官方 GeckoLib 插件导出。
- 元素级 ``rotation`` / 面 ``rotation`` 未做完整烘焙，复杂模型请用插件。
"""

from __future__ import annotations

from typing import Any


def _face_to_bedrock(face: dict[str, Any]) -> dict[str, Any] | None:
    if face.get("texture", 0) != 0:
        return None
    uv = face.get("uv")
    if not isinstance(uv, (list, tuple)) or len(uv) != 4:
        return None
    u1, v1, u2, v2 = (float(x) for x in uv)
    return {"uv": [u1, v1], "uv_size": [u2 - u1, v2 - v1]}


def _element_to_cube(el: dict[str, Any]) -> dict[str, Any] | None:
    if el.get("type") != "cube" or not el.get("export", True):
        return None
    f, t = el.get("from"), el.get("to")
    if not isinstance(f, list) or not isinstance(t, list) or len(f) != 3 or len(t) != 3:
        return None
    origin = [float(f[0]), float(f[1]), float(f[2])]
    size = [float(t[i]) - float(f[i]) for i in range(3)]
    uv_block: dict[str, Any] = {}
    faces = el.get("faces")
    if isinstance(faces, dict):
        for side in ("north", "south", "east", "west", "up", "down"):
            fd = faces.get(side)
            if not isinstance(fd, dict):
                continue
            conv = _face_to_bedrock(fd)
            if conv is not None:
                uv_block[side] = conv
    cube: dict[str, Any] = {"origin": origin, "size": size}
    if uv_block:
        cube["uv"] = uv_block
    return cube


def _process_outline_node(
    node: Any,
    parent_bone: str | None,
    bones: list[dict[str, Any]],
    cubes_by_bone: dict[str, list[dict[str, Any]]],
    elements_by_uuid: dict[str, dict[str, Any]],
    groups_by_uuid: dict[str, dict[str, Any]],
) -> None:
    if isinstance(node, str):
        el = elements_by_uuid.get(node)
        if el is None or parent_bone is None:
            return
        cube = _element_to_cube(el)
        if cube is not None:
            cubes_by_bone.setdefault(parent_bone, []).append(cube)
        return

    if not isinstance(node, dict):
        return

    gid = node.get("uuid")
    if not isinstance(gid, str):
        return
    grp = groups_by_uuid.get(gid)
    if grp is None:
        return

    bone_name = str(grp.get("name") or "bone")
    pivot = grp.get("origin")
    if not isinstance(pivot, list) or len(pivot) != 3:
        pivot = [0.0, 0.0, 0.0]
    else:
        pivot = [float(pivot[0]), float(pivot[1]), float(pivot[2])]

    bone: dict[str, Any] = {"name": bone_name, "pivot": pivot}
    if parent_bone is not None:
        bone["parent"] = parent_bone

    rot = grp.get("rotation")
    if isinstance(rot, list) and len(rot) == 3:
        rf = [float(rot[0]), float(rot[1]), float(rot[2])]
        if any(r != 0.0 for r in rf):
            bone["rotation"] = rf

    bones.append(bone)

    for ch in node.get("children") or []:
        _process_outline_node(ch, bone_name, bones, cubes_by_bone, elements_by_uuid, groups_by_uuid)


def bbmodel_to_geo(
    data: dict[str, Any],
    *,
    format_version: str = "1.21.110",
    geometry_prefix: str = "geometry.",
) -> dict[str, Any]:
    mid = data.get("model_identifier")
    if not isinstance(mid, str) or not mid.strip():
        raise ValueError("bbmodel 缺少 model_identifier（导出脚本会写入 asset_id）")

    res = data.get("resolution") or {}
    tw = int(res.get("width", 16))
    th = int(res.get("height", 16))

    elements = data.get("elements")
    if not isinstance(elements, list):
        raise ValueError("bbmodel 缺少 elements 数组")

    elements_by_uuid: dict[str, dict[str, Any]] = {}
    for el in elements:
        if isinstance(el, dict):
            u = el.get("uuid")
            if isinstance(u, str):
                elements_by_uuid[u] = el

    groups = data.get("groups")
    if not isinstance(groups, list):
        groups = []

    groups_by_uuid: dict[str, dict[str, Any]] = {}
    for g in groups:
        if isinstance(g, dict):
            u = g.get("uuid")
            if isinstance(u, str):
                groups_by_uuid[u] = g

    bones: list[dict[str, Any]] = [{"name": "bb_main", "pivot": [0, 0, 0], "cubes": []}]
    cubes_by_bone: dict[str, list[dict[str, Any]]] = {}

    outliner = data.get("outliner")
    if isinstance(outliner, list):
        for root in outliner:
            _process_outline_node(root, None, bones, cubes_by_bone, elements_by_uuid, groups_by_uuid)

    for b in bones:
        name = b["name"]
        if name == "bb_main":
            continue
        b["cubes"] = cubes_by_bone.get(name, [])

    desc: dict[str, Any] = {
        "identifier": f"{geometry_prefix}{mid}",
        "texture_width": tw,
        "texture_height": th,
        "visible_bounds_width": 1,
        "visible_bounds_height": 1,
        "visible_bounds_offset": [0, 0, 0],
    }

    return {
        "format_version": format_version,
        "minecraft:geometry": [{"description": desc, "bones": bones}],
    }
