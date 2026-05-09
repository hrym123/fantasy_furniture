package org.lanye.fantasy_furniture.bootstrap.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.bootstrap.block.PlainGlassWindowRegistration;

/**
 * 本模组创造模式物品栏（CreativeModeTab）注册。
 */
public final class ModCreativeTabs {

    private ModCreativeTabs() {}

    /**
     * 「幻想家具」主创造栏中物品的**唯一展示顺序**。新增方块物品或独立 {@link net.minecraft.world.item.Item}
     * 时须在此追加条目，勿在 {@link #MAIN} 的 {@code displayItems} 中重复手写。
     */
    public static final List<Supplier<? extends ItemLike>> MAIN_TAB_DISPLAY_ORDER = buildMainTabDisplayOrder();

    private static List<Supplier<? extends ItemLike>> buildMainTabDisplayOrder() {
        List<Supplier<? extends ItemLike>> list = new ArrayList<>();
        Collections.addAll(
                list,
                ModBlocks.PINK_CERAMIC_TILE_ITEM::get,
                ModBlocks.YELLOW_CERAMIC_TILE_ITEM::get,
                ModBlocks.BLUE_CERAMIC_TILE_ITEM::get,
                ModBlocks.GREEN_CERAMIC_TILE_ITEM::get,
                ModBlocks.CYAN_CERAMIC_TILE_ITEM::get,
                ModBlocks.PURPLE_CERAMIC_TILE_ITEM::get,
                ModBlocks.DECORATIVE_SCREEN_ITEM::get);
        Collections.addAll(
                list,
                ModBlocks.PINK_WALLPAPER_ITEM::get,
                ModBlocks.RED_WALLPAPER_ITEM::get,
                ModBlocks.YELLOW_WALLPAPER_ITEM::get,
                ModBlocks.YELLOW_WAINSCOT_ITEM::get,
                ModBlocks.BLUE_WALLPAPER_ITEM::get,
                ModBlocks.GREEN_WALLPAPER_ITEM::get,
                ModBlocks.PURPLE_WALLPAPER_ITEM::get,
                ModItems.PAINT_BRUSH::get,
                ModItems.TANGHULU::get,
                ModItems.ARCANE_WAND::get,
                ModItems.DECORATIVE_HELMET_BLUE_TOP_HAT::get,
                ModItems.DECORATIVE_HELMET_PINK_TOP_HAT::get,
                ModItems.DECORATIVE_HELMET_LOP_EARED_RABBIT::get,
                ModBlocks.BANQUETTE.item()::get,
                ModBlocks.MIXING_BOWL.item()::get,
                ModBlocks.JAM_POT.item()::get,
                ModBlocks.OVEN.item()::get,
                ModBlocks.PESTLE_BOWL.item()::get,
                ModBlocks.HALF_HALF_POT.item()::get,
                ModBlocks.LOTTERY_MACHINE.item()::get,
                ModBlocks.SWEEPER_DOCK.item()::get,
                ModBlocks.GREEN_SOFA.item()::get,
                ModBlocks.BED_PLATE6.item()::get,
                ModItems.BED_PLATE6_DUVET_1::get,
                ModItems.BED_PLATE6_DUVET_2::get,
                ModItems.BED_PLATE6_DUVET_3::get,
                ModItems.BED_PLATE6_DUVET_4::get,
                ModItems.BED_PLATE6_DUVET_5::get,
                ModItems.BED_PLATE6_DUVET_6::get,
                ModItems.BED_PLATE6_DUVET_7::get,
                ModItems.BED_PLATE6_DUVET_COVER_1::get,
                ModItems.BED_PLATE6_DUVET_COVER_2::get,
                ModItems.BED_PLATE6_DUVET_COVER_3::get,
                ModItems.BED_PLATE6_DUVET_COVER_4::get,
                ModItems.BED_PLATE6_DUVET_COVER_5::get,
                ModItems.BED_PLATE6_DUVET_COVER_6::get);
        for (var pillow : ModItems.BED_PLATE6_PILLOW_LARGE_ITEMS) {
            list.add(pillow::get);
        }
        for (var pillow : ModItems.BED_PLATE6_PILLOW_MEDIUM_ITEMS) {
            list.add(pillow::get);
        }
        for (var pillow : ModItems.BED_PLATE6_PILLOW_SMALL_ITEMS) {
            list.add(pillow::get);
        }
        Collections.addAll(
                list,
                ModBlocks.KITCHEN_COUNTER.item()::get,
                ModBlocks.KITCHEN_COUNTER_CABINET.item()::get,
                ModBlocks.COMBINED_ORNAMENT.item()::get);
        for (var ro : PlainGlassWindowRegistration.items()) {
            list.add(ro::get);
        }
        return List.copyOf(list);
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FantasyFurniture.MODID);

    /** 主物品栏（图标为粉色瓷砖） */
    public static final RegistryObject<CreativeModeTab> MAIN =
            CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.fantasy_furniture.main"))
                    .icon(() -> new ItemStack(ModBlocks.PINK_CERAMIC_TILE_ITEM.get()))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((params, output) -> {
                        for (Supplier<? extends ItemLike> entry : MAIN_TAB_DISPLAY_ORDER) {
                            output.accept(entry.get());
                        }
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
