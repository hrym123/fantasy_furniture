package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.GreenSofaBlock;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BanquetteBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.GreenSofaBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.HalfHalfPotBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.JamPotBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.CombinedOrnamentBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.KitchenCounterBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.KitchenCounterCabinetBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.LotteryMachineBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.MixingBowlBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.OvenBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.kitchen.blockentity.PestleBowlBlockEntity;
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BanquetteBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.block.CombinedOrnamentBlock;
import org.lanye.fantasy_furniture.content.furniture.kitchen.block.HalfHalfPotBlock;
import org.lanye.fantasy_furniture.content.furniture.kitchen.block.JamPotBlock;
import org.lanye.fantasy_furniture.content.furniture.kitchen.block.KitchenCounterBlock;
import org.lanye.fantasy_furniture.content.furniture.kitchen.block.KitchenCounterCabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.block.LotteryMachineBlock;
import org.lanye.fantasy_furniture.content.furniture.kitchen.block.MixingBowlBlock;
import org.lanye.fantasy_furniture.content.furniture.kitchen.block.OvenBlock;
import org.lanye.fantasy_furniture.content.furniture.kitchen.block.PestleBowlBlock;
import org.lanye.fantasy_furniture.content.sweeper.block.SweeperDockBlock;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.AnimatedBlockRegistration;
import org.lanye.reverie_core.geolib.AnimatedBlockSpec;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/**
 * GeckoLib 动画 / 带方块实体渲染的家具方块注册与属性。
 */
public final class FurnitureAnimatedBlocks {

    private FurnitureAnimatedBlocks() {}

    private static BiFunction<Block, Item.Properties, Item> defaultGeolibBlockItem(String assetBasename) {
        return (block, p) ->
                new GeolibBlockItem(block, p, GeolibItemAssets.blockAsset(FantasyFurniture.MODID, assetBasename));
    }

