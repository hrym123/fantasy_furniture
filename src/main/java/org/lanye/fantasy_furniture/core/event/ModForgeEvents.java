package org.lanye.fantasy_furniture.core.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;

/**
 * 通用逻辑侧事件（主逻辑在服务端执行）。
 */
@Mod.EventBusSubscriber(modid = FantasyFurniture.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModForgeEvents {

    private ModForgeEvents() {}

    /**
     * 屏风在逻辑上占两格高：禁止在屏风正上方一格放置固体类方块（与门占用上格类似；流体仍允许）。
     */
    @SubscribeEvent
    public static void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(ModBlocks.DECORATIVE_SCREEN_BLOCK.get())) {
            return;
        }
        BlockState placed = event.getPlacedBlock();
        if (placed.isAir()) {
            return;
        }
        if (placed.getBlock() instanceof LiquidBlock) {
            return;
        }
        event.setCanceled(true);
    }
}
