package org.lanye.fantasy_furniture.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.geolib.GeolibItemAssets;

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

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
