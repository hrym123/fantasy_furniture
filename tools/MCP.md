# 幻想家具开发工具 · MCP

让 Cursor Agent 通过 MCP 直接调用 `tools/` 下的导出、碰撞、预览等脚本（无需手动点 Web 界面）。

## 1. 安装依赖

在 **fantasy_furniture 仓库根目录**：

```bash
pip install -r tools/requirements-mcp.txt
```

（若已装 `requirements-web.txt`，只需额外装 `mcp`。）

## 2. 在 Cursor 里注册 MCP

### 方式 A：项目配置（推荐，随仓库共享）

仓库已包含 `.cursor/mcp.json`。打开后把 `cwd` 改成你本机的 **fantasy_furniture 根目录**（若路径不同）。

### 方式 B：用户全局配置

1. Cursor → **Settings** → **MCP** → **Add new MCP server**
2. 或编辑用户级配置（见 [Cursor MCP 文档](https://docs.cursor.com/context/mcp)）

示例（Windows，请改路径）：

```json
{
  "mcpServers": {
    "fantasy-furniture-tools": {
      "command": "python",
      "args": ["tools/mcp_ff_tools_server.py"],
      "cwd": "D:\\warehouse\\Lanye-mod\\development\\core\\fantasy_furniture"
    }
  }
}
```

- **command**：与执行 `pip install` **同一套** Python（`python` / 虚拟环境路径；勿用未装 `mcp` 的解释器）
- **cwd**：必须是 **fantasy_furniture 根目录**（含 `src/`、`tools/` 的那一层）
- 保存后 **Reload MCP** 或重启 Cursor

## 3. 提供的工具（MCP Tools）

| 工具名 | 作用 |
|--------|------|
| `ff_get_info` | 模组根目录、默认 assets、Python 路径 |
| `ff_list_tools` | 全部 `tool_id` 与表单字段 |
| `ff_get_tool_history` | 某工具历史路径 / 上次参数 |
| `ff_build_command` | 只拼命令、不执行 |
| `ff_preview` | 预览导出结果（不写盘） |
| `ff_run_tool` | 运行任意工具并等待结束 |
| `ff_export_bbmodel` | 快捷 bbmodel → assets 导出 |

## 4. Agent 使用示例

自然语言即可，例如：

- 「列出幻想家具开发工具有哪些」
- 「预览导出 `D:\...\普通窗户_22.5度.bbmodel`，asset-id 为 `plain_glass_window_shape_225`」
- 「把该 bbmodel 保存到默认 assets」

Agent 会先调 `ff_list_tools` / `ff_preview`，确认后再 `ff_export_bbmodel` 或 `ff_run_tool`。

## 5. 与 Web 界面的关系

| 方式 | 说明 |
|------|------|
| **MCP** | Cursor Agent 自动调脚本；适合对话里批量操作 |
| **启动开发工具.bat** | 图形界面 + 预览；适合人工核对贴图/geo |

二者共用同一套 `launcher/registry.py` 逻辑，结果一致。

## 6. 故障排查

- **MCP 显示红点 / 无法连接**：在终端手动运行  
  `cd <fantasy_furniture根>` → `python tools/mcp_ff_tools_server.py`  
  若报 `No module named mcp`，执行上一节 pip install。
- **路径错误**：确认 `cwd` 为仓库根，且 `tools/mcp_ff_tools_server.py` 存在。
- **导出失败**：看 `ff_run_tool` 返回的 `lines` 字段（与 Web 终端输出相同）。
