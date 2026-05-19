#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""``voxel_pick_from_geo`` 通用管线单测。

在仓库根 ``fantasy_furniture`` 下执行::

    python tools/collision/test_voxel_pick_from_geo.py
"""

from __future__ import annotations

import json
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

_COLL = Path(__file__).resolve().parent
_TOOLS_ROOT = _COLL.parent
if str(_TOOLS_ROOT) not in sys.path:
    sys.path.insert(0, str(_TOOLS_ROOT))
if str(_COLL) not in sys.path:
    sys.path.insert(0, str(_COLL))

import voxel_pick_from_geo as m  # noqa: E402


class TestGeoPipeline(unittest.TestCase):
    def _minimal_geo(self) -> Path:
        data = {
            "format_version": "1.21.110",
            "minecraft:geometry": [
                {
                    "description": {
                        "identifier": "geometry.voxel_pick_test",
                        "texture_width": 16,
                        "texture_height": 16,
                    },
                    "bones": [
                        {
                            "name": "root",
                            "pivot": [0, 8, 0],
                            "cubes": [{"origin": [2, 6, 2], "size": [3, 2, 3]}],
                        }
                    ],
                }
            ],
        }
        tmp = Path(tempfile.mkdtemp()) / "test.geo.json"
        tmp.write_text(json.dumps(data), encoding="utf-8")
        return tmp

    def test_north_pick_boxes_single_cell_clip(self) -> None:
        p = self._minimal_geo()
        try:
            boxes = m.north_pick_boxes_from_geo(p, snap_half=False)
            self.assertTrue(len(boxes) >= 1)
            for b in boxes:
                x0, x1, y0, y1, z0, z1 = b
                self.assertGreaterEqual(x1 - x0, 0.5 - 1e-9)
                self.assertGreaterEqual(y1 - y0, 0.5 - 1e-9)
                self.assertGreaterEqual(z1 - z0, 0.5 - 1e-9)
                self.assertGreaterEqual(x0, 0)
                self.assertLessEqual(x1, 16)
                self.assertGreaterEqual(y0, 0)
                self.assertLessEqual(y1, 16)
                self.assertGreaterEqual(z0, 0)
                self.assertLessEqual(z1, 16)
        finally:
            shutil.rmtree(p.parent, ignore_errors=True)

    def test_emit_contains_provenance(self) -> None:
        p = self._minimal_geo()
        try:
            boxes = m.north_pick_boxes_from_geo(p, snap_half=False)
            text = m.emit_java_shapes_or(boxes, p, precision=2)
            self.assertIn("voxel_pick_from_geo:", text)
            self.assertIn("sha256[:12]=", text)
            self.assertIn("source_geo=", text)
            self.assertIn("geometry.voxel_pick_test", text)
            self.assertIn("Shapes.or", text)
            self.assertIn("Block.box", text)
        finally:
            shutil.rmtree(p.parent, ignore_errors=True)


class TestMarkdownExport(unittest.TestCase):
    def test_upsert_replaces_same_model_key(self) -> None:
        tmp = Path(tempfile.mkdtemp()) / "export.md"
        try:
            m.upsert_export_markdown(
                tmp, "model_a", "<!-- model: model_a -->\n## `model_a`\n\nv1\n\n<!-- /model: model_a -->\n"
            )
            m.upsert_export_markdown(
                tmp, "model_a", "<!-- model: model_a -->\n## `model_a`\n\nv2\n\n<!-- /model: model_a -->\n"
            )
            text = tmp.read_text(encoding="utf-8")
            self.assertEqual(text.count("<!-- model: model_a -->"), 1)
            self.assertIn("v2", text)
            self.assertNotIn("v1", text)
            self.assertIn("EXPORT_INDEX", text)
        finally:
            shutil.rmtree(tmp.parent, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()
