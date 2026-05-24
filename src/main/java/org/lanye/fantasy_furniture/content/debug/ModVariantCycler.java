package org.lanye.fantasy_furniture.content.debug;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.furniture.common.state.BanquetteShape;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowShapes;
import org.lanye.fantasy_furniture.content.furniture.decor.block.CombinedOrnamentBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainGlassWindowBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.CombinedOrnamentBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.item.PlainGlassWindowBlockItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BanquetteBlock;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapBarWear;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapRackBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapBoxBlockItem;

/** 变体调试棒：循环本模组模型变体（磨损 geo、盖态、造型等），不切换材质/颜料。 */
public final class ModVariantCycler {

    private static final int SOAP_WEAR_MIN = 0;
    private static final int SOAP_WEAR_MAX = 2;

    private ModVariantCycler() {}

    public static Optional<Component> cycleBlock(Level level, BlockPos pos, boolean reverse) {
        BlockState state = level.getBlockState(pos);
        if (!isModBlock(state.getBlock())) {
            return Optional.empty();
        }
        Block block = state.getBlock();
        if (block instanceof SoapBarBlock) {
            return cycleSoapBarBlock(level, pos, state, reverse);
        }
        if (block instanceof SoapBoxBlock) {
            return cycleSoapBoxBlock(level, pos, state, reverse);
        }
        if (block instanceof SoapRackBlock) {
            return cycleSoapRackBlock(level, pos, state, reverse);
        }
        if (block instanceof CombinedOrnamentBlock) {
            return cycleCombinedOrnament(level, pos, reverse);
        }
        if (block instanceof PlainGlassWindowBlock) {
            return cyclePlainGlassWindow(level, pos, state, reverse);
        }
        if (block instanceof BanquetteBlock) {
            return cycleBanquette(level, pos, state, reverse);
        }
        return Optional.empty();
    }

    public static Optional<Component> cycleItemStack(ItemStack stack, boolean reverse) {
        if (stack.isEmpty() || !isModItem(stack.getItem())) {
            return Optional.empty();
        }
        Item item = stack.getItem();
        if (item instanceof SoapBarBlockItem) {
            SoapBarAppearance current = SoapBarAppearance.fromStack(stack);
            SoapBarAppearance next = advanceSoapWear(current, reverse);
            if (next.equals(current)) {
                return Optional.empty();
            }
            SoapBarAppearance.writeToStack(stack, next);
            return Optional.of(describeSoapWear(next));
        }
        if (item instanceof SoapBoxBlockItem) {
            return Optional.empty();
        }
        if (item instanceof PlainGlassWindowBlockItem) {
            int shape = readPlainGlassShape(stack);
            int nextShape = reverse ? prevPlainGlassShape(shape) : PlainGlassWindowShapes.nextShapeInCycle(shape);
            if (nextShape == shape) {
                return Optional.empty();
            }
            if (nextShape == 0) {
                if (stack.hasTag()) {
                    stack.getTag().remove(PlainGlassWindowBlockItem.TAG_SHAPE);
                    if (stack.getTag().isEmpty()) {
                        stack.setTag(null);
                    }
                }
            } else {
                stack.getOrCreateTag().putInt(PlainGlassWindowBlockItem.TAG_SHAPE, nextShape);
            }
            return Optional.of(
                    Component.translatable(
                            "debug.fantasy_furniture.variant.plain_glass_shape",
                            PlainGlassWindowShapes.geoBasename(nextShape)));
        }
        return Optional.empty();
    }

    private static Optional<Component> cycleSoapBarBlock(
            Level level, BlockPos pos, BlockState state, boolean reverse) {
        int wear = state.getValue(SoapBarBlock.WEAR);
        int mat = state.getValue(SoapBarBlock.MATERIAL);
        SoapBarAppearance next = advanceSoapWear(new SoapBarAppearance(wear, mat), reverse);
        if (next.wear() == wear) {
            return Optional.empty();
        }
        level.setBlock(pos, state.setValue(SoapBarBlock.WEAR, next.wear()), Block.UPDATE_ALL);
        return Optional.of(describeSoapWear(next));
    }

