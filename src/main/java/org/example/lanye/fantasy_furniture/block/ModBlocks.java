package org.example.lanye.fantasy_furniture.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;
import org.example.lanye.fantasy_furniture.block.facing.BanquetteBlock;
import org.example.lanye.fantasy_furniture.block.facing.HalfHalfPotBlock;
import org.example.lanye.fantasy_furniture.block.facing.JamPotBlock;
import org.example.lanye.fantasy_furniture.block.facing.KitchenCounterBlock;
import org.example.lanye.fantasy_furniture.block.facing.KitchenCounterCabinetBlock;
import org.example.lanye.fantasy_furniture.block.facing.LotteryMachineBlock;
import org.example.lanye.fantasy_furniture.block.facing.MixingBowlBlock;
import org.example.lanye.fantasy_furniture.block.facing.OvenBlock;
import org.example.lanye.fantasy_furniture.block.facing.PestleBowlBlock;
import org.example.lanye.fantasy_furniture.block.entity.BanquetteBlockEntity;
import org.example.lanye.fantasy_furniture.block.entity.GreenSofaBlockEntity;
import org.example.lanye.fantasy_furniture.block.entity.HalfHalfPotBlockEntity;
import org.example.lanye.fantasy_furniture.block.entity.JamPotBlockEntity;
import org.example.lanye.fantasy_furniture.block.entity.KitchenCounterBlockEntity;
import org.example.lanye.fantasy_furniture.block.entity.KitchenCounterCabinetBlockEntity;
import org.example.lanye.fantasy_furniture.block.entity.LotteryMachineBlockEntity;
import org.example.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;
import org.example.lanye.fantasy_furniture.block.entity.OvenBlockEntity;
import org.example.lanye.fantasy_furniture.block.entity.PestleBowlBlockEntity;
import org.example.lanye.fantasy_furniture.geolib.AnimatedBlockEntry;
import org.example.lanye.fantasy_furniture.geolib.AnimatedBlockRegistration;
import org.example.lanye.fantasy_furniture.geolib.GeolibBlockItem;
import org.example.lanye.fantasy_furniture.geolib.GeolibItemAssets;
import org.example.lanye.fantasy_furniture.registry.ModBlockEntities;

/**
 * 本模组方块及对应 {@link BlockItem} 的注册与引用。
 */
