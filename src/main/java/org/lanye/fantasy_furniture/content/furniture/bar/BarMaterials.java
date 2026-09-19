package org.lanye.fantasy_furniture.content.furniture.bar;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;

public final class BarMaterials {

    private BarMaterials() {}

    public static ResourceLocation sharedTexture(BarMaterialVariant variant) {
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/" + variant.textureStem() + ".png");
    }
}
