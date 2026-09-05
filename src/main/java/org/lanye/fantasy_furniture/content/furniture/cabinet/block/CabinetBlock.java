package org.lanye.fantasy_furniture.content.furniture.cabinet.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetCollisionShapes;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetKind;
import org.lanye.fantasy_furniture.content.furniture.cabinet.CabinetSlot;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 柜子：四向放置；三层各可放入一件任意物品（展示框式）。
 *
 * <ul>
 *   <li>持物右击空槽 → 放入 1 件
 *   <li>空手右击有物槽 → 取出
 *   <li>准心高低决定下 / 中 / 上层
 * </ul>
 */
public final class CabinetBlock extends GeolibFacingEntityBlockWithFactory<CabinetBlockEntity> {

    private final CabinetKind kind;

    public CabinetBlock(BlockBehaviour.Properties properties, CabinetKind kind) {
        super(properties, CabinetBlockEntity::new);
        this.kind = kind;
    }

    public CabinetKind kind() {
        return kind;
    }

    private VoxelShape shape(BlockState state) {
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                CabinetCollisionShapes.north(kind), state.getValue(FACING));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shape(state);
    }

    @Override
    protected InteractionResult onUseClient(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        CabinetBlockEntity be = blockEntity(level, pos);
        if (be == null) {
            return InteractionResult.FAIL;
        }
        CabinetSlot slot = kind.slotFromLook(pos, player);
        ItemStack held = player.getItemInHand(hand);

        if (held.isEmpty()) {
            ItemStack taken = be.takeItem(slot);
            if (taken.isEmpty()) {
                return InteractionResult.FAIL;
            }
            if (!player.getInventory().add(taken)) {
                player.drop(taken, false);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
            return InteractionResult.CONSUME;
        }

        if (!be.placeItem(slot, held)) {
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.0f);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            CabinetBlockEntity be = blockEntity(level, pos);
            if (be != null) {
                be.dropContents();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    private static CabinetBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof CabinetBlockEntity cabinet ? cabinet : null;
    }
}
