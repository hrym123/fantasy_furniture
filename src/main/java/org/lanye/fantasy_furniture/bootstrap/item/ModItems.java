package org.lanye.fantasy_furniture.bootstrap.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.common.item.ArcaneWandItem;
import org.lanye.fantasy_furniture.content.furniture.common.item.DecorativeHelmetItem;
import org.lanye.fantasy_furniture.core.geolib.GeolibItemAssets;

/**
 * 无对应方块的独立物品注册。
 */
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FantasyFurniture.MODID);

    public static final RegistryObject<Item> PAINT_BRUSH =
            ITEMS.register("paint_brush", () -> new Item(new Item.Properties()));

    /**
     * 糖葫芦：木级剑属性基底，额外攻击伤害 +1（与木剑的 +3 不同，作低伤玩具武器）。
     */
    public static final RegistryObject<Item> TANGHULU =
            ITEMS.register("tanghulu", () -> new SwordItem(Tiers.WOOD, 1, -2.4F, new Item.Properties()));

    /** GeckoLib 手持魔杖；长按施法动画见 {@link ArcaneWandItem}。 */
    public static final RegistryObject<Item> ARCANE_WAND = ITEMS.register(
            "arcane_wand",
            () -> new ArcaneWandItem(
                    new Item.Properties().stacksTo(1),
                    GeolibItemAssets.itemAsset(FantasyFurniture.MODID, "arcane_wand"),
                    "animation.arcane_wand.idle"));

    /** 蓝色小礼帽（Geo atlas 与物品图标分离）。 */
    public static final RegistryObject<Item> DECORATIVE_HELMET_BLUE_TOP_HAT =
            registerDecorativeHelmet(
                    "decorative_helmet_blue_top_hat", "decorative_helmet_blue_top_hat_atlas");

    /** 粉色小礼帽。 */
    public static final RegistryObject<Item> DECORATIVE_HELMET_PINK_TOP_HAT =
            registerDecorativeHelmet("decorative_helmet_pink_top_hat", "decorative_helmet_pink_top_hat");

    /** 垂耳兔头饰。 */
    public static final RegistryObject<Item> DECORATIVE_HELMET_LOP_EARED_RABBIT =
            registerDecorativeHelmet(
                    "decorative_helmet_lop_eared_rabbit", "decorative_helmet_lop_eared_rabbit");

    /**
     * @param registryId 注册名，与 geo/animation 资源 basename 一致。
     * @param atlasTextureBasename {@code textures/item/&lt;basename&gt;.png}，供 Geo 采样（与物品栏 {@code *_icon} 分离）。
     */
    private static RegistryObject<Item> registerDecorativeHelmet(
            String registryId, String atlasTextureBasename) {
        return ITEMS.register(
                registryId,
                () -> new DecorativeHelmetItem(
                        new Item.Properties().stacksTo(1),
                        GeolibItemAssets.itemGeoAtlas(
                                FantasyFurniture.MODID, registryId, atlasTextureBasename),
                        "animation." + registryId + ".idle"));
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
