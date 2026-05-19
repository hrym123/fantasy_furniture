# -*- coding: utf-8 -*-
"""子进程任务与输出流（供 Web SSE / 其它 UI 使用）。"""
from __future__ import annotations

import subprocess
import sys
import threading
import uuid
from collections import deque
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any, Iterator

from pathlib import Path

from paths import FF_ROOT

from launcher.registry import build_command


def apply_asset_renames(assets_root: str, renames: list[dict[str, str]]) -> list[str]:
    """导出完成后按预览区指定的路径重命名文件。"""
    root = Path(assets_root).expanduser().resolve()
    logs: list[str] = []
    for entry in renames:
        src = Path(entry.get("from", "")).expanduser()
        dst = Path(entry.get("to", "")).expanduser()
        if not src.is_absolute():
            src = root / src
        if not dst.is_absolute():
            dst = root / dst
        try:
            src = src.resolve()
            dst = dst.resolve()
        except OSError:
            logs.append(f"[跳过] 无效路径: {entry}")
            continue
        if src == dst:
            continue
        if not src.is_file():
            logs.append(f"[跳过] 源文件不存在: {src}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        if dst.exists():
            dst.unlink()
        src.rename(dst)
        logs.append(f"重命名: {src.name} → {dst.name}")
    return logs


@dataclass
class JobRecord:
    id: str
    tool_id: str
    argv_display: str
    status: str = "pending"  # pending | running | done | error
    exit_code: int | None = None
    error_message: str | None = None
    created_at: str = field(
        default_factory=lambda: datetime.now(timezone.utc).isoformat()
    )
    _lines: deque[str] = field(default_factory=deque, repr=False)
    _lock: threading.Lock = field(default_factory=threading.Lock, repr=False)
    _cond: threading.Condition = field(default_factory=threading.Condition, repr=False)
    _finished: bool = field(default=False, repr=False)

    def append_line(self, text: str) -> None:
        with self._lock:
            self._lines.append(text)
        # 同步打到启动服务的终端（bat / python tools_webview.py）
        if text:
            try:
                sys.stdout.write(text)
                sys.stdout.flush()
            except OSError:
                pass
        with self._cond:
            self._cond.notify_all()

    def mark_done(self, code: int, *, error: str | None = None) -> None:
        with self._lock:
            self._finished = True
            self.exit_code = code
            if error:
                self.error_message = error
                self.status = "error"
            elif code == 0:
                self.status = "done"
            else:
                self.status = "failed"
        with self._cond:
            self._cond.notify_all()

    def snapshot_lines(self) -> list[str]:
        with self._lock:
            return list(self._lines)

    def is_finished(self) -> bool:
        with self._lock:
            return self._finished

    def iter_events(self, *, poll_timeout: float = 0.25) -> Iterator[dict[str, Any]]:
        """从当前缓冲起增量产出日志行，结束后产出 ``done`` 事件。"""
        index = 0
        while True:
            with self._lock:
                lines = list(self._lines)
                finished = self._finished
                code = self.exit_code
                err = self.error_message
            while index < len(lines):
                yield {"type": "line", "text": lines[index]}
                index += 1
            if finished:
                yield {
                    "type": "done",
                    "exit_code": code,
                    "error": err,
                }
                return
            with self._cond:
                self._cond.wait(timeout=poll_timeout)


class JobManager:
    def __init__(self) -> None:
        self._jobs: dict[str, JobRecord] = {}
        self._lock = threading.Lock()

    def get(self, job_id: str) -> JobRecord | None:
        with self._lock:
            return self._jobs.get(job_id)

    def start(self, tool_id: str, params: dict[str, Any]) -> JobRecord:
        run_params = {k: v for k, v in params.items() if not str(k).startswith("_")}
        asset_renames = params.get("_asset_renames")
        argv_tail = build_command(tool_id, run_params)
        argv = [sys.executable, "-u", *argv_tail]
        job_id = uuid.uuid4().hex[:12]
        rec = JobRecord(
            id=job_id,
            tool_id=tool_id,
            argv_display=" ".join(argv),
            status="running",
        )
        with self._lock:
            self._jobs[job_id] = rec

        def worker() -> None:
            rec.append_line(f"$ {' '.join(argv)}\n{'=' * 60}\n")
            try:
                proc = subprocess.Popen(
                    argv,
                    cwd=str(FF_ROOT),
                    stdout=subprocess.PIPE,
                    stderr=subprocess.STDOUT,
                    text=True,
                    encoding="utf-8",
                    errors="replace",
                    bufsize=1,
                )
                assert proc.stdout is not None
                for line in proc.stdout:
                    rec.append_line(line)
                code = proc.wait(timeout=3600)
                if code != 0:
                    rec.append_line(f"\n进程退出码: {code}\n")
                elif (
                    tool_id == "export_bbmodel"
                    and code == 0
                    and isinstance(asset_renames, list)
                    and asset_renames
                ):
                    assets_root = str(run_params.get("assets_root") or "").strip()
                    if assets_root:
                        rec.append_line("\n--- 按预览造型段重命名 ---\n")
                        for log_line in apply_asset_renames(assets_root, asset_renames):
                            rec.append_line(log_line + "\n")
                rec.mark_done(code)
            except subprocess.TimeoutExpired:
                rec.append_line("\n[超时]\n")
                rec.mark_done(-1, error="timeout")
            except Exception as e:
                rec.append_line(f"\n[异常] {e}\n")
                rec.mark_done(-1, error=str(e))

        threading.Thread(target=worker, daemon=True).start()
        return rec
