package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.BodyCreamBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.BodyWashBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.ShampooBlockItem;

/** 肥皂套系可摞放瓶罐种类（沐浴露 / 洗发露 / 乳霜）。 */
public enum SoapBottleKind {
    BODY_WASH,
    SHAMPOO,
    BODY_CREAM;

    public static final int MIXED_MAX_STACK = 4;

    public Item item() {
        return switch (this) {
            case BODY_WASH -> ModBlocks.BODY_WASH.item().get();
            case SHAMPOO -> ModBlocks.SHAMPOO.item().get();
            case BODY_CREAM -> ModBlocks.BODY_CREAM.item().get();
        };
    }

    public static boolean isSoapBottleItem(ItemStack stack) {
        return fromItem(stack) != null;
    }

    public static SoapBottleKind fromItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        Item item = stack.getItem();
        if (item == ModBlocks.BODY_WASH.item().get()) {
            return BODY_WASH;
        }
        if (item == ModBlocks.SHAMPOO.item().get()) {
            return SHAMPOO;
        }
        if (item == ModBlocks.BODY_CREAM.item().get()) {
            return BODY_CREAM;
        }
        return null;
    }

    public static ItemStack stackWithMaterial(SoapBottleKind kind, int materialId) {
        return switch (kind) {
            case BODY_WASH -> BodyWashBlockItem.stackWithMaterial(kind.item(), materialId);
            case SHAMPOO -> ShampooBlockItem.stackWithMaterial(kind.item(), materialId);
            case BODY_CREAM -> BodyCreamBlockItem.stackWithMaterial(kind.item(), materialId);
        };
    }

    public int materialFromStack(ItemStack stack) {
        return switch (this) {
            case BODY_WASH -> BodyWashAppearance.fromStack(stack).materialId();
            case SHAMPOO -> ShampooAppearance.fromStack(stack).materialId();
            case BODY_CREAM -> BodyCreamAppearance.fromStack(stack).materialId();
        };
    }

    public boolean isValidMaterial(int materialId) {
        return switch (this) {
            case BODY_WASH -> BodyWashMaterials.isValid(materialId);
            case SHAMPOO -> ShampooMaterials.isValid(materialId);
            case BODY_CREAM -> BodyCreamMaterials.isValid(materialId);
        };
    }

    public int defaultMaterial() {
        return switch (this) {
            case BODY_WASH -> BodyWashMaterials.DEFAULT;
            case SHAMPOO -> ShampooMaterials.DEFAULT;
            case BODY_CREAM -> BodyCreamMaterials.DEFAULT;
        };
    }

    /** 堆叠 geo 中陈列位 {@code slot}（1…4 或乳霜纯摞 5）对应骨骼名。 */
    public String stackBoneName(int slot) {
        return SoapBottleStackSlots.boneForSlot(this, slot);
    }
}
