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


def _box4(b: tuple[float, ...]) -> tuple[float, ...]:
    return tuple(round(v, 4) for v in b)


class TestJavaConstantSync(unittest.TestCase):
    """与 BedPlate6PickShapesNorth 粘贴值一致，防止脚本已更新而 Java 未跟贴。"""

    def test_medium_pair_front_matches_java(self) -> None:
        geo = (
            ROOT
            / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_medium_pair_front.geo.json"
        )
        if not geo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        boxes = m.north_pick_boxes_from_geo(geo)
        self.assertEqual(len(boxes), 1)
        self.assertEqual(_box4(boxes[0]), (5.0, 15.0, 6.2284, 13.4609, 24.7042, 29.2307))

    def test_medium_pair_rear_matches_java(self) -> None:
        geo = (
            ROOT
            / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_medium_pair_rear.geo.json"
        )
        if not geo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        boxes = m.north_pick_boxes_from_geo(geo)
        self.assertEqual(len(boxes), 1)
        self.assertEqual(_box4(boxes[0]), (1.0, 11.0, 7.06, 14.06, 28.7, 30.7))

    def test_duvet_cover_matches_java(self) -> None:
        geo = (
            ROOT
            / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_duvet_cover.geo.json"
        )
        if not geo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        boxes = m.north_pick_boxes_from_geo(geo)
        self.assertEqual(len(boxes), 1)
        self.assertEqual(
            _box4(boxes[0]),
            (0.0, 16.0, 3.5522, 9.4, 0.0, 24.0),
        )

    def test_small_stack_matches_java(self) -> None:
        geo = (
            ROOT
            / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_small_stack.geo.json"
        )
        if not geo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        boxes = m.north_pick_boxes_from_geo(geo)
        self.assertEqual(
            [_box4(b) for b in boxes],
            [
                (0.4, 4.4, 7.3085, 11.0422, 27.2706, 28.8937),
                (0.9, 3.9, 7.6173, 10.7716, 27.0, 29.0719),
            ],
        )

    def test_solo_not_mirror_x_whitelisted(self) -> None:
        self.assertNotIn(
            "bed_plate6_pillow_medium_solo",
            m.PICK_MIRROR_X_NORTH_MODELS,
        )

    def test_mirror_x_only_whitelisted_models(self) -> None:
        geo_dir = ROOT / "src/main/resources/assets/fantasy_furniture/geo/block"
        solo = geo_dir / "bed_plate6_pillow_medium_solo.geo.json"
        if not solo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        raw_solo = m.north_pick_boxes_raw_from_geo(solo)
        out_solo = m.north_pick_boxes_from_geo(solo)
        self.assertEqual(raw_solo[0], out_solo[0])
        front = geo_dir / "bed_plate6_pillow_medium_pair_front.geo.json"
        if not front.is_file():
            self.skipTest("仓库内无该 geo 资源")
        raw_f = m.north_pick_boxes_raw_from_geo(front)[0]
        prepared_f = m.ensure_min_extents_bounds(raw_f, m.BED_PLATE6_PICK_CLIP_DEFAULT)
        out_f = m.north_pick_boxes_from_geo(front)[0]
        self.assertEqual(m.mirror_x_box(prepared_f, 0.0, 16.0), out_f)


class TestPickRenderAligned(unittest.TestCase):
    def test_pair_front_axis_aligned_tilted_has_depth_z(self) -> None:
        geo = (
            ROOT
            / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_medium_pair_front.geo.json"
        )
        if not geo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        boxes = m.north_pick_boxes_from_geo(geo)
        self.assertEqual(len(boxes), 1)
        x0, x1, y0, y1, z0, z1 = boxes[0]
        self.assertGreater(y1 - y0, 5.0)
        self.assertGreater(z1 - z0, 4.0)
        self.assertAlmostEqual(x1 - x0, 10.0, places=1)
        self.assertGreater(x0, 4.0)
        self.assertGreater(x1, 14.0)

    def test_pair_rear_axis_aligned_from_geo_no_pick_hack(self) -> None:
        geo = (
            ROOT
            / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_medium_pair_rear.geo.json"
        )
        if not geo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        boxes = m.north_pick_boxes_from_geo(geo)
        self.assertEqual(len(boxes), 1)
        _, _, y0, y1, z0, z1 = boxes[0]
        self.assertGreater(y1 - y0, 6.0)
        self.assertLess(z1 - z0, 3.0)

    def test_pair_rear_z_ahead_of_front(self) -> None:
        front = (
            ROOT
            / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_medium_pair_front.geo.json"
        )
        rear = (
            ROOT
            / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_medium_pair_rear.geo.json"
        )
        if not front.is_file() or not rear.is_file():
            self.skipTest("仓库内无该 geo 资源")
        f = m.north_pick_boxes_from_geo(front)[0]
        r = m.north_pick_boxes_from_geo(rear)[0]
        self.assertGreater(r[4], f[4])


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
            self.assertTrue("generated_utc=" in text or "生成时间" in text)
            self.assertIn("source_geo=", text)
            self.assertIn("geometry.bed_plate6_voxel_pick_test", text)
            self.assertIn("Shapes.or", text)
            self.assertIn("Block.box", text)
        finally:
            shutil.rmtree(p.parent, ignore_errors=True)


class TestMarkdownExport(unittest.TestCase):
    def test_upsert_replaces_same_model_key(self) -> None:
        tmp = Path(tempfile.mkdtemp()) / "export.md"
        try:
            m.upsert_export_markdown(tmp, "model_a", "<!-- model: model_a -->\n## `model_a`\n\nv1\n\n<!-- /model: model_a -->\n")
            m.upsert_export_markdown(tmp, "model_a", "<!-- model: model_a -->\n## `model_a`\n\nv2\n\n<!-- /model: model_a -->\n")
            m.upsert_export_markdown(tmp, "model_b", "<!-- model: model_b -->\n## `model_b`\n\nb\n\n<!-- /model: model_b -->\n")
            text = tmp.read_text(encoding="utf-8")
            self.assertEqual(text.count("<!-- model: model_a -->"), 1)
            self.assertIn("v2", text)
            self.assertNotIn("v1", text)
            self.assertIn("model_b", text)
            self.assertIn("EXPORT_INDEX", text)
        finally:
            shutil.rmtree(tmp.parent, ignore_errors=True)

    def test_export_geo_writes_section(self) -> None:
        geo = ROOT / "src/main/resources/assets/fantasy_furniture/geo/block/bed_plate6_pillow_medium_solo.geo.json"
        if not geo.is_file():
            self.skipTest("仓库内无该 geo 资源")
        tmp = Path(tempfile.mkdtemp()) / "export.md"
        try:
            key = m.export_geo_to_markdown(geo, tmp)
            self.assertEqual(key, "bed_plate6_pillow_medium_solo")
            text = tmp.read_text(encoding="utf-8")
            self.assertIn("geometry.bed_plate6_pillow_medium_solo", text)
            self.assertIn("阶段 B", text)
            self.assertIn("| x | y | z | 宽 | 高 | 深 |", text)
            key2 = m.export_geo_to_markdown(geo, tmp)
            self.assertEqual(key, key2)
            self.assertEqual(text.count("<!-- model: bed_plate6_pillow_medium_solo -->"), 1)
        finally:
            shutil.rmtree(tmp.parent, ignore_errors=True)


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
