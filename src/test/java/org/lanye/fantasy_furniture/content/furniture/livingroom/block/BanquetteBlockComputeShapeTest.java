package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import org.lanye.fantasy_furniture.content.furniture.common.state.BanquetteShape;

/**
 * 单元测试 {@link BanquetteShapeRules#computeShapeFromNeighborhood}（纯 Direction 逻辑）。
 * <p>
 * 该测试不触达 {@code BlockState}/{@code Blocks}/{@code Level}，避免 JVM 单测环境下 Minecraft 注册表/引导初始化差异。
 */
class BanquetteBlockComputeShapeTest {

    @Test
    void straight_whenAheadNotBanquette() {
        BanquetteShape shape =
                BanquetteShapeRules.computeShapeFromNeighborhood(
                        Direction.NORTH,
                        false,
                        null,
                        false,
                        null,
                        false,
                        null);
        assertEquals(BanquetteShape.STRAIGHT, shape);
    }

    @Test
    void cornerLeft_whenAheadFacesClockwise() {
        BanquetteShape shape =
                BanquetteShapeRules.computeShapeFromNeighborhood(
                        Direction.NORTH,
                        true,
                        Direction.EAST,
                        false,
                        null,
                        false,
                        null);
        assertEquals(BanquetteShape.CORNER_LEFT, shape);
    }

    @Test
    void cornerLeft_suppressed_whenLeftNeighborSameFacing() {
        BanquetteShape shape =
                BanquetteShapeRules.computeShapeFromNeighborhood(
                        Direction.NORTH,
                        true,
                        Direction.EAST,
                        true,
                        Direction.NORTH,
                        false,
                        null);
        assertEquals(BanquetteShape.STRAIGHT, shape);
    }

    @Test
    void cornerRight_whenAheadFacesCounterClockwise() {
        BanquetteShape shape =
                BanquetteShapeRules.computeShapeFromNeighborhood(
                        Direction.NORTH,
                        true,
                        Direction.WEST,
                        false,
                        null,
                        false,
                        null);
        assertEquals(BanquetteShape.CORNER_RIGHT, shape);
    }

    @Test
    void cornerRight_suppressed_whenRightNeighborSameFacing() {
        BanquetteShape shape =
                BanquetteShapeRules.computeShapeFromNeighborhood(
                        Direction.NORTH,
                        true,
                        Direction.WEST,
                        false,
                        null,
                        true,
                        Direction.NORTH);
        assertEquals(BanquetteShape.STRAIGHT, shape);
    }
}
