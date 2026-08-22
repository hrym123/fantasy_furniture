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
import org.lanye.fantasy_furniture.content.furniture.decor.StyledWindow0Shapes;
import org.lanye.fantasy_furniture.content.furniture.decor.block.StyledWindow0Block;
import org.lanye.fantasy_furniture.content.furniture.decor.item.StyledWindow0BlockItem;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesBlock;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesBlockItem;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesCatalog;
import org.lanye.fantasy_furniture.content.furniture.decor.series.StyledWindowSeriesSpec;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BanquetteBlock;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapBarDurability;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapRackBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapBoxBlockItem;

/** 变体调试棒：循环本模组模型变体（耐久度 geo、盖态、造型等），不切换材质/颜料。 */
public final class ModVariantCycler {

    private static final int SOAP_DURABILITY_MIN = SoapBarDurability.MIN;
    private static final int SOAP_DURABILITY_MAX = SoapBarDurability.MAX;

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
        if (block instanceof StyledWindow0Block) {
            return cycleStyledWindow0(level, pos, state, reverse);
        }
        if (block instanceof StyledWindowSeriesBlock) {
            return cycleStyledWindowSeries(level, pos, state, reverse);
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
            SoapBarAppearance next = advanceSoapDurability(current, reverse);
            if (next.equals(current)) {
                return Optional.empty();
            }
            SoapBarAppearance.writeToStack(stack, next);
            return Optional.of(describeSoapDurability(next));
        }
        if (item instanceof SoapBoxBlockItem) {
            return Optional.empty();
        }
        if (item instanceof StyledWindow0BlockItem) {
            int shape = readStyledWindow0Shape(stack);
            int nextShape = reverse ? prevStyledWindow0Shape(shape) : StyledWindow0Shapes.nextShapeInCycle(shape);
            if (nextShape == shape) {
                return Optional.empty();
            }
            if (nextShape == 0) {
                if (stack.hasTag()) {
                    stack.getTag().remove(StyledWindow0BlockItem.TAG_SHAPE);
                    stack.getTag().remove(StyledWindow0BlockItem.TAG_SHAPE_LEGACY);
                    if (stack.getTag().isEmpty()) {
                        stack.setTag(null);
                    }
                }
            } else {
                stack.getOrCreateTag().remove(StyledWindow0BlockItem.TAG_SHAPE_LEGACY);
                stack.getOrCreateTag().putInt(StyledWindow0BlockItem.TAG_SHAPE, nextShape);
            }
            return Optional.of(
                    Component.translatable(
                            "debug.fantasy_furniture.variant.styled_window_0_shape",
                            StyledWindow0Shapes.geoBasename(nextShape)));
        }
        if (item instanceof StyledWindowSeriesBlockItem seriesItem) {
            StyledWindowSeriesSpec spec = StyledWindowSeriesCatalog.get(seriesItem.seriesId());
            String nbt = seriesItem.seriesId().shapeNbtKey();
            int shape = 0;
            if (stack.hasTag() && stack.getTag().contains(nbt)) {
                shape = stack.getTag().getInt(nbt);
            }
            int nextShape =
                    reverse
                            ? prevSeriesShape(spec, shape)
                            : spec.nextShapeInCycle(shape);
            if (nextShape == shape) {
                return Optional.empty();
            }
            if (nextShape == 0) {
                if (stack.hasTag()) {
                    stack.getTag().remove(nbt);
                    if (stack.getTag().isEmpty()) {
                        stack.setTag(null);
                    }
                }
            } else {
                stack.getOrCreateTag().putInt(nbt, nextShape);
            }
            return Optional.of(
                    Component.literal(spec.geoBasename(nextShape)));
        }
        return Optional.empty();
    }

    private static Optional<Component> cycleSoapBarBlock(
            Level level, BlockPos pos, BlockState state, boolean reverse) {
        int durability = state.getValue(SoapBarBlock.DURABILITY);
        int mat = state.getValue(SoapBarBlock.MATERIAL);
        SoapBarAppearance next =
                advanceSoapDurability(new SoapBarAppearance(durability, mat), reverse);
        if (next.durability() == durability) {
            return Optional.empty();
        }
        level.setBlock(
                pos, state.setValue(SoapBarBlock.DURABILITY, next.durability()), Block.UPDATE_ALL);
        return Optional.of(describeSoapDurability(next));
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
                if (soap.durability() < SOAP_DURABILITY_MAX) {
                    SoapBarAppearance prev = withDurability(soap, soap.durability() + 1);
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
            if (soap.durability() > SOAP_DURABILITY_MIN) {
                SoapBarAppearance next = withDurability(soap, soap.durability() - 1);
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
                            describeSoapDurability(inserted)));
        }
        if (rack == null) {
            return Optional.empty();
        }
        SoapBarAppearance soap = rack.containedSoap();
        SoapBarAppearance advanced = advanceSoapDurability(soap, reverse);
        if (!advanced.equals(soap)) {
            rack.setContainedSoap(advanced);
            return Optional.of(
                    Component.translatable(
                            "debug.fantasy_furniture.variant.soap_rack_soap", describeSoapDurability(advanced)));
        }
        if (reverse) {
            return Optional.empty();
        }
        rack.clearContainedSoap();
        level.setBlock(pos, state.setValue(SoapRackBlock.HAS_SOAP, false), Block.UPDATE_ALL);
        return Optional.of(Component.translatable("debug.fantasy_furniture.variant.soap_rack_empty"));
    }

    private static Optional<Component> cycleStyledWindow0(
            Level level, BlockPos pos, BlockState state, boolean reverse) {
        int shape = state.getValue(StyledWindow0Block.SHAPE);
        int next = reverse ? prevStyledWindow0Shape(shape) : StyledWindow0Shapes.nextShapeInCycle(shape);
        if (next == shape) {
            return Optional.empty();
        }
        level.setBlock(pos, state.setValue(StyledWindow0Block.SHAPE, next), Block.UPDATE_ALL);
        return Optional.of(
                Component.translatable(
                        "debug.fantasy_furniture.variant.styled_window_0_shape",
                        StyledWindow0Shapes.geoBasename(next)));
    }

    private static Optional<Component> cycleStyledWindowSeries(
            Level level, BlockPos pos, BlockState state, boolean reverse) {
        StyledWindowSeriesBlock block = (StyledWindowSeriesBlock) state.getBlock();
        StyledWindowSeriesSpec spec = block.spec();
        int shape = state.getValue(StyledWindowSeriesBlock.SHAPE);
        int next = reverse ? prevSeriesShape(spec, shape) : spec.nextShapeInCycle(shape);
        if (next == shape) {
            return Optional.empty();
        }
        BlockPos master = StyledWindowSeriesBlock.masterPos(state, pos);
        BlockState masterState = level.getBlockState(master);
        StyledWindowSeriesBlock.setShapeOnFootprint(level, master, masterState, next);
        return Optional.of(Component.literal(spec.geoBasename(next)));
    }

    private static int prevSeriesShape(StyledWindowSeriesSpec spec, int shape) {
        int probe = shape;
        for (int i = 0; i < spec.shapeCount(); i++) {
            int next = spec.nextShapeInCycle(probe);
            if (next == shape) {
                return probe;
            }
            probe = next;
        }
        return 0;
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
                describeSoapDurability(soap));
    }

    /** 仅推进耐久度档 geo（正向消耗、反向恢复），颜料档不变。 */
    private static SoapBarAppearance advanceSoapDurability(SoapBarAppearance current, boolean reverse) {
        int durability = current.durability();
        if (reverse) {
            if (durability >= SOAP_DURABILITY_MAX) {
                return current;
            }
            return withDurability(current, durability + 1);
        }
        if (durability <= SOAP_DURABILITY_MIN) {
            return current;
        }
        return withDurability(current, durability - 1);
    }

    private static SoapBarAppearance withDurability(SoapBarAppearance appearance, int durability) {
        return new SoapBarAppearance(
                durability,
                appearance.materialId(),
                appearance.bagMaterialId(),
                appearance.packagingTorn(),
                appearance.particleMatId(),
                appearance.boxMaterialId());
    }

    /** 调试棒入皂占位：满耐久 + 指定 pigment（盒体同色），循环中不切换 pigment。 */
    private static SoapBarAppearance debugPlaceholderSoap(int materialId) {
        return new SoapBarAppearance(SoapBarAppearance.DEFAULT_DURABILITY, materialId);
    }

    private static Component describeSoapDurability(SoapBarAppearance appearance) {
        return Component.translatable(
                "debug.fantasy_furniture.variant.soap_durability",
                Component.translatable(SoapBarDurability.translationKey(appearance.durability())),
                Component.translatable(SoapBarMaterials.colorTranslationKey(appearance.materialId())));
    }

    private static int readStyledWindow0Shape(ItemStack stack) {
        if (stack.getTag() == null) {
            return 0;
        }
        if (stack.getTag().contains(StyledWindow0BlockItem.TAG_SHAPE)) {
            return stack.getTag().getInt(StyledWindow0BlockItem.TAG_SHAPE);
        }
        if (stack.getTag().contains(StyledWindow0BlockItem.TAG_SHAPE_LEGACY)) {
            return stack.getTag().getInt(StyledWindow0BlockItem.TAG_SHAPE_LEGACY);
        }
        return 0;
    }

    private static int prevStyledWindow0Shape(int shape) {
        int probe = shape;
        for (int i = 0; i < StyledWindow0Shapes.COUNT; i++) {
            int next = StyledWindow0Shapes.nextShapeInCycle(probe);
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
