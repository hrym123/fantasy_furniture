#!/usr/bin/env python3
"""
将 MoonStarfish「普通窗户」Blockbench **工程** JSON（java_block，内嵌 PNG）一键转为模组资源：

- 写出 ``textures/block/{prefix}_{slot}.png``（同名纹理 id 在工程内多条时，与 Blockbench 画布一致：**末条覆盖**）；
- 写出 ``models/block/{prefix}.json``，对齐 Blockbench 菜单「导出 → Minecraft 方块模型」的常见规则：
  - 面 UV：由工程 ``uv_width``×``uv_height`` 画布坐标换算为 MC 使用的 **16 基准** 坐标（``u *= 16/uw``, ``v *= 16/vh``）；
  - ``elements`` 顺序：按 ``outliner`` 深度优先遍历（与 Blockbench 导出顺序一致，而非 ``elements`` 数组下标）；
  - ``rotation`` 键序 ``angle, axis, origin``；``from``/``to`` 不钳位；
  - ``display`` / ``groups`` 自工程保留（含 ``on_shelf`` 等）；会对 ``firstperson_*`` 的 ``scale`` 做修正：若 Z 为负（BB 镜像），改为 ``|Z|``，避免翻绕序导致 cutout 下手持“背面盖正面”；不写 ``ambientocclusion``（与 BB 导出一致，游戏默认 true）；
  - ``credit`` / ``format_version`` 与 BB 导出风格一致。

唯一输入为工程 JSON；**不读取** ``*_对比用`` 文件。

注册表 9×6 变体的包装模型 / blockstate / 语言等由 ``tools/generate_plain_window_variant_assets.py`` 生成。
贴图策略为「仅 9 张」时，在导出后执行 ``tools/plain_window_consolidate_nine_textures.py``（统一为 ``plain_window_1``…``_9``）。
"""
from __future__ import annotations

import base64
import copy
import json
from pathlib import Path

MOON = Path(r"d:\warehouse\MoonStarfish素材\普通窗户")
ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/fantasy_furniture"
TEX_OUT = ASSETS / "textures/block"
MODEL_OUT = ASSETS / "models/block"

PAIRS = [
    ("plain_window.json", "plain_window"),
    ("plain_window_y180.json", "plain_window_y180"),
    ("plain_window_y22_5.json", "plain_window_y22_5"),
    ("plain_window_y45.json", "plain_window_y45"),
    ("plain_window_y67_5.json", "plain_window_y67_5"),
    ("plain_window_diagonal.json", "plain_window_diagonal"),
]


def decode_texture_png(tex: dict) -> bytes | None:
    src = tex.get("source") or ""
    if not src.startswith("data:image/png;base64,"):
        return None
    b64 = src.split(",", 1)[1]
    return base64.b64decode(b64)


def _json_angle(a: float) -> float | int:
    """MC 方块模型旋转角常用 22.5 的倍数；写整数避免部分解析器对 45.0 与层烘焙异常。"""
    r = round(a, 5)
    if abs(r - round(r)) < 1e-4:
        return int(round(r))
    return r


def rotation_to_mc(
    rot: list | None,
    origin: list | None,
    *,
    key_order: str = "mc",
) -> dict | None:
    if not rot or len(rot) < 3:
        return None
    rx, ry, rz = float(rot[0]), float(rot[1]), float(rot[2])
    o = origin if origin and len(origin) == 3 else [8.0, 8.0, 8.0]
    o = [float(x) for x in o]
    if abs(rx) > 1e-6 and abs(ry) < 1e-6 and abs(rz) < 1e-6:
        axis, angle = "x", _json_angle(rx)
    elif abs(ry) > 1e-6 and abs(rx) < 1e-6 and abs(rz) < 1e-6:
        axis, angle = "y", _json_angle(ry)
    elif abs(rz) > 1e-6 and abs(rx) < 1e-6 and abs(ry) < 1e-6:
        axis, angle = "z", _json_angle(rz)
    elif abs(rx) >= abs(ry) and abs(rx) >= abs(rz):
        axis, angle = "x", _json_angle(rx)
    elif abs(ry) >= abs(rz):
        axis, angle = "y", _json_angle(ry)
    else:
        axis, angle = "z", _json_angle(rz)

    if key_order == "bb":
        return {"angle": angle, "axis": axis, "origin": o}
    return {"origin": o, "axis": axis, "angle": angle}


