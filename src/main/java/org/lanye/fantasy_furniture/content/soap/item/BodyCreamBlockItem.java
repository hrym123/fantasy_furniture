package org.lanye.fantasy_furniture.content.soap.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.soap.BodyCreamAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyCreamMaterials;
import org.lanye.fantasy_furniture.content.soap.client.BodyCreamItemRenderer;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 乳霜物品：单 id，颜料存 NBT。 */
public final class BodyCreamBlockItem extends GeolibBlockItem {

    public BodyCreamBlockItem(Block block, Item.Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public Component getName(ItemStack stack) {
        BodyCreamAppearance appearance = BodyCreamAppearance.fromStack(stack);
        if (appearance.materialId() == BodyCreamMaterials.DEFAULT && stack.getTag() == null) {
            return super.getName(stack);
        }
        return Component.translatable(
                "item.fantasy_furniture.body_cream.named",
                Component.translatable(BodyCreamMaterials.colorTranslationKey(appearance.materialId())));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new BodyCreamItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    public static ItemStack stackWithMaterial(Item item, int materialId) {
        ItemStack stack = new ItemStack(item);
        BodyCreamAppearance.writeToStack(stack, new BodyCreamAppearance(materialId));
        return stack;
    }
}
