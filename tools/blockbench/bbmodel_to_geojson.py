# -*- coding: utf-8 -*-
"""
将 Blockbench .bbmodel（Java / GeckoLib 项目格式）转为 Bedrock ``minecraft:geometry`` JSON，
供 GeckoLib 读取。

**与 Blockbench 预览 / Bedrock 导出一致**（见 Blockbench 仓库 ``js/formats/bedrock/bedrock.js``）：

- **骨骼**（``compileGroup``）：``pivot.x`` 相对工程内取反；``rotation`` 的 ``rx``、``ry`` 取反，``rz`` 不变。
- **立方体**（``compileCube``）：``origin[0] = -(from.x + size.x)``（与 ``to.x`` 的相反数一致）；``y``/``z`` 取 ``from``。

若根组在 Blockbench 里绕枢轴旋转，省略 ``rotation`` 会导致与预览不一致。

元素级 ``rotation`` / 面 ``rotation`` 未做完整烘焙时，复杂模型请用 Blockbench 官方 Bedrock 导出或插件。

限制（与 ``export_bbmodel_to_fantasy_furniture_assets`` 说明一致）：

- 面的 UV：凡 ``texture`` 已绑定（非 ``null`` / ``false``）且能解析 ``uv`` 即导出，与 Blockbench ``compileCube`` 一致。
  顶/底面在写出前做与 ``bedrock.js`` 相同的角点偏移与 ``uv_size`` 取反（见 ``_face_uv_to_bedrock``）。
"""

from __future__ import annotations

from typing import Any


def _face_uv_to_bedrock(face: dict[str, Any], side: str) -> dict[str, Any] | None:
    """
    Blockbench ``bedrock.js`` ``compileCube``：顶/底面在 ``uv``/``uv_size`` 初值上再平移角点并翻转尺寸。
    """
    tex = face.get("texture")
    if tex is None or tex is False:
        return None

    uv_raw = face.get("uv")
    u0 = v0 = su = sv = None
    if isinstance(uv_raw, (list, tuple)) and len(uv_raw) == 4:
        u1, v1, u2, v2 = (float(x) for x in uv_raw)
        u0, v0, su, sv = u1, v1, u2 - u1, v2 - v1
    elif isinstance(uv_raw, (list, tuple)) and len(uv_raw) == 2:
        us = face.get("uv_size")
        if not isinstance(us, (list, tuple)) or len(us) != 2:
            return None
        u0, v0 = float(uv_raw[0]), float(uv_raw[1])
        su, sv = float(us[0]), float(us[1])
    else:
        return None

    if side in ("up", "down"):
        u0 = u0 + su
        v0 = v0 + sv
        su = -su
        sv = -sv

    out: dict[str, Any] = {"uv": [u0, v0], "uv_size": [su, sv]}
    rot = face.get("rotation")
    if isinstance(rot, (int, float)) and rot != 0:
        out["uv_rotation"] = int(rot) % 360
    return out


def _element_to_cube(el: dict[str, Any]) -> dict[str, Any] | None:
    if el.get("type") != "cube" or not el.get("export", True):
        return None
    f, t = el.get("from"), el.get("to")
    if not isinstance(f, list) or not isinstance(t, list) or len(f) != 3 or len(t) != 3:
        return None
    fx, fy, fz = (float(f[0]), float(f[1]), float(f[2]))
    tx, ty, tz = (float(t[0]), float(t[1]), float(t[2]))
    sx, sy, sz = tx - fx, ty - fy, tz - fz
    # Blockbench bedrock.js compileCube: origin[0] = -(from[0] + size[0])
    origin = [-(fx + sx), fy, fz]
    size = [sx, sy, sz]
    uv_block: dict[str, Any] = {}
    faces = el.get("faces")
    if isinstance(faces, dict):
        for side in ("north", "south", "east", "west", "up", "down"):
            fd = faces.get(side)
            if not isinstance(fd, dict):
                continue
            conv = _face_uv_to_bedrock(fd, side)
            if conv is not None:
                uv_block[side] = conv
    cube: dict[str, Any] = {"origin": origin, "size": size}
    rot = el.get("rotation")
    if isinstance(rot, list) and len(rot) == 3:
        rf = [float(rot[0]), float(rot[1]), float(rot[2])]
        if any(r != 0.0 for r in rf):
            piv = el.get("origin")
            if isinstance(piv, list) and len(piv) == 3:
                cube["pivot"] = [-float(piv[0]), float(piv[1]), float(piv[2])]
            cube["rotation"] = [-rf[0], -rf[1], rf[2]]
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
        px, py, pz = (float(pivot[0]), float(pivot[1]), float(pivot[2]))
        # bedrock.js compileGroup: bone.pivot[0] *= -1
        pivot = [-px, py, pz]

    bone: dict[str, Any] = {"name": bone_name, "pivot": pivot}
    if parent_bone is not None:
        bone["parent"] = parent_bone

    rot = grp.get("rotation")
    if isinstance(rot, list) and len(rot) == 3:
        rx, ry, rz = (float(rot[0]), float(rot[1]), float(rot[2]))
        if any(r != 0.0 for r in (rx, ry, rz)):
            # bedrock.js compileGroup: bone.rotation[0,1] *= -1
            bone["rotation"] = [-rx, -ry, rz]

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
