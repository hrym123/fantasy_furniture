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
import org.lanye.fantasy_furniture.Config;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6BedDecorRemoval;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;

/**
 * 床板 6：在 {@link net.minecraft.world.level.block.BedBlock#use} 之前处理拆卸手套、被套、大号、中号、小号、床单。
 * 卸下/替换床品改由主手 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DisassemblyGloveItem} 按叠放逆序逐层拆除。
 * 顺序：<strong>拆卸手套 → 被套 →（主手大号且另一手中号、且床已摆大件且仍可加中号时先放中号）→ 大号 → 中号 → 小号 → 床单</strong>。
 */
public final class BedPlate6Block extends BedPlateBlock {

    /**
     * 被单在本格上的轴对齐外接盒（薄层，叠在床垫顶 y=5 之上）：整格水平 16×16、高 2/16。床尾/床头两格各加一份，不再按朝向切条带。
     * 用于 {@link #getShape} 与射线；是否并入 {@link #getCollisionShape} 由通用配置 {@link Config#bedPlate6DuvetCollision()} 决定（默认关闭）。
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
        return shapeWithOptionalDuvet(
                state, level, pos, super.getShape(state, level, pos, context), true);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeWithOptionalDuvet(
                state,
                level,
                pos,
                super.getCollisionShape(state, level, pos, context),
                Config.bedPlate6DuvetCollision());
    }

    private static VoxelShape shapeWithOptionalDuvet(
            BlockState state, BlockGetter level, BlockPos pos, VoxelShape base, boolean mergeDuvetCollision) {
        if (!mergeDuvetCollision) {
            return base;
        }
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
        InteractionResult glove = BedPlate6BedDecorRemoval.tryRemoveLastWithMainHandGlove(level, state, pos, player, hand);
        if (glove != InteractionResult.PASS) {
            return glove;
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6DuvetCoverItem) {
            InteractionResult cover = BedPlate6DuvetCoverItem.applyToBed(level, pos, state, player, hand);
            if (cover != InteractionResult.PASS) {
                return cover;
            }
        }
        InteractionResult mediumBeforeLarge =
                tryMediumWhenHeldLargeWouldHideOtherHandMedium(level, pos, state, player, hand);
        if (mediumBeforeLarge != InteractionResult.PASS) {
            return mediumBeforeLarge;
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6LargePillowItem) {
            InteractionResult pillow = BedPlate6LargePillowItem.applyToBed(level, pos, state, player, hand);
            if (pillow != InteractionResult.PASS) {
                return pillow;
            }
        }
        InteractionHand mediumHand = handHoldingMediumPillowPreferUsed(player, hand);
        if (mediumHand != null) {
            InteractionResult medium = BedPlate6MediumPillowItem.applyToBed(level, pos, state, player, mediumHand);
            if (medium != InteractionResult.PASS) {
                return medium;
            }
        }
        InteractionHand smallHand = handHoldingSmallPillowPreferUsed(player, hand);
        if (smallHand != null) {
            InteractionResult small = BedPlate6SmallPillowItem.applyToBed(level, pos, state, player, smallHand);
            if (small != InteractionResult.PASS) {
                return small;
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

    /**
     * 当前用于右键的手拿大号、另一手拿中号时，大号分支会先成功并吞掉交互，副手中号永远放不上。
     * 在「床已摆大号或至少一只中号」且中号未满两只时，改为先消耗另一手中的中号放置逻辑。
     */
    private static InteractionResult tryMediumWhenHeldLargeWouldHideOtherHandMedium(
            Level level, BlockPos pos, BlockState state, Player player, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!state.is(ModBlocks.BED_PLATE6.block().get())) {
            return InteractionResult.PASS;
        }
        if (!(player.getItemInHand(usedHand).getItem() instanceof BedPlate6LargePillowItem)) {
            return InteractionResult.PASS;
        }
        InteractionHand other =
                usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (!(player.getItemInHand(other).getItem() instanceof BedPlate6MediumPillowItem)) {
            return InteractionResult.PASS;
        }
        var be = level.getBlockEntity(footPos(state, pos));
        if (!(be instanceof BedPlate6BlockEntity plate) || !plate.hasDuvet()) {
            return InteractionResult.PASS;
        }
        if (plate.hasLargePillow() && plate.getMediumPillowCount() >= 1) {
            return InteractionResult.PASS;
        }
        if (!plate.hasLargePillow() && plate.getMediumPillowCount() >= 2) {
            return InteractionResult.PASS;
        }
        if (!plate.hasLargePillow() && plate.getMediumPillowCount() == 0) {
            return InteractionResult.PASS;
        }
        return BedPlate6MediumPillowItem.applyToBed(level, pos, state, player, other);
    }

    /** 优先使用本次交互的手上的中号，否则若另一手为中号则使用该手（例如仅副手持有中号）。 */
    private static InteractionHand handHoldingMediumPillowPreferUsed(Player player, InteractionHand usedHand) {
        if (player.getItemInHand(usedHand).getItem() instanceof BedPlate6MediumPillowItem) {
            return usedHand;
        }
        InteractionHand other =
                usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (player.getItemInHand(other).getItem() instanceof BedPlate6MediumPillowItem) {
            return other;
        }
        return null;
    }

    private static InteractionHand handHoldingSmallPillowPreferUsed(Player player, InteractionHand usedHand) {
        if (player.getItemInHand(usedHand).getItem() instanceof BedPlate6SmallPillowItem) {
            return usedHand;
        }
        InteractionHand other =
                usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (player.getItemInHand(other).getItem() instanceof BedPlate6SmallPillowItem) {
            return other;
        }
        return null;
    }
}
