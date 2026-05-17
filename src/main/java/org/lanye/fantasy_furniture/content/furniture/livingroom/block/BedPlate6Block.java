package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.Config;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DecorStorage;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.BedPlate6ClientPick;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6BedDecorRemoval;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6ComponentPick;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;
import org.lanye.reverie_core.util.VoxelShapeTranslation;

/**
 * 床板 6：在 {@link net.minecraft.world.level.block.BedBlock#use} 之前处理拆卸手套、被套、大号、中号、小号、床单。
 * 卸下床品改由主手 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DisassemblyGloveItem} 按准心选中组件拆除（与选取体素同源）。
 * 顺序：<strong>拆卸手套 → 被套 →（主手大号且另一手中号、且床已摆大件且仍可加中号时先放中号）→ 大号 → 中号 → 小号 → 床单</strong>。
 *
 * <p>准心/中键/玉 HUD 选取：客户端由 {@link BedPlate6ClientPick} 读 {@link net.minecraft.client.Minecraft#hitResult}（无 Mixin），见 {@link BedPlate6ComponentPick}。
 *
 * <p>落地弹跳与摔落减免：仅当床尾格 {@link BedPlate6BlockEntity#hasDuvet()} 为真时沿用 {@link BedBlock} 行为；裸床垫无被单时按普通方块受伤、不弹起。
 *
 * <p>寝具存储：数据在床尾格 {@link BedPlate6BlockEntity}；生存破坏时在床尾格散落全部床品（{@link BedPlate6DecorStorage}）。创造模式破坏不掉落床板方块物品。
 */
public final class BedPlate6Block extends BedPlateBlock {

    /** 供 {@link #onRemove} 判断是否为创造模式破坏（与 {@link PlainGlassWindowBlock} 同思路）。 */
    private static final ThreadLocal<Player> BREAKING_PLAYER = new ThreadLocal<>();

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

    /** 仅当床尾格方块实体已铺被单时，沿用 {@link BedBlock} 的落地弹跳与摔落减免。 */
    private static boolean duvetEnablesBedLanding(BlockGetter level, BlockState state, BlockPos pos) {
        if (!state.is(ModBlocks.BED_PLATE6.block().get())) {
            return false;
        }
        var be = level.getBlockEntity(footPos(state, pos));
        return be instanceof BedPlate6BlockEntity plate && plate.hasDuvet();
    }

    private static void landLikeOrdinaryBlock(Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y < 0.0D) {
            entity.setDeltaMovement(motion.x, 0.0D, motion.z);
        }
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        BlockState onState = level.getBlockState(entity.getOnPos());
        if (duvetEnablesBedLanding(level, onState, entity.getOnPos())) {
            super.updateEntityAfterFallOn(level, entity);
        } else {
            landLikeOrdinaryBlock(entity);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (duvetEnablesBedLanding(level, state, pos)) {
            super.fallOn(level, state, pos, entity, fallDistance);
        } else {
            entity.causeFallDamage(fallDistance, 1.0F, level.damageSources().fall());
        }
    }

    public BedPlate6Block(
            BlockBehaviour.Properties properties,
            BlockEntityType.BlockEntitySupplier<? extends BedPlateBaseBlockEntity> entitySupplier) {
        super(properties, entitySupplier);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BREAKING_PLAYER.set(player);
        try {
            super.playerWillDestroy(level, pos, state, player);
        } finally {
            BREAKING_PLAYER.remove();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            Player breaker = BREAKING_PLAYER.get();
            boolean creative = breaker != null && breaker.getAbilities().instabuild;
            BlockPos foot = bedFootWorldPos(state, pos);
            BlockEntity be = level.getBlockEntity(foot);
            if (be instanceof BedPlate6BlockEntity plate && BedPlate6DecorStorage.hasStoredDecor(plate)) {
                if (creative) {
                    BedPlate6DecorStorage.clearAllStoredDecor(plate);
                } else {
                    BedPlate6DecorStorage.spillAllAsWorldDrops(level, foot, plate);
                }
            }
            if (!creative && state.getValue(BedBlock.PART) == BedPart.FOOT) {
                Block.popResource(level, foot, new ItemStack(ModBlocks.BED_PLATE6.item().get()));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    /** 床板掉落改由 {@link #onRemove} 在床尾格手动处理，避免战利品表在创造模式等路径仍掉落。 */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level instanceof Level l && l.isClientSide()) {
            ItemStack picked = BedPlate6ClientPick.resolveCloneItemStack(l, state, pos);
            if (!picked.isEmpty()) {
                return picked;
            }
        }
        return new ItemStack(ModBlocks.BED_PLATE6.item().get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return pickShapeForBedPlate6(
                state, level, pos, super.getShape(state, level, pos, context), context);
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
     * 床头格：与床尾<strong>同一套</strong>选取并集，沿 {@link BedBlock#FACING} 平移 −1 格到床头局部原点，使轮廓 / 射线命中与仅床尾绘制的 Geo 一致（不再仅用 {@link #DUVET_OUTER_BOX} 近似）。
     *
     * <p>客户端且 {@link CollisionContext} 含玩家时：若准心与本床同一锚点，则用 {@link BedPlate6PickShapesNorth#northOutlinePieceNorth} 仅合并当前解析子件体素，避免多枕同亮（T007 #6）。
     */
    private static VoxelShape pickShapeForBedPlate6(
            BlockState state, BlockGetter level, BlockPos pos, VoxelShape base, CollisionContext context) {
        var be = level.getBlockEntity(footPos(state, pos));
        if (!(be instanceof BedPlate6BlockEntity plate) || !plate.hasDuvet()) {
            return base;
        }
        VoxelShape north = BedPlate6PickShapesNorth.unionNorthForPick(plate);
        if (north.isEmpty()) {
            return Shapes.or(base, DUVET_OUTER_BOX);
        }
        Direction facing = state.getValue(BedBlock.FACING);
        VoxelShape orientedFull = applyBedPlateFacingToNorthPick(north, facing);
        if (level instanceof Level lvl
                && lvl.isClientSide()
                && context instanceof EntityCollisionContext ecc
                && ecc.getEntity() instanceof Player) {
            return BedPlate6ClientPick.clientPlayerOutlineShape(lvl, state, pos, base, plate, facing);
        }
        if (state.getValue(BedBlock.PART) != BedPart.FOOT) {
            double tx = -facing.getStepX();
            double ty = -facing.getStepY();
            double tz = -facing.getStepZ();
            return Shapes.or(base, VoxelShapeTranslation.translate(orientedFull, tx, ty, tz));
        }
        return Shapes.or(base, orientedFull);
    }

    private static VoxelShape applyBedPlateFacingToNorthPick(VoxelShape northShape, Direction facing) {
        return BedPlate6PickShapesNorth.orientForBedFacing(northShape, facing);
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
        InteractionResult glove =
                BedPlate6BedDecorRemoval.tryRemoveSelectedWithMainHandGlove(level, state, pos, player, hand, hit);
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
