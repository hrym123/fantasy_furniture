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
import socket
import sys
import threading
import time
import webbrowser
from pathlib import Path

_TOOLS_ROOT = Path(__file__).resolve().parent
if str(_TOOLS_ROOT) not in sys.path:
    sys.path.insert(0, str(_TOOLS_ROOT))

from paths import FF_ROOT  # noqa: E402


def _free_port() -> int:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind(("127.0.0.1", 0))
        return int(s.getsockname()[1])


def _start_uvicorn(host: str, port: int) -> None:
    import uvicorn

    from web.server import app

    uvicorn.run(app, host=host, port=port, log_level="warning")


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
    if not FF_ROOT.is_dir() or not (FF_ROOT / "src").is_dir():
        print("请在 fantasy_furniture 仓库根目录运行 tools/tools_webview.py", file=sys.stderr)
        sys.exit(1)

    parser = argparse.ArgumentParser(description="幻想家具 tools · Web 内嵌界面")
    parser.add_argument(
        "--server-only",
        action="store_true",
        help="不打开 pywebview，仅启动 HTTP 服务",
    )
    parser.add_argument("--host", default="127.0.0.1", help="监听地址（默认仅本机）")
    parser.add_argument("--port", type=int, default=0, help="端口（0 表示自动选取）")
    args = parser.parse_args()

    if args.host not in ("127.0.0.1", "localhost"):
        print("警告：仅建议在 127.0.0.1 上使用，勿暴露到公网。", file=sys.stderr)

    port = args.port or _free_port()
    url = f"http://{args.host}:{port}/"

    thread = threading.Thread(
        target=_start_uvicorn,
        args=(args.host, port),
        daemon=True,
        name="ff-tools-uvicorn",
    )
    thread.start()

    for _ in range(50):
        try:
            with socket.create_connection((args.host, port), timeout=0.2):
                break
        except OSError:
            time.sleep(0.1)
    else:
        print("HTTP 服务启动超时", file=sys.stderr)
        sys.exit(1)

    print(f"幻想家具开发工具: {url}")

    if args.server_only:
        webbrowser.open(url)
        print("按 Ctrl+C 结束。")
        try:
            while True:
                time.sleep(3600)
        except KeyboardInterrupt:
            return

    import webview

    webview.create_window(
        "幻想家具 · 开发工具",
        url,
        width=1024,
        height=720,
        min_size=(720, 520),
        js_api=NativeDialogs(),
    )
    webview.start(debug=False)


if __name__ == "__main__":
    main()
