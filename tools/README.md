# `fantasy_furniture/tools` 开发辅助脚本一览

本目录为「幻想家具」模组配套 Python 工具。多数脚本假定在仓库 **`fantasy_furniture` 根目录** 下执行（`python tools/<子目录>/<脚本>.py …`），具体参数与示例以各文件顶部 **docstring** 为准。

**一键启动**：双击仓库根目录 [`启动开发工具.bat`](../启动开发工具.bat)（或 `python tools/tools_webview.py`）。

---

## 界面分类（与启动器左侧一致）

| 分类 | 说明 |
|------|------|
| **① BlockBench 工具链** | Blockbench 工程 → 模组标准资源 → 体素选取形状（两步主流程） |
| **② Blockbench 导出贴图** | 仅从 `.bbmodel` 导出 PNG 或写回工程内纹理名（床板6 等） |
| **③ 方块模型 JSON** | Java 方块模型 JSON 处理 |
| **④ Geo / 碰撞** | 碰撞外接盒 / 多盒明细 |
| **⑤ 语言 / 贴图对照** | 已导出 PNG 与译名、主色核对 |
| **⑥ 测试** | 开发用单测 |

### ① BlockBench 工具链

| 启动器项 | 脚本 |
|----------|------|
| bbmodel → 材质 / geo.json / 动画 | [`blockbench/export_bbmodel_to_fantasy_furniture_assets.py`](blockbench/export_bbmodel_to_fantasy_furniture_assets.py) |
| geo.json → 体素形状（VoxelShape） | [`collision/voxel_pick_from_geo.py`](collision/voxel_pick_from_geo.py) |

典型顺序：先导出 bbmodel → 再对生成的 `geo/block/*.geo.json` 跑体素形状。床板6 在体素工具里将「系列预设」选为 `bed-plate6`。

被引用库：[`blockbench/bbmodel_to_geojson.py`](blockbench/bbmodel_to_geojson.py)

### ② Blockbench 导出贴图（床板6 等）

| 启动器项 | 脚本 |
|----------|------|
| 床板6 · 主贴图 / 被单 / 被套 / 枕头 | [`bed6/export_*.py`](bed6/)、[`bed6/extract_*.py`](bed6/) |
| 被单纹理中文名 | [`bed6/bed_plate6_duvet_bbmodel_rename_textures_by_color.py`](bed6/bed_plate6_duvet_bbmodel_rename_textures_by_color.py) |

---

## 子目录

| 目录 | 内容 |
|------|------|
| [`blockbench/`](blockbench/) | bbmodel → geo / 贴图 / 动画 |
| [`bed6/`](bed6/) | 床板6 贴图导出、命名 |
| [`block_model/`](block_model/) | 方块模型 JSON |
| [`collision/`](collision/) | 碰撞、voxel_pick |
| [`glass/`](glass/) | 玻璃窗命名与语言 |
| [`launcher/`](launcher/) | 注册表（GUI / Web 共用） |
| [`web/`](web/) | FastAPI + 静态页 |

---

## 依赖说明

- 多数脚本仅依赖 **标准库**。
- 玻璃窗共享贴图、主色命名：**Pillow / NumPy**；被单重命名可选 **SciPy**。
- Web 界面：`pip install -r tools/requirements-web.txt`
- **Cursor MCP**：`pip install -r tools/requirements-mcp.txt`，配置见 [`MCP.md`](MCP.md)

以 [`launcher/registry.py`](launcher/registry.py) 与各脚本 **docstring** 为准。

