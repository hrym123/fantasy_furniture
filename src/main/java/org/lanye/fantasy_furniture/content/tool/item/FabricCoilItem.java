package org.lanye.fantasy_furniture.content.tool.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.tag.ModTags;
import org.lanye.fantasy_furniture.content.tool.CoilRecolor;

/** 线圈（展示名待定）：主手对 {@link ModTags#COIL_RECOLORABLE_BLOCKS} 上布衣床品右击循环换色。 */
public final class FabricCoilItem extends Item {

    public FabricCoilItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (!state.is(ModTags.COIL_RECOLORABLE_BLOCKS)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return CoilRecolor.apply(level, state, context.getClickedPos(), context.getClickLocation())
                ? InteractionResult.CONSUME
                : InteractionResult.PASS;
    }
}
