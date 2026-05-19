# -*- coding: utf-8 -*-
"""表单字段历史：路径/文本最近记录（每字段最多 ``MAX_PER_KEY`` 条），供 Web / GUI 下拉复用。"""
from __future__ import annotations

import json
import threading
from pathlib import Path
from typing import Any

from paths import TOOLS_ROOT

HISTORY_FILE = TOOLS_ROOT / ".form_history.json"
MAX_PER_KEY = 100
_HISTORY_TYPES = frozenset({"file", "directory", "text"})

_lock = threading.RLock()


def _empty_store() -> dict[str, Any]:
    return {"version": 1, "global": {}, "tools": {}, "last_params": {}}


def _load_unlocked() -> dict[str, Any]:
    if not HISTORY_FILE.is_file():
        return _empty_store()
    try:
        data = json.loads(HISTORY_FILE.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return _empty_store()
    if not isinstance(data, dict):
        return _empty_store()
    data.setdefault("version", 1)
    data.setdefault("global", {})
    data.setdefault("tools", {})
    data.setdefault("last_params", {})
    return data


def _save_unlocked(data: dict[str, Any]) -> None:
    HISTORY_FILE.parent.mkdir(parents=True, exist_ok=True)
    HISTORY_FILE.write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def _norm_value(raw: Any) -> str:
    if raw is None:
        return ""
    s = str(raw).strip()
    return s


def _push_unique(bucket: dict[str, list[str]], key: str, value: str) -> None:
    if not value:
        return
    lst = bucket.setdefault(key, [])
    if value in lst:
        lst.remove(value)
    lst.insert(0, value)
    del lst[MAX_PER_KEY:]


def remember_tool_params(
    tool_id: str,
    params: dict[str, Any],
    *,
    field_types: dict[str, str] | None = None,
) -> None:
    """记录一次提交；``field_types`` 为 ``key -> file|directory|text|...``。"""
    if not tool_id or not params:
        return
    with _lock:
        data = _load_unlocked()
        global_b = data["global"]
        tools_b = data["tools"]
        tool_bucket = tools_b.setdefault(tool_id, {})
        last = data["last_params"].setdefault(tool_id, {})

        for key, raw in params.items():
            ftype = (field_types or {}).get(key, "text")
            if ftype not in _HISTORY_TYPES:
                continue
            val = _norm_value(raw)
            if not val:
                continue
            last[key] = val
            _push_unique(tool_bucket, key, val)
            _push_unique(global_b, key, val)

        _save_unlocked(data)


def get_field_suggestions(
    tool_id: str,
    field_key: str,
    *,
    limit: int = MAX_PER_KEY,
) -> list[str]:
    with _lock:
        data = _load_unlocked()
        seen: set[str] = set()
        out: list[str] = []

        def add_from(seq: list[str] | None) -> None:
            if not seq:
                return
            for v in seq:
                if not v or v in seen:
                    continue
                seen.add(v)
                out.append(v)
                if len(out) >= limit:
                    return

        tool_bucket = data.get("tools", {}).get(tool_id, {})
        add_from(tool_bucket.get(field_key) if isinstance(tool_bucket, dict) else None)
        if len(out) < limit:
            global_b = data.get("global", {})
            add_from(global_b.get(field_key) if isinstance(global_b, dict) else None)
        return out


def get_tool_history(tool_id: str) -> dict[str, Any]:
    """返回 ``{ suggestions: {key: [..]}, last_params: {key: val} }``。"""
    with _lock:
        data = _load_unlocked()
        tool_bucket = data.get("tools", {}).get(tool_id, {})
        if not isinstance(tool_bucket, dict):
            tool_bucket = {}
        global_b = data.get("global", {})
        if not isinstance(global_b, dict):
            global_b = {}

        suggestions: dict[str, list[str]] = {}
        keys = set(tool_bucket.keys()) | set(global_b.keys())
        last_raw = data.get("last_params", {}).get(tool_id, {})
        if isinstance(last_raw, dict):
            keys |= set(last_raw.keys())

        for key in keys:
            seen: set[str] = set()
            merged: list[str] = []

            def add_from(seq: list[str] | None) -> None:
                if not seq:
                    return
                for v in seq:
                    if not v or v in seen:
                        continue
                    seen.add(v)
                    merged.append(v)
                    if len(merged) >= MAX_PER_KEY:
                        return

            add_from(tool_bucket.get(key) if isinstance(tool_bucket.get(key), list) else None)
            if len(merged) < MAX_PER_KEY:
                add_from(global_b.get(key) if isinstance(global_b.get(key), list) else None)
            if merged:
                suggestions[key] = merged

        last_params = last_raw if isinstance(last_raw, dict) else {}
        return {"suggestions": suggestions, "last_params": last_params}


def clear_history(*, tool_id: str | None = None) -> None:
    with _lock:
        data = _load_unlocked()
        if tool_id is None:
            data = _empty_store()
        else:
            data.get("tools", {}).pop(tool_id, None)
            data.get("last_params", {}).pop(tool_id, None)
        _save_unlocked(data)