def _fit_axis_to_unit_cube(lo: float, hi: float, bound: float = 16.0) -> tuple[float, float]:
    """将 [lo,hi] 移入 [0,bound]；若跨度 > bound 则先以中点收缩到 bound，再平移夹紧。避免独立钳角导致 to==from 退化。"""
    if lo > hi:
        lo, hi = hi, lo
    span = hi - lo
    if span < 1e-6:
        hi = lo + 1e-3
        span = hi - lo
    if span > bound:
        mid = (lo + hi) / 2.0
        lo = mid - bound / 2.0
        hi = mid + bound / 2.0
    if lo < 0.0:
        hi -= lo
        lo = 0.0
    if hi > bound:
        lo -= hi - bound
        hi = bound
    lo = max(0.0, min(lo, bound))
    hi = max(0.0, min(hi, bound))
    if hi <= lo + 1e-6:
        hi = min(bound, lo + 1e-3)
    return lo, hi


def clamp_box_to_block_bounds(fr: list, to: list) -> tuple[list, list]:
    """原版方块 JSON：未旋转 AABB 落在 [0,16]^3。越界易在 Forge translucent 下花屏（错误 atlas 采样）。"""
    a = [float(fr[i]) for i in range(3)]
    b = [float(to[i]) for i in range(3)]
    lo = [min(a[i], b[i]) for i in range(3)]
    hi = [max(a[i], b[i]) for i in range(3)]
    nlo = []
    nhi = []
    for i in range(3):
        l, h = _fit_axis_to_unit_cube(lo[i], hi[i])
        nlo.append(l)
        nhi.append(h)

    def _json_num(x: float) -> float | int:
        r = round(x, 5)
        if abs(r - round(r)) < 1e-4:
            return int(round(r))
        return r

    return [_json_num(x) for x in nlo], [_json_num(x) for x in nhi]


def resolve_bb_texture_ref(tref, textures_arr: list) -> str:
    """Blockbench java_block：数字 texture 优先匹配槽位 id；若无同名 id 再按 textures[] 下标。"""
    if isinstance(tref, bool):
        return str(int(tref))
    if not isinstance(tref, (int, float)):
        return str(tref)
    fi = float(tref)
    if abs(fi - round(fi)) >= 1e-6:
        return str(tref)
    n = int(round(fi))
    key = str(n)
    for tex in textures_arr:
        tid = tex.get("id")
        if tid is not None and str(tid) == key:
            return str(tid)
    if 0 <= n < len(textures_arr):
        slot = textures_arr[n].get("id")
        if slot is not None:
            return str(slot)
    return key


def _from_to_unclamped(fr: list, to: list) -> tuple[list, list]:
    def _json_num(x: float) -> float | int:
        r = round(x, 5)
        if abs(r - round(r)) < 1e-4:
            return int(round(r))
        return r

    return (
        [_json_num(float(fr[i])) for i in range(3)],
        [_json_num(float(to[i])) for i in range(3)],
    )


def _json_num_uv(x: float) -> float | int:
    r = round(x, 5)
    if abs(r - round(r)) < 1e-4:
        return int(round(r))
    return r


def texture_canvas_for_id(tid_s: str, textures_arr: list, res: dict) -> tuple[int, int]:
    """单张纹理在工程里的 UV 画布尺寸（Blockbench 用其将像素 UV 换到 16 基准 MC UV）。"""
    for tex in textures_arr:
        if str(tex.get("id")) == str(tid_s):
            uw = int(tex.get("uv_width") or res.get("width") or tex.get("width") or 16)
            uh = int(tex.get("uv_height") or res.get("height") or tex.get("height") or 16)
            return max(1, uw), max(1, uh)
    rw = int(res.get("width") or 16)
    rh = int(res.get("height") or 16)
    return max(1, rw), max(1, rh)


