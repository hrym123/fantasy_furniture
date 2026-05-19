# `fantasy_furniture/tools` 开发辅助脚本一览

本目录为「幻想家具」模组配套 Python 工具。多数脚本假定在仓库 **`fantasy_furniture` 根目录** 下执行（`python tools/<子目录>/<脚本>.py …`），具体参数与示例以各文件顶部 **docstring** 为准。

**子目录（按职责）**

| 目录 | 对应分类 | 内容 |
|------|-----------|------|
| [`blockbench/`](blockbench/) | ① | `.bbmodel` → geo / 贴图 / 动画；含 `bbmodel_to_geojson.py`（库，无 CLI） |
| [`bed6/`](bed6/) | ① | 床板 6 从 `.bbmodel` 导出 PNG、被单纹理命名等 |
| [`block_model/`](block_model/) | ② | 原版方块模型 JSON 拆分等 |
| [`collision/`](collision/) | ③④ | `geo` / 方块模型 → 碰撞、选取 `VoxelShape` |
| [`glass/`](glass/) | ⑤ | 普通玻璃窗：贴图 stem 库、语言/主色对照 |
| [`launcher/`](launcher/) | — | 工具注册表与命令拼装（GUI / Web 共用） |
| [`web/`](web/) | — | FastAPI 服务与静态前端 |
| （根目录） | — | [`paths.py`](paths.py)、[`tools_webview.py`](tools_webview.py)、[`启动开发工具.bat`](../启动开发工具.bat) |

**图形界面**

| 方式 | 操作 |
|------|------|
| **一键启动（推荐）** | 双击仓库根目录 [`启动开发工具.bat`](../启动开发工具.bat) |
| 内嵌 Web | `python tools/tools_webview.py` |
| 仅浏览器 | `python tools/tools_webview.py --server-only` |
| tkinter（旧） | `python tools/tools_gui.py` |

---

## 界面分类（与启动器左侧一致）

| 分类 | 说明 | 主要脚本 |
|------|------|----------|
| **① Blockbench 导出资源** | 从 `.bbmodel` 写出 geo / 贴图 / 动画，或仅导出 PNG、写回工程内纹理名 | `blockbench/export_bbmodel_*`、`bed6/export_*`、`bed6/extract_*`、`bed6/*_rename_*` |
| **② 方块模型 JSON** | 非 Blockbench 的 Java 方块模型 JSON 处理 | `block_model/split_screen_model.py` |
| **③ Geo / 碰撞** | 由 geo（或方块模型 JSON）算碰撞盒 | `collision/geo_collision_box.py`、`collision/block_collision_detail.py` |
| **④ Geo 选取** | 由 geo 生成选取用 `VoxelShape` Java 片段 | `collision/voxel_pick_from_geo.py` |
| **⑤ 语言 / 贴图对照** | 已导出 PNG 与译名、主色核对（不写 bbmodel） | `glass/plain_glass_window_lang_*`、`bed6/bed_plate6_pillow_lang_*` |

### ① Blockbench 导出资源（明细）

| 启动器项 | 脚本 |
|----------|------|
| bbmodel → geo / 贴图 / 动画 | [`blockbench/export_bbmodel_to_fantasy_furniture_assets.py`](blockbench/export_bbmodel_to_fantasy_furniture_assets.py) |
| 床板6 · 主贴图 PNG | [`bed6/export_bed_plate6_texture_from_bbmodel.py`](bed6/export_bed_plate6_texture_from_bbmodel.py) |
| 床板6 · 被单 7 张 | [`bed6/export_bed_plate6_duvet_textures_from_bbmodel.py`](bed6/export_bed_plate6_duvet_textures_from_bbmodel.py) |
| 床板6 · 被套 | [`bed6/export_bed_plate6_duvet_cover_textures_from_bbmodel.py`](bed6/export_bed_plate6_duvet_cover_textures_from_bbmodel.py) |
| 床板6 · 中号枕头 | [`bed6/export_bed_plate6_pillow_medium_textures_from_bbmodel.py`](bed6/export_bed_plate6_pillow_medium_textures_from_bbmodel.py) |
| 床板6 · 大号枕头 | [`bed6/extract_bed_plate6_pillow_large_textures_from_bbmodel.py`](bed6/extract_bed_plate6_pillow_large_textures_from_bbmodel.py) |
| 床板6 · 被单纹理中文名 | [`bed6/bed_plate6_duvet_bbmodel_rename_textures_by_color.py`](bed6/bed_plate6_duvet_bbmodel_rename_textures_by_color.py) |

被引用库（无 CLI）：[`blockbench/bbmodel_to_geojson.py`](blockbench/bbmodel_to_geojson.py)、[`glass/plain_glass_window_texture_naming.py`](glass/plain_glass_window_texture_naming.py)

---

## 依赖说明

- 多数脚本仅依赖 **标准库**。
- 玻璃窗主色命名等使用 **Pillow / NumPy**；被单重命名可选 **SciPy**。
- Web 界面：`pip install -r tools/requirements-web.txt`

若本一览与实现不一致，以 [`launcher/registry.py`](launcher/registry.py) 与各脚本 **docstring** 为准。
