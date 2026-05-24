package org.lanye.fantasy_furniture.content.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;
import org.lanye.fantasy_furniture.content.debug.blockentity.GeolibAlignmentProbeBlockEntity;

/**
 * 非对称 Gecko 对齐探针：验证 {@link VoxelShapeRotation#rotateYFromNorthLikeGeckoBlockRenderer} 与默认
 * {@code GeoBlockRenderer} 是否一致。
 *
 * <p>北向 geo 特征（勿改对称）：主体偏西、+X 伸出「臂」、−Z 伸出「鼻」。碰撞由
 * {@code geo_collision_box.py --gecko-block} 导出（相对 raw export 做 X 镜像，与 default
 * {@code GeoBlockRenderer} FACING=NORTH 渲染一致）：
 * {@code Block.box(0, 0, 3, 12, 10, 11)}。
 *
 * <p>四向 F3+B 期望：N {@code minX≈0}（臂），S {@code maxX≈16}，W {@code maxZ≈16}，E {@code minZ≈0}。
 * 若与渲染错位，见 {@code 资料库/docs/04流程/03方案/GeckoLib体素与渲染朝向对齐方案.md} §2.2。
 */
public class GeolibAlignmentProbeBlock extends GeolibFacingEntityBlockWithFactory<GeolibAlignmentProbeBlockEntity> {

    /** {@code geo_collision_box.py --gecko-block} 北向外接盒。 */
    public static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 3.0, 12.0, 10.0, 11.0);

    public GeolibAlignmentProbeBlock(BlockBehaviour.Properties properties) {
        super(properties, GeolibAlignmentProbeBlockEntity::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(SHAPE_NORTH, state.getValue(FACING));
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()) {
            player.displayClientMessage(
                    Component.translatable("debug.fantasy_furniture.geolib_alignment_probe.hint", state.getValue(FACING)),
                    true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
