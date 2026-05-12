#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将 Blockbench .bbmodel 导出为「幻想家具」模组可直接引用的资源：

- ``geo/block/<asset_id>.geo.json``
- ``textures/block/<asset_id>.png``；多槽位时默认还会写 ``<asset_id>_tex<N>.png``
- ``animations/.../<asset_id>.animation.json``：**仅当** bbmodel 有内嵌动画数据，或 ``geckolib_filepath_cache.animation`` 指向已存在的 JSON 时才写入（否则跳过，静态窗不会生成占位）。``--delete-stale-animation`` 可在无动画时删除目标路径旧文件。
- 窗户等多造型共用贴图：``--shared-textures plain_glass_window`` → ``textures/block/plain_glass_window_<槽>_<颜色>.png``（颜色由像素主色推断；**导出开始前会删除** ``textures/block/plain_glass_window_*.png``，避免旧占位图与新文件名并存导致「九张看起来一样」；须同步 Java ``PlainGlassWindowSharedTextures.TEXTURE_STEMS``）。勿与 ``--only-primary-texture`` 联用除非只需槽 0。

几何转换默认使用同目录下的 ``bbmodel_to_geojson.py``（含分组骨骼 ``rotation``）；也可用
``--moonstar-tools`` 指向其它目录中的同名脚本。
与《C009-Blockbench-bbmodel-导出GeckoLib-geo与贴图-调查》一致：本流程为务实折中，与 Blockbench 官方 GeckoLib
插件逐字节一致不保证；复杂 mesh / 多贴图 UV 请以插件导出为准并对照 golden。

**普通玻璃窗导出核对（模型「不对」时优先看）**

- **原则**：普通玻璃窗在 **Java 客户端不得做任何姿态/渲染补偿**（不覆盖 ``GeoBlockRenderer#rotateBlock``、不追加 ``mulPose``）。与 Gecko / MC 方块朝向、倾角、薄片位置的差异，**只能**通过改 ``bbmodel_to_geojson.py`` 的 geo 生成（或直接改已生成的 ``geo/block/plain_glass_window_shape_*.geo.json``）解决；改 geo 后须对对应文件运行 ``tools/collision/geo_collision_box.py`` 更新 ``PlainGlassWindowBlock`` 碰撞。
- **分组旋转 / 立方体原点**：``bbmodel_to_geojson.py`` 按 Blockbench 源码 ``js/formats/bedrock/bedrock.js`` 的 ``compileGroup`` / ``compileCube`` 写骨骼 ``pivot``/``rotation`` 与 cube ``origin``，与 Bedrock 导出及预览一致；仍不一致时用 Blockbench 官方 Bedrock 导出对照。
- **薄片窗扇**：Bedrock geo 里若某一轴 ``size`` 极小（如 0.4），对应 Blockbench 里窗厚所在轴；放置后应对齐 MC 水平 ``facing`` 与 Blockbench 前向约定。
- **多槽位贴图**：非 0 号槽的 UV 在本流程可能不完整；若 BB 里多张贴图而游戏里只认一张，请用 Blockbench GeckoLib 插件导出 geo，或合并为单张 atlas 再导出。
- **九张 PNG**：``--shared-textures plain_glass_window`` 会按槽位主色命名；须与 Java ``PlainGlassWindowSharedTextures.TEXTURE_STEMS`` 一致，否则绑定会错图。

示例::

  python tools/blockbench/export_bbmodel_to_fantasy_furniture_assets.py \\
    "D:/warehouse/MoonStarfish素材/窗户/普通窗户90°.bbmodel" \\
    --asset-id plain_glass_window_shape_90 \\
    --shared-textures plain_glass_window

  python tools/blockbench/export_bbmodel_to_fantasy_furniture_assets.py model.bbmodel --dry-run
