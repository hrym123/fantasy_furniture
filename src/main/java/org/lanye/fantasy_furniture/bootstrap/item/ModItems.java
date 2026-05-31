package org.lanye.fantasy_furniture.bootstrap.item;

import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate6Registration;
import org.lanye.fantasy_furniture.content.debug.item.DebugVariantStickItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapDebugStickItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapFlatLiquidItem;
import org.lanye.fantasy_furniture.content.tool.item.FabricCoilItem;
import org.lanye.fantasy_furniture.content.tool.item.PaintBrushItem;
import org.lanye.fantasy_furniture.content.furniture.common.item.ArcaneWandItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;
import org.lanye.reverie_core.item.DecorativeHelmetRegistration;

/**
 * 无对应方块的独立物品注册。
 *
 * <p>床板 6 组件物品见 {@link BedPlate6Registration}。
 */
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FantasyFurniture.MODID);

    public static final RegistryObject<Item> PAINT_BRUSH =
            ITEMS.register("paint_brush", () -> new PaintBrushItem(new Item.Properties()));

    /** 布衣换色工具（展示名待定）；暂用木棍模型。 */
    public static final RegistryObject<Item> FABRIC_COIL =
            ITEMS.register("fabric_coil", () -> new FabricCoilItem(new Item.Properties()));

    /** 开发：循环本模组方块 / 物品变体；木棍外观 + 附魔光效。 */
    public static final RegistryObject<Item> DEBUG_VARIANT_STICK =
            ITEMS.register(
                    "debug_variant_stick",
                    () -> new DebugVariantStickItem(new Item.Properties().stacksTo(1)));

    /** 床板 6 组件：由 {@link BedPlate6Registration} 写入同一 {@link #ITEMS}。 */
    public static final RegistryObject<Item> BED_PLATE6_DISASSEMBLY_GLOVE = BedPlate6Registration.disassemblyGlove();

    public static final List<RegistryObject<Item>> BED_PLATE6_DUVET_ITEMS = BedPlate6Registration.duvetItems();

    public static final List<RegistryObject<Item>> BED_PLATE6_DUVET_COVER_ITEMS = BedPlate6Registration.duvetCoverItems();

    public static final List<RegistryObject<Item>> BED_PLATE6_PILLOW_LARGE_ITEMS = BedPlate6Registration.pillowLargeItems();

    public static final List<RegistryObject<Item>> BED_PLATE6_PILLOW_MEDIUM_ITEMS = BedPlate6Registration.pillowMediumItems();

    public static final List<RegistryObject<Item>> BED_PLATE6_PILLOW_SMALL_ITEMS = BedPlate6Registration.pillowSmallItems();

    /** 沐浴液：单材质图原料，六色；不可放置。 */
    public static final RegistryObject<Item> BODY_WASH_LIQUID =
            ITEMS.register(
                    "body_wash_liquid",
                    () ->
                            new SoapFlatLiquidItem(
                                    new Item.Properties().stacksTo(64),
                                    "body_wash_liquid",
                                    "item.fantasy_furniture.body_wash_liquid.named"));

    /** 洗发液：单材质图原料，六色；不可放置。 */
    public static final RegistryObject<Item> SHAMPOO_LIQUID =
            ITEMS.register(
                    "shampoo_liquid",
                    () ->
                            new SoapFlatLiquidItem(
                                    new Item.Properties().stacksTo(64),
                                    "shampoo_liquid",
                                    "item.fantasy_furniture.shampoo_liquid.named"));

    /**
     * 糖葫芦：木级剑属性基底，额外攻击伤害 +1（与木剑的 +3 不同，作低伤玩具武器）。
     */
    public static final RegistryObject<Item> TANGHULU =
            ITEMS.register("tanghulu", () -> new SwordItem(Tiers.WOOD, 1, -2.4F, new Item.Properties()));

    /** 肥皂套系调试棒：2D 手持，不参与泡泡头饰池。 */
    public static final RegistryObject<Item> SOAP_DEBUG_STICK =
            ITEMS.register("soap_debug_stick", () -> new SoapDebugStickItem(new Item.Properties().stacksTo(1)));

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
