package org.lanye.fantasy_furniture.bootstrap.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.blockentity.ModBlockEntities;
import org.lanye.fantasy_furniture.bootstrap.item.ModItems;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6MediumPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6PillowPalette;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6SmallPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.renderer.BedPlate6GeoBlockRenderer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DisassemblyGloveItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;
import org.lanye.reverie_core.geolib.AnimatedBlockEntry;
import org.lanye.reverie_core.geolib.AnimatedBlockRegistration;
import org.lanye.reverie_core.geolib.GeolibFurnitureBundle;
import org.lanye.reverie_core.geolib.GeolibFurnitureRegistration;
import org.lanye.reverie_core.geolib.GeolibItemAssets;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlockItem;
import org.lanye.reverie_core.geolib.client.AnimatedBlockClientRegistration;

/**
 * 床板 6 完整注册链：主方块 + 组件物品 + 创造栏片段 + 客户端 BER（参考 {@link GeolibFurnitureBundle}）。
 */
public final class BedPlate6Registration {

    private static final Item.Properties COMPONENT_PROPS = new Item.Properties().stacksTo(16);

    private static final AnimatedBlockEntry<BedPlateBaseBlockEntity> MAIN = registerMainBlock();

    private static final List<RegistryObject<Item>> DUVET_ITEMS = registerDuvets();
    private static final List<RegistryObject<Item>> DUVET_COVER_ITEMS = registerDuvetCovers();
    private static final List<RegistryObject<Item>> PILLOW_LARGE_ITEMS = registerLargePillows();
    private static final List<RegistryObject<Item>> PILLOW_MEDIUM_ITEMS = registerMediumPillows();
    private static final List<RegistryObject<Item>> PILLOW_SMALL_ITEMS = registerSmallPillows();

    private static final RegistryObject<Item> DISASSEMBLY_GLOVE = registerDisassemblyGlove();

    private static final GeolibFurnitureBundle<BedPlateBaseBlockEntity> BUNDLE = buildBundle();

    private BedPlate6Registration() {}

    private static AnimatedBlockEntry<BedPlateBaseBlockEntity> registerMainBlock() {
        return AnimatedBlockRegistration.registerSpec(
                ModBlocks.BLOCKS,
                ModBlocks.BLOCK_ITEMS,
                ModBlockEntities.BLOCK_ENTITY_TYPES,
                AnimatedBlockRegistration.spec(
                        "bed_plate6",
                        FurnitureBlockProperties::cherryWoodFurnitureNoOcclusion,
                        p -> new BedPlate6Block(p, BedPlate6BlockEntity::new),
                        BedPlate6BlockEntity::new,
                        (Block block, Item.Properties itemProps) ->
                                new BedPlateBlockItem(
                                        block,
                                        itemProps,
                                        GeolibItemAssets.blockAsset(FantasyFurniture.MODID, "bed_plate6"))));
    }

    private static GeolibFurnitureBundle<BedPlateBaseBlockEntity> buildBundle() {
        List<RegistryObject<Item>> components = new ArrayList<>();
        components.add(DISASSEMBLY_GLOVE);
        components.addAll(DUVET_ITEMS);
        components.addAll(DUVET_COVER_ITEMS);
        components.addAll(PILLOW_LARGE_ITEMS);
        components.addAll(PILLOW_MEDIUM_ITEMS);
        components.addAll(PILLOW_SMALL_ITEMS);
        return GeolibFurnitureBundle.ofCustomRenderer(
                FantasyFurniture.MODID,
                "bed_plate6",
                MAIN,
                components,
                ctx -> new BedPlate6GeoBlockRenderer());
    }

    private static RegistryObject<Item> registerDisassemblyGlove() {
        return ModItems.ITEMS.register(
                "bed_plate6_disassembly_glove",
                () -> new BedPlate6DisassemblyGloveItem(new Item.Properties().stacksTo(1)));
    }

    private static List<RegistryObject<Item>> registerDuvets() {
        return GeolibFurnitureRegistration.registerIndexedComponents(
                ModItems.ITEMS,
                "bed_plate6_duvet_",
                1,
                BedPlate6DuvetMaterials.COUNT,
                COMPONENT_PROPS,
                BedPlate6DuvetItem::new);
    }

    private static List<RegistryObject<Item>> registerDuvetCovers() {
        return GeolibFurnitureRegistration.registerIndexedComponents(
                ModItems.ITEMS,
                "bed_plate6_duvet_cover_",
                1,
                BedPlate6DuvetCoverMaterials.COUNT,
                COMPONENT_PROPS,
                BedPlate6DuvetCoverItem::new);
    }

    private static List<RegistryObject<Item>> registerLargePillows() {
        List<RegistryObject<Item>> list = new ArrayList<>();
        for (int s = 1; s <= BedPlate6LargePillowStyles.COUNT; s++) {
            for (int m = 1; m <= BedPlate6DuvetMaterials.COUNT; m++) {
                if (BedPlate6LargePillowItem.isUnavailableLargeVariant(s, m)) {
                    continue;
                }
                final int style = s;
                final int mat = m;
                String regId =
                        "bed_plate6_pillow_large_"
                                + BedPlate6LargePillowStyles.resourceSlug(s)
                                + "_"
                                + BedPlate6PillowPalette.colorSlug(m);
                list.add(
                        ModItems.ITEMS.register(
                                regId, () -> new BedPlate6LargePillowItem(COMPONENT_PROPS, style, mat)));
            }
        }
        return List.copyOf(list);
    }

    private static List<RegistryObject<Item>> registerMediumPillows() {
        return GeolibFurnitureRegistration.registerIndexedComponents(
                ModItems.ITEMS,
                "bed_plate6_pillow_medium_",
                1,
                BedPlate6MediumPillowMaterials.COUNT,
                COMPONENT_PROPS,
                BedPlate6MediumPillowItem::new);
    }

    private static List<RegistryObject<Item>> registerSmallPillows() {
        return GeolibFurnitureRegistration.registerIndexedComponents(
                ModItems.ITEMS,
                "bed_plate6_pillow_small_",
                1,
                BedPlate6SmallPillowMaterials.COUNT,
                COMPONENT_PROPS,
                BedPlate6SmallPillowItem::new);
    }

    public static AnimatedBlockEntry<BedPlateBaseBlockEntity> mainEntry() {
        return MAIN;
    }

    public static GeolibFurnitureBundle<BedPlateBaseBlockEntity> bundle() {
        return BUNDLE;
    }

    public static List<RegistryObject<Item>> duvetItems() {
        return DUVET_ITEMS;
    }

    public static List<RegistryObject<Item>> duvetCoverItems() {
        return DUVET_COVER_ITEMS;
    }

    public static List<RegistryObject<Item>> pillowLargeItems() {
        return PILLOW_LARGE_ITEMS;
    }

    public static List<RegistryObject<Item>> pillowMediumItems() {
        return PILLOW_MEDIUM_ITEMS;
    }

    public static List<RegistryObject<Item>> pillowSmallItems() {
        return PILLOW_SMALL_ITEMS;
    }

    public static RegistryObject<Item> disassemblyGlove() {
        return DISASSEMBLY_GLOVE;
    }

    /** 与 {@link org.lanye.fantasy_furniture.bootstrap.item.ModCreativeTabs} 床板 6 段顺序一致。 */
    public static List<Supplier<? extends ItemLike>> creativeTabSegment() {
        return BUNDLE.creativeTabItems();
    }

    public static void registerClientRenderer() {
        AnimatedBlockClientRegistration.registerBlockEntityRenderer(
                BUNDLE.main(), ctx -> new BedPlate6GeoBlockRenderer());
    }
}
