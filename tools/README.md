# `fantasy_furniture/tools` 开发辅助脚本一览

本目录为「幻想家具」模组配套 Python 工具。多数脚本假定在仓库 **`fantasy_furniture` 根目录** 下执行（`python tools/<子目录>/<脚本>.py …`），具体参数与示例以各文件顶部 **docstring** 为准。

**子目录（按职责）**

| 目录 | 对应分类 | 内容 |
|------|-----------|------|
| [`blockbench/`](blockbench/) | ① | `.bbmodel` → geo / 贴图 / 动画；含 `bbmodel_to_geojson.py`（库，无 CLI） |
| [`block_model/`](block_model/) | ② | 原版方块模型 JSON 拆分等 |
| [`collision/`](collision/) | ③ | `geo` / 方块模型 → 碰撞外接盒、多盒明细 |
| [`bed6/`](bed6/) | ④⑤ | 床板 6：选取 `VoxelShape`、贴图导出、被单命名、枕头主色对照、单测 |
| [`glass/`](glass/) | ⑥ | 普通玻璃窗：贴图 stem 库、语言/主色对照 |
| （根目录） | ⑦ | [`paths.py`](paths.py) 路径常量、[`tools_gui.py`](tools_gui.py) 图形启动器、本 README |

图形启动器：在仓库根执行 `python tools/tools_gui.py`。

---

## 分类索引

| 分类 | 路径 | 脚本 | 角色 |
|------|------|------|------|
| **① Blockbench → Gecko 资源** | `tools/blockbench/` | [`export_bbmodel_to_fantasy_furniture_assets.py`](blockbench/export_bbmodel_to_fantasy_furniture_assets.py) | 入口：`.bbmodel` → `geo` / 贴图 / 动画 |
| 同上（被引用） | `tools/blockbench/` | [`bbmodel_to_geojson.py`](blockbench/bbmodel_to_geojson.py) | **库**：无独立 CLI |
| **② 原版方块模型 JSON** | `tools/block_model/` | [`split_screen_model.py`](block_model/split_screen_model.py) | 双格屏风 lower / upper |
| **③ Geo / 模型 → 碰撞** | `tools/collision/` | [`geo_collision_box.py`](collision/geo_collision_box.py)、[`block_collision_detail.py`](collision/block_collision_detail.py) | 外接盒与多盒明细 |
| **④ 床板 6 → 选取形状** | `tools/bed6/` | [`bed_plate6_voxel_pick_from_geo.py`](bed6/bed_plate6_voxel_pick_from_geo.py)、[`test_bed_plate6_voxel_pick_from_geo.py`](bed6/test_bed_plate6_voxel_pick_from_geo.py) | `VoxelShape` 片段 + 单测 |
| **⑤ 床板 6 → 贴图与工程** | `tools/bed6/` | [`export_bed_plate6_texture_from_bbmodel.py`](bed6/export_bed_plate6_texture_from_bbmodel.py) 等 | 见下表 |
| **⑥ 普通玻璃窗** | `tools/glass/` | [`plain_glass_window_texture_naming.py`](glass/plain_glass_window_texture_naming.py)、[`plain_glass_window_lang_display_colors.py`](glass/plain_glass_window_lang_display_colors.py) | 命名库 + 语言对照 |
| **⑦ 开发环境** | `tools/` | [`paths.py`](paths.py)、[`tools_gui.py`](tools_gui.py) | 路径常量、GUI |

### ⑤ 床板 6 脚本列表（均在 `bed6/`）

| 脚本 | 作用 |
|------|------|
| [`export_bed_plate6_texture_from_bbmodel.py`](bed6/export_bed_plate6_texture_from_bbmodel.py) | 主床板单张 `bed_plate6.png`（脚本内默认路径） |
| [`export_bed_plate6_duvet_textures_from_bbmodel.py`](bed6/export_bed_plate6_duvet_textures_from_bbmodel.py) | 被单 7 张贴图 |
| [`export_bed_plate6_duvet_cover_textures_from_bbmodel.py`](bed6/export_bed_plate6_duvet_cover_textures_from_bbmodel.py) | 被套 1…6 |
| [`export_bed_plate6_pillow_medium_textures_from_bbmodel.py`](bed6/export_bed_plate6_pillow_medium_textures_from_bbmodel.py) | 中号枕头贴图 |
| [`extract_bed_plate6_pillow_large_textures_from_bbmodel.py`](bed6/extract_bed_plate6_pillow_large_textures_from_bbmodel.py) | 大号枕头（脚本内 MoonStarfish 路径） |
| [`bed_plate6_duvet_bbmodel_rename_textures_by_color.py`](bed6/bed_plate6_duvet_bbmodel_rename_textures_by_color.py) | 被单 bbmodel 纹理中文显示名 |
| [`bed_plate6_pillow_lang_display_colors.py`](bed6/bed_plate6_pillow_lang_display_colors.py) | 枕头 PNG 主色 ↔ 译名对照（打印） |

---

## 依赖说明

- 多数脚本仅依赖 **标准库**。
- 玻璃窗主色命名等使用 **Pillow / NumPy**（见 `glass/plain_glass_window_texture_naming.py` 等）；被单重命名可选 **SciPy**。

子目录内脚本通过根目录 [`paths.py`](paths.py) 取得 `FF_ROOT` / `TOOLS_ROOT`，请勿随意改动目录层级。

若本一览与实现不一致，以各脚本 **源码与 docstring** 为准。
