# -*- coding: utf-8 -*-
"""本地文件与 bbmodel 导出预览（供 Web 预览面板）。"""
from __future__ import annotations

import base64
import importlib.util
import json
import mimetypes
import tempfile
from pathlib import Path
from typing import Any

from paths import DEFAULT_ASSETS, TOOLS_ROOT
_IMAGE_SUFFIXES = {".png", ".jpg", ".jpeg", ".webp", ".gif", ".bmp"}
_JSON_SUFFIXES = {".json", ".bbmodel"}
_MAX_JSON_CHARS = 280_000
_MAX_IMAGE_BYTES = 12 * 1024 * 1024


def _load_export_bbmodel_module() -> Any:
    script = TOOLS_ROOT / "blockbench" / "export_bbmodel_to_fantasy_furniture_assets.py"
    spec = importlib.util.spec_from_file_location("export_bbmodel_ff", script)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"无法加载导出脚本: {script}")
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def _bool_param(params: dict[str, Any], key: str) -> bool:
    v = params.get(key)
    if isinstance(v, bool):
        return v
    if v is None:
        return False
    return str(v).lower() in ("1", "true", "yes", "on")


def _str_param(params: dict[str, Any], key: str) -> str:
    v = params.get(key)
    if v is None:
        return ""
    return str(v).strip()


def resolve_assets_root(params: dict[str, Any]) -> Path:
    ar = _str_param(params, "assets_root")
    if ar:
        return Path(ar).expanduser().resolve()
    return DEFAULT_ASSETS.resolve()


def _mime_for_path(path: Path) -> str:
    guessed, _ = mimetypes.guess_type(path.name)
    if guessed:
        return guessed
    ext = path.suffix.lower()
    if ext == ".png":
        return "image/png"
    if ext in (".jpg", ".jpeg"):
        return "image/jpeg"
    if ext == ".webp":
        return "image/webp"
    if ext == ".gif":
        return "image/gif"
    return "application/octet-stream"


def _item_from_file(file_path: Path, *, name: str, target: Path) -> dict[str, Any]:
    suffix = file_path.suffix.lower()
    rel_name = name.replace("\\", "/")
    target_s = str(target).replace("\\", "/")
    item: dict[str, Any] = {
        "name": rel_name,
        "target": target_s,
        "size": file_path.stat().st_size,
    }
    if suffix in _IMAGE_SUFFIXES:
        raw = file_path.read_bytes()
        if len(raw) > _MAX_IMAGE_BYTES:
            item["kind"] = "binary"
            item["message"] = f"图片过大（>{_MAX_IMAGE_BYTES // 1024 // 1024}MB），无法在界面预览"
            return item
        mime = _mime_for_path(file_path)
        b64 = base64.standard_b64encode(raw).decode("ascii")
        item["kind"] = "image"
        item["data_url"] = f"data:{mime};base64,{b64}"
        return item
    if suffix in _JSON_SUFFIXES or file_path.name.endswith(".geo.json"):
        text = file_path.read_text(encoding="utf-8")
        truncated = False
        if len(text) > _MAX_JSON_CHARS:
            text = text[:_MAX_JSON_CHARS] + "\n…（预览已截断）"
            truncated = True
        item["kind"] = "json"
        item["text"] = text
        item["truncated"] = truncated
        return item
    item["kind"] = "binary"
    item["message"] = "此类型仅显示目标路径"
    return item


def preview_local_file(path_str: str) -> dict[str, Any]:
    if not path_str or not str(path_str).strip():
        return {"ok": False, "error": "未填写路径"}
    p = Path(path_str).expanduser()
    try:
        p = p.resolve()
    except OSError:
        return {"ok": False, "error": f"无效路径: {path_str}"}
    if not p.is_file():
        return {"ok": False, "error": f"文件不存在: {p}"}
    try:
        item = _item_from_file(p, name=p.name, target=p)
    except OSError as e:
        return {"ok": False, "error": str(e)}
    return {"ok": True, "mode": "file", "items": [item]}


