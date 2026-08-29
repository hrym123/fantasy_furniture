package org.lanye.fantasy_furniture.content.furniture.decor.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.furniture.decor.ComputerAppearance;
import org.lanye.fantasy_furniture.content.furniture.decor.ComputerMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.block.ComputerBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.client.ComputerItemRenderer;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 电脑物品：单 id，色存 NBT；展示名「电脑N型（色名）」。 */
public final class ComputerBlockItem extends GeolibBlockItem {

    public ComputerBlockItem(Block block, Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    public String closedAssetId() {
        Block block = getBlock();
        if (block instanceof ComputerBlock computer) {
            return computer.closedAssetId();
        }
        return "computer_1";
    }

    @Override
    public Component getName(ItemStack stack) {
        ComputerAppearance appearance = ComputerAppearance.fromStack(stack);
        return Component.translatable(
                getDescriptionId() + ".named",
                Component.translatable(ComputerMaterials.colorTranslationKey(appearance.materialId())));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new ComputerItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    public static ItemStack stackWithMaterial(Item item, int materialId) {
        ItemStack stack = new ItemStack(item);
        ComputerAppearance.writeToStack(stack, new ComputerAppearance(materialId));
        return stack;
    }
}
