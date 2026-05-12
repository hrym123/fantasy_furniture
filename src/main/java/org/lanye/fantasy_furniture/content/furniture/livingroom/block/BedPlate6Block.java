package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.Config;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6CrosshairPick;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6BedDecorRemoval;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6ComponentPick;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 床板 6：在 {@link net.minecraft.world.level.block.BedBlock#use} 之前处理拆卸手套、被套、大号、中号、小号、床单。
 * 卸下/替换床品改由主手 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DisassemblyGloveItem} 按叠放逆序逐层拆除。
 * 顺序：<strong>拆卸手套 → 被套 →（主手大号且另一手中号、且床已摆大件且仍可加中号时先放中号）→ 大号 → 中号 → 小号 → 床单</strong>。
 *
 * <p>准心/中键/玉 HUD 选取：{@link #getCloneItemStack(BlockGetter, BlockPos, BlockState)} 在客户端结合
 * {@link BedPlate6CrosshairPick} 记录的准心射线，按击中高度映射到当前床品物品，见 {@link BedPlate6ComponentPick}。
 */
public final class BedPlate6Block extends BedPlateBlock {

    /**
     * 被单薄层外接盒（整格 16×16、床垫顶 y=5 起高约 2/16）：用于床头格 {@link #getShape} 回退、以及 {@link #getCollisionShape}（配置开启时）。
     * 床尾格选取以 {@link BedPlate6PickShapesNorth} 的 geo 并集为主。被套无碰撞。
     */
    private static final VoxelShape DUVET_OUTER_BOX = Block.box(0, 5, 0, 16, 7, 16);

    private static BlockPos footPos(BlockState state, BlockPos pos) {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return pos;
        }
        return pos.relative(state.getValue(BedBlock.FACING).getOpposite());
    }

    /** 床尾格世界坐标（与 {@link BedPlate6BlockEntity} 数据所在格一致），供选取与床品逻辑共用。 */
    public static BlockPos bedFootWorldPos(BlockState state, BlockPos anyPartPos) {
        return footPos(state, anyPartPos);
    }

    public BedPlate6Block(
            BlockBehaviour.Properties properties,
            BlockEntityType.BlockEntitySupplier<? extends BedPlateBaseBlockEntity> entitySupplier) {
        super(properties, entitySupplier);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level instanceof Level l && l.isClientSide()) {
            HitResult hit = BedPlate6CrosshairPick.peek();
            if (hit instanceof BlockHitResult bhr && hit.getType() == HitResult.Type.BLOCK) {
                BlockState hitState = level.getBlockState(bhr.getBlockPos());
                // 双人床两格：准心可能在床头、而 Jade / pick 用床尾格（或相反）。仅 pos==击中格会整段失效，改为同一床尾锚点。
                if (hitState.getBlock() == this && state.getBlock() == this) {
                    BlockPos footQueried = bedFootWorldPos(state, pos);
                    BlockPos footHit = bedFootWorldPos(hitState, bhr.getBlockPos());
                    if (footQueried.equals(footHit)) {
                        return BedPlate6ComponentPick.stackForHit(
                                level, state, pos, bhr.getLocation(), bhr.getBlockPos());
                    }
                }
            }
        }
        return new ItemStack(ModBlocks.BED_PLATE6.item().get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return pickShapeForBedPlate6(state, level, pos, super.getShape(state, level, pos, context));
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeWithOptionalDuvetCollision(
                state,
                level,
                pos,
                super.getCollisionShape(state, level, pos, context),
                Config.bedPlate6DuvetCollision());
    }

    /**
     * 床尾格：床垫 + 由 geo 导出的被单/被套/枕头北向并集，再按朝向旋转（与 {@link org.lanye.reverie_core.geolib.client.BedPlateGeoBlockRenderer} 一致补 180° Y）。
     * 床头格：床垫 + 薄被单盒 {@link #DUVET_OUTER_BOX}，避免依赖床尾方块实体上的 geo 并集。
     */
    private static VoxelShape pickShapeForBedPlate6(
            BlockState state, BlockGetter level, BlockPos pos, VoxelShape base) {
        var be = level.getBlockEntity(footPos(state, pos));
        if (!(be instanceof BedPlate6BlockEntity plate) || !plate.hasDuvet()) {
            return base;
        }
        if (state.getValue(BedBlock.PART) != BedPart.FOOT) {
            return Shapes.or(base, DUVET_OUTER_BOX);
        }
        VoxelShape north = BedPlate6PickShapesNorth.unionNorthForPick(plate);
        if (north.isEmpty()) {
            return Shapes.or(base, DUVET_OUTER_BOX);
        }
        Direction facing = state.getValue(BedBlock.FACING);
        VoxelShape oriented = applyBedPlateFacingToNorthPick(north, facing);
        return Shapes.or(base, oriented);
    }

    private static VoxelShape applyBedPlateFacingToNorthPick(VoxelShape northShape, Direction facing) {
        VoxelShape r = VoxelShapeRotation.rotateYFromNorth(northShape, facing);
        if (facing.getAxis() != Direction.Axis.Y) {
            r = VoxelShapeRotation.rotate(r, Rotation.CLOCKWISE_180);
        }
        return r;
    }

    /** 可选并入薄被单碰撞盒（配置开启时），与 geo 选取形独立。 */
    private static VoxelShape shapeWithOptionalDuvetCollision(
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
