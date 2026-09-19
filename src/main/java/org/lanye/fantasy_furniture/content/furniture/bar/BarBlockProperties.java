package org.lanye.fantasy_furniture.content.furniture.bar;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** 吧台系方块属性。 */
public final class BarBlockProperties {

    private BarBlockProperties() {}

    public static BlockBehaviour.Properties woodCabinetNoOcclusion() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0f, 6.0f)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }
}
