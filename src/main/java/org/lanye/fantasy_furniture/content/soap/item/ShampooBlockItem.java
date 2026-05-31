package org.lanye.fantasy_furniture.content.soap.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.soap.ShampooAppearance;
import org.lanye.fantasy_furniture.content.soap.ShampooMaterials;
import org.lanye.fantasy_furniture.content.soap.client.ShampooItemRenderer;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 洗发露物品：单 id，颜料存 NBT。 */
public final class ShampooBlockItem extends GeolibBlockItem {

    public ShampooBlockItem(Block block, Item.Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public Component getName(ItemStack stack) {
        ShampooAppearance appearance = ShampooAppearance.fromStack(stack);
        if (appearance.materialId() == ShampooMaterials.DEFAULT && stack.getTag() == null) {
            return super.getName(stack);
        }
        return Component.translatable(
                "item.fantasy_furniture.shampoo.named",
                Component.translatable(ShampooMaterials.colorTranslationKey(appearance.materialId())));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new ShampooItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    public static ItemStack stackWithMaterial(Item item, int materialId) {
        ItemStack stack = new ItemStack(item);
        ShampooAppearance.writeToStack(stack, new ShampooAppearance(materialId));
        return stack;
    }
}
