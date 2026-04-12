#!/usr/bin/env python3
"""Compare key sets between zh_cn.json and en_us.json (Minecraft lang files)."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


def _flatten_keys(obj: object, prefix: str = "") -> list[str]:
    if isinstance(obj, dict):
        out: list[str] = []
        for k in sorted(obj.keys()):
            p = f"{prefix}.{k}" if prefix else str(k)
            v = obj[k]
            if isinstance(v, dict):
                out.extend(_flatten_keys(v, p))
            else:
                out.append(p)
        return out
    return []


def main() -> int:
    root = Path(__file__).resolve().parent.parent
    default_dir = root / "src" / "main" / "resources" / "assets" / "fantasy_furniture" / "lang"

    p = argparse.ArgumentParser(description="Ensure zh_cn.json and en_us.json have identical key sets.")
    p.add_argument(
        "--lang-dir",
        type=Path,
        default=default_dir,
        help=f"Directory containing zh_cn.json and en_us.json (default: {default_dir})",
    )
    args = p.parse_args()
    lang_dir: Path = args.lang_dir

    zh_path = lang_dir / "zh_cn.json"
    en_path = lang_dir / "en_us.json"

    for path in (zh_path, en_path):
        if not path.is_file():
            print(f"error: missing file: {path}", file=sys.stderr)
            return 2

    with zh_path.open(encoding="utf-8") as f:
        zh = json.load(f)
    with en_path.open(encoding="utf-8") as f:
        en = json.load(f)

    zh_keys = set(_flatten_keys(zh))
    en_keys = set(_flatten_keys(en))

    only_zh = sorted(zh_keys - en_keys)
    only_en = sorted(en_keys - zh_keys)

    if not only_zh and not only_en:
        print(f"OK: {len(zh_keys)} keys match in zh_cn.json and en_us.json")
        return 0

    print("Language key mismatch between zh_cn.json and en_us.json:", file=sys.stderr)
    if only_zh:
        print("\nKeys only in zh_cn.json:", file=sys.stderr)
        for k in only_zh:
            print(f"  + {k}", file=sys.stderr)
    if only_en:
        print("\nKeys only in en_us.json:", file=sys.stderr)
        for k in only_en:
            print(f"  - {k}", file=sys.stderr)
    return 1


if __name__ == "__main__":
    sys.exit(main())
