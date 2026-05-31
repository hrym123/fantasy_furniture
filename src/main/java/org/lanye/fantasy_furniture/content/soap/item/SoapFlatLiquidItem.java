package org.lanye.fantasy_furniture.content.soap.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapFlatLiquidAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapFlatLiquidMaterials;

/** 单材质图液体原料：单 id + NBT 六色；不可放置。 */
public final class SoapFlatLiquidItem extends Item {

    private final String textureStem;
    private final String namedTranslationKey;

    public SoapFlatLiquidItem(Item.Properties properties, String textureStem, String namedTranslationKey) {
        super(properties);
        this.textureStem = textureStem;
        this.namedTranslationKey = namedTranslationKey;
    }

    public String textureStem() {
        return textureStem;
    }

    @Override
    public Component getName(ItemStack stack) {
        SoapFlatLiquidAppearance appearance = SoapFlatLiquidAppearance.fromStack(stack, textureStem);
        if (appearance.materialId() == SoapBarAppearance.DEFAULT_MATERIAL && stack.getTag() == null) {
            return super.getName(stack);
        }
        return Component.translatable(
                namedTranslationKey,
                Component.translatable(SoapFlatLiquidMaterials.colorTranslationKey(appearance.materialId())));
    }

    public static ItemStack stackWithMaterial(Item item, String textureStem, int materialId) {
        ItemStack stack = new ItemStack(item);
        SoapFlatLiquidAppearance.writeToStack(
                stack, new SoapFlatLiquidAppearance(materialId, textureStem));
        return stack;
    }
}
