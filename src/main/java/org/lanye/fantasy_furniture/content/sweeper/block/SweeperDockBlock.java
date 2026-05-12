package org.lanye.fantasy_furniture.content.sweeper.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.Config;
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.sweeper.entity.SweeperRobotEntity;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlock;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.fantasy_furniture.bootstrap.entity.ModEntities;

/**
 * 扫地机器人机仓：放置后立刻尝试生成绑定机器人；服务端每 tick 若机仓仍有效但无绑定体则补生成（如虚空清除等）。
 * 玩家右键仍可手动触发一次补生成（与放置/补位逻辑相同）。
 */
public class SweeperDockBlock extends GeolibFacingEntityBlockWithFactory<SweeperDockBlockEntity> {

    /** 由 tools/collision/geo_collision_box.py 计算得出（sweeper_dock.geo.json）。 */
    private static final VoxelShape SHAPE = box(0.0, 0.0, 0.0, 16.0, 8.5, 16.0);

    /**
     * 水平发现绑定 {@link SweeperRobotEntity} 用的 AABB 膨胀量：取 {@link Config#sweeperPatrolRadius()} 与 24 的较大值，
     * 避免巡逻半径调大后补位/拆仓检测不到机体。
     */
    public static double boundRobotSearchInflateBlocks() {
        return Math.max(24.0, Config.sweeperPatrolRadius());
    }

    public SweeperDockBlock(BlockBehaviour.Properties properties) {
        super(properties, SweeperDockBlockEntity::new);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : BaseEntityBlock.createTickerHelper(
                        type, ModBlocks.SWEEPER_DOCK.blockEntityType().get(), SweeperDockBlockEntity::serverTick);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            net.minecraft.world.level.BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        // 允许扫地机器人进入机仓内部，其它实体仍使用正常碰撞。
        if (context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof SweeperRobotEntity robot
                && robot.ignoresDockBlockCollision()) {
            return Shapes.empty();
        }
        return SHAPE;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        if (trySpawnBoundRobotIfAbsent(serverLevel, pos, state)) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            trySpawnBoundRobotIfAbsent(serverLevel, pos, state);
        }
    }

    /**
     * 若当前世界尚无绑定到 {@code dockPos} 的 {@link SweeperRobotEntity}，则创建一只并加入世界。
     *
     * @return 若本调用前已存在绑定体，或成功加入新实体，则为 true；创建失败则为 false
     */
    public static boolean trySpawnBoundRobotIfAbsent(ServerLevel level, BlockPos dockPos, BlockState state) {
        AABB searchBox = new AABB(dockPos).inflate(boundRobotSearchInflateBlocks());
        List<SweeperRobotEntity> robots =
                level.getEntitiesOfClass(SweeperRobotEntity.class, searchBox, r -> dockPos.equals(r.getDockPos()));
        if (!robots.isEmpty()) {
            return true;
        }
        SweeperRobotEntity robot = ModEntities.SWEEPER_ROBOT.get().create(level);
        if (robot == null) {
            return false;
        }
        robot.bindDock(dockPos);
        Direction facing = state.getValue(GeolibFacingEntityBlock.FACING);
        float yaw =
                Mth.wrapDegrees(
                        (float) (Mth.atan2(-facing.getStepX(), facing.getStepZ()) * (180.0 / Math.PI)));
        robot.moveTo(dockPos.getX() + 0.5, dockPos.getY() + 0.20, dockPos.getZ() + 0.5, yaw, 0f);
        return level.addFreshEntity(robot);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            AABB searchBox = new AABB(pos).inflate(boundRobotSearchInflateBlocks());
            List<SweeperRobotEntity> robots =
                    serverLevel.getEntitiesOfClass(
                            SweeperRobotEntity.class, searchBox, robot -> pos.equals(robot.getDockPos()));
            for (SweeperRobotEntity robot : robots) {
                robot.removeBecauseDockInvalid();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
