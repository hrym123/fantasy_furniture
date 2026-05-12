#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""``bed_plate6_voxel_pick_from_geo`` 单元测试。

在仓库根 ``fantasy_furniture`` 下执行::

    python tools/bed6/test_bed_plate6_voxel_pick_from_geo.py
"""

from __future__ import annotations

import json
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

_TOOLS_ROOT = Path(__file__).resolve().parent.parent
if str(_TOOLS_ROOT) not in sys.path:
    sys.path.insert(0, str(_TOOLS_ROOT))
from paths import FF_ROOT as ROOT  # noqa: E402

import bed_plate6_voxel_pick_from_geo as m  # noqa: E402


class TestClipAndExtent(unittest.TestCase):
    def test_clip_full_cell_outside(self) -> None:
        self.assertIsNone(m.clip_aabb_to_cell_full(17, 18, 0, 1, 0, 1))

    def test_clip_full_cell_inside(self) -> None:
        b = m.clip_aabb_to_cell_full(1, 3, 5, 5.2, 8, 10)
        self.assertIsNotNone(b)
        assert b is not None
        x0, x1, y0, y1, z0, z1 = b
        self.assertAlmostEqual(x0, 1)
        self.assertAlmostEqual(x1, 3)

    def test_ensure_min_extents_thin_y(self) -> None:
        b = (2.0, 14.0, 5.0, 5.2, 4.0, 12.0)
        e = m.ensure_min_extents_cell(b, min_extent=0.5)
        _, _, y0, y1, _, _ = e
        self.assertGreaterEqual(y1 - y0, 0.5 - 1e-9)

    def test_snap_half_grid(self) -> None:
        b = (1.11, 14.89, 0, 16, 2.34, 15.66)
        cell = (0.0, 16.0, 0.0, 16.0, 0.0, 16.0)
        s = m.snap_half_grid(b, cell)
        for v in (s[0], s[1], s[2], s[3], s[4], s[5]):
            self.assertAlmostEqual(v * 2, round(v * 2), places=6)


class TestGeoPipeline(unittest.TestCase):
    def _minimal_geo(self) -> Path:
        data = {
            "format_version": "1.21.110",
            "minecraft:geometry": [
                {
                    "description": {
                        "identifier": "geometry.bed_plate6_voxel_pick_test",
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

    def test_north_pick_boxes_non_empty(self) -> None:
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
                self.assertLessEqual(z1, 32)
        finally:
            shutil.rmtree(p.parent, ignore_errors=True)

    def test_emit_contains_provenance(self) -> None:
        p = self._minimal_geo()
        try:
            boxes = m.north_pick_boxes_from_geo(p, snap_half=False)
            text = m.emit_java_shapes_or(boxes, p, precision=2)
            self.assertIn("sha256[:12]=", text)
            self.assertIn("generated_utc=", text)
            self.assertIn("source_geo=", text)
            self.assertIn("geometry.bed_plate6_voxel_pick_test", text)
            self.assertIn("Shapes.or", text)
            self.assertIn("Block.box", text)
        finally:
            shutil.rmtree(p.parent, ignore_errors=True)


class TestRealAssetIfPresent(unittest.TestCase):
    def test_duvet_geo(self) -> None:
        geo = ROOT / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_duvet.geo.json"
        if not geo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        boxes = m.north_pick_boxes_from_geo(geo)
        self.assertTrue(len(boxes) >= 1)
        text = m.emit_java_shapes_or(boxes[:5], geo)
        self.assertIn("bed_plate6_duvet", geo.name)
        self.assertIn("sha256[:12]=", text)

    def test_pillow_large_striped_geo(self) -> None:
        geo = ROOT / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_large_striped.geo.json"
        if not geo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        boxes = m.north_pick_boxes_from_geo(geo)
        self.assertTrue(len(boxes) >= 1)
        self.assertTrue(any(b[5] > 16.0 for b in boxes), "至少一选取盒应落在 z>16（床头半段）")


if __name__ == "__main__":
    unittest.main()
