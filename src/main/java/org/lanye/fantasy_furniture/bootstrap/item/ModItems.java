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
import org.lanye.fantasy_furniture.content.soap.item.SoapDebugStickItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapFlatLiquidItem;
import org.lanye.fantasy_furniture.content.furniture.common.item.ArcaneWandItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/**
 * 无对应方块的独立物品注册。
 *
 * <p>床板 6 组件物品见 {@link BedPlate6Registration}。
 */
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FantasyFurniture.MODID);

    /** 线团原料（展示名待定）；换色请用 reverie_core {@code fantasy_spool}。 */
    public static final RegistryObject<Item> FABRIC_COIL =
            ITEMS.register("fabric_coil", () -> new Item(new Item.Properties()));

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

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
