package org.lanye.fantasy_furniture.block.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.block.entity.BanquetteBlockEntity;
import org.lanye.fantasy_furniture.block.entity.GreenSofaBlockEntity;
import org.lanye.fantasy_furniture.block.entity.HalfHalfPotBlockEntity;
import org.lanye.fantasy_furniture.block.entity.JamPotBlockEntity;
import org.lanye.fantasy_furniture.block.entity.KitchenCounterBlockEntity;
import org.lanye.fantasy_furniture.block.entity.KitchenCounterCabinetBlockEntity;
import org.lanye.fantasy_furniture.block.entity.LotteryMachineBlockEntity;
import org.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;
import org.lanye.fantasy_furniture.block.entity.OvenBlockEntity;
import org.lanye.fantasy_furniture.block.entity.PestleBowlBlockEntity;
import org.lanye.fantasy_furniture.geolib.AnimatedBlockEntry;

/**
 * 模组方块与对应 {@link net.minecraft.world.item.BlockItem} 的 {@link DeferredRegister} 入口，以及对外
 * {@link RegistryObject} 聚合（具体登记见 {@link CeramicTileBlocks}、{@link WallpaperBlocks}、{@link DecorSimpleBlocks}、
 * {@link FurnitureAnimatedBlocks}）。
 */
public final class ModBlocks {

    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FantasyFurniture.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FantasyFurniture.MODID);

    // --- 瓷砖（{@link CeramicTileBlocks}）---

    public static final RegistryObject<Block> PINK_CERAMIC_TILE_BLOCK = CeramicTileBlocks.PINK_CERAMIC_TILE.block();
    public static final RegistryObject<Item> PINK_CERAMIC_TILE_ITEM = CeramicTileBlocks.PINK_CERAMIC_TILE.item();

    public static final RegistryObject<Block> YELLOW_CERAMIC_TILE_BLOCK = CeramicTileBlocks.YELLOW_CERAMIC_TILE.block();
    public static final RegistryObject<Item> YELLOW_CERAMIC_TILE_ITEM = CeramicTileBlocks.YELLOW_CERAMIC_TILE.item();

    public static final RegistryObject<Block> BLUE_CERAMIC_TILE_BLOCK = CeramicTileBlocks.BLUE_CERAMIC_TILE.block();
    public static final RegistryObject<Item> BLUE_CERAMIC_TILE_ITEM = CeramicTileBlocks.BLUE_CERAMIC_TILE.item();

    public static final RegistryObject<Block> GREEN_CERAMIC_TILE_BLOCK = CeramicTileBlocks.GREEN_CERAMIC_TILE.block();
    public static final RegistryObject<Item> GREEN_CERAMIC_TILE_ITEM = CeramicTileBlocks.GREEN_CERAMIC_TILE.item();

    public static final RegistryObject<Block> CYAN_CERAMIC_TILE_BLOCK = CeramicTileBlocks.CYAN_CERAMIC_TILE.block();
    public static final RegistryObject<Item> CYAN_CERAMIC_TILE_ITEM = CeramicTileBlocks.CYAN_CERAMIC_TILE.item();

    public static final RegistryObject<Block> PURPLE_CERAMIC_TILE_BLOCK = CeramicTileBlocks.PURPLE_CERAMIC_TILE.block();
    public static final RegistryObject<Item> PURPLE_CERAMIC_TILE_ITEM = CeramicTileBlocks.PURPLE_CERAMIC_TILE.item();

    // --- 壁纸 / 墙裙（{@link WallpaperBlocks}）---

    public static final RegistryObject<Block> PINK_WALLPAPER_BLOCK = WallpaperBlocks.PINK_WALLPAPER.block();
    public static final RegistryObject<Item> PINK_WALLPAPER_ITEM = WallpaperBlocks.PINK_WALLPAPER.item();

    public static final RegistryObject<Block> RED_WALLPAPER_BLOCK = WallpaperBlocks.RED_WALLPAPER.block();
    public static final RegistryObject<Item> RED_WALLPAPER_ITEM = WallpaperBlocks.RED_WALLPAPER.item();

    public static final RegistryObject<Block> YELLOW_WALLPAPER_BLOCK = WallpaperBlocks.YELLOW_WALLPAPER.block();
    public static final RegistryObject<Item> YELLOW_WALLPAPER_ITEM = WallpaperBlocks.YELLOW_WALLPAPER.item();

    public static final RegistryObject<Block> YELLOW_WAINSCOT_BLOCK = WallpaperBlocks.YELLOW_WAINSCOT.block();
    public static final RegistryObject<Item> YELLOW_WAINSCOT_ITEM = WallpaperBlocks.YELLOW_WAINSCOT.item();

    public static final RegistryObject<Block> BLUE_WALLPAPER_BLOCK = WallpaperBlocks.BLUE_WALLPAPER.block();
    public static final RegistryObject<Item> BLUE_WALLPAPER_ITEM = WallpaperBlocks.BLUE_WALLPAPER.item();

    public static final RegistryObject<Block> GREEN_WALLPAPER_BLOCK = WallpaperBlocks.GREEN_WALLPAPER.block();
    public static final RegistryObject<Item> GREEN_WALLPAPER_ITEM = WallpaperBlocks.GREEN_WALLPAPER.item();

    public static final RegistryObject<Block> PURPLE_WALLPAPER_BLOCK = WallpaperBlocks.PURPLE_WALLPAPER.block();
    public static final RegistryObject<Item> PURPLE_WALLPAPER_ITEM = WallpaperBlocks.PURPLE_WALLPAPER.item();

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

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}
