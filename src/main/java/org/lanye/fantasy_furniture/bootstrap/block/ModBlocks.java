package org.lanye.fantasy_furniture.bootstrap.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BanquetteBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.CombinedOrnamentBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.GreenSofaBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.HalfHalfPotBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.JamPotBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.KitchenCounterBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.KitchenCounterCabinetBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.LotteryMachineBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.MixingBowlBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.OvenBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.PestleBowlBlockEntity;
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.core.geolib.AnimatedBlockEntry;

/**
 * 模组方块与对应 {@link net.minecraft.world.item.BlockItem} 的 {@link DeferredRegister} 入口，以及对外
 * {@link RegistryObject} 聚合（简单色表见 {@link CeramicTileBlocks.TileVariant}、{@link WallpaperBlocks.WallpaperVariant}；
 * 其余见 {@link DecorSimpleBlocks}、{@link FurnitureAnimatedBlocks}）。
 */
public final class ModBlocks {

    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FantasyFurniture.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FantasyFurniture.MODID);

    // --- 瓷砖（{@link CeramicTileBlocks.TileVariant}）---

    public static final RegistryObject<Block> PINK_CERAMIC_TILE_BLOCK =
            CeramicTileBlocks.TileVariant.PINK_CERAMIC_TILE.entry().block();
    public static final RegistryObject<Item> PINK_CERAMIC_TILE_ITEM =
            CeramicTileBlocks.TileVariant.PINK_CERAMIC_TILE.entry().item();

    public static final RegistryObject<Block> YELLOW_CERAMIC_TILE_BLOCK =
            CeramicTileBlocks.TileVariant.YELLOW_CERAMIC_TILE.entry().block();
    public static final RegistryObject<Item> YELLOW_CERAMIC_TILE_ITEM =
            CeramicTileBlocks.TileVariant.YELLOW_CERAMIC_TILE.entry().item();

    public static final RegistryObject<Block> BLUE_CERAMIC_TILE_BLOCK =
            CeramicTileBlocks.TileVariant.BLUE_CERAMIC_TILE.entry().block();
    public static final RegistryObject<Item> BLUE_CERAMIC_TILE_ITEM =
            CeramicTileBlocks.TileVariant.BLUE_CERAMIC_TILE.entry().item();

    public static final RegistryObject<Block> GREEN_CERAMIC_TILE_BLOCK =
            CeramicTileBlocks.TileVariant.GREEN_CERAMIC_TILE.entry().block();
    public static final RegistryObject<Item> GREEN_CERAMIC_TILE_ITEM =
            CeramicTileBlocks.TileVariant.GREEN_CERAMIC_TILE.entry().item();

    public static final RegistryObject<Block> CYAN_CERAMIC_TILE_BLOCK =
            CeramicTileBlocks.TileVariant.CYAN_CERAMIC_TILE.entry().block();
    public static final RegistryObject<Item> CYAN_CERAMIC_TILE_ITEM =
            CeramicTileBlocks.TileVariant.CYAN_CERAMIC_TILE.entry().item();

    public static final RegistryObject<Block> PURPLE_CERAMIC_TILE_BLOCK =
            CeramicTileBlocks.TileVariant.PURPLE_CERAMIC_TILE.entry().block();
    public static final RegistryObject<Item> PURPLE_CERAMIC_TILE_ITEM =
            CeramicTileBlocks.TileVariant.PURPLE_CERAMIC_TILE.entry().item();

    // --- 壁纸 / 墙裙（{@link WallpaperBlocks.WallpaperVariant}）---

    public static final RegistryObject<Block> PINK_WALLPAPER_BLOCK =
            WallpaperBlocks.WallpaperVariant.PINK_WALLPAPER.entry().block();
    public static final RegistryObject<Item> PINK_WALLPAPER_ITEM =
            WallpaperBlocks.WallpaperVariant.PINK_WALLPAPER.entry().item();

    public static final RegistryObject<Block> RED_WALLPAPER_BLOCK =
            WallpaperBlocks.WallpaperVariant.RED_WALLPAPER.entry().block();
    public static final RegistryObject<Item> RED_WALLPAPER_ITEM =
            WallpaperBlocks.WallpaperVariant.RED_WALLPAPER.entry().item();

    public static final RegistryObject<Block> YELLOW_WALLPAPER_BLOCK =
            WallpaperBlocks.WallpaperVariant.YELLOW_WALLPAPER.entry().block();
    public static final RegistryObject<Item> YELLOW_WALLPAPER_ITEM =
            WallpaperBlocks.WallpaperVariant.YELLOW_WALLPAPER.entry().item();

    public static final RegistryObject<Block> YELLOW_WAINSCOT_BLOCK =
            WallpaperBlocks.WallpaperVariant.YELLOW_WAINSCOT.entry().block();
    public static final RegistryObject<Item> YELLOW_WAINSCOT_ITEM =
            WallpaperBlocks.WallpaperVariant.YELLOW_WAINSCOT.entry().item();

    public static final RegistryObject<Block> BLUE_WALLPAPER_BLOCK =
            WallpaperBlocks.WallpaperVariant.BLUE_WALLPAPER.entry().block();
    public static final RegistryObject<Item> BLUE_WALLPAPER_ITEM =
            WallpaperBlocks.WallpaperVariant.BLUE_WALLPAPER.entry().item();

    public static final RegistryObject<Block> GREEN_WALLPAPER_BLOCK =
            WallpaperBlocks.WallpaperVariant.GREEN_WALLPAPER.entry().block();
    public static final RegistryObject<Item> GREEN_WALLPAPER_ITEM =
            WallpaperBlocks.WallpaperVariant.GREEN_WALLPAPER.entry().item();

    public static final RegistryObject<Block> PURPLE_WALLPAPER_BLOCK =
            WallpaperBlocks.WallpaperVariant.PURPLE_WALLPAPER.entry().block();
    public static final RegistryObject<Item> PURPLE_WALLPAPER_ITEM =
            WallpaperBlocks.WallpaperVariant.PURPLE_WALLPAPER.entry().item();

    // --- 屏风、玻璃窗（{@link DecorSimpleBlocks}）---

    public static final RegistryObject<Block> DECORATIVE_SCREEN_BLOCK = DecorSimpleBlocks.DECORATIVE_SCREEN_BLOCK;
    public static final RegistryObject<Item> DECORATIVE_SCREEN_ITEM = DecorSimpleBlocks.DECORATIVE_SCREEN_ITEM;

    public static final RegistryObject<Block> GLASS_WINDOW_BLOCK = DecorSimpleBlocks.GLASS_WINDOW_BLOCK;
    public static final RegistryObject<Item> GLASS_WINDOW_ITEM = DecorSimpleBlocks.GLASS_WINDOW_ITEM;

    // --- GeckoLib 家具（{@link FurnitureAnimatedBlocks}）---

    public static final AnimatedBlockEntry<BanquetteBlockEntity> BANQUETTE = FurnitureAnimatedBlocks.BANQUETTE;
    public static final AnimatedBlockEntry<MixingBowlBlockEntity> MIXING_BOWL = FurnitureAnimatedBlocks.MIXING_BOWL;
    public static final AnimatedBlockEntry<JamPotBlockEntity> JAM_POT = FurnitureAnimatedBlocks.JAM_POT;
    public static final AnimatedBlockEntry<OvenBlockEntity> OVEN = FurnitureAnimatedBlocks.OVEN;
    public static final AnimatedBlockEntry<PestleBowlBlockEntity> PESTLE_BOWL = FurnitureAnimatedBlocks.PESTLE_BOWL;
    public static final AnimatedBlockEntry<LotteryMachineBlockEntity> LOTTERY_MACHINE = FurnitureAnimatedBlocks.LOTTERY_MACHINE;
    public static final AnimatedBlockEntry<HalfHalfPotBlockEntity> HALF_HALF_POT = FurnitureAnimatedBlocks.HALF_HALF_POT;
    public static final AnimatedBlockEntry<GreenSofaBlockEntity> GREEN_SOFA = FurnitureAnimatedBlocks.GREEN_SOFA;
    public static final AnimatedBlockEntry<KitchenCounterCabinetBlockEntity> KITCHEN_COUNTER_CABINET =
            FurnitureAnimatedBlocks.KITCHEN_COUNTER_CABINET;
    public static final AnimatedBlockEntry<KitchenCounterBlockEntity> KITCHEN_COUNTER = FurnitureAnimatedBlocks.KITCHEN_COUNTER;
    public static final AnimatedBlockEntry<CombinedOrnamentBlockEntity> COMBINED_ORNAMENT =
            FurnitureAnimatedBlocks.COMBINED_ORNAMENT;
    public static final AnimatedBlockEntry<SweeperDockBlockEntity> SWEEPER_DOCK =
            FurnitureAnimatedBlocks.SWEEPER_DOCK;

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}
