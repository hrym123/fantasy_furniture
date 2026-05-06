package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;

/**
 * 床板 6：在 {@link net.minecraft.world.level.block.BedBlock#use} 之前处理被套、大号枕头、中号枕头、床单；否则原版床会先消耗交互，
 * 物品的 {@link BedPlate6DuvetItem#useOn} 等无法正常触发。顺序：<strong>被套 → 大号枕头 → 中号枕头 → 床单</strong>。
 */
public final class BedPlate6Block extends BedPlateBlock {

    /**
     * 被单在本格上的轴对齐外接盒（薄层，叠在床垫顶 y=5 之上）：整格水平 16×16、高 2/16。床尾/床头两格各加一份，不再按朝向切条带。
     * 被套无碰撞。
     */
    private static final VoxelShape DUVET_OUTER_BOX = Block.box(0, 5, 0, 16, 7, 16);

    private static BlockPos footPos(BlockState state, BlockPos pos) {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return pos;
        }
        return pos.relative(state.getValue(BedBlock.FACING).getOpposite());
    }

    public BedPlate6Block(
            BlockBehaviour.Properties properties,
            BlockEntityType.BlockEntitySupplier<? extends BedPlateBaseBlockEntity> entitySupplier) {
        super(properties, entitySupplier);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeWithOptionalDuvet(state, level, pos, super.getShape(state, level, pos, context));
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeWithOptionalDuvet(state, level, pos, super.getCollisionShape(state, level, pos, context));
    }

    private static VoxelShape shapeWithOptionalDuvet(
            BlockState state, BlockGetter level, BlockPos pos, VoxelShape base) {
        var be = level.getBlockEntity(footPos(state, pos));
        if (!(be instanceof BedPlate6BlockEntity plate) || !plate.hasDuvet()) {
            return base;
        }
        return Shapes.or(base, DUVET_OUTER_BOX);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6DuvetCoverItem) {
            InteractionResult cover = BedPlate6DuvetCoverItem.applyToBed(level, pos, state, player, hand);
            if (cover != InteractionResult.PASS) {
                return cover;
            }
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6LargePillowItem) {
            InteractionResult pillow = BedPlate6LargePillowItem.applyToBed(level, pos, state, player, hand);
            if (pillow != InteractionResult.PASS) {
                return pillow;
            }
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6MediumPillowItem) {
            InteractionResult medium = BedPlate6MediumPillowItem.applyToBed(level, pos, state, player, hand);
            if (medium != InteractionResult.PASS) {
                return medium;
            }
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6DuvetItem) {
            InteractionResult duvet = BedPlate6DuvetItem.applyToBed(level, pos, state, player, hand);
            if (duvet.consumesAction()) {
                return duvet;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
