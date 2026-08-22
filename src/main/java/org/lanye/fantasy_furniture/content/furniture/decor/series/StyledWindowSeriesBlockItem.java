package org.lanye.fantasy_furniture.content.furniture.decor.series;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.reverie_core.geolib.GeolibBlockItem;
import org.lanye.reverie_core.geolib.GeolibItemAssets;
import org.lanye.reverie_core.geolib.multiblock.WallPlaneFootprint;

public final class StyledWindowSeriesBlockItem extends GeolibBlockItem {

    private final StyledWindowSeriesId seriesId;
    private final int colorIndex;

    public StyledWindowSeriesBlockItem(
            Block block,
            Properties properties,
            GeolibItemAssets assets,
            StyledWindowSeriesId seriesId,
            int colorIndex) {
        super(block, properties, assets);
        this.seriesId = seriesId;
        this.colorIndex = colorIndex;
    }

    public StyledWindowSeriesId seriesId() {
        return seriesId;
    }

    public int colorIndex() {
        return colorIndex;
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
        StyledWindowSeriesSpec spec = StyledWindowSeriesCatalog.get(seriesId);
        if (tag != null && tag.contains(seriesId.shapeNbtKey(), Tag.TAG_INT)) {
            s = Mth.clamp(tag.getInt(seriesId.shapeNbtKey()), 0, spec.shapeCount() - 1);
        }
        if (state.getBlock() instanceof StyledWindowSeriesBlock) {
            state =
                    state.setValue(StyledWindowSeriesBlock.SHAPE, s)
                            .setValue(StyledWindowSeriesBlock.PART_U, 0)
                            .setValue(StyledWindowSeriesBlock.PART_V, 0);
        }
        Direction facing = state.getValue(StyledWindowSeriesBlock.FACING);
        WallPlaneFootprint fp = spec.footprint();
        BlockPos origin = fp.originFromClick(facing, context.getClickedPos());
        BlockPlaceContext atOrigin = BlockPlaceContext.at(context, origin, context.getClickedFace());
        return super.placeBlock(atOrigin, state);
    }
}
