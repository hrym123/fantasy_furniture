package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
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
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.CombinedOrnamentBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.PlainGlassWindowBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.LotteryMachineBlockEntity;
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BanquetteBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.block.CombinedOrnamentBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.block.LotteryMachineBlock;
import org.lanye.fantasy_furniture.content.sweeper.block.SweeperDockBlock;
import org.lanye.fantasy_furniture.content.soap.block.BodyCreamBlock;
import org.lanye.fantasy_furniture.content.soap.block.BodyWashBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import org.lanye.fantasy_furniture.content.soap.SoapSeriesBlockAssets;
import org.lanye.fantasy_furniture.content.soap.block.SoapMoldBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBagBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBoxBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapRackBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyCreamBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyWashBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.ShampooBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBagBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBoxBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapSeriesBlockItem;
import org.lanye.fantasy_furniture.content.debug.block.GeolibAlignmentProbeBlock;
import org.lanye.fantasy_furniture.content.debug.blockentity.GeolibAlignmentProbeBlockEntity;
import org.lanye.fantasy_furniture.content.debug.item.GeolibAlignmentProbeBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapBoxBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.BodyCreamBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.BodyWashBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBagBlockItem;
import org.lanye.fantasy_furniture.bootstrap.block.PlainGlassWindowRegistration;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.AnimatedBlockRegistration;
import org.lanye.reverie_core.geolib.AnimatedBlockSpec;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;
import org.lanye.reverie_core.geolib.SimpleGeolibEntityBlock;

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

    private static GeolibItemAssets geolibBanquetteItemAssets() {
        return GeolibItemAssets.blockAssetWithTexture(FantasyFurniture.MODID, "banquette_straight", "banquette");
    }

    private static GeolibItemAssets combinedOrnamentItemAssets() {
        return new GeolibItemAssets(
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "geo/block/combined_ornament_figurine_a.geo.json"),
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "textures/block/combined_ornament_figurine_a.png"),
                ResourceLocation.fromNamespaceAndPath(
                        FantasyFurniture.MODID, "animations/block/combined_ornament_figurine_a.animation.json"));
    }

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
                                    "lottery_machine",
                                    FurnitureBlockProperties::metalNoOcclusion,
                                    LotteryMachineBlock::new,
                                    LotteryMachineBlockEntity::new),
                            AnimatedBlockRegistration.spec(
                                    "soap_bar",
                                    () -> FurnitureBlockProperties.bathroomSmallDecor(MapColor.COLOR_LIGHT_BLUE),
                                    SoapBarBlock::new,
                                    SoapBarBlockEntity::new,
                                    (block, props) ->
                                            new SoapBarBlockItem(
                                                    block,
                                                    props,
                                                    GeolibItemAssets.blockAssetWithTexture(
                                                            FantasyFurniture.MODID, "soap_bar", "soap_bar_1"))),
                            AnimatedBlockRegistration.spec(
                                    "soap_box",
                                    () -> FurnitureBlockProperties.bathroomSmallDecor(MapColor.COLOR_LIGHT_GREEN),
                                    SoapBoxBlock::new,
                                    SoapBoxBlockEntity::new,
                                    (block, props) ->
                                            new SoapBoxBlockItem(
                                                    block,
                                                    props,
                                                    GeolibItemAssets.blockAssetWithTexture(
                                                            FantasyFurniture.MODID, "soap_box", "soap_box_1"))),
                            AnimatedBlockRegistration.spec(
                                    "soap_rack",
                                    () -> FurnitureBlockProperties.bathroomSmallDecor(MapColor.WOOD),
                                    SoapRackBlock::new,
                                    SoapRackBlockEntity::new,
                                    (block, props) ->
                                            new GeolibBlockItem(
                                                    block,
                                                    props,
                                                    GeolibItemAssets.blockAssetWithTexture(
                                                            FantasyFurniture.MODID, "soap_rack", "soap_rack_1"))),
                            AnimatedBlockRegistration.spec(
                                    "soap_paper_bag",
                                    () -> FurnitureBlockProperties.bathroomSmallDecor(MapColor.SNOW),
                                    SoapPaperBagBlock::new,
                                    SoapPaperBagBlockEntity::new,
                                    (block, props) ->
                                            new SoapPaperBagBlockItem(
                                                    block,
                                                    props,
                                                    GeolibItemAssets.blockAssetWithTexture(
                                                            FantasyFurniture.MODID,
                                                            "soap_paper_bag",
                                                            "soap_paper_bag_2"))),
                            AnimatedBlockRegistration.spec(
                                    "body_wash",
                                    () -> FurnitureBlockProperties.bathroomSmallDecor(MapColor.COLOR_BLUE),
                                    BodyWashBlock::new,
                                    BodyWashBlockEntity::new,
                                    (block, props) ->
                                            new BodyWashBlockItem(
                                                    block,
                                                    props.stacksTo(64),
                                                    SoapSeriesBlockAssets.blockPrimaryTexture("body_wash"))),
                            AnimatedBlockRegistration.spec(
                                    "shampoo",
                                    () -> FurnitureBlockProperties.bathroomSmallDecor(MapColor.COLOR_CYAN),
                                    props ->
                                            new SimpleGeolibEntityBlock<>(
                                                    props,
                                                    ShampooBlockEntity::new,
                                                    Block.box(5.0, 0.0, 4.0, 11.0, 11.2, 10.0),
                                                    InteractionResult.PASS),
                                    ShampooBlockEntity::new,
                                    (block, props) ->
                                            new SoapSeriesBlockItem(
                                                    block,
                                                    props.stacksTo(64),
                                                    SoapSeriesBlockAssets.blockPrimaryTexture("shampoo"))),
                            AnimatedBlockRegistration.spec(
                                    "body_cream",
                                    () -> FurnitureBlockProperties.bathroomSmallDecor(MapColor.QUARTZ),
                                    BodyCreamBlock::new,
                                    BodyCreamBlockEntity::new,
                                    (block, props) ->
                                            new BodyCreamBlockItem(
                                                    block,
                                                    props.stacksTo(64),
                                                    SoapSeriesBlockAssets.blockPrimaryTexture("body_cream"))),
                            AnimatedBlockRegistration.spec(
                                    "soap_paper_box",
                                    () -> FurnitureBlockProperties.bathroomSmallDecor(MapColor.COLOR_LIGHT_GRAY),
                                    SoapPaperBoxBlock::new,
                                    SoapPaperBoxBlockEntity::new,
                                    (block, props) ->
                                            new SoapPaperBoxBlockItem(
                                                    block,
                                                    props.stacksTo(64),
                                                    SoapSeriesBlockAssets.blockPrimaryTexture("soap_paper_box"))),
                            AnimatedBlockRegistration.spec(
                                    "soap_mold",
                                    () -> FurnitureBlockProperties.bathroomSmallDecor(MapColor.STONE),
                                    SoapMoldBlock::new,
                                    SoapMoldBlockEntity::new,
                                    (block, props) ->
                                            new SoapSeriesBlockItem(
                                                    block,
                                                    props,
                                                    SoapSeriesBlockAssets.blockPrimaryTexture("soap_mold"))),
                            defaultAnimatedSpec(
                                    "green_sofa",
                                    () ->
                                            FurnitureBlockProperties.woolFurnitureNoOcclusion(
                                                    MapColor.COLOR_GREEN),
                                    GreenSofaBlock::new,
                                    GreenSofaBlockEntity::new),
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
                                    SweeperDockBlockEntity::new),
                            AnimatedBlockRegistration.spec(
                                    "geolib_alignment_probe",
                                    FurnitureBlockProperties::woodCabinetNoOcclusion,
                                    GeolibAlignmentProbeBlock::new,
                                    GeolibAlignmentProbeBlockEntity::new,
                                    (block, props) ->
                                            new GeolibAlignmentProbeBlockItem(
                                                    block,
                                                    props,
                                                    GeolibItemAssets.blockAsset(
                                                            FantasyFurniture.MODID, "geolib_alignment_probe")))));

    private static final int I_BANQUETTE = 0;
    private static final int I_LOTTERY_MACHINE = 1;
    private static final int I_SOAP_BAR = 2;
    private static final int I_SOAP_BOX = 3;
    private static final int I_SOAP_RACK = 4;
    private static final int I_SOAP_PAPER_BAG = 5;
    private static final int I_BODY_WASH = 6;
    private static final int I_SHAMPOO = 7;
    private static final int I_BODY_CREAM = 8;
    private static final int I_SOAP_PAPER_BOX = 9;
    private static final int I_SOAP_MOLD = 10;
    private static final int I_GREEN_SOFA = 11;
    private static final int I_COMBINED_ORNAMENT = 12;
    private static final int I_SWEEPER_DOCK = 13;
    private static final int I_GEOLIB_ALIGNMENT_PROBE = 14;

    @SuppressWarnings("unchecked")
    private static <BE extends BlockEntity> AnimatedBlockEntry<BE> animatedEntry(int index) {
        return (AnimatedBlockEntry<BE>) ENTRIES.get(index);
    }

    public static final AnimatedBlockEntry<BanquetteBlockEntity> BANQUETTE = animatedEntry(I_BANQUETTE);

    public static final AnimatedBlockEntry<LotteryMachineBlockEntity> LOTTERY_MACHINE =
            animatedEntry(I_LOTTERY_MACHINE);

    public static final AnimatedBlockEntry<SoapBarBlockEntity> SOAP_BAR = animatedEntry(I_SOAP_BAR);

    public static final AnimatedBlockEntry<SoapBoxBlockEntity> SOAP_BOX = animatedEntry(I_SOAP_BOX);

    public static final AnimatedBlockEntry<SoapRackBlockEntity> SOAP_RACK = animatedEntry(I_SOAP_RACK);

    public static final AnimatedBlockEntry<SoapPaperBagBlockEntity> SOAP_PAPER_BAG =
            animatedEntry(I_SOAP_PAPER_BAG);

    public static final AnimatedBlockEntry<BodyWashBlockEntity> BODY_WASH = animatedEntry(I_BODY_WASH);

    public static final AnimatedBlockEntry<ShampooBlockEntity> SHAMPOO = animatedEntry(I_SHAMPOO);

    public static final AnimatedBlockEntry<BodyCreamBlockEntity> BODY_CREAM = animatedEntry(I_BODY_CREAM);

    public static final AnimatedBlockEntry<SoapPaperBoxBlockEntity> SOAP_PAPER_BOX =
            animatedEntry(I_SOAP_PAPER_BOX);

    public static final AnimatedBlockEntry<SoapMoldBlockEntity> SOAP_MOLD = animatedEntry(I_SOAP_MOLD);

    public static final AnimatedBlockEntry<GreenSofaBlockEntity> GREEN_SOFA = animatedEntry(I_GREEN_SOFA);

    public static final AnimatedBlockEntry<CombinedOrnamentBlockEntity> COMBINED_ORNAMENT =
            animatedEntry(I_COMBINED_ORNAMENT);

    public static final AnimatedBlockEntry<SweeperDockBlockEntity> SWEEPER_DOCK = animatedEntry(I_SWEEPER_DOCK);

    public static final AnimatedBlockEntry<GeolibAlignmentProbeBlockEntity> GEOLIB_ALIGNMENT_PROBE =
            animatedEntry(I_GEOLIB_ALIGNMENT_PROBE);

    public static final AnimatedBlockEntry<BedPlateBaseBlockEntity> BED_PLATE6 = BedPlate6Registration.mainEntry();

    public static final AnimatedBlockEntry<PlainGlassWindowBlockEntity> PLAIN_GLASS_WINDOW =
            PlainGlassWindowRegistration.entry();
}
