#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""幻想家具开发工具 MCP 服务（stdio），供 Cursor 等客户端调用。

启动（仓库根目录）::

    py -3 tools/mcp_ff_tools_server.py

或在 Cursor 的 MCP 配置中注册此脚本（见 tools/MCP.md）。
"""
from __future__ import annotations

import json
import sys
import time
from pathlib import Path
from typing import Any

_TOOLS_ROOT = Path(__file__).resolve().parent
if str(_TOOLS_ROOT) not in sys.path:
    sys.path.insert(0, str(_TOOLS_ROOT))

from launcher.form_history import get_tool_history, remember_tool_params
from launcher.preview_service import preview_tool
from launcher.registry import CommandBuildError, build_command, catalog_for_api, get_tool
from launcher.runner import JobManager
from paths import DEFAULT_ASSETS, FF_ROOT

try:
    from mcp.server.fastmcp import FastMCP
except ImportError as e:
    print(
        "缺少 mcp 包。请执行: pip install -r tools/requirements-mcp.txt",
        file=sys.stderr,
    )
    raise SystemExit(1) from e

mcp = FastMCP(
    "fantasy-furniture-tools",
    instructions=(
        "幻想家具模组本地开发工具链：bbmodel 导出、geo 碰撞、体素选取等。"
        "先调用 ff_list_tools 查看 tool_id 与参数字段，再 ff_preview 或 ff_run_tool。"
        "export_bbmodel 常用参数: path(.bbmodel), asset_id, assets_root, shared。"
    ),
)

_job_manager = JobManager()


def _tool_or_error(tool_id: str):
    tool = get_tool(tool_id)
    if tool is None:
        raise ValueError(f"未知工具: {tool_id}")
    return tool


def _run_sync(tool_id: str, params: dict[str, Any], *, timeout_s: float = 3600) -> dict[str, Any]:
    _tool_or_error(tool_id)
    try:
        rec = _job_manager.start(tool_id, params or {})
    except CommandBuildError as e:
        return {"ok": False, "error": str(e), "field": e.field}

    tool = get_tool(tool_id)
    if tool:
        field_types = {f.key: f.type for f in tool.fields}
        remember_tool_params(tool_id, params or {}, field_types=field_types)

    deadline = time.monotonic() + timeout_s
    while not rec.is_finished():
        if time.monotonic() > deadline:
            return {
                "ok": False,
                "error": "timeout",
                "job_id": rec.id,
                "lines": rec.snapshot_lines(),
            }
        time.sleep(0.15)

    return {
        "ok": rec.exit_code == 0,
        "job_id": rec.id,
        "exit_code": rec.exit_code,
        "error": rec.error_message,
        "argv": rec.argv_display,
        "lines": rec.snapshot_lines(),
    }


def _summarize_preview(result: dict[str, Any]) -> dict[str, Any]:
    out = dict(result)
    items: list[dict[str, Any]] = []
    for it in result.get("items") or []:
        row = {k: v for k, v in it.items() if k != "data_url"}
        if it.get("kind") == "image":
            row["preview"] = "(image omitted for MCP)"
        text = it.get("text")
        if isinstance(text, str) and len(text) > 6000:
            row["text"] = text[:6000] + "\n…（MCP 响应已截断）"
            row["truncated"] = True
        items.append(row)
    out["items"] = items
    return out


@mcp.tool()
def ff_get_info() -> dict[str, Any]:
    """返回模组根目录、默认 assets 路径与 Python 解释器路径。"""
    return {
        "ff_root": str(FF_ROOT),
        "default_assets": str(DEFAULT_ASSETS),
        "python": sys.executable,
        "tools_root": str(_TOOLS_ROOT),
    }


@mcp.tool()
def ff_list_tools() -> list[dict[str, Any]]:
    """列出全部工具的 id、名称、说明与表单字段 schema。"""
    return catalog_for_api()


@mcp.tool()
def ff_get_tool_history(tool_id: str) -> dict[str, Any]:
    """获取某工具的历史路径建议与上次参数。"""
    _tool_or_error(tool_id)
    return get_tool_history(tool_id)


@mcp.tool()
def ff_build_command(tool_id: str, params: dict[str, Any] | None = None) -> dict[str, Any]:
    """仅拼装命令行（不执行），用于检查参数是否正确。"""
    _tool_or_error(tool_id)
    try:
        argv = build_command(tool_id, params or {})
    except CommandBuildError as e:
        return {"ok": False, "error": str(e), "field": e.field}
    return {
        "ok": True,
        "argv": [sys.executable, "-u", *argv],
        "argv_display": " ".join([sys.executable, "-u", *map(str, argv)]),
    }


@mcp.tool()
def ff_preview(tool_id: str, params: dict[str, Any] | None = None) -> dict[str, Any]:
    """预览工具输出（如 bbmodel 导出结果），不写入 assets。"""
    _tool_or_error(tool_id)
    try:
        result = preview_tool(tool_id, params or {})
    except SystemExit as e:
        return {"ok": False, "error": str(e)}
    except Exception as e:
        return {"ok": False, "error": f"{type(e).__name__}: {e}"}
    if not result.get("ok"):
        return {"ok": False, "error": result.get("error", "预览失败")}
    return {"ok": True, **_summarize_preview(result)}


@mcp.tool()
def ff_run_tool(
    tool_id: str,
    params: dict[str, Any] | None = None,
    timeout_seconds: int = 3600,
) -> dict[str, Any]:
    """运行指定工具并等待结束，返回完整终端输出与退出码。"""
    return _run_sync(tool_id, params or {}, timeout_s=float(timeout_seconds))


@mcp.tool()
def ff_export_bbmodel(
    bbmodel_path: str,
    asset_id: str,
    assets_root: str | None = None,
    shared_textures: str = "",
    skip_geo: bool = False,
    skip_textures: bool = False,
    only_primary_texture: bool = False,
    delete_stale_animation: bool = False,
) -> dict[str, Any]:
    """快捷导出 .bbmodel 到 assets（等同 ff_run_tool export_bbmodel）。"""
    params: dict[str, Any] = {
        "path": bbmodel_path,
        "asset_id": asset_id,
        "shared": shared_textures,
        "skip_geo": skip_geo,
        "skip_textures": skip_textures,
        "only_primary": only_primary_texture,
        "del_anim": delete_stale_animation,
        "dry_run": False,
    }
    if assets_root:
        params["assets_root"] = assets_root
    else:
        params["assets_root"] = str(DEFAULT_ASSETS)
    return _run_sync("export_bbmodel", params)


def main() -> None:
    mcp.run()


if __name__ == "__main__":
    main()
