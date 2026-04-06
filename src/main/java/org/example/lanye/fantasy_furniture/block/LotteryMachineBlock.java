package org.example.lanye.fantasy_furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.example.lanye.fantasy_furniture.block.entity.LotteryMachineBlockEntity;
import org.example.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlockWithFactory;

/**
 * 抽奖机（MoonStarfish Geo）：右键播放抽奖动画。
 * <p>
 * 碰撞为单格内外接轴对齐盒：由 {@code tools/geo_collision_box.py} 默认输出（各裁切盒的全局 min/max，非几何并集体积）。
 * 本 geo 外接盒接近整格、游戏中体感像实心方块属正常现象；需要保留模型镂空碰撞应使用脚本的 {@code --emit-java}。
 */
public class LotteryMachineBlock extends GeolibFacingEntityBlockWithFactory<LotteryMachineBlockEntity> {

    /** 北向基准：与脚本默认输出一致；非整格（整格为 z∈[0,16]），北侧贴地一侧约空出 z∈[0,3)。 */
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 3.0, 16.0, 16.0, 16.0);

    private static final VoxelShape SHAPE_EAST = VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, Direction.EAST);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, Direction.SOUTH);
    private static final VoxelShape SHAPE_WEST = VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, Direction.WEST);

    // #region agent log
    private static final java.util.concurrent.atomic.AtomicInteger AGENT_SHAPE_N = new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.concurrent.atomic.AtomicInteger AGENT_COLL_N = new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.concurrent.atomic.AtomicBoolean AGENT_INIT = new java.util.concurrent.atomic.AtomicBoolean();

    private static void agentLog(String hypothesisId, String location, String message, String dataJson) {
        try {
            long ts = System.currentTimeMillis();
            String line = String.format(
                    "{\"sessionId\":\"34ccf7\",\"runId\":\"post-fix\",\"hypothesisId\":\"%s\",\"location\":\"%s\",\"message\":\"%s\",\"data\":%s,\"timestamp\":%d}%n",
                    hypothesisId, location, message.replace("\"", "'"), dataJson, ts);
            String ud = System.getProperty("user.dir");
            java.nio.file.Path[] paths = new java.nio.file.Path[] {
                java.nio.file.Paths.get(ud, "debug-34ccf7.log"),
                java.nio.file.Paths.get(ud, "run", "debug-34ccf7.log")
            };
            synchronized (LotteryMachineBlock.class) {
                for (java.nio.file.Path p : paths) {
                    java.nio.file.Files.writeString(
                            p, line, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
                }
            }
        } catch (Throwable ignored) {
        }
    }
    // #endregion

    public LotteryMachineBlock(BlockBehaviour.Properties properties) {
        super(properties, LotteryMachineBlockEntity::new);
        // #region agent log
        if (AGENT_INIT.compareAndSet(false, true)) {
            agentLog(
                    "D",
                    "LotteryMachineBlock.<init>",
                    "constructed",
                    "{\"class\":\"org.example.lanye.fantasy_furniture.block.LotteryMachineBlock\"}");
        }
        // #endregion
    }

    private static VoxelShape computeShape(BlockState state) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape out = computeShape(state);
        // #region agent log
        int n = AGENT_SHAPE_N.incrementAndGet();
        if (n <= 40) {
            var b = out.bounds();
            boolean client = level instanceof Level ? ((Level) level).isClientSide() : false;
            agentLog(
                    "B",
                    "LotteryMachineBlock.getShape",
                    "outline",
                    String.format(
                            "{\"n\":%d,\"facing\":\"%s\",\"client\":%s,\"minX\":%.5f,\"minY\":%.5f,\"minZ\":%.5f,\"maxX\":%.5f,\"maxY\":%.5f,\"maxZ\":%.5f}",
                            n,
                            state.getValue(FACING),
                            client,
                            b.minX,
                            b.minY,
                            b.minZ,
                            b.maxX,
                            b.maxY,
                            b.maxZ));
        }
        // #endregion
        return out;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape out = computeShape(state);
        // #region agent log
        int n = AGENT_COLL_N.incrementAndGet();
        if (n <= 60) {
            var b = out.bounds();
            boolean client = level instanceof Level ? ((Level) level).isClientSide() : false;
            agentLog(
                    "A",
                    "LotteryMachineBlock.getCollisionShape",
                    "collision",
                    String.format(
                            "{\"n\":%d,\"facing\":\"%s\",\"client\":%s,\"minX\":%.5f,\"minY\":%.5f,\"minZ\":%.5f,\"maxX\":%.5f,\"maxY\":%.5f,\"maxZ\":%.5f}",
                            n,
                            state.getValue(FACING),
                            client,
                            b.minX,
                            b.minY,
                            b.minZ,
                            b.maxX,
                            b.maxY,
                            b.maxZ));
        }
        // #endregion
        return out;
    }

    @Override
    public InteractionResult use(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LotteryMachineBlockEntity machine) {
                machine.onServerDraw();
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
