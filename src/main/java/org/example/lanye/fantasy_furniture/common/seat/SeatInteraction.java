package org.example.lanye.fantasy_furniture.common.seat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.example.lanye.fantasy_furniture.entity.FurnitureSeatEntity;

/** 对方块使用（右键）尝试入座：按 {@link SeatRegistry} 顺序匹配第一条规则。 */
public final class SeatInteraction {

    private SeatInteraction() {}

    /**
     * 在服务端对锚点方块执行交互入座（玩家需在入座范围内且未骑乘其他实体）。
     *
     * @return 是否已成功开始骑乘
     */
    public static boolean trySitFromBlockUse(
            ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state) {
        if (player.isPassenger()) {
            return false;
        }
        if (!SeatCooldown.canSit(player, level)) {
            return false;
        }

        AABB playerBox = player.getBoundingBox();

        for (var e : SeatRegistry.entries()) {
            String configId = e.getKey();
            SeatConfig cfg = e.getValue();
            if (!cfg.blockValid().test(state)) {
                continue;
            }
            if (!playerBox.intersects(cfg.toWorldSitRange(pos))) {
                continue;
            }

            clearOrphanSeats(level, pos);
            FurnitureSeatEntity seat = FurnitureSeatEntity.create(level, pos, state, configId);
            if (!level.addFreshEntity(seat)) {
                return false;
            }
            player.startRiding(seat, true);
            return true;
        }
        return false;
    }

    private static void clearOrphanSeats(ServerLevel level, BlockPos anchor) {
        for (FurnitureSeatEntity seat :
                level.getEntitiesOfClass(
                        FurnitureSeatEntity.class,
                        new AABB(anchor),
                        s -> s.getAnchorPos().equals(anchor))) {
            if (seat.getPassengers().isEmpty()) {
                seat.discard();
            }
        }
    }
}
