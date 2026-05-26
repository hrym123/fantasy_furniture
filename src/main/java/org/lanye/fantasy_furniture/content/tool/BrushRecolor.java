package org.lanye.fantasy_furniture.content.tool;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBagBlockEntity;
import org.lanye.fantasy_furniture.bootstrap.block.CeramicTileBlocks;
import org.lanye.fantasy_furniture.bootstrap.block.WallpaperBlocks;
import org.lanye.fantasy_furniture.bootstrap.item.ModItems;
import org.lanye.fantasy_furniture.bootstrap.tag.ModTags;
import org.lanye.fantasy_furniture.content.furniture.common.state.PlainGlassWindowMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainGlassWindowBlock;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagMaterials;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBagBlock;

/** 刷子对 {@link ModTags#BRUSH_RECOLORABLE_BLOCKS} 成员循环换色的服务端逻辑。 */
public final class BrushRecolor {

    private BrushRecolor() {}

    /**
     * 主手刷子对 {@link ModTags#BRUSH_RECOLORABLE_BLOCKS} 目标右击时，方块自身 {@code use} 应让出（由 {@link
     * org.lanye.fantasy_furniture.content.tool.item.PaintBrushItem} 换色）。
     */
    public static boolean defersBlockUse(Player player, InteractionHand hand, BlockState state) {
        return hand == InteractionHand.MAIN_HAND
                && state.is(ModTags.BRUSH_RECOLORABLE_BLOCKS)
                && player.getItemInHand(hand).is(ModItems.PAINT_BRUSH.get());
    }

    /**
     * 将目标方块换至同族下一档颜色；顺序为各变种枚举 {@code values()} 声明序。
     *
     * @return 换色后的 BlockState；无法识别时 empty
     */
    public static Optional<BlockState> nextColorState(BlockState state) {
        if (!state.is(ModTags.BRUSH_RECOLORABLE_BLOCKS)) {
            return Optional.empty();
        }
        if (state.getBlock() instanceof PlainGlassWindowBlock && PlainGlassWindowBlock.MATERIAL != null) {
            PlainGlassWindowMaterialVariant current = state.getValue(PlainGlassWindowBlock.MATERIAL);
            PlainGlassWindowMaterialVariant next = nextInCycle(current);
            return Optional.of(state.setValue(PlainGlassWindowBlock.MATERIAL, next));
        }
        for (CeramicTileBlocks.TileVariant variant : CeramicTileBlocks.TileVariant.values()) {
            if (state.is(variant.entry().block().get())) {
                CeramicTileBlocks.TileVariant next = nextInCycle(variant);
                return Optional.of(next.entry().block().get().defaultBlockState());
            }
        }
        for (WallpaperBlocks.WallpaperVariant variant : WallpaperBlocks.WallpaperVariant.values()) {
            if (state.is(variant.entry().block().get())) {
                WallpaperBlocks.WallpaperVariant next = nextInCycle(variant);
                return Optional.of(next.entry().block().get().defaultBlockState());
            }
        }
        if (state.getBlock() instanceof SoapBoxBlock) {
            int current = state.getValue(SoapBoxBlock.MATERIAL);
            int next = nextMaterialId(current, SoapBarMaterials.COUNT);
            return Optional.of(state.setValue(SoapBoxBlock.MATERIAL, next));
        }
        if (state.getBlock() instanceof SoapPaperBagBlock) {
            int current = state.getValue(SoapPaperBagBlock.MATERIAL);
            int next = nextMaterialId(current, SoapPaperBagMaterials.COUNT);
            if (next == SoapPaperBagMaterials.RAINBOW) {
                next = 1;
            }
            return Optional.of(state.setValue(SoapPaperBagBlock.MATERIAL, next));
        }
        return Optional.empty();
    }

    private static int nextMaterialId(int current, int count) {
        if (current < 1 || current > count) {
            return 1;
        }
        return current % count + 1;
    }

    /** 服务端：写入下一档颜色。 */
    public static boolean apply(Level level, BlockPos pos, BlockState state) {
        Optional<BlockState> next = nextColorState(state);
        if (next.isEmpty()) {
            return false;
        }
        level.setBlock(pos, next.get(), Block.UPDATE_ALL_IMMEDIATE);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SoapPaperBagBlockEntity stack) {
            stack.replaceTopMaterial(next.get().getValue(SoapPaperBagBlock.MATERIAL));
        }
        return true;
    }

    private static <E extends Enum<E>> E nextInCycle(E current) {
        E[] values = current.getDeclaringClass().getEnumConstants();
        return values[(current.ordinal() + 1) % values.length];
    }
}
