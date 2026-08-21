package org.lanye.fantasy_furniture.bootstrap.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.common.state.StyledWindow0MaterialVariant;

/**
 * 旧 id → {@code styled_window_0_*}：
 * <ul>
 *   <li>{@code plain_glass_window} → 白色
 *   <li>{@code plain_glass_window_<色>} / {@code styled_window_plain_<色>} → {@code styled_window_0_<色>}
 * </ul>
 */
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StyledWindow0LegacyMappings {

    private StyledWindow0LegacyMappings() {}

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        remapBlocks(event);
        remapItems(event);
    }

    private static void remapBlocks(MissingMappingsEvent event) {
        ResourceLocation legacySingle =
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "plain_glass_window");
        for (MissingMappingsEvent.Mapping<Block> mapping :
                event.getMappings(ForgeRegistries.Keys.BLOCKS, FantasyFurniture.MODID)) {
            ResourceLocation key = mapping.getKey();
            if (legacySingle.equals(key)) {
                mapping.remap(
                        StyledWindow0Registration.block(StyledWindow0MaterialVariant.WHITE).get());
                continue;
            }
            StyledWindow0MaterialVariant color = colorFromLegacyPath(key);
            if (color != null) {
                mapping.remap(StyledWindow0Registration.block(color).get());
            }
        }
    }

    private static void remapItems(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Item> mapping :
                event.getMappings(ForgeRegistries.Keys.ITEMS, FantasyFurniture.MODID)) {
            StyledWindow0MaterialVariant color = colorFromLegacyPath(mapping.getKey());
            if (color != null) {
                mapping.remap(StyledWindow0Registration.items().get(color.ordinal()).get());
            }
        }
    }

    /** 历史色物品/方块 path → 色枚举。 */
    private static StyledWindow0MaterialVariant colorFromLegacyPath(ResourceLocation key) {
        if (!FantasyFurniture.MODID.equals(key.getNamespace())) {
            return null;
        }
        String path = key.getPath();
        for (String prefix : new String[] {"plain_glass_window_", "styled_window_plain_"}) {
            if (path.startsWith(prefix)) {
                String suffix = path.substring(prefix.length());
                for (StyledWindow0MaterialVariant v : StyledWindow0MaterialVariant.values()) {
                    if (v.getSerializedName().equals(suffix)) {
                        return v;
                    }
                }
            }
        }
        return null;
    }
}
