package org.lanye.fantasy_furniture.content.soap;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBottleBlockEntity;
import org.lanye.reverie_core.composite.CompositePartId;
import org.lanye.reverie_core.composite.PartHitPicker;

/**
 * 瓶罐摞准心/中键/玉选取：按命中分件解析物品栈（只读，不改 BE），供
 * {@code getCloneItemStack} 与 Jade {@code usePickedResult} 使用。
 */
public final class SoapBottleComponentPick {

    private SoapBottleComponentPick() {}

    public static ItemStack stackForHit(
            BlockGetter level, BlockState state, BlockPos pos, Vec3 hitLocation) {
        BlockEntity raw = level.getBlockEntity(pos);
        if (!(raw instanceof SoapBottleBlockEntity be)) {
            return ItemStack.EMPTY;
        }
        SoapBottleStackData data = be.stackData();
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        CompositePartId part =
                PartHitPicker.pick(
                        hitLocation, pos, facing, SoapBottlePartPicks.entries(data), true);
        if (part == null) {
            return peekDisplayOrFirst(data);
        }
        ItemStack stack = peekPart(data, part);
        return stack.isEmpty() ? peekDisplayOrFirst(data) : stack;
    }

    /** 只读取出分件对应物品；不 pop。 */
    public static ItemStack peekPart(SoapBottleStackData data, @Nullable CompositePartId part) {
        if (part == null) {
            return ItemStack.EMPTY;
        }
        if (SoapBottleParts.isCarrier(part)) {
            SoapStackCarrierKind carrier = data.carrier();
            if (carrier == null) {
                return ItemStack.EMPTY;
            }
            return carrier.toItemStack(data.carrierBoxMaterialId());
        }
        int idx = SoapBottleParts.bottleIndex(part);
        if (idx < 0) {
            return ItemStack.EMPTY;
        }
        SoapBottleLayer layer = data.slotAt(idx);
        if (layer == null) {
            return ItemStack.EMPTY;
        }
        return SoapBottleKind.stackWithMaterial(layer.kind(), layer.materialId());
    }

    public static ItemStack peekDisplayOrFirst(SoapBottleStackData data) {
        int first = data.firstOccupiedSlot();
        if (first >= 0) {
            SoapBottleLayer layer = data.slotAt(first);
            if (layer != null) {
                return SoapBottleKind.stackWithMaterial(layer.kind(), layer.materialId());
            }
        }
        if (data.hasCarrier()) {
            return data.carrier().toItemStack(data.carrierBoxMaterialId());
        }
        return SoapBottleKind.stackWithMaterial(data.hostKind(), data.displayMaterial());
    }
}