def preview_export_bbmodel(params: dict[str, Any]) -> dict[str, Any]:
    path_str = _str_param(params, "path")
    if not path_str:
        return {"ok": False, "error": "请选择 .bbmodel 文件"}
    bbmodel_path = Path(path_str).expanduser()
    try:
        bbmodel_path = bbmodel_path.resolve()
    except OSError:
        return {"ok": False, "error": f"无效路径: {path_str}"}
    if not bbmodel_path.is_file() or bbmodel_path.suffix.lower() != ".bbmodel":
        return {"ok": False, "error": f"不是有效的 .bbmodel: {bbmodel_path}"}

    exp = _load_export_bbmodel_module()
    aid_override = _str_param(params, "asset_id") or None
    try:
        asset_id = exp.derive_asset_id(bbmodel_path, aid_override)
    except SystemExit as e:
        return {"ok": False, "error": str(e)}

    shared_key: str | None = None
    sh = _str_param(params, "shared")
    if sh:
        if not exp.ASSET_ID_RE.match(sh):
            return {"ok": False, "error": f"共享贴图键不合法: {sh!r}"}
        shared_key = sh

    skip_geo = _bool_param(params, "skip_geo")
    skip_textures = _bool_param(params, "skip_textures")
    only_primary = _bool_param(params, "only_primary")
    del_anim = _bool_param(params, "del_anim")

    try:
        data = json.loads(bbmodel_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as e:
        return {"ok": False, "error": f"无法读取 bbmodel: {e}"}

    data["model_identifier"] = asset_id
    messages: list[str] = []
    target_root = resolve_assets_root(params)

    with tempfile.TemporaryDirectory(prefix="ff_bbmodel_preview_") as td:
        assets_root = Path(td) / "fantasy_furniture"
        geo_dir = assets_root / "geo" / "block"
        tex_dir = assets_root / "textures" / "block"
        anim_dir = assets_root / "animations" / "block"

        if not skip_textures:
            exp.export_textures_to_mod(
                data,
                bbmodel_path,
                tex_dir,
                asset_id,
                dry_run=False,
                only_primary_texture=only_primary,
                shared_textures_key=shared_key,
            )

        geo_mod = None
        try:
            tools_dir = exp.resolve_moonstar_tools(None)
            geo_mod = exp.load_bbmodel_geo_module(tools_dir)
        except SystemExit as e:
            if not skip_geo:
                return {"ok": False, "error": str(e)}

        if not skip_geo:
            try:
                geo = geo_mod.bbmodel_to_geo(data)
            except ValueError as e:
                return {"ok": False, "error": f"转换 geo 失败: {e}"}
            out_file = geo_dir / f"{asset_id}.geo.json"
            body = json.dumps(geo, ensure_ascii=False, indent="\t") + "\n"
            geo_dir.mkdir(parents=True, exist_ok=True)
            out_file.write_text(body, encoding="utf-8", newline="\n")
            messages.append(f"geo → {asset_id}.geo.json")

        exp.export_animation_json(
            data,
            asset_id,
            anim_dir,
            geo_mod,
            dry_run=False,
            delete_stale=del_anim,
        )

        items: list[dict[str, Any]] = []
        if not assets_root.exists():
            return {
                "ok": True,
                "mode": "export_bbmodel",
                "asset_id": asset_id,
                "items": [],
                "messages": messages or ["未生成任何文件（可能全部跳过）"],
            }

        for f in sorted(assets_root.rglob("*")):
            if not f.is_file():
                continue
            rel = f.relative_to(assets_root)
            target = target_root / rel
            items.append(_item_from_file(f, name=str(rel), target=target))

    return {
        "ok": True,
        "mode": "export_bbmodel",
        "asset_id": asset_id,
        "assets_root": str(target_root).replace("\\", "/"),
        "items": items,
        "messages": messages,
    }


def preview_tool(tool_id: str, params: dict[str, Any]) -> dict[str, Any]:
    if tool_id == "export_bbmodel":
        return preview_export_bbmodel(params)

    for key in ("path", "bbmodel"):
        p = _str_param(params, key)
        if p:
            suffix = Path(p).suffix.lower()
            if suffix in _IMAGE_SUFFIXES or suffix in _JSON_SUFFIXES or p.lower().endswith(
                ".geo.json"
            ):
                result = preview_local_file(p)
                if result.get("ok"):
                    result["mode"] = "file"
                return result
    return {"ok": True, "mode": "none", "items": [], "messages": ["填写 JSON 或图片路径后可预览"]}


def primary_preview_field(tool_id: str) -> str | None:
    if tool_id == "export_bbmodel":
        return "path"
    return "path"


def tool_supports_export_preview(tool_id: str) -> bool:
    return tool_id == "export_bbmodel"
