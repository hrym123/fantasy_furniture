package org.lanye.fantasy_furniture.bootstrap.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate6Registration;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.bootstrap.block.PlainGlassWindowRegistration;
import org.lanye.fantasy_furniture.content.soap.SoapBarCreativeTab;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagCreativeTab;
import org.lanye.fantasy_furniture.content.soap.SoapBoxCreativeTab;
import org.lanye.fantasy_furniture.content.debug.DevelopmentMode;

/**
 * 本模组创造模式物品栏（CreativeModeTab）注册。
 */
public final class ModCreativeTabs {

    private ModCreativeTabs() {}

    /**
     * 「幻想家具」主创造栏中物品的**唯一展示顺序**。新增方块物品或独立 {@link net.minecraft.world.item.Item}
     * 时须在此 {@link #mainTabDisplayOrder()} 追加条目，勿在 {@link #MAIN} 的 {@code displayItems} 中重复手写。
     */

    /**
     * 创造栏展示顺序；在 {@link #MAIN} 的 {@code displayItems} 回调中构建，避免早于注册表完成时静态捕获物品。
     */
    private static List<Consumer<CreativeModeTab.Output>> mainTabDisplayOrder() {
        List<Consumer<CreativeModeTab.Output>> list = new ArrayList<>();
        list.add(out -> out.accept(ModBlocks.PINK_CERAMIC_TILE_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.YELLOW_CERAMIC_TILE_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.BLUE_CERAMIC_TILE_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.GREEN_CERAMIC_TILE_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.CYAN_CERAMIC_TILE_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.PURPLE_CERAMIC_TILE_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.DECORATIVE_SCREEN_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.PINK_WALLPAPER_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.RED_WALLPAPER_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.YELLOW_WALLPAPER_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.YELLOW_WAINSCOT_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.BLUE_WALLPAPER_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.GREEN_WALLPAPER_ITEM.get()));
        list.add(out -> out.accept(ModBlocks.PURPLE_WALLPAPER_ITEM.get()));
        list.add(out -> out.accept(ModItems.PAINT_BRUSH.get()));
        list.add(out -> out.accept(ModItems.FABRIC_COIL.get()));
        if (DevelopmentMode.enabled()) {
            list.add(out -> out.accept(ModItems.DEBUG_VARIANT_STICK.get()));
            list.add(out -> out.accept(ModBlocks.GEOLIB_ALIGNMENT_PROBE.item().get()));
        }
        list.add(out -> out.accept(ModItems.TANGHULU.get()));
        list.add(out -> out.accept(ModItems.ARCANE_WAND.get()));
        list.add(out -> out.accept(ModItems.DECORATIVE_HELMET_BLUE_TOP_HAT.get()));
        list.add(out -> out.accept(ModItems.DECORATIVE_HELMET_PINK_TOP_HAT.get()));
        list.add(out -> out.accept(ModItems.DECORATIVE_HELMET_LOP_EARED_RABBIT.get()));
        list.add(out -> out.accept(ModBlocks.BANQUETTE.item().get()));
        SoapBarCreativeTab.appendDefaultWearEntries(list);
        SoapBoxCreativeTab.appendClosedEmptyBoxEntries(list);
        list.add(out -> out.accept(ModBlocks.SOAP_RACK.item().get()));
        SoapPaperBagCreativeTab.appendEntries(list);
        list.add(out -> out.accept(ModBlocks.LOTTERY_MACHINE.item().get()));
        list.add(out -> out.accept(ModBlocks.SWEEPER_DOCK.item().get()));
        list.add(out -> out.accept(ModBlocks.GREEN_SOFA.item().get()));
        for (Supplier<? extends ItemLike> bedPlate6 : BedPlate6Registration.creativeTabSegment()) {
            list.add(out -> out.accept(bedPlate6.get()));
        }
        list.add(out -> out.accept(ModBlocks.COMBINED_ORNAMENT.item().get()));
        for (var ro : PlainGlassWindowRegistration.items()) {
            list.add(out -> out.accept(ro.get()));
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
                        for (Consumer<CreativeModeTab.Output> entry : mainTabDisplayOrder()) {
                            entry.accept(output);
                        }
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
