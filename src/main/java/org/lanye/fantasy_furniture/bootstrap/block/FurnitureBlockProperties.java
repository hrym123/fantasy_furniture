package org.lanye.fantasy_furniture.bootstrap.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * 本模组方块 {@link BlockBehaviour.Properties} 的语义化组合（材质手感与碰撞参数），避免各处散落相同链式调用。
 */
public final class FurnitureBlockProperties {

    private FurnitureBlockProperties() {}

    /**
     * 釉面陶瓷体：与厨房动画方块、彩色瓷砖共用强度与音效；实心整格方块勿加 {@code noOcclusion()}。
     */
    public static BlockBehaviour.Properties ceramicBody(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(1.2f, 6.0f)
                .sound(SoundType.DEEPSLATE_TILES);
    }

    /** 厨房陶瓷系家具（透明 / 非整格碰撞模型）。 */
    public static BlockBehaviour.Properties kitchenCeramic(MapColor mapColor) {
        return ceramicBody(mapColor).noOcclusion();
    }

    /** 金属外壳 / 电器感：金属色、金属声，非整格模型。 */
    public static BlockBehaviour.Properties metalNoOcclusion() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.5f, 6.0f)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    /** 木质柜体、台面（与厨房成套柜一致）。 */
    public static BlockBehaviour.Properties woodCabinetNoOcclusion() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0f, 6.0f)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    /** 樱桃木饰面（如卡座），与普柜 {@link #woodCabinetNoOcclusion()} 区分音效。 */
    public static BlockBehaviour.Properties cherryWoodFurnitureNoOcclusion() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0f, 6.0f)
                .sound(SoundType.CHERRY_WOOD)
                .noOcclusion();
    }

    /** 软垫织物类（沙发等）。 */
    public static BlockBehaviour.Properties woolFurnitureNoOcclusion(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(0.8f, 6.0f)
                .sound(SoundType.WOOL)
                .noOcclusion();
    }
}
