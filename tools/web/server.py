# -*- coding: utf-8 -*-
"""FastAPI 本地服务：工具目录 API、运行任务、SSE 日志流。"""
from __future__ import annotations

import asyncio
import json
import sys
from pathlib import Path
from typing import Any

_TOOLS_ROOT = Path(__file__).resolve().parent.parent
if str(_TOOLS_ROOT) not in sys.path:
    sys.path.insert(0, str(_TOOLS_ROOT))

from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse, StreamingResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

from launcher.form_history import clear_history, get_tool_history, remember_tool_params
from launcher.preview_service import preview_tool, preview_local_file
from launcher.registry import CommandBuildError, catalog_for_api, get_tool
from launcher.runner import JobManager
from paths import FF_ROOT

STATIC_DIR = Path(__file__).resolve().parent / "static"
job_manager = JobManager()


class RunRequest(BaseModel):
    tool_id: str
    params: dict[str, Any] = Field(default_factory=dict)


class RememberRequest(BaseModel):
    tool_id: str
    params: dict[str, Any] = Field(default_factory=dict)


class PreviewRequest(BaseModel):
    tool_id: str
    params: dict[str, Any] = Field(default_factory=dict)


def create_app() -> FastAPI:
    app = FastAPI(title="幻想家具 · 开发工具", docs_url=None, redoc_url=None)

    @app.get("/api/info")
    def api_info() -> dict[str, Any]:
        return {
            "ff_root": str(FF_ROOT),
            "python": sys.executable,
        }

    @app.get("/api/catalog")
    def api_catalog() -> dict[str, Any]:
        return {"categories": catalog_for_api()}

    @app.get("/api/form-history/{tool_id}")
    def api_form_history(tool_id: str) -> dict[str, Any]:
        if get_tool(tool_id) is None:
            raise HTTPException(404, f"未知工具: {tool_id}")
        return get_tool_history(tool_id)

    @app.post("/api/form-history/remember")
    def api_form_history_remember(body: RememberRequest) -> dict[str, str]:
        tool = get_tool(body.tool_id)
        if tool is None:
            raise HTTPException(404, f"未知工具: {body.tool_id}")
        field_types = {f.key: f.type for f in tool.fields}
        remember_tool_params(body.tool_id, body.params, field_types=field_types)
        return {"status": "ok"}

    @app.delete("/api/form-history")
    def api_form_history_clear(tool_id: str | None = None) -> dict[str, str]:
        clear_history(tool_id=tool_id)
        return {"status": "ok"}

    @app.get("/api/file-preview")
    def api_file_preview(path: str) -> dict[str, Any]:
        return preview_local_file(path)

    @app.post("/api/preview")
    def api_preview(body: PreviewRequest) -> dict[str, Any]:
        tool = get_tool(body.tool_id)
        if tool is None:
            raise HTTPException(404, f"未知工具: {body.tool_id}")
        try:
            result = preview_tool(body.tool_id, body.params)
        except SystemExit as e:
            raise HTTPException(
                400,
                detail={"message": str(e) or "预览失败"},
            ) from e
        except Exception as e:
            raise HTTPException(
                500,
                detail={"message": f"预览异常: {e}"},
            ) from e
        if not result.get("ok"):
            raise HTTPException(
                400,
                detail={"message": result.get("error", "预览失败")},
            )
        return result

    @app.post("/api/run")
    def api_run(body: RunRequest) -> dict[str, str]:
        tool = get_tool(body.tool_id)
        if tool is None:
            raise HTTPException(404, f"未知工具: {body.tool_id}")
        try:
            rec = job_manager.start(body.tool_id, body.params)
        except CommandBuildError as e:
            raise HTTPException(400, detail={"message": str(e), "field": e.field}) from e
        field_types = {f.key: f.type for f in tool.fields}
        save_params = {
            k: v for k, v in body.params.items() if not str(k).startswith("_")
        }
        remember_tool_params(body.tool_id, save_params, field_types=field_types)
        return {"job_id": rec.id, "argv": rec.argv_display}

    @app.get("/api/jobs/{job_id}")
    def api_job_status(job_id: str) -> dict[str, Any]:
        rec = job_manager.get(job_id)
        if rec is None:
            raise HTTPException(404, "任务不存在")
        return {
            "id": rec.id,
            "tool_id": rec.tool_id,
            "status": rec.status,
            "exit_code": rec.exit_code,
            "error": rec.error_message,
            "argv": rec.argv_display,
            "lines": rec.snapshot_lines(),
        }

    @app.get("/api/jobs/{job_id}/events")
    async def api_job_events(job_id: str) -> StreamingResponse:
        rec = job_manager.get(job_id)
        if rec is None:
            raise HTTPException(404, "任务不存在")

        async def stream() -> Any:
            index = 0
            while True:
                lines = rec.snapshot_lines()
                while index < len(lines):
                    payload = json.dumps(
                        {"type": "line", "text": lines[index]},
                        ensure_ascii=False,
                    )
                    yield f"data: {payload}\n\n"
                    index += 1
                if rec.is_finished():
                    payload = json.dumps(
                        {
                            "type": "done",
                            "exit_code": rec.exit_code,
                            "error": rec.error_message,
                        },
                        ensure_ascii=False,
                    )
                    yield f"data: {payload}\n\n"
                    break
                await asyncio.sleep(0.15)

        return StreamingResponse(
            stream(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "Connection": "keep-alive",
                "X-Accel-Buffering": "no",
            },
        )

    @app.get("/")
    def index() -> FileResponse:
        return FileResponse(STATIC_DIR / "index.html")

    app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")
    return app


app = create_app()
