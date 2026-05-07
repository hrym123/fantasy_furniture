package org.lanye.fantasy_furniture.content.furniture.livingroom.client.model;

import net.minecraft.resources.ResourceLocation;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6PillowPalette;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6GeolibDecorItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;
import software.bernie.geckolib.model.GeoModel;

/** 床板 6 附属品：物品栏 / 手持与世界里同套 GeckoLib 方块 Geo。 */
public final class BedPlate6AccessoryGeoItemModel extends GeoModel<BedPlate6GeolibDecorItem> {

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, path);
    }

    @Override
    public ResourceLocation getModelResource(BedPlate6GeolibDecorItem item) {
        if (item instanceof BedPlate6DuvetItem) {
            return rl("geo/block/bed_plate6_duvet.geo.json");
        }
        if (item instanceof BedPlate6DuvetCoverItem) {
            return rl("geo/block/bed_plate6_duvet_cover.geo.json");
        }
        if (item instanceof BedPlate6LargePillowItem lp) {
            return rl(
                    "geo/block/bed_plate6_pillow_large_"
                            + BedPlate6LargePillowStyles.resourceSlug(lp.getStyleId())
                            + ".geo.json");
        }
        if (item instanceof BedPlate6MediumPillowItem) {
            // 与 Blockbench「床板6枕头（中 1个放置的样子）」一致，物品栏用 solo
            return rl("geo/block/bed_plate6_pillow_medium_solo.geo.json");
        }
        if (item instanceof BedPlate6SmallPillowItem) {
            return rl("geo/block/bed_plate6_pillow_small_stack.geo.json");
        }
        throw new IllegalArgumentException("Unknown decor item: " + item);
    }

    @Override
    public ResourceLocation getTextureResource(BedPlate6GeolibDecorItem item) {
        if (item instanceof BedPlate6DuvetItem d) {
            return rl("textures/block/bed_plate6_duvet_" + d.getMaterialId() + ".png");
        }
        if (item instanceof BedPlate6DuvetCoverItem c) {
            return rl("textures/block/bed_plate6_duvet_cover_" + c.getMaterialId() + ".png");
        }
        if (item instanceof BedPlate6LargePillowItem lp) {
            String slug = BedPlate6LargePillowStyles.resourceSlug(lp.getStyleId());
            String color = BedPlate6PillowPalette.colorSlug(lp.getMaterialId());
            return rl("textures/block/bed_plate6_pillow_large_" + slug + "_" + color + ".png");
        }
        if (item instanceof BedPlate6MediumPillowItem mp) {
            return rl("textures/block/bed_plate6_pillow_medium_" + mp.getMaterialId() + ".png");
        }
        if (item instanceof BedPlate6SmallPillowItem sp) {
            return rl("textures/block/bed_plate6_pillow_small_" + sp.getMaterialId() + ".png");
        }
        throw new IllegalArgumentException("Unknown decor item: " + item);
    }

    @Override
    public ResourceLocation getAnimationResource(BedPlate6GeolibDecorItem item) {
        if (item instanceof BedPlate6DuvetItem) {
            return rl("animations/block/bed_plate6_duvet.animation.json");
        }
        if (item instanceof BedPlate6DuvetCoverItem) {
            return rl("animations/block/bed_plate6_duvet_cover.animation.json");
        }
        if (item instanceof BedPlate6LargePillowItem lp) {
            return rl(
                    "animations/block/bed_plate6_pillow_large_"
                            + BedPlate6LargePillowStyles.resourceSlug(lp.getStyleId())
                            + ".animation.json");
        }
        if (item instanceof BedPlate6MediumPillowItem) {
            return rl("animations/block/bed_plate6_pillow_medium_solo.animation.json");
        }
        if (item instanceof BedPlate6SmallPillowItem) {
            return rl("animations/block/bed_plate6_pillow_small_stack.animation.json");
        }
        throw new IllegalArgumentException("Unknown decor item: " + item);
    }
}
