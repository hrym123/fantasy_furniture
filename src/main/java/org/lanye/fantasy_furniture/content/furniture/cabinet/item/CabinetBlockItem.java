package org.lanye.fantasy_furniture.content.furniture.cabinet.item;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetAppearance;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetMaterials;
import org.lanye.fantasy_furniture.content.furniture.cabinet.block.CabinetBlock;
import org.lanye.fantasy_furniture.content.furniture.cabinet.client.CabinetItemRenderer;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/** 柜子物品：单 id，色存 NBT；展示名「柜子N型（色名）」。 */
public final class CabinetBlockItem extends GeolibBlockItem {

    public CabinetBlockItem(Block block, Properties properties, GeolibItemAssets assets) {
        super(block, properties, assets);
    }

    public CabinetKind kind() {
        Block block = getBlock();
        if (block instanceof CabinetBlock cabinet) {
            return cabinet.kind();
        }
        return CabinetKind.CABINET_1;
    }

    @Override
    public Component getName(ItemStack stack) {
        CabinetKind kind = kind();
        CabinetAppearance appearance = CabinetAppearance.fromStack(stack, kind);
        return Component.translatable(
                getDescriptionId() + ".named",
                Component.translatable(
                        CabinetMaterials.colorTranslationKey(kind, appearance.materialId())));
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new CabinetItemRenderer();
                        }
                        return renderer;
                    }
                });
    }

    public static ItemStack stackWithMaterial(Item item, int materialId) {
        ItemStack stack = new ItemStack(item);
        CabinetKind kind = CabinetKind.CABINET_1;
        if (item instanceof CabinetBlockItem cabinetItem) {
            kind = cabinetItem.kind();
        }
        CabinetAppearance.writeToStack(
                stack, new CabinetAppearance(CabinetMaterials.clamp(kind, materialId)));
        return stack;
    }
}
