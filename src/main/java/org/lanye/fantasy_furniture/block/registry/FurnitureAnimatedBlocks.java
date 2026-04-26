package org.lanye.fantasy_furniture.block.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.block.decor.GreenSofaBlock;
import org.lanye.fantasy_furniture.block.entity.BanquetteBlockEntity;
import org.lanye.fantasy_furniture.block.entity.GreenSofaBlockEntity;
import org.lanye.fantasy_furniture.block.entity.HalfHalfPotBlockEntity;
import org.lanye.fantasy_furniture.block.entity.JamPotBlockEntity;
import org.lanye.fantasy_furniture.block.entity.CombinedOrnamentBlockEntity;
import org.lanye.fantasy_furniture.block.entity.KitchenCounterBlockEntity;
import org.lanye.fantasy_furniture.block.entity.KitchenCounterCabinetBlockEntity;
import org.lanye.fantasy_furniture.block.entity.LotteryMachineBlockEntity;
import org.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;
import org.lanye.fantasy_furniture.block.entity.OvenBlockEntity;
import org.lanye.fantasy_furniture.block.entity.PestleBowlBlockEntity;
import org.lanye.fantasy_furniture.block.entity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.block.facing.BanquetteBlock;
import org.lanye.fantasy_furniture.block.facing.CombinedOrnamentBlock;
import org.lanye.fantasy_furniture.block.facing.HalfHalfPotBlock;
import org.lanye.fantasy_furniture.block.facing.JamPotBlock;
import org.lanye.fantasy_furniture.block.facing.KitchenCounterBlock;
import org.lanye.fantasy_furniture.block.facing.KitchenCounterCabinetBlock;
import org.lanye.fantasy_furniture.block.facing.LotteryMachineBlock;
import org.lanye.fantasy_furniture.block.facing.MixingBowlBlock;
import org.lanye.fantasy_furniture.block.facing.OvenBlock;
import org.lanye.fantasy_furniture.block.facing.PestleBowlBlock;
import org.lanye.fantasy_furniture.block.facing.SweeperDockBlock;
import org.lanye.fantasy_furniture.geolib.AnimatedBlockEntry;
import org.lanye.fantasy_furniture.geolib.AnimatedBlockRegistration;
import org.lanye.fantasy_furniture.geolib.GeolibBlockItem;
import org.lanye.fantasy_furniture.geolib.GeolibItemAssets;
import org.lanye.fantasy_furniture.registry.ModBlockEntities;

/**
 * GeckoLib 动画 / 带方块实体渲染的家具方块注册与属性。
 */
public final class FurnitureAnimatedBlocks {

    private FurnitureAnimatedBlocks() {}

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
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "geo/block/banquette_straight.geo.json"),
                ResourceLocation.fromNamespaceAndPath(FantasyFurniture.MODID, "textures/block/banquette.png"),
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "animations/block/banquette_straight.animation.json"));
    }

    /**
     * 卡座：直段 / 拐角两套 Geo；方块实体渲染见
     * {@link org.lanye.fantasy_furniture.geolib.client.GeolibAnimatedBlockRenderers#variableBasenameGeoRendererProvider}。
     */
    public static final AnimatedBlockEntry<BanquetteBlockEntity> BANQUETTE =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "banquette",
                            FurnitureAnimatedBlocks::banquetteProperties,
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

    private static BlockBehaviour.Properties combinedOrnamentProperties() {
        return kitchenCounterCabinetProperties();
    }

    private static BlockBehaviour.Properties sweeperDockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.5f, 6.0f)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    /**
     * 组合摆件物品：手持预览使用玩偶 A 子模型（世界中为底座+玩偶双 Geo 差分渲染）。
     */
    private static GeolibItemAssets combinedOrnamentItemAssets() {
        return new GeolibItemAssets(
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "geo/block/combined_ornament_figurine_a.geo.json"),
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "textures/block/combined_ornament_figurine_a.png"),
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "animations/block/combined_ornament_figurine_a.animation.json"));
    }

    public static final AnimatedBlockEntry<MixingBowlBlockEntity> MIXING_BOWL =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "mixing_bowl",
                            FurnitureAnimatedBlocks::mixingBowlProperties,
                            MixingBowlBlock::new,
                            MixingBowlBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "mixing_bowl"))));

    public static final AnimatedBlockEntry<JamPotBlockEntity> JAM_POT =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "jam_pot",
                            FurnitureAnimatedBlocks::jamPotProperties,
                            JamPotBlock::new,
                            JamPotBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "jam_pot"))));

    public static final AnimatedBlockEntry<OvenBlockEntity> OVEN =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "oven",
                            FurnitureAnimatedBlocks::ovenProperties,
                            OvenBlock::new,
                            OvenBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "oven"))));

    public static final AnimatedBlockEntry<PestleBowlBlockEntity> PESTLE_BOWL =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "pestle_bowl",
                            FurnitureAnimatedBlocks::pestleBowlProperties,
                            PestleBowlBlock::new,
                            PestleBowlBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "pestle_bowl"))));

    public static final AnimatedBlockEntry<LotteryMachineBlockEntity> LOTTERY_MACHINE =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "lottery_machine",
                            FurnitureAnimatedBlocks::lotteryMachineProperties,
                            LotteryMachineBlock::new,
                            LotteryMachineBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "lottery_machine"))));

    public static final AnimatedBlockEntry<HalfHalfPotBlockEntity> HALF_HALF_POT =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "half_half_pot",
                            FurnitureAnimatedBlocks::halfHalfPotProperties,
                            HalfHalfPotBlock::new,
                            HalfHalfPotBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "half_half_pot"))));

    public static final AnimatedBlockEntry<GreenSofaBlockEntity> GREEN_SOFA =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "green_sofa",
                            FurnitureAnimatedBlocks::greenSofaProperties,
                            GreenSofaBlock::new,
                            GreenSofaBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "green_sofa"))));

    public static final AnimatedBlockEntry<KitchenCounterCabinetBlockEntity> KITCHEN_COUNTER_CABINET =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "kitchen_counter_cabinet",
                            FurnitureAnimatedBlocks::kitchenCounterCabinetProperties,
                            KitchenCounterCabinetBlock::new,
                            KitchenCounterCabinetBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(
                                                    FantasyFurniture.MODID, "kitchen_counter_cabinet"))));

    public static final AnimatedBlockEntry<KitchenCounterBlockEntity> KITCHEN_COUNTER =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "kitchen_counter",
                            FurnitureAnimatedBlocks::kitchenCounterProperties,
                            KitchenCounterBlock::new,
                            KitchenCounterBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "kitchen_counter"))));

    public static final AnimatedBlockEntry<CombinedOrnamentBlockEntity> COMBINED_ORNAMENT =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "combined_ornament",
                            FurnitureAnimatedBlocks::combinedOrnamentProperties,
                            CombinedOrnamentBlock::new,
                            CombinedOrnamentBlockEntity::new,
                            (block, p) -> new GeolibBlockItem(block, p, combinedOrnamentItemAssets())));

    public static final AnimatedBlockEntry<SweeperDockBlockEntity> SWEEPER_DOCK =
            AnimatedBlockRegistration.registerSpec(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    AnimatedBlockRegistration.spec(
                            "sweeper_dock",
                            FurnitureAnimatedBlocks::sweeperDockProperties,
                            SweeperDockBlock::new,
                            SweeperDockBlockEntity::new,
                            (block, p) ->
                                    new GeolibBlockItem(
                                            block,
                                            p,
                                            GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "sweeper_dock"))));
}
