package org.lanye.fantasy_furniture.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.block.ModBlocks;

/**
 * 本模组创造模式物品栏（CreativeModeTab）注册。
 */
public final class ModCreativeTabs {

    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FantasyFurniture.MODID);

    /** 主物品栏（图标为粉色瓷砖） */
    public static final RegistryObject<CreativeModeTab> MAIN =
            CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.fantasy_furniture.main"))
                    .icon(() -> new ItemStack(ModBlocks.PINK_CERAMIC_TILE_ITEM.get()))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.PINK_CERAMIC_TILE_ITEM.get());
                        output.accept(ModBlocks.YELLOW_CERAMIC_TILE_ITEM.get());
                        output.accept(ModBlocks.BLUE_CERAMIC_TILE_ITEM.get());
                        output.accept(ModBlocks.GREEN_CERAMIC_TILE_ITEM.get());
                        output.accept(ModBlocks.CYAN_CERAMIC_TILE_ITEM.get());
                        output.accept(ModBlocks.PURPLE_CERAMIC_TILE_ITEM.get());
                        output.accept(ModBlocks.PINK_WALLPAPER_ITEM.get());
                        output.accept(ModBlocks.RED_WALLPAPER_ITEM.get());
                        output.accept(ModBlocks.YELLOW_WALLPAPER_ITEM.get());
                        output.accept(ModBlocks.YELLOW_WAINSCOT_ITEM.get());
                        output.accept(ModBlocks.BLUE_WALLPAPER_ITEM.get());
                        output.accept(ModBlocks.GREEN_WALLPAPER_ITEM.get());
                        output.accept(ModBlocks.PURPLE_WALLPAPER_ITEM.get());
                        output.accept(ModBlocks.DECORATIVE_SCREEN_ITEM.get());
                        output.accept(ModBlocks.GLASS_WINDOW_ITEM.get());
                        output.accept(ModItems.PAINT_BRUSH.get());
                        output.accept(ModItems.TANGHULU.get());
                        output.accept(ModBlocks.BANQUETTE.item().get());
                        output.accept(ModBlocks.MIXING_BOWL.item().get());
                        output.accept(ModBlocks.JAM_POT.item().get());
                        output.accept(ModBlocks.OVEN.item().get());
                        output.accept(ModBlocks.PESTLE_BOWL.item().get());
                        output.accept(ModBlocks.HALF_HALF_POT.item().get());
                        output.accept(ModBlocks.LOTTERY_MACHINE.item().get());
                        output.accept(ModBlocks.GREEN_SOFA.item().get());
                        output.accept(ModBlocks.KITCHEN_COUNTER.item().get());
                        output.accept(ModBlocks.KITCHEN_COUNTER_CABINET.item().get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
