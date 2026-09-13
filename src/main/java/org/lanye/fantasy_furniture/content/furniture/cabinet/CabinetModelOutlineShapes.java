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
        return northShell(kind, shelvesMask, CabinetSegment.ALONE, false, false);
    }

    public static VoxelShape northShell(CabinetKind kind, int shelvesMask, CabinetSegment segment) {
        return northShell(kind, shelvesMask, segment, false, false);
    }

    /** @deprecated 使用 {@link #northShell(CabinetKind, int, CabinetSegment, boolean, boolean)} */
    @Deprecated
    public static VoxelShape northShell(
            CabinetKind kind, int shelvesMask, CabinetSegment segment, boolean sideOpen) {
        return northShell(kind, shelvesMask, segment, sideOpen, false);
    }

    public static VoxelShape northShell(
            CabinetKind kind,
            int shelvesMask,
            CabinetSegment segment,
            boolean openNeg,
            boolean openPos) {
        return switch (kind) {
            case CABINET_1 -> cabinet1(segment, shelvesMask, openNeg, openPos);
            case CABINET_2 -> cabinet2(shelvesMask);
        };
    }

    private static VoxelShape cabinet1(
            CabinetSegment segment, int shelvesMask, boolean openNeg, boolean openPos) {
        if (!openNeg && !openPos) {
            return switch (segment) {
                case ALONE -> cabinet1Cell(shelvesMask);
                case BOTTOM -> cabinet1Bottom(shelvesMask);
                case MIDDLE -> cabinet1Middle(shelvesMask);
                case TOP -> cabinet1Top(shelvesMask);
            };
        }
        return switch (segment) {
            case ALONE -> cabinet1OpenCell(shelvesMask, openNeg, openPos);
            case BOTTOM -> cabinet1OpenBottom(shelvesMask, openNeg, openPos);
            case MIDDLE -> cabinet1OpenMiddle(openNeg, openPos);
            case TOP -> cabinet1OpenTop(shelvesMask, openNeg, openPos);
        };
    }

    private static VoxelShape cabinet1OpenCell(int shelvesMask, boolean openNeg, boolean openPos) {
        VoxelShape shell = px(-8, 0, 6, 16, 16, 2); // back
        if (!openNeg) {
            shell = Shapes.or(shell, px(-8, 0, -8, 2, 16, 14)); // left
        }
        if (!openPos) {
            shell = Shapes.or(shell, px(6, 0, -8, 2, 16, 14)); // right
        }
        if ((shelvesMask & (1 << 0)) != 0) {
            double ox;
            double sx;
            if (openNeg && openPos) {
                ox = -8;
                sx = 16;
            } else if (openNeg) {
                ox = -8;
                sx = 14;
            } else if (openPos) {
                ox = -6;
                sx = 14;
            } else {
                ox = -6;
                sx = 12;
            }
            shell = Shapes.or(shell, px(ox, 0, -8, sx, 2, 14)); // floor
        }
        if ((shelvesMask & (1 << 3)) != 0) {
            double ox;
            double sx;
            if (openNeg && openPos) {
                ox = -8;
                sx = 16;
            } else if (openNeg) {
                ox = -8;
                sx = 14;
            } else if (openPos) {
                ox = -6;
                sx = 14;
            } else {
                ox = -6;
                sx = 12;
            }
            shell = Shapes.or(shell, px(ox, 14, -8, sx, 2, 14)); // lid
        }
        return shell.optimize();
    }

    private static VoxelShape cabinet1Cell(int shelvesMask) {
        VoxelShape shell =
                Shapes.or(
                        px(-8, 0, -8, 2, 16, 14), // left (−X）
                        px(6, 0, -8, 2, 16, 14), // right（+X）
                        px(-8, 0, 6, 16, 16, 2)); // back
        if ((shelvesMask & (1 << 0)) != 0) {
            shell = Shapes.or(shell, px(-6, 0, -8, 12, 2, 14)); // floor
        }
        if ((shelvesMask & (1 << 3)) != 0) {
            shell = Shapes.or(shell, px(-6, 14, -8, 12, 2, 14)); // lid
        }
        return shell.optimize();
    }

    private static VoxelShape cabinet1Bottom(int shelvesMask) {
        VoxelShape shell =
                Shapes.or(
                        px(-8, 0, 6, 16, 16, 2), // back
                        px(-8, 0, -8, 2, 16, 14), // left
                        px(6, 0, -8, 2, 16, 14)); // right
        if ((shelvesMask & (1 << 0)) != 0) {
            shell = Shapes.or(shell, px(-6, 0, -8, 12, 2, 14)); // floor
        }
        return shell.optimize();
    }

    private static VoxelShape cabinet1Middle(int shelvesMask) {
        return Shapes.or(
                        px(-8, 0, 6, 16, 16, 2), // back
                        px(-8, 0, -8, 2, 16, 14), // left
                        px(6, 0, -8, 2, 16, 14)) // right
                .optimize();
    }

    private static VoxelShape cabinet1Top(int shelvesMask) {
        VoxelShape shell =
                Shapes.or(
                        px(-8, 0, -8, 2, 16, 14), // left
                        px(-8, 0, 6, 16, 16, 2), // back
                        px(6, 0, -8, 2, 16, 14)); // right
        if ((shelvesMask & (1 << 3)) != 0) {
            shell = Shapes.or(shell, px(-7, 14, -8, 13, 2, 14)); // lid
        }
        return shell.optimize();
    }

    private static VoxelShape cabinet1OpenBottom(int shelvesMask, boolean openNeg, boolean openPos) {
        VoxelShape shell = px(-8, 0, 6, 16, 16, 2); // back
        if (!openNeg) {
            shell = Shapes.or(shell, px(-8, 0, -8, 2, 16, 14)); // left
        }
        if (!openPos) {
            shell = Shapes.or(shell, px(6, 0, -8, 2, 16, 14)); // right
        }
        if ((shelvesMask & (1 << 0)) != 0) {
            double ox = openNeg ? -8 : -6;
            double sx = (openNeg ? 14 : 12) + (openPos && !openNeg ? 2 : 0);
            if (openNeg && openPos) {
                ox = -8;
                sx = 16;
            } else if (openPos) {
                ox = -6;
                sx = 14;
            }
            shell = Shapes.or(shell, px(ox, 0, -8, sx, 2, 14));
        }
        return shell.optimize();
    }

    private static VoxelShape cabinet1OpenMiddle(boolean openNeg, boolean openPos) {
        VoxelShape shell = px(-8, 0, 6, 16, 16, 2); // back
        if (!openNeg) {
            shell = Shapes.or(shell, px(-8, 0, -8, 2, 16, 14));
        }
        if (!openPos) {
            shell = Shapes.or(shell, px(6, 0, -8, 2, 16, 14));
        }
        return shell.optimize();
    }

    private static VoxelShape cabinet1OpenTop(int shelvesMask, boolean openNeg, boolean openPos) {
        VoxelShape shell = px(-8, 0, 6, 16, 16, 2); // back
        if (!openNeg) {
            shell = Shapes.or(shell, px(-8, 0, -8, 2, 16, 14));
        }
        if (!openPos) {
            shell = Shapes.or(shell, px(6, 0, -8, 2, 16, 14));
        }
        if ((shelvesMask & (1 << 3)) != 0) {
            double ox = openNeg ? -8 : -6;
            double sx = openNeg && openPos ? 16 : 14;
            if (!openNeg && openPos) {
                ox = -6;
                sx = 14;
            } else if (openNeg && !openPos) {
                ox = -8;
                sx = 14;
            } else if (!openNeg && !openPos) {
                ox = -7;
                sx = 13;
            }
            shell = Shapes.or(shell, px(ox, 14, -8, sx, 2, 14));
        }
        return shell.optimize();
    }

    private static VoxelShape cabinet2(int shelvesMask) {
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

    private static VoxelShape px(double ox, double oy, double oz, double sx, double sy, double sz) {
        return Block.box(ox + 8.0, oy, oz + 8.0, ox + sx + 8.0, oy + sy, oz + sz + 8.0);
    }
}
