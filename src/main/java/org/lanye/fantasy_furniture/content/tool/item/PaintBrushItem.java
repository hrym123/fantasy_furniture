package org.lanye.fantasy_furniture.content.tool.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.tag.ModTags;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;

/** 刷子：主手对 {@link ModTags#BRUSH_RECOLORABLE_BLOCKS} 成员右击，按枚举序循环换色。 */
public final class PaintBrushItem extends Item {

    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (!state.is(ModTags.BRUSH_RECOLORABLE_BLOCKS)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return BrushRecolor.apply(level, context.getClickedPos(), state)
                ? InteractionResult.CONSUME
                : InteractionResult.PASS;
    }
}
