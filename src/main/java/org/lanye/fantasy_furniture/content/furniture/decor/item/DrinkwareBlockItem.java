package org.lanye.fantasy_furniture.content.furniture.decor.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareAppearance;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.client.DrinkwareItemRenderer;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 杯具物品：单 id，色存 NBT；展示名「杯具（色名）」。 */
public final class DrinkwareBlockItem extends GeolibBlockItem {

    public DrinkwareBlockItem(Block block, Item.Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public Component getName(ItemStack stack) {
        DrinkwareAppearance appearance = DrinkwareAppearance.fromStack(stack);
        return Component.translatable(
                getDescriptionId() + ".named",
                Component.translatable(DrinkwareMaterials.colorTranslationKey(appearance.materialId())));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new DrinkwareItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    public static ItemStack stackWithMaterial(Item item, int materialId) {
        ItemStack stack = new ItemStack(item);
        DrinkwareAppearance.writeToStack(stack, new DrinkwareAppearance(materialId));
        return stack;
    }
}
