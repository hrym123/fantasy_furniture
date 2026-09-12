package org.lanye.fantasy_furniture.content.furniture.cabinet;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 柜子准心描边：按 geo 立方逐块并集（侧板/立柱/背板/隔板），贴合模型细节而非整格外接盒。
 */
public final class CabinetModelOutlineShapes {

    private CabinetModelOutlineShapes() {}

    /** {@code shelvesMask} bit i = shelf_i 仍在。 */
    public static VoxelShape northShell(CabinetKind kind, int shelvesMask) {
        return switch (kind) {
            case CABINET_1 -> cabinet1(shelvesMask);
            case CABINET_2 -> cabinet2(shelvesMask);
        };
    }

    private static VoxelShape cabinet1(int shelvesMask) {
        // group3 walls — geo origin/size → Block.box（原点底心：+8 XZ）
        VoxelShape shell =
                Shapes.or(
                        px(-8, 0, 6, 16, 48, 2), // back
                        px(-8, 0, -8, 2, 48, 14), // left
                        px(6, 0, -8, 2, 48, 14)); // right
        // shelf_0..3
        VoxelShape[] shelves =
                new VoxelShape[] {
                    px(-6, 0, -8, 12, 2, 14),
                    px(-6, 14, -8, 12, 2, 14),
                    px(-6, 30, -8, 12, 2, 14),
                    px(-6, 46, -8, 12, 2, 14)
                };
        for (int i = 0; i < shelves.length; i++) {
            if ((shelvesMask & (1 << i)) != 0) {
                shell = Shapes.or(shell, shelves[i]);
            }
        }
        return shell.optimize();
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

    /** Blockbench 像素 origin+size → 方块 0..16 盒。 */
    private static VoxelShape px(double ox, double oy, double oz, double sx, double sy, double sz) {
        return Block.box(ox + 8.0, oy, oz + 8.0, ox + sx + 8.0, oy + sy, oz + sz + 8.0);
    }
}