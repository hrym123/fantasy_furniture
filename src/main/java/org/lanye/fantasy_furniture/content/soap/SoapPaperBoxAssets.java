package org.lanye.fantasy_furniture.content.soap;



import net.minecraft.resources.ResourceLocation;

import org.lanye.fantasy_furniture.FantasyFurniture;



/** 包装盒 Geo / 贴图；堆叠模型 {@code block1}…{@code block7} 对应层 1…7。 */

public final class SoapPaperBoxAssets {



    public static final int MAX_STACK = 7;

    public static final int STACK_STYLE_COUNT = 2;

    public static final int DEFAULT_STACK_STYLE = 1;



    private static final ResourceLocation STATIC_ANIMATION =

            ResourceLocation.fromNamespaceAndPath(

                    FantasyFurniture.MODID, "animations/block/geolib_static.animation.json");



    private SoapPaperBoxAssets() {}



    public static ResourceLocation singleModelLocation() {

        return ResourceLocation.fromNamespaceAndPath(

                FantasyFurniture.MODID, "geo/block/soap_paper_box.geo.json");

    }



    public static ResourceLocation singleTornModelLocation() {

        return ResourceLocation.fromNamespaceAndPath(

                FantasyFurniture.MODID, "geo/block/soap_paper_box_torn.geo.json");

    }



    public static ResourceLocation stackModelLocation(int stackStyle) {

        int style = normalizeStackStyle(stackStyle);

        return ResourceLocation.fromNamespaceAndPath(

                FantasyFurniture.MODID, "geo/block/soap_paper_box_stack_" + style + ".geo.json");

    }



    public static ResourceLocation textureLocation(int materialId) {

        int id = materialId >= 1 && materialId <= SoapPaperBoxMaterials.COUNT ? materialId : 1;

        return ResourceLocation.fromNamespaceAndPath(

                FantasyFurniture.MODID, "textures/block/soap_paper_box_" + id + ".png");

    }



    public static ResourceLocation singleAnimationLocation() {

        return ResourceLocation.fromNamespaceAndPath(

                FantasyFurniture.MODID, "animations/block/soap_paper_box.animation.json");

    }



    public static ResourceLocation stackAnimationLocation() {

        return STATIC_ANIMATION;

    }



    public static int normalizeStackStyle(int stackStyle) {

        return stackStyle >= 1 && stackStyle <= STACK_STYLE_COUNT ? stackStyle : DEFAULT_STACK_STYLE;

    }



    public static int nextStackStyle(int stackStyle, boolean reverse) {

        int current = normalizeStackStyle(stackStyle);

        if (reverse) {

            return current <= 1 ? STACK_STYLE_COUNT : current - 1;

        }

        return current >= STACK_STYLE_COUNT ? 1 : current + 1;

    }

}


