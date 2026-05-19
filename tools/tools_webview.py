#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""幻想家具开发工具：FastAPI + pywebview 内嵌 Web 界面。

在 ``fantasy_furniture`` 仓库根目录执行::

    pip install -r tools/requirements-web.txt
    python tools/tools_webview.py

仅启动本地 HTTP（用系统浏览器打开）::

    python tools/tools_webview.py --server-only

可选指定端口::

    python tools/tools_webview.py --port 8765
"""
from __future__ import annotations

import argparse
import atexit
import os
import socket
import sys
import threading
import time
import traceback
import webbrowser
from pathlib import Path

_BOOT_LOG = Path(os.environ.get("LOCALAPPDATA", Path.home())) / "fantasy_furniture_tools.log"
_LOCK_FILE = Path(os.environ.get("TEMP", ".")) / "fantasy_furniture_tools.lock"
_HTTP_WAIT_SEC = 20.0
_HTTP_POLL_INTERVAL = 0.1

_TOOLS_ROOT = Path(__file__).resolve().parent
if str(_TOOLS_ROOT) not in sys.path:
    sys.path.insert(0, str(_TOOLS_ROOT))

from paths import FF_ROOT  # noqa: E402


def _free_port() -> int:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind(("127.0.0.1", 0))
        return int(s.getsockname()[1])


def _boot_log(msg: str) -> None:
    try:
        stamp = time.strftime("%Y-%m-%d %H:%M:%S")
        with _BOOT_LOG.open("a", encoding="utf-8") as f:
            f.write(f"[{stamp}] {msg}\n")
    except OSError:
        pass


def _show_win_message(
    text: str,
    title: str = "幻想家具 · 开发工具",
    *,
    icon: int = 0x10,
) -> None:
    if sys.platform != "win32":
        return
    try:
        import ctypes

        ctypes.windll.user32.MessageBoxW(  # type: ignore[attr-defined]
            0,
            text,
            title,
            icon,
        )
    except Exception:
        pass


def _fatal(msg: str, *, log_detail: str | None = None) -> None:
    if log_detail:
        _boot_log(f"{msg}\n{log_detail}")
    else:
        _boot_log(msg)
    if sys.stderr and getattr(sys.stderr, "isatty", lambda: False)():
        print(msg, file=sys.stderr)
        if log_detail:
            print(log_detail, file=sys.stderr)
    hint = f"{msg}\n\n日志: {_BOOT_LOG}"
    _show_win_message(hint)


def _ensure_stdio() -> None:
    """pythonw.exe 无控制台，stdout/stderr 为 None 会导致 uvicorn 日志初始化失败。"""
    import io

    if sys.stdout is None:
        sys.stdout = io.TextIOWrapper(open(os.devnull, "wb"), encoding="utf-8", errors="replace")
    if sys.stderr is None:
        sys.stderr = io.TextIOWrapper(open(os.devnull, "wb"), encoding="utf-8", errors="replace")


def _hide_windows_console() -> None:
    """由启动脚本设置 FF_TOOLS_HIDE_CONSOLE=1 时隐藏多余 CMD 窗口（仅 python.exe）。"""
    if sys.platform != "win32":
        return
    if Path(sys.executable).name.lower() == "pythonw.exe":
        return
    try:
        import ctypes

        hwnd = ctypes.windll.kernel32.GetConsoleWindow()  # type: ignore[attr-defined]
        if hwnd:
            ctypes.windll.user32.ShowWindow(hwnd, 0)  # SW_HIDE  # type: ignore[attr-defined]
    except Exception:
        pass


def _pid_alive(pid: int) -> bool:
    if pid <= 0:
        return False
    if sys.platform == "win32":
        import ctypes

        PROCESS_QUERY_LIMITED_INFORMATION = 0x1000
        STILL_ACTIVE = 259
        handle = ctypes.windll.kernel32.OpenProcess(  # type: ignore[attr-defined]
            PROCESS_QUERY_LIMITED_INFORMATION, False, pid
        )
        if not handle:
            return False
        code = ctypes.c_ulong()
        ok = ctypes.windll.kernel32.GetExitCodeProcess(  # type: ignore[attr-defined]
            handle, ctypes.byref(code)
        )
        ctypes.windll.kernel32.CloseHandle(handle)  # type: ignore[attr-defined]
        return bool(ok) and code.value == STILL_ACTIVE
    try:
        os.kill(pid, 0)
    except OSError:
        return False
    return True


def _kill_pid(pid: int) -> None:
    if sys.platform == "win32":
        import ctypes

        PROCESS_TERMINATE = 0x0001
        handle = ctypes.windll.kernel32.OpenProcess(PROCESS_TERMINATE, False, pid)  # type: ignore[attr-defined]
        if handle:
            ctypes.windll.kernel32.TerminateProcess(handle, 1)  # type: ignore[attr-defined]
            ctypes.windll.kernel32.CloseHandle(handle)  # type: ignore[attr-defined]
        return
    try:
        os.kill(pid, 9)
    except OSError:
        pass


def _read_lock() -> tuple[int, int] | None:
    try:
        parts = _LOCK_FILE.read_text(encoding="utf-8").split()
        if len(parts) < 2:
            return None
        return int(parts[0]), int(parts[1])
    except (OSError, ValueError):
        return None


def _write_lock(port: int) -> None:
    try:
        _LOCK_FILE.write_text(f"{os.getpid()} {port}\n", encoding="utf-8")
    except OSError:
        pass


def _clear_lock() -> None:
    try:
        if not _LOCK_FILE.is_file():
            return
        locked = _read_lock()
        if locked is None or locked[0] == os.getpid():
            _LOCK_FILE.unlink(missing_ok=True)
    except OSError:
        pass


def _http_ready(host: str, port: int) -> bool:
    try:
        with socket.create_connection((host, port), timeout=0.25):
            return True
    except OSError:
        return False


def _prepare_single_instance(host: str) -> bool:
    """若已有健康实例则交给其处理并返回 False；否则清理陈旧锁/僵尸进程。"""
    locked = _read_lock()
    if locked is None:
        return True

    old_pid, old_port = locked
    if _pid_alive(old_pid):
        if _http_ready(host, old_port):
            url = f"http://{host}:{old_port}/"
            _boot_log(f"handoff to pid={old_pid} port={old_port}")
            webbrowser.open(url)
            _show_win_message(
                f"开发工具已在运行（进程 {old_pid}）。\n已在浏览器中打开:\n{url}",
                title="幻想家具 · 开发工具",
                icon=0x40,
            )
            return False
        _boot_log(f"stale instance pid={old_pid}, terminating")
        _kill_pid(old_pid)
        time.sleep(0.3)

    try:
        _LOCK_FILE.unlink(missing_ok=True)
    except OSError:
        pass
    return True


def _start_uvicorn(host: str, port: int, errors: list[str]) -> None:
    try:
        import uvicorn

        from web.server import app

        uvicorn.run(app, host=host, port=port, log_level="warning")
    except Exception:
        errors.append(traceback.format_exc())


class NativeDialogs:
    """供内嵌 WebView 调用的原生文件/文件夹选择。"""

    def pick_file(self) -> list[str] | None:
        import webview

        win = webview.active_window()
        if win is None:
            return None
        result = win.create_file_dialog(
            webview.OPEN_DIALOG,
            allow_multiple=False,
            file_types=("All files (*.*)", "Blockbench (*.bbmodel)", "JSON (*.json)"),
        )
        if not result:
            return None
        if isinstance(result, (list, tuple)):
            return [str(p) for p in result]
        return [str(result)]

    def pick_directory(self) -> list[str] | None:
        import webview

        win = webview.active_window()
        if win is None:
            return None
        result = win.create_file_dialog(webview.FOLDER_DIALOG)
        if not result:
            return None
        if isinstance(result, (list, tuple)):
            return [str(p) for p in result]
        return [str(result)]


def main() -> None:
    _ensure_stdio()
    os.chdir(FF_ROOT)

    if not FF_ROOT.is_dir() or not (FF_ROOT / "src").is_dir():
        _fatal("请在 fantasy_furniture 仓库根目录运行 tools/tools_webview.py")
        sys.exit(1)

    parser = argparse.ArgumentParser(description="幻想家具 tools · Web 内嵌界面")
    parser.add_argument(
        "--server-only",
        action="store_true",
        help="不打开 pywebview，仅启动 HTTP 服务",
    )
    parser.add_argument("--host", default="127.0.0.1", help="监听地址（默认仅本机）")
    parser.add_argument("--port", type=int, default=0, help="端口（0 表示自动选取）")
    parser.add_argument(
        "--show-console",
        action="store_true",
        help="保留/显示控制台（调试用；默认 GUI 模式会隐藏）",
    )
    args = parser.parse_args()

    if args.host not in ("127.0.0.1", "localhost"):
        print("警告：仅建议在 127.0.0.1 上使用，勿暴露到公网。", file=sys.stderr)

    if not args.server_only and not _prepare_single_instance(args.host):
        return

    port = args.port or _free_port()
    url = f"http://{args.host}:{port}/"
    server_errors: list[str] = []

    thread = threading.Thread(
        target=_start_uvicorn,
        args=(args.host, port, server_errors),
        daemon=True,
        name="ff-tools-uvicorn",
    )
    thread.start()

    deadline = time.monotonic() + _HTTP_WAIT_SEC
    while time.monotonic() < deadline:
        if server_errors:
            break
        if _http_ready(args.host, port):
            break
        time.sleep(_HTTP_POLL_INTERVAL)
    else:
        if not server_errors and thread.is_alive():
            server_errors.append("(no exception captured; uvicorn thread still running)")

    if server_errors or not _http_ready(args.host, port):
        detail = server_errors[0] if server_errors else "unknown"
        _fatal(
            f"HTTP 服务在 {_HTTP_WAIT_SEC:.0f} 秒内未能启动（端口 {port}）。",
            log_detail=detail,
        )
        sys.exit(1)

    _write_lock(port)
    atexit.register(_clear_lock)

    if args.server_only or args.show_console:
        print(f"幻想家具开发工具: {url}")

    if args.server_only:
        webbrowser.open(url)
        print("按 Ctrl+C 结束。")
        try:
            while True:
                time.sleep(3600)
        except KeyboardInterrupt:
            return

    if not args.show_console and os.environ.get("FF_TOOLS_HIDE_CONSOLE") == "1":
        _hide_windows_console()

    import webview

    webview.create_window(
        "幻想家具 · 开发工具",
        url,
        width=1180,
        height=760,
        min_size=(880, 560),
        js_api=NativeDialogs(),
    )
    webview.start(debug=False)


if __name__ == "__main__":
    try:
        _boot_log(f"start pid={os.getpid()} exe={sys.executable} cwd={os.getcwd()}")
        main()
        _boot_log(f"done pid={os.getpid()}")
    except SystemExit as e:
        if e.code not in (0, None):
            _boot_log(f"exit {e.code}")
        raise
    except Exception:
        _boot_log(traceback.format_exc())
        _fatal("开发工具启动失败", log_detail=traceback.format_exc())
        raise
    finally:
        _clear_lock()