"""

from __future__ import annotations

import argparse
import base64
import importlib.util
import json
import re
import shutil
import sys
from pathlib import Path
from typing import Any

DATA_URL_RE = re.compile(
    r"^data:image/(?P<fmt>png|jpeg|jpg|webp);base64,(?P<b64>.+)$",
    re.IGNORECASE | re.DOTALL,
)

_TOOLS_BOOT = Path(__file__).resolve().parent.parent
if str(_TOOLS_BOOT) not in sys.path:
    sys.path.insert(0, str(_TOOLS_BOOT))
from paths import FF_ROOT, TOOLS_ROOT  # noqa: E402

DEFAULT_ASSETS = FF_ROOT / "src/main/resources/assets/fantasy_furniture"


def _plain_glass_texture_file(
    textures_out: Path, slot: int, ext: str, *, raw: bytes | None = None, src_file: Path | None = None
) -> Path:
    """``--shared-textures plain_glass_window``：``textures/block/plain_glass_window_<槽>_<颜色>.png``。"""
    _glass = TOOLS_ROOT / "glass"
    for _p in (TOOLS_ROOT, _glass):
        if str(_p) not in sys.path:
            sys.path.insert(0, str(_p))
    from plain_glass_window_texture_naming import (
        dominant_rgb,
        dominant_rgb_from_bytes,
        texture_stem_from_rgb,
    )

    if raw is not None:
        rgb = dominant_rgb_from_bytes(raw)
    elif src_file is not None:
        rgb = dominant_rgb(src_file)
    else:
        raise ValueError("plain_glass_window naming requires raw or src_file")
    stem = texture_stem_from_rgb(slot, rgb)
    return textures_out / f"{stem}.{ext}"


def ext_from_format(file_format: str | None) -> str:
    if not file_format:
        return "png"
    f = file_format.lower().strip(".")
    if f in ("jpeg", "jpg"):
        return "jpg"
    return f if f in ("png", "webp") else "png"


def decode_data_url(source: str) -> tuple[bytes, str] | None:
    m = DATA_URL_RE.match(source.strip())
    if not m:
        return None
    fmt = m.group("fmt").lower()
    ext = "jpg" if fmt in ("jpeg", "jpg") else fmt
    try:
        raw = base64.b64decode(m.group("b64"), validate=True)
    except Exception:
        raw = base64.b64decode(m.group("b64"), validate=False)
    return raw, ext


def resolve_moonstar_tools(explicit: Path | None) -> Path:
    ff_tools = Path(__file__).resolve().parent
    if explicit is not None:
        p = explicit.expanduser().resolve()
        if not (p / "bbmodel_to_geojson.py").is_file():
            raise SystemExit(f"未找到 bbmodel_to_geojson.py: {p}")
        return p
    if (ff_tools / "bbmodel_to_geojson.py").is_file():
        return ff_tools
    start = Path(__file__).resolve()
    for anc in [start.parent, *start.parents]:
        cand = anc / "MoonStarfish素材" / "tools" / "bbmodel_to_geojson.py"
        if cand.is_file():
            return cand.parent
    raise SystemExit(
        "未自动找到 MoonStarfish素材/tools/bbmodel_to_geojson.py；"
        "请用 --moonstar-tools 指定包含该文件的目录。"
    )


def load_bbmodel_geo_module(tools_dir: Path) -> Any:
    path = tools_dir / "bbmodel_to_geojson.py"
    spec = importlib.util.spec_from_file_location("_bbmodel_to_geojson_ff_export", path)
    if spec is None or spec.loader is None:
        raise SystemExit(f"无法加载模块: {path}")
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


ASSET_ID_RE = re.compile(r"^[a-z0-9][a-z0-9._-]*$")


def derive_asset_id(bbmodel_path: Path, override: str | None) -> str:
    if override is not None:
        aid = override.strip()
        if not ASSET_ID_RE.match(aid):
            raise SystemExit(
                f"--asset-id 须为合法资源名片段（小写字母数字与 ._-，且不以 . 或 - 开头）: {aid!r}"
            )
        return aid
    stem = bbmodel_path.stem
    candidate = stem.lower().replace(" ", "_")
    if ASSET_ID_RE.match(candidate):
        return candidate
    raise SystemExit(
        f"无法从文件名推导 asset-id（请显式传入 --asset-id）: {bbmodel_path.name!r}"
    )


def export_textures_to_mod(
    data: dict[str, Any],
    bbmodel_path: Path,
    textures_out: Path,
    asset_id: str,
    *,
    dry_run: bool,
    only_primary_texture: bool = False,
    shared_textures_key: str | None = None,
) -> list[Path]:
    """将 bbmodel 内 textures 写入模组 textures/block；返回已写入路径列表。"""
    textures = data.get("textures")
    if not isinstance(textures, list):
        print("无 textures 数组，跳过贴图导出。", file=sys.stderr)
        return []

    written: list[Path] = []
    base_dir = bbmodel_path.parent
    plain_glass_flat = shared_textures_key == "plain_glass_window"

    if not dry_run:
        textures_out.mkdir(parents=True, exist_ok=True)
        if shared_textures_key and not plain_glass_flat:
            (textures_out / shared_textures_key).mkdir(parents=True, exist_ok=True)
        # 避免旧占位（如全 _white）与新主色文件名并存，导致误以为「导出全是同一张」
        if plain_glass_flat:
            for stale in sorted(textures_out.glob("plain_glass_window_*.png")):
                stale.unlink()

    for idx, tex in enumerate(textures):
        if only_primary_texture and idx != 0:
            continue
        if not isinstance(tex, dict):
            continue

        source = tex.get("source")
        rel_path = tex.get("path")
        file_format = tex.get("file_format")
        written_one = False

        def out_path_for_ext(
            ext: str, *, raw: bytes | None = None, src: Path | None = None
        ) -> Path:
            if plain_glass_flat:
                return _plain_glass_texture_file(textures_out, idx, ext, raw=raw, src_file=src)
            if shared_textures_key:
                return textures_out / shared_textures_key / f"t{idx}.{ext}"
            if idx == 0:
                return textures_out / f"{asset_id}.{ext}"
            return textures_out / f"{asset_id}_tex{idx}.{ext}"

        if isinstance(source, str) and source.startswith("data:"):
            decoded = decode_data_url(source)
            if decoded is None:
                print(f"无法解析 data URL: [#{idx}]", file=sys.stderr)
                continue
            raw, ext = decoded
            out_file = out_path_for_ext(ext, raw=raw) if plain_glass_flat else out_path_for_ext(ext)
            if dry_run:
                print(f"[dry-run] texture -> {out_file}")
            else:
                out_file.write_bytes(raw)
                print(f"OK texture -> {out_file.relative_to(FF_ROOT)}")
            written.append(out_file)
            written_one = True

        if not written_one and isinstance(rel_path, str) and rel_path.strip():
            src_file = (base_dir / rel_path).resolve()
            if src_file.is_file():
                ext = src_file.suffix.lower().lstrip(".") or ext_from_format(file_format)
                if ext not in ("png", "jpg", "jpeg", "webp"):
                    ext = ext_from_format(file_format)
                out_file = (
                    out_path_for_ext(ext, src=src_file)
                    if plain_glass_flat
                    else out_path_for_ext(ext)
                )
                if dry_run:
                    print(f"[dry-run] copy {src_file} -> {out_file}")
                else:
                    shutil.copy2(src_file, out_file)
                    print(f"OK texture (copy) -> {out_file.relative_to(FF_ROOT)}")
                written.append(out_file)
                written_one = True

        if not written_one:
            print(
                f"跳过贴图槽 #{idx}（无内嵌 data URL 且外部 path 不可用）",
                file=sys.stderr,
            )

    return written


def bbmodel_has_animation(data: dict[str, Any]) -> bool:
    """
    GeckoLib / Blockbench 5 bbmodel：动画可能在顶层 ``animations``，或在 ``timeline_setups`` 中。
    二者皆空则视为无动画（静态模型不应再导出占位 ``.animation.json``）。
    """
    anim = data.get("animations")
    if isinstance(anim, dict) and len(anim) > 0:
        return True
    if isinstance(anim, list) and len(anim) > 0:
        return True
    ts = data.get("timeline_setups")
    if not isinstance(ts, list):
        return False
    for t in ts:
        if not isinstance(t, dict) or not t:
            continue
        for key in ("keyframes", "channels", "animators", "bones", "data_points"):
            v = t.get(key)
            if isinstance(v, dict) and len(v) > 0:
                return True
            if isinstance(v, list) and len(v) > 0:
                return True
    return False


def export_animation_json(
    data: dict[str, Any],
    asset_id: str,
    anim_out: Path,
    geo_mod: Any | None,
    *,
    dry_run: bool,
    delete_stale: bool,
) -> None:
    """
    写出动画 JSON 的条件（任一满足即导出，否则跳过并可删陈旧占位）：

    - ``geckolib_filepath_cache.animation`` 指向**已存在**的 ``.animation.json``（常见：Blockbench 外链导出，bbmodel 内无时间轴）；
    - 或 bbmodel 内嵌动画数据（``bbmodel_has_animation``），此时优先调用 ``bbmodel_to_animation_json``（若模块提供）。
    """
    dest = anim_out / f"{asset_id}.animation.json"
    try:
        rel = dest.relative_to(FF_ROOT)
    except ValueError:
        rel = dest

    cache = data.get("geckolib_filepath_cache")
    cache_anim: Path | None = None
    if isinstance(cache, dict):
        ap = cache.get("animation")
        if isinstance(ap, str) and ap.strip():
            cp = Path(ap.strip()).expanduser()
            if cp.is_file():
                cache_anim = cp

    has_embedded = bbmodel_has_animation(data)

    if cache_anim is None and not has_embedded:
        if delete_stale and dest.is_file():
            if dry_run:
                print(f"[dry-run] delete stale animation -> {rel}")
            else:
                dest.unlink()
                print(f"OK 删除无动画时的旧文件 -> {rel}")
        else:
            print(f"跳过 animation（无内嵌动画且无 geckolib_filepath_cache.animation 文件）: {rel}")
        return

    # 内嵌动画优先走转换器；否则复制 Blockbench 外链文件
    fn = getattr(geo_mod, "bbmodel_to_animation_json", None) if geo_mod is not None else None
    if has_embedded and callable(fn):
        try:
            doc = fn(data, asset_id=asset_id)
        except TypeError:
            doc = fn(data)
        if not isinstance(doc, dict):
            print("bbmodel_to_animation_json 未返回 dict，跳过动画导出。", file=sys.stderr)
        else:
            body = json.dumps(doc, ensure_ascii=False, indent="\t") + "\n"
            if dry_run:
                print(f"[dry-run] animation -> {rel}")
            else:
                anim_out.mkdir(parents=True, exist_ok=True)
                dest.write_text(body, encoding="utf-8", newline="\n")
                print(f"OK animation -> {rel}")
        return

    if cache_anim is not None:
        if dry_run:
            print(f"[dry-run] copy animation {cache_anim} -> {rel}")
        else:
            anim_out.mkdir(parents=True, exist_ok=True)
            shutil.copy2(cache_anim, dest)
            print(f"OK animation (copy) -> {rel}")
        return

    if has_embedded:
        print(
            "bbmodel 含内嵌动画数据但未实现写出：请在 ``bbmodel_to_geojson`` 模块提供 ``bbmodel_to_animation_json``，"
            "或在 Blockbench 将动画导出为 JSON 后填入 ``geckolib_filepath_cache.animation``。"
            f"未写入: {rel}",
            file=sys.stderr,
        )


def print_java_hint(
    asset_id: str, modid: str, *, shared_textures_key: str | None = None
) -> None:
    print()
    print("Java / GeckoLib 引用示例（请按实际类名调整）：")
    print(
        f'  ResourceLocation.fromNamespaceAndPath("{modid}", "geo/block/{asset_id}.geo.json")'
    )
    if shared_textures_key == "plain_glass_window":
        print(
            f'  ResourceLocation.fromNamespaceAndPath("{modid}", '
            f'"textures/block/plain_glass_window_0_white.png")  # 示例主槽；颜色后缀以导出时为准，须与 PlainGlassWindowSharedTextures.TEXTURE_STEMS 一致'
        )
    elif shared_textures_key:
        print(
            f'  ResourceLocation.fromNamespaceAndPath("{modid}", '
            f'"textures/block/{shared_textures_key}/t0.png")  # 主槽'
        )
    else:
        print(
            f'  ResourceLocation.fromNamespaceAndPath("{modid}", "textures/block/{asset_id}.png")'
        )
    print()
    if shared_textures_key == "plain_glass_window":
        print(
            f'  ResourceLocation.fromNamespaceAndPath("{modid}", '
            f'"animations/block/geolib_static.animation.json")  # 普通玻璃窗多造型共用静态动画，勿再为每造型保留 {asset_id}.animation.json'
        )
    else:
        print(
            f'  ResourceLocation.fromNamespaceAndPath("{modid}", "animations/block/{asset_id}.animation.json")'
            f"  # 仅当 bbmodel 含动画且本次已导出时存在"
        )
    print()
    print(
        "注意：bbmodel_to_geojson 当前仅对面使用 texture 索引 0 的 UV 做完整映射；"
        "多贴图模型请用 Blockbench 插件导出或合并 atlas。"
    )


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(
        description="将 .bbmodel 导出到 fantasy_furniture 的 geo/block 与 textures/block。"
    )
    p.add_argument(
        "bbmodel",
        type=Path,
        help="单个 .bbmodel 文件路径",
    )
    p.add_argument(
        "--asset-id",
        default=None,
        help=(
            "资源 basename（小写 snake_case），用于 geometry 标识与输出文件名；"
            "省略时仅在 .bbmodel 文件名本身已是合法资源名时自动推导"
        ),
    )
    p.add_argument(
        "--assets-root",
        type=Path,
        default=DEFAULT_ASSETS,
        help=f"assets/fantasy_furniture 根目录（默认: {DEFAULT_ASSETS}）",
    )
    p.add_argument(
        "--geo-subdir",
        default="geo/block",
        help="geo 相对 assets 根的子目录（默认 geo/block）",
    )
    p.add_argument(
        "--textures-subdir",
        default="textures/block",
        help="贴图相对 assets 根的子目录（默认 textures/block）",
    )
    p.add_argument(
        "--animations-subdir",
        default="animations/block",
        help="动画 JSON 相对 assets 根的子目录（默认 animations/block）",
    )
    p.add_argument(
        "--skip-animation",
        action="store_true",
        help="不处理 animations（不写入、不删除）",
    )
    p.add_argument(
        "--delete-stale-animation",
        action="store_true",
        help="当 bbmodel 无动画时，若目标 ``<asset_id>.animation.json`` 已存在则删除（清理旧占位）",
    )
    p.add_argument(
        "--moonstar-tools",
        type=Path,
        default=None,
        help="包含 bbmodel_to_geojson.py 的目录（默认向上查找 MoonStarfish素材/tools）",
    )
    p.add_argument(
        "--format-version",
        default="1.21.110",
        help='geo.json format_version（默认与仓库内样本一致 "1.21.110"）',
    )
    p.add_argument(
        "--geometry-prefix",
        default="geometry.",
        help='geometry identifier 前缀（默认 "geometry."）',
    )
    p.add_argument(
        "--modid",
        default="fantasy_furniture",
        help="打印 Java 示例时使用的命名空间",
    )
    p.add_argument(
        "--skip-geo",
        action="store_true",
        help="只导出贴图，不写 geo.json",
    )
    p.add_argument(
        "--skip-textures",
        action="store_true",
        help="只写 geo.json，不导出贴图",
    )
    p.add_argument(
        "--only-primary-texture",
        action="store_true",
        help="仅导出 textures[0]，不导出后续槽位（与 per-asset 命名或 --shared-textures 联用）",
    )
    p.add_argument(
        "--shared-textures",
        default=None,
        metavar="KEY",
        help=(
            "将所有贴图槽写入 textures/block/<KEY>/t<N>.ext，供多 geo 共用；"
            "普通玻璃窗请用 plain_glass_window（写入 plain_glass_window_<槽>_<颜色>.png，见 plain_glass_window_texture_naming.py）"
        ),
    )
    p.add_argument(
        "-n",
        "--dry-run",
        action="store_true",
        help="只打印将写入的路径",
    )
    return p.parse_args()


def main() -> int:
    args = parse_args()
    bbmodel_path = args.bbmodel.expanduser().resolve()
    if not bbmodel_path.is_file() or bbmodel_path.suffix.lower() != ".bbmodel":
        print(f"不是有效的 .bbmodel 文件: {bbmodel_path}", file=sys.stderr)
        return 1

    asset_id = derive_asset_id(bbmodel_path, args.asset_id)
    assets_root = args.assets_root.expanduser().resolve()
    geo_dir = assets_root / args.geo_subdir
    tex_dir = assets_root / args.textures_subdir
    anim_dir = assets_root / args.animations_subdir

    try:
        data = json.loads(bbmodel_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as e:
        print(f"无法读取或解析: {bbmodel_path}: {e}", file=sys.stderr)
        return 1

    # 强制与输出文件名、geometry.* 一致
    data["model_identifier"] = asset_id

    shared_key: str | None = None
    if isinstance(args.shared_textures, str):
        raw = args.shared_textures.strip()
        if raw:
            if not ASSET_ID_RE.match(raw):
                raise SystemExit(
                    f"--shared-textures 须为合法资源名片段（与 --asset-id 规则相同）: {raw!r}"
                )
            shared_key = raw

    if not args.skip_textures:
        export_textures_to_mod(
            data,
            bbmodel_path,
            tex_dir,
            asset_id,
            dry_run=args.dry_run,
            only_primary_texture=args.only_primary_texture,
            shared_textures_key=shared_key,
        )

    geo_mod = None
    if not args.skip_geo or not args.skip_animation:
        tools_dir = resolve_moonstar_tools(args.moonstar_tools)
        geo_mod = load_bbmodel_geo_module(tools_dir)

    if not args.skip_geo:
        try:
            geo = geo_mod.bbmodel_to_geo(
                data,
                format_version=args.format_version,
                geometry_prefix=args.geometry_prefix,
            )
        except ValueError as e:
            print(f"转换 geo 失败: {e}", file=sys.stderr)
            return 1

        out_file = geo_dir / f"{asset_id}.geo.json"
        body = json.dumps(geo, ensure_ascii=False, indent="\t") + "\n"
        if args.dry_run:
            print(f"[dry-run] geo -> {out_file}")
        else:
            geo_dir.mkdir(parents=True, exist_ok=True)
            out_file.write_text(body, encoding="utf-8", newline="\n")
            print(f"OK geo -> {out_file.relative_to(FF_ROOT)}")

    if not args.skip_animation:
        export_animation_json(
            data,
            asset_id,
            anim_dir,
            geo_mod,
            dry_run=args.dry_run,
            delete_stale=args.delete_stale_animation,
        )

    print_java_hint(asset_id, args.modid, shared_textures_key=shared_key)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
