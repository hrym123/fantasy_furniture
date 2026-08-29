package org.lanye.fantasy_furniture.content.furniture.decor;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

/** 楼梯共享 geo / 贴图 ResourceLocation。 */
public final class StyledStairsMaterials {

    public static final String GEO_STEM = "styled_stairs";

    private StyledStairsMaterials() {}

    public static ResourceLocation texture(StyledStairsMaterialVariant variant) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + variant.textureStem() + ".png");
    }

    public static String colorTranslationKey(StyledStairsMaterialVariant variant) {
        return "block.fantasy_furniture.styled_stairs.color." + variant.getSerializedName();
    }
}
