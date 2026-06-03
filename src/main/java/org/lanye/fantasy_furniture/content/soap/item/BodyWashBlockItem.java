package org.lanye.fantasy_furniture.content.soap.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.soap.BodyWashAppearance;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetBottleInsert;
import org.lanye.fantasy_furniture.content.soap.BodyWashMaterials;
import org.lanye.fantasy_furniture.content.soap.client.BodyWashItemRenderer;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 沐浴露物品：单 id，颜料存 NBT。 */
public final class BodyWashBlockItem extends GeolibBlockItem {

    public BodyWashBlockItem(Block block, Item.Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult cabinet = DisplayCabinetBottleInsert.useOnOpenCabinet(context);
        if (cabinet != null) {
            return cabinet;
        }
        return super.useOn(context);
    }

    @Override
    public Component getName(ItemStack stack) {
        BodyWashAppearance appearance = BodyWashAppearance.fromStack(stack);
        if (appearance.materialId() == BodyWashMaterials.DEFAULT && stack.getTag() == null) {
            return super.getName(stack);
        }
        return Component.translatable(
                "item.fantasy_furniture.body_wash.named",
                Component.translatable(BodyWashMaterials.colorTranslationKey(appearance.materialId())));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new BodyWashItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    public static ItemStack stackWithMaterial(Item item, int materialId) {
        ItemStack stack = new ItemStack(item);
        BodyWashAppearance.writeToStack(stack, new BodyWashAppearance(materialId));
        return stack;
    }
}
