#!/usr/bin/env python3
"""
为 PlainWindowBlocks（9 色 × 6 造型方块 + 9 个物品）生成包装模型、blockstate、战利品、标签与语言。

- **不按颜色复制** ``models/block``：54 个方块 id 的 blockstate 直接引用 6 个几何母版；颜色由 ``PlainWindowColors`` + ``tintindex: 0`` 区分；
- 贴图仅保留 9 张：运行 ``tools/plain_window_consolidate_nine_textures.py``（``plain_window_1``…``_9``）；
- 战利品：材质物品 + SNBT ``FFShape``；
- 中文方块名：如「白色窗户」「白色22.5度」「白色斜角」。

在 fantasy_furniture 仓库根目录执行：python tools/generate_plain_window_variant_assets.py
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/fantasy_furniture"
DATA = ROOT / "src/main/resources/data/fantasy_furniture"
MODID = "fantasy_furniture"

MATERIAL_IDS: tuple[str, ...] = (
    "white",
    "cream",
    "rose",
    "mint",
    "sky",
    "lavender",
    "peach",
    "cocoa",
    "silver",
)

MATERIALS: list[tuple[str, str, str]] = [
    ("white", "White", "白色"),
    ("cream", "Cream", "奶油色"),
    ("rose", "Rose", "玫瑰粉"),
    ("mint", "Mint", "薄荷绿"),
    ("sky", "Sky", "天蓝色"),
    ("lavender", "Lavender", "薰衣草紫"),
    ("peach", "Peach", "桃色"),
    ("cocoa", "Cocoa", "可可棕"),
    ("silver", "Silver", "银灰"),
]

SHAPES: list[tuple[str, str]] = [
    ("default", "plain_window"),
    ("y180", "plain_window_y180"),
    ("y22_5", "plain_window_y22_5"),
    ("y45", "plain_window_y45"),
    ("y67_5", "plain_window_y67_5"),
    ("diagonal", "plain_window_diagonal"),
]

SHAPE_QUALIFIER_EN: dict[str, str | None] = {
    "default": None,
    "y180": "180°",
    "y22_5": "22.5°",
    "y45": "45°",
    "y67_5": "67.5°",
    "diagonal": "Diagonal",
}

# 方块中文名：「白色窗户」「白色22.5度」…
SHAPE_BLOCK_ZH: dict[str, str] = {
    "default": "窗户",
    "y180": "180度",
    "y22_5": "22.5度",
    "y45": "45度",
    "y67_5": "67.5度",
    "diagonal": "斜角",
}

SHAPE_TIP_EN: dict[str, str] = {
    "default": "Shape: default",
    "y180": "Shape: 180°",
    "y22_5": "Shape: 22.5°",
    "y45": "Shape: 45°",
    "y67_5": "Shape: 67.5°",
    "diagonal": "Shape: diagonal",
}

SHAPE_TIP_ZH: dict[str, str] = {
    "default": "造型：默认",
    "y180": "造型：180度",
    "y22_5": "造型：22.5度",
    "y45": "造型：45度",
    "y67_5": "造型：67.5度",
    "diagonal": "造型：斜角",
}

OLD_BLOCK_IDS = [
    "plain_window",
    "plain_window_y180",
    "plain_window_y22_5",
    "plain_window_y45",
    "plain_window_y67_5",
    "plain_window_diagonal",
]

GEOMETRY_STEMS = frozenset(s for _, s in SHAPES)


def block_id(mat: str, shape: str) -> str:
    return f"plain_window_{mat}_{shape}"


def material_item_id(mat: str) -> str:
    return f"plain_window_{mat}"


def write_json(path: Path, data: dict | list) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def blockstate_model_entry(geometry_stem: str) -> dict:
    """54 种方块 id 共用 6 个几何模型（不按颜色复制 JSON）。"""
    return {
        "variants": {
            "facing=north": {"model": f"{MODID}:block/{geometry_stem}"},
            "facing=south": {"model": f"{MODID}:block/{geometry_stem}", "y": 180},
            "facing=west": {"model": f"{MODID}:block/{geometry_stem}", "y": 270},
            "facing=east": {"model": f"{MODID}:block/{geometry_stem}", "y": 90},
        }
    }


def delete_obsolete_plain_window_block_models(block_models: Path) -> int:
    """删除除 6 个几何母版外的所有 plain_window*.json（含历史木材 id 包装模型）。"""
    n = 0
    for p in block_models.glob("plain_window*.json"):
        if p.stem in GEOMETRY_STEMS:
            continue
        p.unlink()
        n += 1
    return n


def loot_drop_material_item(mat: str, shape_id: str) -> dict:
    item = material_item_id(mat)
    nbt = f'{{FFShape:"{shape_id}"}}'
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": f"{MODID}:{item}",
                        "functions": [{"function": "minecraft:set_nbt", "tag": nbt}],
                    }
                ],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }
        ],
    }


def purge_plain_window_lang_keys(data: dict) -> None:
    pat = re.compile(r"^block\.[^.]+\.plain_window_|^item\.[^.]+\.plain_window_")
    for k in list(data.keys()):
        if pat.match(k):
            data.pop(k, None)


def block_name_zh(zh_color: str, sid: str) -> str:
    return f"{zh_color}{SHAPE_BLOCK_ZH[sid]}"


def block_name_en(en_color: str, sid: str) -> str:
    q = SHAPE_QUALIFIER_EN[sid]
    if q is None:
        return f"{en_color} Window"
    if sid == "diagonal":
        return f"{en_color} Diagonal"
    return f"{en_color} {q}"


def main() -> None:
    block_models = ASSETS / "models/block"
    blockstates = ASSETS / "blockstates"
    item_models = ASSETS / "models/item"
    loot_blocks = DATA / "loot_tables/blocks"
    lang_en = ASSETS / "lang/en_us.json"
    lang_zh = ASSETS / "lang/zh_cn.json"

    removed = delete_obsolete_plain_window_block_models(block_models)
    if removed:
        print(f"Removed {removed} obsolete plain_window block model json (keep {len(GEOMETRY_STEMS)} geometry stems).")

    all_block_ids: list[str] = []
    for sid, geom in SHAPES:
        geom_path = block_models / f"{geom}.json"
        if not geom_path.is_file():
            raise SystemExit(f"Missing geometry model {geom_path}")
        for mid, _, _ in MATERIALS:
            bid = block_id(mid, sid)
            all_block_ids.append(bid)
            write_json(blockstates / f"{bid}.json", blockstate_model_entry(geom))
            write_json(loot_blocks / f"{bid}.json", loot_drop_material_item(mid, sid))
            legacy_item = item_models / f"{bid}.json"
            if legacy_item.is_file():
                legacy_item.unlink()

    default_geom = "plain_window"
    for mid, _, _ in MATERIALS:
        iid = material_item_id(mid)
        write_json(item_models / f"{iid}.json", {"parent": f"{MODID}:block/{default_geom}"})

    material_item_ids = [material_item_id(mid) for mid, _, _ in MATERIALS]
    write_json(
        DATA / "tags/blocks/plain_windows.json",
        {"replace": False, "values": [f"{MODID}:{i}" for i in all_block_ids]},
    )
    write_json(
        DATA / "tags/items/plain_windows.json",
        {"replace": False, "values": [f"{MODID}:{i}" for i in material_item_ids]},
    )

    for old in OLD_BLOCK_IDS:
        for base in (blockstates, item_models, loot_blocks):
            p = base / f"{old}.json"
            if p.is_file():
                p.unlink()

    def patch_lang_zh() -> None:
        data = json.loads(lang_zh.read_text(encoding="utf-8"))
        for old in OLD_BLOCK_IDS:
            data.pop(f"block.{MODID}.{old}", None)
        purge_plain_window_lang_keys(data)
        for sid, _ in SHAPES:
            for mid, _, zh_name in MATERIALS:
                bid = block_id(mid, sid)
                data[f"block.{MODID}.{bid}"] = block_name_zh(zh_name, sid)
        for mid, _, zh_name in MATERIALS:
            iid = material_item_id(mid)
            data[f"item.{MODID}.{iid}"] = block_name_zh(zh_name, "default")
        for sid in SHAPE_TIP_ZH:
            data[f"tooltip.{MODID}.plain_window.shape.{sid}"] = SHAPE_TIP_ZH[sid]
        data[f"tooltip.{MODID}.plain_window.cycle_hint"] = "潜行并右键已放置的窗户以切换造型（同色内循环角度）"
        lang_zh.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    def patch_lang_en() -> None:
        data = json.loads(lang_en.read_text(encoding="utf-8"))
        for old in OLD_BLOCK_IDS:
            data.pop(f"block.{MODID}.{old}", None)
        purge_plain_window_lang_keys(data)
        for sid, _ in SHAPES:
            for mid, en_name, _ in MATERIALS:
                bid = block_id(mid, sid)
                data[f"block.{MODID}.{bid}"] = block_name_en(en_name, sid)
        for mid, en_name, _ in MATERIALS:
            iid = material_item_id(mid)
            data[f"item.{MODID}.{iid}"] = block_name_en(en_name, "default")
        for sid in SHAPE_TIP_EN:
            data[f"tooltip.{MODID}.plain_window.shape.{sid}"] = SHAPE_TIP_EN[sid]
        data[f"tooltip.{MODID}.plain_window.cycle_hint"] = "Sneak + use placed window to cycle shape (same color)"
        lang_en.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    patch_lang_en()
    patch_lang_zh()

    print(
        f"Wrote {len(all_block_ids)} block variants, {len(material_item_ids)} material items, loot+tags+lang."
    )


if __name__ == "__main__":
    main()