public final class ModBlocks {

    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Fantasy_furniture.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Fantasy_furniture.MODID);

    private static BlockBehaviour.Properties ceramicTileProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(1.2f, 6.0f)
                .sound(SoundType.DEEPSLATE_TILES);
    }

    /** 壁纸：较软，侧面颜色决定 {@link MapColor} */
    private static BlockBehaviour.Properties wallpaperProperties(MapColor sideColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(sideColor)
                .strength(0.6f)
                .sound(SoundType.WOOL);
    }

    /** 粉色瓷砖（材质见 {@code textures/block/pink_ceramic_tile.png}） */
    public static final RegistryObject<Block> PINK_CERAMIC_TILE_BLOCK = BLOCKS.register("pink_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_PINK)));
    public static final RegistryObject<Item> PINK_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("pink_ceramic_tile", () -> new BlockItem(PINK_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> YELLOW_CERAMIC_TILE_BLOCK = BLOCKS.register("yellow_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_YELLOW)));
    public static final RegistryObject<Item> YELLOW_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("yellow_ceramic_tile", () -> new BlockItem(YELLOW_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> BLUE_CERAMIC_TILE_BLOCK = BLOCKS.register("blue_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_BLUE)));
    public static final RegistryObject<Item> BLUE_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("blue_ceramic_tile", () -> new BlockItem(BLUE_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> GREEN_CERAMIC_TILE_BLOCK = BLOCKS.register("green_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_GREEN)));
    public static final RegistryObject<Item> GREEN_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("green_ceramic_tile", () -> new BlockItem(GREEN_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> CYAN_CERAMIC_TILE_BLOCK = BLOCKS.register("cyan_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_CYAN)));
    public static final RegistryObject<Item> CYAN_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("cyan_ceramic_tile", () -> new BlockItem(CYAN_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PURPLE_CERAMIC_TILE_BLOCK = BLOCKS.register("purple_ceramic_tile", () -> new Block(ceramicTileProperties(MapColor.TERRACOTTA_PURPLE)));
    public static final RegistryObject<Item> PURPLE_CERAMIC_TILE_ITEM = BLOCK_ITEMS.register("purple_ceramic_tile", () -> new BlockItem(PURPLE_CERAMIC_TILE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PINK_WALLPAPER_BLOCK = BLOCKS.register("pink_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_PINK)));
    public static final RegistryObject<Item> PINK_WALLPAPER_ITEM = BLOCK_ITEMS.register("pink_wallpaper", () -> new BlockItem(PINK_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> RED_WALLPAPER_BLOCK = BLOCKS.register("red_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_RED)));
    public static final RegistryObject<Item> RED_WALLPAPER_ITEM = BLOCK_ITEMS.register("red_wallpaper", () -> new BlockItem(RED_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> YELLOW_WALLPAPER_BLOCK = BLOCKS.register("yellow_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_YELLOW)));
    public static final RegistryObject<Item> YELLOW_WALLPAPER_ITEM = BLOCK_ITEMS.register("yellow_wallpaper", () -> new BlockItem(YELLOW_WALLPAPER_BLOCK.get(), new Item.Properties()));

    /** 黄色墙裙：顶 / 侧 / 底分贴图（与壁纸同类属性，锄头可挖）。 */
    public static final RegistryObject<Block> YELLOW_WAINSCOT_BLOCK =
            BLOCKS.register("yellow_wainscot", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_YELLOW)));
    public static final RegistryObject<Item> YELLOW_WAINSCOT_ITEM =
            BLOCK_ITEMS.register("yellow_wainscot", () -> new BlockItem(YELLOW_WAINSCOT_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> BLUE_WALLPAPER_BLOCK = BLOCKS.register("blue_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_BLUE)));
    public static final RegistryObject<Item> BLUE_WALLPAPER_ITEM = BLOCK_ITEMS.register("blue_wallpaper", () -> new BlockItem(BLUE_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> GREEN_WALLPAPER_BLOCK = BLOCKS.register("green_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_GREEN)));
    public static final RegistryObject<Item> GREEN_WALLPAPER_ITEM = BLOCK_ITEMS.register("green_wallpaper", () -> new BlockItem(GREEN_WALLPAPER_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Block> PURPLE_WALLPAPER_BLOCK = BLOCKS.register("purple_wallpaper", () -> new Block(wallpaperProperties(MapColor.TERRACOTTA_PURPLE)));
    public static final RegistryObject<Item> PURPLE_WALLPAPER_ITEM = BLOCK_ITEMS.register("purple_wallpaper", () -> new BlockItem(PURPLE_WALLPAPER_BLOCK.get(), new Item.Properties()));

    private static BlockBehaviour.Properties decorativeScreenProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0f)
                .sound(SoundType.CHERRY_WOOD)
                .noOcclusion();
    }

    /** 屏风：{@link DecorativeScreenBlock}（两格高、四向，无方块实体）。 */
    public static final RegistryObject<Block> DECORATIVE_SCREEN_BLOCK =
            BLOCKS.register("decorative_screen", () -> new DecorativeScreenBlock(decorativeScreenProperties()));
    public static final RegistryObject<Item> DECORATIVE_SCREEN_ITEM =
            BLOCK_ITEMS.register("decorative_screen", () -> new BlockItem(DECORATIVE_SCREEN_BLOCK.get(), new Item.Properties()));

    /** 玻璃窗：与原版玻璃相近的透明整格方块（无方块实体）。 */
    public static final RegistryObject<Block> GLASS_WINDOW_BLOCK =
            BLOCKS.register("glass_window", () -> new Block(BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<Item> GLASS_WINDOW_ITEM =
            BLOCK_ITEMS.register("glass_window", () -> new BlockItem(GLASS_WINDOW_BLOCK.get(), new Item.Properties()));

    private static BlockBehaviour.Properties banquetteProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0f, 6.0f)
                .sound(SoundType.CHERRY_WOOD)
                .noOcclusion();
    }

    /**
     * 卡座物品：geo/动画 basename 为 {@code banquette_straight}，贴图与方块内渲染一致为 {@code banquette.png}（勿用
     * {@link GeolibItemAssets#blockAsset} 单 basename，否则会错误指向 {@code banquette_straight.png}）。
     */
    private static GeolibItemAssets geolibBanquetteItemAssets() {
        return new GeolibItemAssets(
                ResourceLocation.fromNamespaceAndPath(Fantasy_furniture.MODID, "geo/block/banquette_straight.geo.json"),
                ResourceLocation.fromNamespaceAndPath(Fantasy_furniture.MODID, "textures/block/banquette.png"),
                ResourceLocation.fromNamespaceAndPath(
                        Fantasy_furniture.MODID, "animations/block/banquette_straight.animation.json"));
    }

    /**
     * 卡座：直段 / 拐角两套 Geo；方块实体渲染见
     * {@link org.example.lanye.fantasy_furniture.geolib.client.GeolibAnimatedBlockRenderers#variableBasenameGeoRendererProvider}。
     */
    public static final AnimatedBlockEntry<BanquetteBlockEntity> BANQUETTE =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "banquette",
                            ModBlocks::banquetteProperties,
                            BanquetteBlock::new,
                            BanquetteBlockEntity::new,
                            (block, p) -> new GeolibBlockItem(block, p, geolibBanquetteItemAssets())));

    private static BlockBehaviour.Properties mixingBowlProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_WHITE)
                .strength(1.2f, 6.0f)
                .sound(SoundType.DEEPSLATE_TILES)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties jamPotProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(1.2f, 6.0f)
                .sound(SoundType.DEEPSLATE_TILES)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties ovenProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.5f, 6.0f)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties pestleBowlProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_WHITE)
                .strength(1.2f, 6.0f)
                .sound(SoundType.DEEPSLATE_TILES)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties halfHalfPotProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_WHITE)
                .strength(1.2f, 6.0f)
                .sound(SoundType.DEEPSLATE_TILES)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties lotteryMachineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.5f, 6.0f)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties greenSofaProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
                .strength(0.8f, 6.0f)
                .sound(SoundType.WOOL)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties kitchenCounterCabinetProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0f, 6.0f)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties kitchenCounterProperties() {
        return kitchenCounterCabinetProperties();
    }

    /**
     * 搅拌碗：GeckoLib 动画方块；由 {@link AnimatedBlockRegistration} 按设计书顺序登记
     * {@link net.minecraft.world.level.block.entity.BlockEntityType} 与物品。
     */
    public static final AnimatedBlockEntry<MixingBowlBlockEntity> MIXING_BOWL =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "mixing_bowl",
                            ModBlocks::mixingBowlProperties,
                            MixingBowlBlock::new,
                            MixingBowlBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(
                                                    Fantasy_furniture.MODID, "mixing_bowl"))));

    /** 果酱锅：GeckoLib 动画方块。 */
    public static final AnimatedBlockEntry<JamPotBlockEntity> JAM_POT =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "jam_pot",
                            ModBlocks::jamPotProperties,
                            JamPotBlock::new,
                            JamPotBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(
                                                    Fantasy_furniture.MODID, "jam_pot"))));

    /** 烤箱：GeckoLib 方块实体渲染，无玩法动画。 */
    public static final AnimatedBlockEntry<OvenBlockEntity> OVEN =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "oven",
                            ModBlocks::ovenProperties,
                            OvenBlock::new,
                            OvenBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                                    GeolibItemAssets.blockAsset(
                                                    Fantasy_furniture.MODID, "oven"))));

    /** 捣蒜碗：GeckoLib 捣碎动画。 */
    public static final AnimatedBlockEntry<PestleBowlBlockEntity> PESTLE_BOWL =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "pestle_bowl",
                            ModBlocks::pestleBowlProperties,
                            PestleBowlBlock::new,
                            PestleBowlBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(
                                                    Fantasy_furniture.MODID, "pestle_bowl"))));

    /** 抽奖机：MoonStarfish Geo；右键播放抽奖动画。 */
    public static final AnimatedBlockEntry<LotteryMachineBlockEntity> LOTTERY_MACHINE =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "lottery_machine",
                            ModBlocks::lotteryMachineProperties,
                            LotteryMachineBlock::new,
                            LotteryMachineBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(
                                                    Fantasy_furniture.MODID, "lottery_machine"))));

    /** 半半锅：GeckoLib 静态模型（MoonStarfish）。 */
    public static final AnimatedBlockEntry<HalfHalfPotBlockEntity> HALF_HALF_POT =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "half_half_pot",
                            ModBlocks::halfHalfPotProperties,
                            HalfHalfPotBlock::new,
                            HalfHalfPotBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(
                                                    Fantasy_furniture.MODID, "half_half_pot"))));

    /** 绿色沙发（三连）：GeckoLib 静态模型。 */
    public static final AnimatedBlockEntry<GreenSofaBlockEntity> GREEN_SOFA =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "green_sofa",
                            ModBlocks::greenSofaProperties,
                            GreenSofaBlock::new,
                            GreenSofaBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(
                                                    Fantasy_furniture.MODID, "green_sofa"))));

    /** 橱柜：GeckoLib 静态模型（MoonStarfish）。 */
    public static final AnimatedBlockEntry<KitchenCounterCabinetBlockEntity> KITCHEN_COUNTER_CABINET =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "kitchen_counter_cabinet",
                            ModBlocks::kitchenCounterCabinetProperties,
                            KitchenCounterCabinetBlock::new,
                            KitchenCounterCabinetBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(
                                                    Fantasy_furniture.MODID, "kitchen_counter_cabinet"))));

    /** 料理台：GeckoLib 静态模型（MoonStarfish）。 */
    public static final AnimatedBlockEntry<KitchenCounterBlockEntity> KITCHEN_COUNTER =
            AnimatedBlockRegistration.registerSpec(
                    BLOCKS,
                    BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "kitchen_counter",
                            ModBlocks::kitchenCounterProperties,
                            KitchenCounterBlock::new,
                            KitchenCounterBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(
                                                    Fantasy_furniture.MODID, "kitchen_counter"))));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}
