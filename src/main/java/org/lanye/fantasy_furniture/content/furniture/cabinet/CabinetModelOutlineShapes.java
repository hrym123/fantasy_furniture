package org.lanye.fantasy_furniture.content.furniture.cabinet;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.cabinet.state.CabinetSegment;

/**
 * 柜子准心描边：按 geo 立方逐块并集（侧板/立柱/背板/隔板），贴合模型细节而非整格外接盒。
 */
public final class CabinetModelOutlineShapes {

    private CabinetModelOutlineShapes() {}

    /** {@code shelvesMask} bit i = shelf_i 仍在（柜子2；柜子1 alone 仅用 bit0 底板 / bit3 顶盖）。 */
    public static VoxelShape northShell(CabinetKind kind, int shelvesMask) {
        return northShell(kind, shelvesMask, CabinetSegment.ALONE);
    }

    public static VoxelShape northShell(CabinetKind kind, int shelvesMask, CabinetSegment segment) {
        return switch (kind) {
            case CABINET_1 -> cabinet1(segment, shelvesMask);
            case CABINET_2 -> cabinet2(shelvesMask);
        };
    }

    /** 对齐 {@code cabinet_1_cell|2x|2z|2s} geo 立方。 */
    private static VoxelShape cabinet1(CabinetSegment segment, int shelvesMask) {
        return switch (segment) {
            case ALONE -> cabinet1Cell(shelvesMask);
            case BOTTOM -> cabinet1Bottom();
            case MIDDLE -> cabinet1Middle();
            case TOP -> cabinet1Top();
        };
    }

    private static VoxelShape cabinet1Cell(int shelvesMask) {
        VoxelShape shell =
                Shapes.or(
                        px(-8, 0, -8, 2, 16, 14), // left
                        px(6, 0, -8, 2, 16, 14), // right
                        px(-8, 0, 6, 16, 16, 2)); // back
        if ((shelvesMask & (1 << 0)) != 0) {
            shell = Shapes.or(shell, px(-6, 0, -8, 12, 2, 14)); // floor
        }
        if ((shelvesMask & (1 << 3)) != 0) {
            shell = Shapes.or(shell, px(-6, 14, -8, 12, 2, 14)); // lid
        }
        return shell.optimize();
    }

    /** {@code cabinet_1_2x}：只描本格内外壳（底板+侧背）；上沿连接板跨格，不描以免接缝横线。 */
    private static VoxelShape cabinet1Bottom() {
        return Shapes.or(
                        px(-6, 0, -8, 12, 2, 14), // floor
                        px(-8, 0, 6, 16, 16, 2), // back
                        px(-8, 0, -8, 2, 16, 14), // left
                        px(6, 0, -8, 2, 16, 14)) // right
                .optimize();
    }

    /** {@code cabinet_1_2z}：只描本格内侧背；连接板跨格不描。 */
    private static VoxelShape cabinet1Middle() {
        return Shapes.or(
                        px(-8, 0, 6, 16, 16, 2), // back
                        px(-8, 0, -8, 2, 16, 14), // left
                        px(6, 0, -8, 2, 16, 14)) // right
                .optimize();
    }

    /** {@code cabinet_1_2s}：无底，有顶盖。 */
    private static VoxelShape cabinet1Top() {
        return Shapes.or(
                        px(-8, 0, -8, 2, 16, 14), // left
                        px(-7, 14, -8, 13, 2, 14), // lid（与 geo 一致略偏）
                        px(-8, 0, 6, 16, 16, 2), // back
                        px(6, 0, -8, 2, 16, 14)) // right
                .optimize();
    }

    private static VoxelShape cabinet2(int shelvesMask) {
        // group structural cubes (非隔板)
        VoxelShape shell =
                Shapes.or(
                        px(2, 1, 1, 1, 4, 6),
                        px(7, 1, 1, 1, 15, 6),
                        px(-8, 1, 1, 1, 15, 6),
                        px(-3, 1, 1, 1, 4, 6),
                        px(-3, 6, 1, 1, 4, 6),
                        px(2, 6, 1, 1, 4, 6),
                        px(2, 11, 1, 1, 4, 6),
                        px(-3, 11, 1, 1, 4, 6),
                        px(-8, 0, 7, 16, 16, 1)); // back
        VoxelShape[] shelves =
                new VoxelShape[] {
                    px(-8, 0, 1, 16, 1, 6),
                    px(-7, 5, 1, 14, 1, 6),
                    px(-7, 10, 1, 14, 1, 6),
                    px(-7, 15, 1, 14, 1, 6)
                };
        for (int i = 0; i < shelves.length; i++) {
            if ((shelvesMask & (1 << i)) != 0) {
                shell = Shapes.or(shell, shelves[i]);
            }
        }
        return shell.optimize();
    }

    /** Blockbench 像素 origin+size → 方块 0..16 盒（可略超出格高，如 2X 连接板）。 */
    private static VoxelShape px(double ox, double oy, double oz, double sx, double sy, double sz) {
        return Block.box(ox + 8.0, oy, oz + 8.0, ox + sx + 8.0, oy + sy, oz + sz + 8.0);
    }
}
