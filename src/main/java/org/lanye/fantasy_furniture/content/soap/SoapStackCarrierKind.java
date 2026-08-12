package org.lanye.fantasy_furniture.content.soap;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.SoapBoxBlockItem;

/**
 * 瓶罐摞特殊场合 2/3 的载体（第 4 位架或盒，二选一）。
 *
 * <p>正式模型应对齐 moonstarfish 组合目录导出；当前渲染可先叠现有 {@code soap_rack} / {@code soap_box} geo。
 */
public enum SoapStackCarrierKind {
    RACK,
    BOX;

    @Nullable
    public static SoapStackCarrierKind fromItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.is(ModBlocks.SOAP_RACK.item().get())) {
            return RACK;
        }
        if (stack.is(ModBlocks.SOAP_BOX.item().get())) {
            return BOX;
        }
        return null;
    }

    /**
     * @param boxMaterialId 仅 {@link #BOX} 使用；架忽略。中间态可用默认色。
     */
    public ItemStack toItemStack(int boxMaterialId) {
        return switch (this) {
            case RACK -> new ItemStack(ModBlocks.SOAP_RACK.item().get());
            case BOX ->
                    SoapBoxBlockItem.stackWithBoxMaterial(
                            ModBlocks.SOAP_BOX.item().get(), boxMaterialId);
        };
    }
}