    private static Optional<Component> cycleSoapBoxBlock(
            Level level, BlockPos pos, BlockState state, boolean reverse) {
        int mat = state.getValue(SoapBoxBlock.MATERIAL);
        boolean open = state.getValue(SoapBoxBlock.OPEN);
        boolean hasSoap = state.getValue(SoapBoxBlock.HAS_SOAP);
        SoapBoxBlockEntity boxBe = requireSoapBox(level, pos);
        SoapBarAppearance soap =
                boxBe != null ? boxBe.containedSoap() : SoapBarAppearance.defaults();

        if (reverse) {
            if (hasSoap && boxBe != null) {
                if (soap.wear() > SOAP_WEAR_MIN) {
                    SoapBarAppearance prev = withWear(soap, soap.wear() - 1);
                    boxBe.setContainedSoap(prev);
                    soap = prev;
                } else {
                    boxBe.clearContainedSoap();
                    hasSoap = false;
                }
            } else if (open) {
                open = false;
            } else {
                return Optional.empty();
            }
        } else if (!hasSoap) {
            if (!open) {
                open = true;
            } else if (boxBe != null) {
                SoapBarAppearance inserted = debugPlaceholderSoap(mat);
                boxBe.setContainedSoap(inserted);
                hasSoap = true;
                open = false;
                soap = inserted;
            } else {
                return Optional.empty();
            }
        } else if (boxBe != null) {
            if (soap.wear() < SOAP_WEAR_MAX) {
                SoapBarAppearance next = withWear(soap, soap.wear() + 1);
                boxBe.setContainedSoap(next);
                soap = next;
            } else {
                boxBe.clearContainedSoap();
                hasSoap = false;
                open = true;
            }
        }

        level.setBlock(
                pos,
                state.setValue(SoapBoxBlock.OPEN, open).setValue(SoapBoxBlock.HAS_SOAP, hasSoap),
                Block.UPDATE_ALL);
        return Optional.of(describeSoapBox(mat, open, hasSoap, soap));
    }

    private static Optional<Component> cycleSoapRackBlock(
            Level level, BlockPos pos, BlockState state, boolean reverse) {
        boolean hasSoap = state.getValue(SoapRackBlock.HAS_SOAP);
        SoapRackBlockEntity rack = requireSoapRack(level, pos);
        if (!hasSoap) {
            if (reverse) {
                return Optional.empty();
            }
            if (rack == null) {
                return Optional.empty();
            }
            SoapBarAppearance inserted = debugPlaceholderSoap(SoapBarAppearance.DEFAULT_MATERIAL);
            rack.setContainedSoap(inserted);
            level.setBlock(pos, state.setValue(SoapRackBlock.HAS_SOAP, true), Block.UPDATE_ALL);
            return Optional.of(
                    Component.translatable(
                            "debug.fantasy_furniture.variant.soap_rack_soap",
                            describeSoapWear(inserted)));
        }
        if (rack == null) {
            return Optional.empty();
        }
        SoapBarAppearance soap = rack.containedSoap();
        SoapBarAppearance advanced = advanceSoapWear(soap, reverse);
        if (!advanced.equals(soap)) {
            rack.setContainedSoap(advanced);
            return Optional.of(
                    Component.translatable(
                            "debug.fantasy_furniture.variant.soap_rack_soap", describeSoapWear(advanced)));
        }
        if (reverse) {
            return Optional.empty();
        }
        rack.clearContainedSoap();
        level.setBlock(pos, state.setValue(SoapRackBlock.HAS_SOAP, false), Block.UPDATE_ALL);
        return Optional.of(Component.translatable("debug.fantasy_furniture.variant.soap_rack_empty"));
    }

