package org.lanye.fantasy_furniture.content.soap.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxMaterials;
import org.lanye.fantasy_furniture.content.soap.client.DisplayCabinetItemRenderer;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 陈列柜物品：单 id，包装盒色存 NBT。 */
public final class DisplayCabinetBlockItem extends GeolibBlockItem {

    public DisplayCabinetBlockItem(Block block, Item.Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public Component getName(ItemStack stack) {
        DisplayCabinetAppearance appearance = DisplayCabinetAppearance.fromStack(stack);
        if (appearance.materialId() == SoapPaperBoxMaterials.DEFAULT && stack.getTag() == null) {
            return super.getName(stack);
        }
        return Component.translatable(
                "item.fantasy_furniture.display_cabinet.named",
                Component.translatable(SoapPaperBoxMaterials.colorTranslationKey(appearance.materialId())));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new DisplayCabinetItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    public static ItemStack stackWithMaterial(Item item, int materialId) {
        ItemStack stack = new ItemStack(item);
        DisplayCabinetAppearance.writeToStack(stack, new DisplayCabinetAppearance(materialId));
        return stack;
    }
}
