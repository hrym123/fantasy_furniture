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
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.sweeper.entity.SweeperRobotEntity;
import org.lanye.fantasy_furniture.core.geolib.GeolibFacingEntityBlock;
import org.lanye.fantasy_furniture.core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.fantasy_furniture.bootstrap.entity.ModEntities;

/** 扫地机器人机仓：右键确保存在一个绑定到当前机仓的机器人。 */
public class SweeperDockBlock extends GeolibFacingEntityBlockWithFactory<SweeperDockBlockEntity> {

    /** 由 tools/geo_collision_box.py 计算得出（sweeper_dock.geo.json）。 */
    private static final VoxelShape SHAPE = box(0.0, 0.0, 0.0, 16.0, 8.5, 16.0);

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
        if (ensureRobot(serverLevel, pos, state)) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private static boolean ensureRobot(ServerLevel level, BlockPos dockPos, BlockState state) {
        AABB searchBox = new AABB(dockPos).inflate(24.0);
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
            AABB searchBox = new AABB(pos).inflate(24.0);
            List<SweeperRobotEntity> robots =
                    serverLevel.getEntitiesOfClass(
                            SweeperRobotEntity.class, searchBox, robot -> pos.equals(robot.getDockPos()));
            for (SweeperRobotEntity robot : robots) {
                robot.discard();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
