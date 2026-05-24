package org.lanye.fantasy_furniture.bootstrap.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 本模组数据包 Tag 的 {@link TagKey} 入口（JSON 见 {@code data/fantasy_furniture/tags/}）。 */
public final class ModTags {

    private ModTags() {}

    /** 可被刷子循环换色的方块（瓷砖、壁纸、普通玻璃窗等）。 */
    public static final TagKey<Block> BRUSH_RECOLORABLE_BLOCKS =
            TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "brush_recolorable"));

    /** 可被刷子循环换色的物品（与 {@link #BRUSH_RECOLORABLE_BLOCKS} 对应）。 */
    public static final TagKey<Item> BRUSH_RECOLORABLE_ITEMS =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "brush_recolorable"));

}
