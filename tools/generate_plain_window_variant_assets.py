#!/usr/bin/env python3
"""
为 PlainWindowBlocks（9 材质 × 6 造型）生成客户端与数据包资源。
列表与标签内顺序为 **造型（模型）→ 材质**，与 Java 中 ``PlainWindowBlocks.entries()`` / 创造栏一致。

- 包装方块模型 parent 到既有几何母版（不覆盖 plain_window / plain_window_y45 等完整 JSON）；
- blockstate、item、loot_table、#plain_windows 标签；
- 更新 en_us / zh_cn 语言并移除旧版 6 id 的翻译键。

在 fantasy_furniture 仓库根目录执行：python tools/generate_plain_window_variant_assets.py
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/fantasy_furniture"
DATA = ROOT / "src/main/resources/data/fantasy_furniture"
MODID = "fantasy_furniture"

MATERIALS: list[tuple[str, str, str]] = [
    ("oak", "Oak", "橡木"),
    ("spruce", "Spruce", "云杉"),
    ("birch", "Birch", "白桦"),
    ("jungle", "Jungle", "丛林木"),
    ("acacia", "Acacia", "金合欢"),
    ("dark_oak", "Dark Oak", "深色橡木"),
    ("mangrove", "Mangrove", "红树"),
    ("cherry", "Cherry", "樱花木"),
    ("bamboo", "Bamboo", "竹"),
]

# (shape_id, geometry_filename_without_json)
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

SHAPE_SUFFIX_ZH: dict[str, str] = {
    "default": "普通窗户",
    "y180": "普通窗户（180度）",
    "y22_5": "普通窗户（22.5度）",
    "y45": "普通窗户（45度）",
    "y67_5": "普通窗户（67.5度）",
    "diagonal": "普通窗户（斜角）",
}

OLD_BLOCK_IDS = [
    "plain_window",
    "plain_window_y180",
    "plain_window_y22_5",
    "plain_window_y45",
    "plain_window_y67_5",
    "plain_window_diagonal",
]


def block_id(mat: str, shape: str) -> str:
    return f"plain_window_{mat}_{shape}"


def write_json(path: Path, data: dict | list) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def blockstate_model_entry(wrapper_name: str) -> dict:
    return {
        "variants": {
            "facing=north": {"model": f"{MODID}:block/{wrapper_name}"},
            "facing=south": {"model": f"{MODID}:block/{wrapper_name}", "y": 180},
            "facing=west": {"model": f"{MODID}:block/{wrapper_name}", "y": 270},
            "facing=east": {"model": f"{MODID}:block/{wrapper_name}", "y": 90},
        }
    }


def loot_table(block_registry_name: str) -> dict:
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "entries": [{"type": "minecraft:item", "name": f"{MODID}:{block_registry_name}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }
        ],
    }


def main() -> None:
    block_models = ASSETS / "models/block"
    blockstates = ASSETS / "blockstates"
    item_models = ASSETS / "models/item"
    loot_blocks = DATA / "loot_tables/blocks"
    lang_en = ASSETS / "lang/en_us.json"
    lang_zh = ASSETS / "lang/zh_cn.json"

    all_ids: list[str] = []
    for sid, geom in SHAPES:
        for mid, _, _ in MATERIALS:
            bid = block_id(mid, sid)
            all_ids.append(bid)
            parent = f"{MODID}:block/{geom}"
            write_json(block_models / f"{bid}.json", {"parent": parent})
            write_json(blockstates / f"{bid}.json", blockstate_model_entry(bid))
            write_json(item_models / f"{bid}.json", {"parent": f"{MODID}:block/{bid}"})
            write_json(loot_blocks / f"{bid}.json", loot_table(bid))

    tag_blocks = DATA / "tags/blocks/plain_windows.json"
    tag_items = DATA / "tags/items/plain_windows.json"
    write_json(tag_blocks, {"replace": False, "values": [f"{MODID}:{i}" for i in all_ids]})
    write_json(tag_items, {"replace": False, "values": [f"{MODID}:{i}" for i in all_ids]})

    for old in OLD_BLOCK_IDS:
        for base in (blockstates, item_models, loot_blocks):
            p = base / f"{old}.json"
            if p.is_file():
                p.unlink()

    def patch_lang_zh() -> None:
        data = json.loads(lang_zh.read_text(encoding="utf-8"))
        for old in OLD_BLOCK_IDS:
            data.pop(f"block.{MODID}.{old}", None)
        for sid, _ in SHAPES:
            for mid, _, zh_name in MATERIALS:
                bid = block_id(mid, sid)
                key = f"block.{MODID}.{bid}"
                if sid == "default":
                    data[key] = f"{zh_name}普通窗户"
                else:
                    inner = SHAPE_SUFFIX_ZH[sid].replace("普通窗户", "")
                    data[key] = f"{zh_name}普通窗户{inner}"
        lang_zh.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    def patch_lang_en() -> None:
        data = json.loads(lang_en.read_text(encoding="utf-8"))
        for old in OLD_BLOCK_IDS:
            data.pop(f"block.{MODID}.{old}", None)
        for sid, _ in SHAPES:
            for mid, en_name, _ in MATERIALS:
                bid = block_id(mid, sid)
                key = f"block.{MODID}.{bid}"
                q = SHAPE_QUALIFIER_EN[sid]
                if q is None:
                    data[key] = f"{en_name} Plain Window"
                else:
                    data[key] = f"{en_name} Plain Window ({q})"
        lang_en.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    patch_lang_en()
    patch_lang_zh()

    print(f"Wrote {len(all_ids)} plain window variants + tags; removed old 6 blockstate/item/loot files.")


if __name__ == "__main__":
    main()
