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
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;
import org.lanye.reverie_core.item.DecorativeHelmetRegistration;

/**
 * 无对应方块的独立物品注册。
 */
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FantasyFurniture.MODID);

    public static final RegistryObject<Item> PAINT_BRUSH =
            ITEMS.register("paint_brush", () -> new Item(new Item.Properties()));

    private static final Item.Properties BED_PLATE6_DUVET_PROPS = new Item.Properties().stacksTo(16);

    /** 床板 6 床单七种材质（{@code bed_plate6_duvet_1} … {@code _7}）。 */
    public static final RegistryObject<Item> BED_PLATE6_DUVET_1 =
            ITEMS.register("bed_plate6_duvet_1", () -> new BedPlate6DuvetItem(BED_PLATE6_DUVET_PROPS, 1));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_2 =
            ITEMS.register("bed_plate6_duvet_2", () -> new BedPlate6DuvetItem(BED_PLATE6_DUVET_PROPS, 2));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_3 =
            ITEMS.register("bed_plate6_duvet_3", () -> new BedPlate6DuvetItem(BED_PLATE6_DUVET_PROPS, 3));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_4 =
            ITEMS.register("bed_plate6_duvet_4", () -> new BedPlate6DuvetItem(BED_PLATE6_DUVET_PROPS, 4));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_5 =
            ITEMS.register("bed_plate6_duvet_5", () -> new BedPlate6DuvetItem(BED_PLATE6_DUVET_PROPS, 5));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_6 =
            ITEMS.register("bed_plate6_duvet_6", () -> new BedPlate6DuvetItem(BED_PLATE6_DUVET_PROPS, 6));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_7 =
            ITEMS.register("bed_plate6_duvet_7", () -> new BedPlate6DuvetItem(BED_PLATE6_DUVET_PROPS, 7));

    /** 床板 6 被套七种材质（仅能在已铺床单的床板上使用）。 */
    public static final RegistryObject<Item> BED_PLATE6_DUVET_COVER_1 =
            ITEMS.register("bed_plate6_duvet_cover_1", () -> new BedPlate6DuvetCoverItem(BED_PLATE6_DUVET_PROPS, 1));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_COVER_2 =
            ITEMS.register("bed_plate6_duvet_cover_2", () -> new BedPlate6DuvetCoverItem(BED_PLATE6_DUVET_PROPS, 2));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_COVER_3 =
            ITEMS.register("bed_plate6_duvet_cover_3", () -> new BedPlate6DuvetCoverItem(BED_PLATE6_DUVET_PROPS, 3));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_COVER_4 =
            ITEMS.register("bed_plate6_duvet_cover_4", () -> new BedPlate6DuvetCoverItem(BED_PLATE6_DUVET_PROPS, 4));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_COVER_5 =
            ITEMS.register("bed_plate6_duvet_cover_5", () -> new BedPlate6DuvetCoverItem(BED_PLATE6_DUVET_PROPS, 5));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_COVER_6 =
            ITEMS.register("bed_plate6_duvet_cover_6", () -> new BedPlate6DuvetCoverItem(BED_PLATE6_DUVET_PROPS, 6));
    public static final RegistryObject<Item> BED_PLATE6_DUVET_COVER_7 =
            ITEMS.register("bed_plate6_duvet_cover_7", () -> new BedPlate6DuvetCoverItem(BED_PLATE6_DUVET_PROPS, 7));

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
            DecorativeHelmetRegistration.register(
                    ITEMS,
                    new Item.Properties().stacksTo(1),
                    FantasyFurniture.MODID,
                    "decorative_helmet_blue_top_hat",
                    "decorative_helmet_blue_top_hat_atlas");

    /** 粉色小礼帽。 */
    public static final RegistryObject<Item> DECORATIVE_HELMET_PINK_TOP_HAT =
            DecorativeHelmetRegistration.register(
                    ITEMS,
                    new Item.Properties().stacksTo(1),
                    FantasyFurniture.MODID,
                    "decorative_helmet_pink_top_hat",
                    "decorative_helmet_pink_top_hat");

    /** 垂耳兔头饰。 */
    public static final RegistryObject<Item> DECORATIVE_HELMET_LOP_EARED_RABBIT =
            DecorativeHelmetRegistration.register(
                    ITEMS,
                    new Item.Properties().stacksTo(1),
                    FantasyFurniture.MODID,
                    "decorative_helmet_lop_eared_rabbit",
                    "decorative_helmet_lop_eared_rabbit");

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
