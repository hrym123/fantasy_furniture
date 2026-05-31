package org.lanye.fantasy_furniture.content.soap.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldContents;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldIngredientSlot;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldIngredients;

/**
 * 盆内展示快照：各原料独立显示，互不影响。
 *
 * <ul>
 *   <li>水桶 → 仅客户端水面对（{@link #showWater()}）
 *   <li>沐浴液 / 洗发液 / 蜜脾 / 染料 → 各自锚点 Item
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public record SoapMoldDisplaySnapshot(SoapMoldContents contents, List<DisplayItem> basinItems, boolean showWater) {

    public record DisplayItem(String anchorBone, ItemStack stack) {}

    public static SoapMoldDisplaySnapshot from(SoapMoldBlockEntity be) {
        if (be == null) {
            return new SoapMoldDisplaySnapshot(SoapMoldContents.empty(), List.of(), false);
        }
        SoapMoldContents contents = be.contents();
        List<DisplayItem> items = new ArrayList<>();
        if (contents.hasLiquid()) {
            items.add(
                    new DisplayItem(
                            "ingredient_liquid",
                            SoapMoldIngredients.liquidStack(contents.liquidKind(), contents.liquidMatId())));
        }
        if (contents.hasHoneycomb()) {
            items.add(
                    new DisplayItem(
                            "ingredient_honey",
                            SoapMoldIngredients.stackForSlot(contents, SoapMoldIngredientSlot.HONEY)));
        }
        if (contents.hasPigment()) {
            items.add(
                    new DisplayItem(
                            "ingredient_pigment",
                            SoapMoldIngredients.stackForSlot(contents, SoapMoldIngredientSlot.PIGMENT)));
        }
        return new SoapMoldDisplaySnapshot(contents, List.copyOf(items), contents.hasWater());
    }
}