    private static Optional<Component> cycleCombinedOrnament(Level level, BlockPos pos, boolean reverse) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CombinedOrnamentBlockEntity ornament)) {
            return Optional.empty();
        }
        if (reverse) {
            ornament.cycleBase();
            return Optional.of(
                    Component.translatable(
                            "debug.fantasy_furniture.variant.combined_ornament_base",
                            ornament.getBaseVariant().id()));
        }
        ornament.cycleFigurine();
        return Optional.of(
                Component.translatable(
                        "debug.fantasy_furniture.variant.combined_ornament_figurine",
                        ornament.getFigurineVariant().id()));
    }

    private static Optional<Component> cyclePlainGlassWindow(
            Level level, BlockPos pos, BlockState state, boolean reverse) {
        int shape = state.getValue(PlainGlassWindowBlock.SHAPE);
        int next = reverse ? prevPlainGlassShape(shape) : PlainGlassWindowShapes.nextShapeInCycle(shape);
        if (next == shape) {
            return Optional.empty();
        }
        level.setBlock(pos, state.setValue(PlainGlassWindowBlock.SHAPE, next), Block.UPDATE_ALL);
        return Optional.of(
                Component.translatable(
                        "debug.fantasy_furniture.variant.plain_glass_shape",
                        PlainGlassWindowShapes.geoBasename(next)));
    }

    private static Optional<Component> cycleBanquette(
            Level level, BlockPos pos, BlockState state, boolean reverse) {
        BanquetteShape current = state.getValue(BanquetteBlock.SHAPE);
        BanquetteShape[] values = BanquetteShape.values();
        int idx =
                reverse
                        ? (current.ordinal() - 1 + values.length) % values.length
                        : (current.ordinal() + 1) % values.length;
        BanquetteShape next = values[idx];
        if (next == current) {
            return Optional.empty();
        }
        level.setBlock(pos, state.setValue(BanquetteBlock.SHAPE, next), Block.UPDATE_ALL);
        return Optional.of(
                Component.translatable(
                        "debug.fantasy_furniture.variant.banquette_shape", next.getSerializedName()));
    }

    private static Component describeSoapBox(int mat, boolean open, boolean hasSoap, SoapBarAppearance soap) {
        if (!hasSoap) {
            return Component.translatable(
                    "debug.fantasy_furniture.variant.soap_box_empty",
                    Component.translatable(SoapBarMaterials.colorTranslationKey(mat)),
                    open
                            ? Component.translatable("debug.fantasy_furniture.lid.open")
                            : Component.translatable("debug.fantasy_furniture.lid.closed"));
        }
        return Component.translatable(
                "debug.fantasy_furniture.variant.soap_box_soap",
                Component.translatable(SoapBarMaterials.colorTranslationKey(mat)),
                open
                        ? Component.translatable("debug.fantasy_furniture.lid.open")
                        : Component.translatable("debug.fantasy_furniture.lid.closed"),
                describeSoapWear(soap));
    }

    /** 仅推进磨损档 geo，颜料档不变。 */
    private static SoapBarAppearance advanceSoapWear(SoapBarAppearance current, boolean reverse) {
        int wear = current.wear();
        if (reverse) {
            if (wear <= SOAP_WEAR_MIN) {
                return current;
            }
            return withWear(current, wear - 1);
        }
        if (wear >= SOAP_WEAR_MAX) {
            return current;
        }
        return withWear(current, wear + 1);
    }

    private static SoapBarAppearance withWear(SoapBarAppearance appearance, int wear) {
        return new SoapBarAppearance(wear, appearance.materialId());
    }

    /** 调试棒入皂占位：默认磨损 + 指定 pigment（盒体同色），循环中不切换 pigment。 */
    private static SoapBarAppearance debugPlaceholderSoap(int materialId) {
        return new SoapBarAppearance(SoapBarAppearance.DEFAULT_WEAR, materialId);
    }

    private static Component describeSoapWear(SoapBarAppearance appearance) {
        return Component.translatable(
                "debug.fantasy_furniture.variant.soap_wear",
                Component.translatable(SoapBarWear.wearTranslationKey(appearance.wear())),
                Component.translatable(SoapBarMaterials.colorTranslationKey(appearance.materialId())));
    }

    private static int readPlainGlassShape(ItemStack stack) {
        if (stack.getTag() != null && stack.getTag().contains(PlainGlassWindowBlockItem.TAG_SHAPE)) {
            return stack.getTag().getInt(PlainGlassWindowBlockItem.TAG_SHAPE);
        }
        return 0;
    }

    private static int prevPlainGlassShape(int shape) {
        int probe = shape;
        for (int i = 0; i < PlainGlassWindowShapes.COUNT; i++) {
            int next = PlainGlassWindowShapes.nextShapeInCycle(probe);
            if (next == shape) {
                return probe;
            }
            probe = next;
        }
        return shape;
    }

    @Nullable
    private static SoapBoxBlockEntity requireSoapBox(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SoapBoxBlockEntity box ? box : null;
    }

    @Nullable
    private static SoapRackBlockEntity requireSoapRack(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SoapRackBlockEntity rack ? rack : null;
    }

    private static boolean isModBlock(Block block) {
        return FantasyFurniture.MODID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace());
    }

    private static boolean isModItem(Item item) {
        return FantasyFurniture.MODID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace());
    }
}
