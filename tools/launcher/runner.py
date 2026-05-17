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

from paths import FF_ROOT

from launcher.registry import build_command


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
        argv_tail = build_command(tool_id, params)
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
                rec.mark_done(code)
            except subprocess.TimeoutExpired:
                rec.append_line("\n[超时]\n")
                rec.mark_done(-1, error="timeout")
            except Exception as e:
                rec.append_line(f"\n[异常] {e}\n")
                rec.mark_done(-1, error=str(e))

        threading.Thread(target=worker, daemon=True).start()
        return rec
