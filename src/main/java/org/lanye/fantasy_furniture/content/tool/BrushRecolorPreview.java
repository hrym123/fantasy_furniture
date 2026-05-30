package org.lanye.fantasy_furniture.content.tool;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.CeramicTileBlocks;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.bootstrap.block.PlainGlassWindowRegistration;
import org.lanye.fantasy_furniture.bootstrap.block.WallpaperBlocks;
import org.lanye.fantasy_furniture.content.furniture.common.state.PlainGlassWindowMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainGlassWindowBlock;
import org.lanye.fantasy_furniture.content.soap.block.BodyCreamBlock;
import org.lanye.fantasy_furniture.content.soap.block.BodyWashBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapBoxBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBagBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBoxBlock;
import org.lanye.fantasy_furniture.content.soap.item.BodyCreamBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.BodyWashBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapBoxBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBagBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBoxBlockItem;

/** 刷子换色 HUD：由当前方块解析「下一档」预览物品与展示名。 */
public final class BrushRecolorPreview {

    public static final int MAX_DISPLAY_CHARS = 12;
    private static final String ELLIPSIS = "…";

    public record Preview(ItemStack stack, Component name) {}

    private BrushRecolorPreview() {}

    public static Optional<Preview> forTargetState(BlockState state) {
        return BrushRecolor.nextColorState(state).flatMap(BrushRecolorPreview::previewForNextState);
    }

    private static Optional<Preview> previewForNextState(BlockState nextState) {
        return itemStackFor(nextState).map(stack -> new Preview(stack, stack.getHoverName()));
    }

    private static Optional<ItemStack> itemStackFor(BlockState state) {
        if (state.getBlock() instanceof PlainGlassWindowBlock && PlainGlassWindowBlock.MATERIAL != null) {
            PlainGlassWindowMaterialVariant material = state.getValue(PlainGlassWindowBlock.MATERIAL);
            int index = material.ordinal();
            var items = PlainGlassWindowRegistration.items();
            if (index < 0 || index >= items.size()) {
                return Optional.empty();
            }
            return Optional.of(new ItemStack(items.get(index).get()));
        }
        for (CeramicTileBlocks.TileVariant variant : CeramicTileBlocks.TileVariant.values()) {
            if (state.is(variant.entry().block().get())) {
                return Optional.of(new ItemStack(variant.entry().item().get()));
            }
        }
        for (WallpaperBlocks.WallpaperVariant variant : WallpaperBlocks.WallpaperVariant.values()) {
            if (state.is(variant.entry().block().get())) {
                return Optional.of(new ItemStack(variant.entry().item().get()));
            }
        }
        if (state.getBlock() instanceof SoapBoxBlock) {
            int material = state.getValue(SoapBoxBlock.MATERIAL);
            return Optional.of(
                    SoapBoxBlockItem.stackWithBoxMaterial(ModBlocks.SOAP_BOX.item().get(), material));
        }
        if (state.getBlock() instanceof SoapPaperBagBlock) {
            int material = state.getValue(SoapPaperBagBlock.MATERIAL);
            return Optional.of(
                    SoapPaperBagBlockItem.stackWithBagMaterial(
                            ModBlocks.SOAP_PAPER_BAG.item().get(), material));
        }
        if (state.getBlock() instanceof SoapPaperBoxBlock) {
            int material = state.getValue(SoapPaperBoxBlock.MATERIAL);
            return Optional.of(
                    SoapPaperBoxBlockItem.stackWithMaterial(
                            ModBlocks.SOAP_PAPER_BOX.item().get(), material));
        }
        if (state.getBlock() instanceof BodyCreamBlock) {
            int material = state.getValue(BodyCreamBlock.MATERIAL);
            return Optional.of(
                    BodyCreamBlockItem.stackWithMaterial(ModBlocks.BODY_CREAM.item().get(), material));
        }
        if (state.getBlock() instanceof BodyWashBlock) {
            int material = state.getValue(BodyWashBlock.MATERIAL);
            return Optional.of(
                    BodyWashBlockItem.stackWithMaterial(ModBlocks.BODY_WASH.item().get(), material));
        }
        return Optional.empty();
    }

    /** 展示名截断：最多 {@link #MAX_DISPLAY_CHARS} 个字符，超出加 {@code …}。 */
    public static String truncateDisplayName(String raw) {
        if (raw.length() <= MAX_DISPLAY_CHARS) {
            return raw;
        }
        return raw.substring(0, MAX_DISPLAY_CHARS) + ELLIPSIS;
    }
}