def project_uv_to_mc_export_uv(uv: list, uw: int, uh: int) -> list:
    """与 Blockbench Java 方块导出一致：在 ``texture_size`` 仍为 uw×uh 的前提下，面 UV 使用 16 基准坐标。"""
    if not isinstance(uv, list) or len(uv) < 4:
        return uv
    su, sv = 16.0 / float(uw), 16.0 / float(uh)
    u1, v1, u2, v2 = (float(uv[0]), float(uv[1]), float(uv[2]), float(uv[3]))
    return [
        _json_num_uv(u1 * su),
        _json_num_uv(v1 * sv),
        _json_num_uv(u2 * su),
        _json_num_uv(v2 * sv),
    ]


def _walk_outliner_node(node, uuid_to_i: dict[str, int], out: list[int]) -> None:
    if isinstance(node, str):
        if node in uuid_to_i:
            out.append(uuid_to_i[node])
    elif isinstance(node, dict):
        for ch in node.get("children") or []:
            _walk_outliner_node(ch, uuid_to_i, out)


def element_export_indices(raw: dict, elements: list) -> list[int]:
    """与 Blockbench 导出顺序一致：outliner 深度优先，仅含可导出的立方体。"""
    uuid_to_i: dict[str, int] = {}
    for i, el in enumerate(elements):
        uid = el.get("uuid")
        if isinstance(uid, str):
            uuid_to_i[uid] = i

    exportable = {i for i, el in enumerate(elements) if el.get("type") == "cube" or "from" in el}

    ordered: list[int] = []
    for root in raw.get("outliner") or []:
        if isinstance(root, dict):
            for ch in root.get("children") or []:
                _walk_outliner_node(ch, uuid_to_i, ordered)

    seen: set[int] = set()
    result: list[int] = []
    for i in ordered:
        if i in exportable and i not in seen:
            result.append(i)
            seen.add(i)
    for i in range(len(elements)):
        if i in exportable and i not in seen:
            result.append(i)
    return result


def sanitize_display_for_mc_cutout_hand(display: dict | None) -> dict:
    """
    Blockbench 有时在 firstperson 使用负 Z scale 做镜像，会反转三角绕序；
    与 RenderType.cutout 的背面剔除组合后，手持易与「半透明排序错乱」观感相似。
    """
    if not isinstance(display, dict):
        return {}
    out = copy.deepcopy(display)
    for key in ("firstperson_righthand", "firstperson_lefthand"):
        slot = out.get(key)
        if not isinstance(slot, dict):
            continue
        sc = slot.get("scale")
        if isinstance(sc, list) and len(sc) >= 3:
            try:
                sz = float(sc[2])
            except (TypeError, ValueError):
                continue
            if sz < 0:
                slot["scale"] = [sc[0], sc[1], abs(sz)]
    return out


def build_mc_groups(raw: dict, element_count: int) -> list | None:
    groups = raw.get("groups") or []
    if not groups:
        return None
    g0 = groups[0]
    origin = g0.get("origin") or [8, 8, 8]
    if isinstance(origin, list) and len(origin) == 3:
        origin_mc = [_json_num_uv(float(x)) for x in origin]  # type: ignore[arg-type]
    else:
        origin_mc = [8, 8, 8]
    return [
        {
            "name": g0.get("name", "group"),
            "origin": origin_mc,
            "scope": int(g0.get("scope", 0)),
            "color": int(g0.get("color", 0)),
            "children": list(range(element_count)),
        }
    ]


