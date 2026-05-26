package org.lanye.fantasy_furniture.content.soap.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagMaterials;
import org.lanye.fantasy_furniture.content.soap.client.SoapPaperBagItemRenderer;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 包装袋物品：单 id，袋色存 NBT。 */
public final class SoapPaperBagBlockItem extends GeolibBlockItem {

    public SoapPaperBagBlockItem(Block block, Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public Component getName(ItemStack stack) {
        SoapPaperBagAppearance appearance = SoapPaperBagAppearance.fromStack(stack);
        if (appearance.bagMaterialId() == SoapPaperBagMaterials.DEFAULT && stack.getTag() == null) {
            return super.getName(stack);
        }
        if (appearance.bagMaterialId() == SoapPaperBagMaterials.RAINBOW) {
            return Component.translatable("item.fantasy_furniture.soap_paper_bag.rainbow");
        }
        return Component.translatable(
                "item.fantasy_furniture.soap_paper_bag.named",
                Component.translatable(SoapPaperBagMaterials.colorTranslationKey(appearance.bagMaterialId())));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new SoapPaperBagItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    public static ItemStack stackWithBagMaterial(Item item, int bagMaterialId) {
        ItemStack stack = new ItemStack(item);
        SoapPaperBagAppearance.writeToStack(stack, new SoapPaperBagAppearance(bagMaterialId));
        return stack;
    }
}
