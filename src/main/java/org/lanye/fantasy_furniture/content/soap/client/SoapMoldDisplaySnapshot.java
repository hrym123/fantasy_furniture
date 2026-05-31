package org.lanye.fantasy_furniture.content.soap.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldContents;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldIngredientSlot;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldIngredients;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldPhase;

/**
 * 盆内展示快照。
 *
 * <ul>
 *   <li>装料：水桶 → 水面；原料 → 各自锚点 Item
 *   <li>凝固中：Geo 凝固动画 + 水面（原料 Item 隐藏）
 *   <li>可取出：{@code soap_product} 锚点渲染成品皂 Geo
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public record SoapMoldDisplaySnapshot(
        SoapMoldContents contents,
        List<DisplayItem> basinItems,
        boolean showWater,
        SoapBarAppearance finishedSoap) {

    public record DisplayItem(String anchorBone, ItemStack stack) {}

    public static SoapMoldDisplaySnapshot from(SoapMoldBlockEntity be) {
        if (be == null) {
            return empty();
        }
        SoapMoldContents contents = be.contents();
        SoapMoldPhase phase = contents.phase();

        if (phase == SoapMoldPhase.READY) {
            return new SoapMoldDisplaySnapshot(
                    contents, List.of(), false, be.pendingSoapAppearance());
        }
        if (phase == SoapMoldPhase.CURING) {
            return new SoapMoldDisplaySnapshot(contents, List.of(), contents.hasWater(), null);
        }

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
        return new SoapMoldDisplaySnapshot(
                contents, List.copyOf(items), contents.hasWater(), null);
    }

    private static SoapMoldDisplaySnapshot empty() {
        return new SoapMoldDisplaySnapshot(SoapMoldContents.empty(), List.of(), false, null);
    }
}
