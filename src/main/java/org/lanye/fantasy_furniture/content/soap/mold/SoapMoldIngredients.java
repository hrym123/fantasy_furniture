package org.lanye.fantasy_furniture.content.soap.mold;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.lanye.fantasy_furniture.bootstrap.item.ModItems;
import org.lanye.fantasy_furniture.bootstrap.tag.ModTags;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapFlatLiquidAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapFlatLiquidMaterials;

/** 模具装料判定、染料映射、容器 Tag。 */
public final class SoapMoldIngredients {

    public record Match(SoapMoldIngredientSlot slot, int pigmentMatId, SoapMoldLiquidKind liquidKind, int liquidMatId) {}

    private SoapMoldIngredients() {}

    public static boolean isLiquidVessel(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.SOAP_MOLD_LIQUID_VESSELS);
    }

    public static Optional<Match> matchInsert(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        Item item = stack.getItem();
        if (item == ModItems.BODY_WASH_LIQUID.get()) {
            int mat = readLiquidMat(stack, "body_wash_liquid");
            if (mat == 0) {
                return Optional.empty();
            }
            return Optional.of(new Match(SoapMoldIngredientSlot.LIQUID, 0, SoapMoldLiquidKind.BODY_WASH, mat));
        }
        if (item == ModItems.SHAMPOO_LIQUID.get()) {
            int mat = readLiquidMat(stack, "shampoo_liquid");
            if (mat == 0) {
                return Optional.empty();
            }
            return Optional.of(new Match(SoapMoldIngredientSlot.LIQUID, 0, SoapMoldLiquidKind.SHAMPOO, mat));
        }
        if (item == Items.HONEYCOMB) {
            return Optional.of(new Match(SoapMoldIngredientSlot.HONEY, 0, SoapMoldLiquidKind.NONE, 0));
        }
        if (item == Items.WATER_BUCKET) {
            return Optional.of(new Match(SoapMoldIngredientSlot.WATER, 0, SoapMoldLiquidKind.NONE, 0));
        }
        OptionalInt pigment = pigmentMatForDye(item);
        if (pigment.isPresent()) {
            return Optional.of(
                    new Match(SoapMoldIngredientSlot.PIGMENT, pigment.getAsInt(), SoapMoldLiquidKind.NONE, 0));
        }
        return Optional.empty();
    }

    private static int readLiquidMat(ItemStack stack, String stem) {
        int mat = SoapFlatLiquidAppearance.fromStack(stack, stem).materialId();
        return SoapFlatLiquidMaterials.isValid(mat) ? mat : 0;
    }

    private static OptionalInt pigmentMatForDye(Item item) {
        if (item == Items.BLUE_DYE) {
            return OptionalInt.of(1);
        }
        if (item == Items.GREEN_DYE) {
            return OptionalInt.of(2);
        }
        if (item == Items.PURPLE_DYE) {
            return OptionalInt.of(3);
        }
        if (item == Items.PINK_DYE) {
            return OptionalInt.of(4);
        }
        if (item == Items.YELLOW_DYE) {
            return OptionalInt.of(5);
        }
        if (item == Items.RED_DYE) {
            return OptionalInt.of(6);
        }
        return OptionalInt.empty();
    }

    public static ItemStack stackForSlot(SoapMoldContents contents, SoapMoldIngredientSlot slot) {
        return switch (slot) {
            case LIQUID -> liquidStack(contents.liquidKind(), contents.liquidMatId());
            case HONEY -> new ItemStack(Items.HONEYCOMB);
            case WATER -> new ItemStack(Items.WATER_BUCKET);
            case PIGMENT -> dyeStackForPigment(contents.pigmentMatId());
        };
    }

    public static ItemStack liquidStack(SoapMoldLiquidKind kind, int liquidMatId) {
        if (kind == SoapMoldLiquidKind.BODY_WASH) {
            return org.lanye.fantasy_furniture.content.soap.item.SoapFlatLiquidItem.stackWithMaterial(
                    ModItems.BODY_WASH_LIQUID.get(), "body_wash_liquid", liquidMatId);
        }
        if (kind == SoapMoldLiquidKind.SHAMPOO) {
            return org.lanye.fantasy_furniture.content.soap.item.SoapFlatLiquidItem.stackWithMaterial(
                    ModItems.SHAMPOO_LIQUID.get(), "shampoo_liquid", liquidMatId);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack dyeStackForPigment(int pigmentMatId) {
        Item dye =
                switch (pigmentMatId) {
                    case 1 -> Items.BLUE_DYE;
                    case 2 -> Items.GREEN_DYE;
                    case 3 -> Items.PURPLE_DYE;
                    case 4 -> Items.PINK_DYE;
                    case 5 -> Items.YELLOW_DYE;
                    case 6 -> Items.RED_DYE;
                    default -> null;
                };
        return dye == null ? ItemStack.EMPTY : new ItemStack(dye);
    }

    /** 制皂产出：颜料 → 皂体 {@link SoapBarMaterials}；液体 → 粒子色 id。 */
    public static boolean isValidPigmentMat(int mat) {
        return SoapBarMaterials.isValid(mat);
    }
}