def convert_elements(
    elements: list,
    textures_arr: list,
    *,
    element_indices: list[int],
    res: dict,
    clamp: bool = False,
    rotation_key_order: str = "bb",
) -> list:
    out: list[dict] = []
    for i in element_indices:
        if i < 0 or i >= len(elements):
            continue
        el = elements[i]
        if el.get("type") != "cube" and "from" not in el:
            continue
        if clamp:
            fr, to = clamp_box_to_block_bounds(el["from"], el["to"])
        else:
            fr, to = _from_to_unclamped(el["from"], el["to"])
        cube: dict = {"from": fr, "to": to}
        rot = el.get("rotation")
        origin = el.get("origin")
        if isinstance(rot, list):
            mc_rot = rotation_to_mc(rot, origin, key_order=rotation_key_order)
            if mc_rot:
                cube["rotation"] = mc_rot
        faces_out: dict = {}
        for face_name, face in (el.get("faces") or {}).items():
            tid_s = resolve_bb_texture_ref(face.get("texture"), textures_arr)
            uw, uh = texture_canvas_for_id(tid_s, textures_arr, res)
            raw_uv = face.get("uv") or [0, 0, 0, 0]
            mc_uv = project_uv_to_mc_export_uv(raw_uv, uw, uh)
            faces_out[face_name] = {"uv": mc_uv, "texture": f"#{tid_s}", "tintindex": 0}
        cube["faces"] = faces_out
        out.append(cube)
    return out


def process_file(bb_name: str, prefix: str) -> None:
    raw = json.loads((MOON / bb_name).read_text(encoding="utf-8"))
    elements = raw.get("elements") or []
    textures_arr = raw.get("textures") or []

    TEX_OUT.mkdir(parents=True, exist_ok=True)
    MODEL_OUT.mkdir(parents=True, exist_ok=True)

    tex_map: dict[str, str] = {}
    particle_key: str | None = None
    dup_texture_ids = 0

    for tex in textures_arr:
        tid = str(tex.get("id", "0"))
        png = decode_texture_png(tex)
        if not png:
            continue
        # 同名 id 多条时：与 Blockbench 对重复槽的常见表现一致，以后出现的内嵌图覆盖磁盘 PNG（「保留首次」会使 plain_window 双 0 槽与 BB 预览不一致）。
        if tid in tex_map:
            dup_texture_ids += 1
        fname = f"{prefix}_{tid}.png"
        out_png = TEX_OUT / fname
        out_png.write_bytes(png)
        tex_map[tid] = f"fantasy_furniture:block/{prefix}_{tid}"
        if tex.get("particle"):
            particle_key = tid

    if not tex_map:
        raise SystemExit(f"No textures decoded for {bb_name}")

    if particle_key is None:
        particle_key = next(iter(tex_map.keys()))

    res = raw.get("resolution") or {}
    tex0 = next((t for t in textures_arr if decode_texture_png(t)), textures_arr[0] if textures_arr else {})
    tw = int(tex0.get("uv_width") or res.get("width") or tex0.get("width") or 16)
    th = int(tex0.get("uv_height") or res.get("height") or tex0.get("height") or 16)

    textures_obj = {k: v for k, v in tex_map.items()}
    textures_obj["particle"] = tex_map[particle_key]

    order = element_export_indices(raw, elements)
    mc_elements = convert_elements(
        elements,
        textures_arr,
        element_indices=order,
        res=res,
        clamp=False,
        rotation_key_order="bb",
    )

    fmt_ver = raw.get("java_block_version")
    if fmt_ver is not None and not isinstance(fmt_ver, str):
        fmt_ver = str(fmt_ver)

    mc_model: dict = {
        "format_version": fmt_ver or "1.9.0",
        "credit": "Made with Blockbench",
        "texture_size": [tw, th],
        "textures": textures_obj,
        "elements": mc_elements,
        "display": sanitize_display_for_mc_cutout_hand(raw.get("display")),
    }
    groups_mc = build_mc_groups(raw, len(mc_elements))
    if groups_mc is not None:
        mc_model["groups"] = groups_mc
    # 与 Blockbench 导出一致：不写 ambientocclusion（游戏默认 true）；勿写 render_type（半透明由客户端层注册）。

    out_path = MODEL_OUT / f"{prefix}.json"
    out_path.write_text(json.dumps(mc_model, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
    extra = f", duplicate_texture_slots_overwritten={dup_texture_ids}" if dup_texture_ids else ""
    print("Wrote", out_path, "textures", len(tex_map), extra)


def main() -> None:
    if not MOON.is_dir():
        raise SystemExit(f"Missing MoonStarfish folder: {MOON}")
    for bb_name, prefix in PAIRS:
        process_file(bb_name, prefix)
    print("Done.")


if __name__ == "__main__":
    main()
