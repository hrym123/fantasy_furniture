package org.lanye.fantasy_furniture.content.soap.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.soap.SoapBottleComponentPick;
import org.lanye.fantasy_furniture.content.soap.block.BodyCreamBlock;
import org.lanye.fantasy_furniture.content.soap.block.BodyWashBlock;
import org.lanye.fantasy_furniture.content.soap.block.ShampooBlock;

/** 瓶罐摞客户端准心选取（读 {@link Minecraft#hitResult}）。 */
@OnlyIn(Dist.CLIENT)
public final class SoapBottleClientPick {

    private SoapBottleClientPick() {}

    public static ItemStack resolveCloneItemStack(Level level, BlockState state, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
            return ItemStack.EMPTY;
        }
        if (!bhr.getBlockPos().equals(pos)) {
            return ItemStack.EMPTY;
        }
        BlockState hitState = level.getBlockState(bhr.getBlockPos());
        if (!isSoapBottleHost(hitState)) {
            return ItemStack.EMPTY;
        }
        return SoapBottleComponentPick.stackForHit(level, state, pos, bhr.getLocation());
    }

    private static boolean isSoapBottleHost(BlockState state) {
        return state.getBlock() instanceof BodyWashBlock
                || state.getBlock() instanceof ShampooBlock
                || state.getBlock() instanceof BodyCreamBlock;
    }
}
