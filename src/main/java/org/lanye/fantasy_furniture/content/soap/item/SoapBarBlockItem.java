package org.lanye.fantasy_furniture.content.soap.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapBarWear;
import org.lanye.fantasy_furniture.content.soap.client.SoapBarItemRenderer;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/**
 * 肥皂物品：单 id，磨损与颜料存于 NBT（{@link SoapBarAppearance}）；物品栏 2D 物品材质，手持 Geo。
 */
public final class SoapBarBlockItem extends GeolibBlockItem {

    public SoapBarBlockItem(Block block, Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    @Override
    public Component getName(ItemStack stack) {
        SoapBarAppearance appearance = SoapBarAppearance.fromStack(stack);
        if (appearance.isPackaged()) {
            return Component.translatable(
                    "item.fantasy_furniture.soap_bar.packaged",
                    Component.translatable(SoapBarMaterials.colorTranslationKey(appearance.bagMaterialId())));
        }
        if (appearance.wear() == SoapBarAppearance.DEFAULT_WEAR) {
            return Component.translatable(
                    "item.fantasy_furniture.soap_bar.named_full",
                    Component.translatable(SoapBarMaterials.colorTranslationKey(appearance.materialId())));
        }
        return Component.translatable(
                "item.fantasy_furniture.soap_bar.named_worn",
                Component.translatable(SoapBarMaterials.colorTranslationKey(appearance.materialId())),
                Component.translatable(SoapBarWear.wearTranslationKey(appearance.wear())));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new SoapBarItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    /** 创造栏 / 指令用：指定颜料与磨损的堆叠。 */
    public static ItemStack stackWithAppearance(Item item, SoapBarAppearance appearance) {
        ItemStack stack = new ItemStack(item);
        appearance.writeToStack(stack, appearance);
        return stack;
    }
}