    private static <BE extends BlockEntity> AnimatedBlockSpec<BE> defaultAnimatedSpec(
            String id,
            Supplier<BlockBehaviour.Properties> propertiesSupplier,
            Function<BlockBehaviour.Properties, ? extends Block> blockFactory,
            BlockEntityType.BlockEntitySupplier<BE> beFactory) {
        return AnimatedBlockRegistration.spec(
                id, propertiesSupplier, blockFactory, beFactory, defaultGeolibBlockItem(id));
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

    /**
     * 表驱动一次注册；列表下标与 {@code I_*} 常量及下方 {@code public static final} 一一对应。卡座、组合摆件为
     * {@link GeolibItemAssets} 特例，其余为默认 {@link GeolibItemAssets#blockAsset}（id 与注册名一致）。
     */
    private static final List<AnimatedBlockEntry<?>> ENTRIES =
            AnimatedBlockRegistration.registerSpecs(
                    ModBlocks.BLOCKS,
                    ModBlocks.BLOCK_ITEMS,
                    ModBlockEntities.BLOCK_ENTITY_TYPES,
                    List.of(
                            AnimatedBlockRegistration.spec(
                                    "banquette",
                                    FurnitureBlockProperties::cherryWoodFurnitureNoOcclusion,
                                    BanquetteBlock::new,
                                    BanquetteBlockEntity::new,
                                    (block, p) ->
                                            new GeolibBlockItem(block, p, geolibBanquetteItemAssets())),
                            defaultAnimatedSpec(
                                    "mixing_bowl",
                                    () -> FurnitureBlockProperties.kitchenCeramic(MapColor.TERRACOTTA_WHITE),
                                    MixingBowlBlock::new,
                                    MixingBowlBlockEntity::new),
                            defaultAnimatedSpec(
                                    "jam_pot",
                                    () -> FurnitureBlockProperties.kitchenCeramic(MapColor.COLOR_RED),
                                    JamPotBlock::new,
                                    JamPotBlockEntity::new),
                            defaultAnimatedSpec(
                                    "oven",
                                    FurnitureBlockProperties::metalNoOcclusion,
                                    OvenBlock::new,
                                    OvenBlockEntity::new),
                            defaultAnimatedSpec(
                                    "pestle_bowl",
                                    () -> FurnitureBlockProperties.kitchenCeramic(MapColor.TERRACOTTA_WHITE),
                                    PestleBowlBlock::new,
                                    PestleBowlBlockEntity::new),
                            defaultAnimatedSpec(
                                    "lottery_machine",
                                    FurnitureBlockProperties::metalNoOcclusion,
                                    LotteryMachineBlock::new,
                                    LotteryMachineBlockEntity::new),
                            defaultAnimatedSpec(
                                    "half_half_pot",
                                    () -> FurnitureBlockProperties.kitchenCeramic(MapColor.TERRACOTTA_WHITE),
                                    HalfHalfPotBlock::new,
                                    HalfHalfPotBlockEntity::new),
                            defaultAnimatedSpec(
                                    "green_sofa",
                                    () ->
                                            FurnitureBlockProperties.woolFurnitureNoOcclusion(
                                                    MapColor.COLOR_GREEN),
                                    GreenSofaBlock::new,
                                    GreenSofaBlockEntity::new),
                            defaultAnimatedSpec(
                                    "kitchen_counter_cabinet",
                                    FurnitureBlockProperties::woodCabinetNoOcclusion,
                                    KitchenCounterCabinetBlock::new,
                                    KitchenCounterCabinetBlockEntity::new),
                            defaultAnimatedSpec(
                                    "kitchen_counter",
                                    FurnitureBlockProperties::woodCabinetNoOcclusion,
                                    KitchenCounterBlock::new,
                                    KitchenCounterBlockEntity::new),
                            AnimatedBlockRegistration.spec(
                                    "combined_ornament",
                                    FurnitureBlockProperties::woodCabinetNoOcclusion,
                                    CombinedOrnamentBlock::new,
                                    CombinedOrnamentBlockEntity::new,
                                    (block, p) ->
                                            new GeolibBlockItem(block, p, combinedOrnamentItemAssets())),
                            defaultAnimatedSpec(
                                    "sweeper_dock",
                                    FurnitureBlockProperties::metalNoOcclusion,
                                    SweeperDockBlock::new,
                                    SweeperDockBlockEntity::new)));

    private static final int I_BANQUETTE = 0;
    private static final int I_MIXING_BOWL = 1;
    private static final int I_JAM_POT = 2;
    private static final int I_OVEN = 3;
    private static final int I_PESTLE_BOWL = 4;
    private static final int I_LOTTERY_MACHINE = 5;
    private static final int I_HALF_HALF_POT = 6;
    private static final int I_GREEN_SOFA = 7;
    private static final int I_KITCHEN_COUNTER_CABINET = 8;
    private static final int I_KITCHEN_COUNTER = 9;
    private static final int I_COMBINED_ORNAMENT = 10;
    private static final int I_SWEEPER_DOCK = 11;

    @SuppressWarnings("unchecked")
    private static <BE extends BlockEntity> AnimatedBlockEntry<BE> animatedEntry(int index) {
        return (AnimatedBlockEntry<BE>) ENTRIES.get(index);
    }

    /**
     * 卡座：直段 / 拐角两套 Geo；方块实体渲染见
     * {@link org.lanye.reverie_core.geolib.client.GeolibAnimatedBlockRenderers#variableBasenameGeoRendererProvider}。
     */
    public static final AnimatedBlockEntry<BanquetteBlockEntity> BANQUETTE = animatedEntry(I_BANQUETTE);

    public static final AnimatedBlockEntry<MixingBowlBlockEntity> MIXING_BOWL = animatedEntry(I_MIXING_BOWL);

    public static final AnimatedBlockEntry<JamPotBlockEntity> JAM_POT = animatedEntry(I_JAM_POT);

    public static final AnimatedBlockEntry<OvenBlockEntity> OVEN = animatedEntry(I_OVEN);

    public static final AnimatedBlockEntry<PestleBowlBlockEntity> PESTLE_BOWL = animatedEntry(I_PESTLE_BOWL);

    public static final AnimatedBlockEntry<LotteryMachineBlockEntity> LOTTERY_MACHINE =
            animatedEntry(I_LOTTERY_MACHINE);

    public static final AnimatedBlockEntry<HalfHalfPotBlockEntity> HALF_HALF_POT = animatedEntry(I_HALF_HALF_POT);

    public static final AnimatedBlockEntry<GreenSofaBlockEntity> GREEN_SOFA = animatedEntry(I_GREEN_SOFA);

    public static final AnimatedBlockEntry<KitchenCounterCabinetBlockEntity> KITCHEN_COUNTER_CABINET =
            animatedEntry(I_KITCHEN_COUNTER_CABINET);

    public static final AnimatedBlockEntry<KitchenCounterBlockEntity> KITCHEN_COUNTER =
            animatedEntry(I_KITCHEN_COUNTER);

    public static final AnimatedBlockEntry<CombinedOrnamentBlockEntity> COMBINED_ORNAMENT =
            animatedEntry(I_COMBINED_ORNAMENT);

    public static final AnimatedBlockEntry<SweeperDockBlockEntity> SWEEPER_DOCK = animatedEntry(I_SWEEPER_DOCK);
}
