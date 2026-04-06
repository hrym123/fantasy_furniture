"""
一次性工具：将「整格两米高」的 decorative_screen 方块模型按 y=16（方块模型坐标系中的一半高度）
拆成下半格 / 上半格两个 JSON，供原版式双格高方块（DoubleBlockHalf.LOWER / UPPER）分别引用。

坐标约定（与 Minecraft 方块模型一致）：
- 单格模型使用 0～32 的坐标（与常见 16 倍像素块一致时，整格高度为 32）。
- 本脚本假定全高模型 decorative_screen_full.json 的竖直范围为 0～32：
  - 下半：保留 y∈[0,16) 的部分，坐标不变；
  - 上半：保留 y∈[16,32) 的部分，整体下移 16（dy=-16），使落在上半格方块局部坐标 0～16 内。

运行方式（在仓库 fantasy_furniture 根目录下）：
    python tools/split_screen_model.py

输入：src/main/resources/assets/fantasy_furniture/models/block/decorative_screen_full.json
输出：decorative_screen_lower.json、decorative_screen_upper.json（同目录）。
"""

import json
from pathlib import Path


def clip_el(el: dict, y0: float, y1: float, dy: float):
    """
    将单个 element 的 y 范围裁剪到 [y0, y1)，并把结果中的 y 整体平移 dy。

    - 若 element 在 y 上与 [y0, y1) 无交集，返回 None（该 element 不属于这一半）。
    - from/to 的 y 可能颠倒（from[1] > to[1]），需保持裁剪后相对方向一致。
    - 若存在 rotation.origin，会按规则调整原点 y（见下）；若原点不在裁剪带内，仍按 dy 平移以配合整体移动。

    :param el:  Blockbench 导出的单个 element 字典（含 from / to，可选 rotation）
    :param y0:  裁剪下界（含）
    :param y1:  裁剪上界（不含）
    :param dy:  裁剪后对所有 y 分量施加的平移（上半段为 -16，使模型归一化到 0～16）
    """
    f = el["from"][1]
    t = el["to"][1]
    lo, hi = min(f, t), max(f, t)
    if hi <= y0 or lo >= y1:
        return None
    nlo, nhi = max(lo, y0), min(hi, y1)
    out = json.loads(json.dumps(el))
    if f < t:
        out["from"][1] = nlo + dy
        out["to"][1] = nhi + dy
    else:
        out["from"][1] = nhi + dy
        out["to"][1] = nlo + dy
    if "rotation" in out:
        o = out["rotation"].get("origin")
        if o:
            oy = o[1]
            if y0 <= oy < y1:
                o[1] = oy + dy
            else:
                o[1] = o[1] + dy
    return out


def main():
    """读取 full 模型，拆分并写入 lower / upper 两个文件。"""
    root = Path(__file__).resolve().parents[1]
    p = root / "src/main/resources/assets/fantasy_furniture/models/block/decorative_screen_full.json"
    data = json.loads(p.read_text(encoding="utf-8"))
    # 保留除 elements / groups 外的顶层字段（如 credit、textures、parent 等），两组输出共用
    base = {k: v for k, v in data.items() if k not in ("elements", "groups")}
    elements = data["elements"]

    lower_els = []
    upper_els = []
    for el in elements:
        lo = clip_el(el, 0, 16, 0)
        if lo:
            lower_els.append(lo)
        up = clip_el(el, 16, 32, -16)
        if up:
            upper_els.append(up)

    lower = dict(base)
    lower["elements"] = lower_els
    upper = dict(base)
    upper["elements"] = upper_els

    out_lo = root / "src/main/resources/assets/fantasy_furniture/models/block/decorative_screen_lower.json"
    out_hi = root / "src/main/resources/assets/fantasy_furniture/models/block/decorative_screen_upper.json"
    out_lo.write_text(json.dumps(lower, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
    out_hi.write_text(json.dumps(upper, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8")
    print("lower elements", len(lower_els), "upper elements", len(upper_els))


if __name__ == "__main__":
    main()
