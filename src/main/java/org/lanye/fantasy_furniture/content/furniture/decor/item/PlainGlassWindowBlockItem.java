package org.lanye.fantasy_furniture.content.furniture.decor.item;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.content.furniture.common.state.PlainGlassWindowMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowShapes;
import org.lanye.fantasy_furniture.content.furniture.decor.block.PlainGlassWindowBlock;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;

/**
 * 0号窗户：每种颜色一对 block/item 同 id（REG-608）；放置时写入 {@link PlainGlassWindowBlock#SHAPE}（颜色由方块 id
 * 决定）。造型由 {@link #TAG_SHAPE} 携带，世界中右键切换。
 */
public final class PlainGlassWindowBlockItem extends GeolibBlockItem {

    public static final String TAG_SHAPE = "FfPlainWinShape";

    private final PlainGlassWindowMaterialVariant variant;

    public PlainGlassWindowBlockItem(
            Block block, Properties properties, GeolibItemAssets assets, PlainGlassWindowMaterialVariant variant) {
        super(block, properties, assets);
        this.variant = variant;
    }

    public PlainGlassWindowMaterialVariant variant() {
        return variant;
    }

    public int materialIndex() {
        return variant.ordinal();
    }

    @Override
    public String getDescriptionId() {
        return Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        ItemStack held = context.getItemInHand();
        CompoundTag tag = held.getTag();
        int s = 0;
        if (tag != null && tag.contains(TAG_SHAPE, Tag.TAG_INT)) {
            s = Mth.clamp(tag.getInt(TAG_SHAPE), 0, PlainGlassWindowShapes.COUNT - 1);
        }
        if (state.getBlock() instanceof PlainGlassWindowBlock) {
            state = state.setValue(PlainGlassWindowBlock.SHAPE, s);
        }
        return super.placeBlock(context, state);
    }

    public static ItemStack presetStack(net.minecraft.world.item.Item item, int shape) {
        ItemStack stack = new ItemStack(item);
        int s = Mth.clamp(shape, 0, PlainGlassWindowShapes.COUNT - 1);
        if (s != 0) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(TAG_SHAPE, s);
            stack.setTag(tag);
        }
        return stack;
    }
}
