package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate1DecorStorage;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate1MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DecorStorage;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate1BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.client.BedPlate1ClientPick;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DisassemblyGloveItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBlock;
import org.lanye.reverie_core.geolib.bed.BedPlateSide;

/**
 * 床板1型：材质档 + <strong>2×2 占地</strong>（床尾×床头 × 左×右）。
 *
 * <p>放置点击为<strong>床尾左</strong>；Geo 绘在<strong>床尾右</strong>（默认 Gecko 北向 X 镜像后向 −X
 * 覆盖左列，避免改 geo 导致物品 display 与 bbmodel 不一致）。碰撞见 {@link BedPlate1CollisionShapes}。
 * 准心 / 中键 / 手套按层选中床体或床单（见客户端 {@code BedPlate1ClientPick}）。
 * 左右两列各为独立睡眠床尾。无床单时不弹跳、不减免摔落伤害。
 */
public final class BedPlate1Block extends BedPlateBlock {

    public static final EnumProperty<BedPlateSide> SIDE = EnumProperty.create("side", BedPlateSide.class);

    private final BedPlate1MaterialVariant variant;

    public BedPlate1Block(BlockBehaviour.Properties properties, BedPlate1MaterialVariant variant) {
        super(properties, BedPlate1BlockEntity::new);
        this.variant = variant;
        registerDefaultState(
                defaultBlockState()
                        .setValue(PART, BedPart.FOOT)
                        .setValue(SIDE, BedPlateSide.LEFT)
                        .setValue(OCCUPIED, false));
    }

    public BedPlate1MaterialVariant variant() {
        return variant;
    }

    /** Geo 绘制锚点（床尾右）。 */
    public static boolean isRenderAnchor(BlockState state) {
        return state.getValue(PART) == BedPart.FOOT && state.getValue(SIDE) == BedPlateSide.RIGHT;
    }

    /** 同列床尾格（睡眠锚点）。 */
    public static BlockPos columnFootPos(BlockState state, BlockPos anyPartPos) {
        if (state.getValue(PART) == BedPart.HEAD) {
            return anyPartPos.relative(state.getValue(FACING).getOpposite());
        }
        return anyPartPos;
    }

    /** 床尾左格（放置点击 / 掉落点）。 */
    public static BlockPos footLeftPos(BlockState state, BlockPos anyPartPos) {
        BlockPos foot = columnFootPos(state, anyPartPos);
        if (state.getValue(SIDE) == BedPlateSide.RIGHT) {
            return foot.relative(state.getValue(FACING).getCounterClockWise());
        }
        return foot;
    }

    /** 床尾右格（Geo 渲染原点）。 */
    public static BlockPos renderAnchorPos(BlockState state, BlockPos anyPartPos) {
        BlockPos foot = columnFootPos(state, anyPartPos);
        if (state.getValue(SIDE) == BedPlateSide.LEFT) {
            return foot.relative(state.getValue(FACING).getClockWise());
        }
        return foot;
    }

    /** 寝具数据所在 BE：优先床尾左（放置格，BlockItem 必建 BE），其次渲染锚点，再扫 2×2。 */
    @Nullable
    public static BedPlate1BlockEntity decorEntity(
            BlockGetter level, BlockState state, BlockPos anyPartPos) {
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();
        BlockPos footLeft = footLeftPos(state, anyPartPos);
        BlockPos[] cells =
                new BlockPos[] {
                    footLeft,
                    renderAnchorPos(state, anyPartPos),
                    footLeft.relative(facing),
                    footLeft.relative(facing).relative(right)
                };
        for (BlockPos cell : cells) {
            if (level.getBlockEntity(cell) instanceof BedPlate1BlockEntity plate) {
                return plate;
            }
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SIDE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BedPlate1BlockEntity plate = decorEntity(level, state, pos);
        boolean hasDuvet = plate != null && plate.hasDuvet();
        boolean hasCover = plate != null && plate.hasCover();
        return BedPlate1CollisionShapes.pickShapeFor(
                state, hasDuvet, hasCover, plate != null ? plate.sheetPillows() : null);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BedPlate1CollisionShapes.bodyShape(state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level instanceof Level l && l.isClientSide()) {
            return BedPlate1ClientPick.resolveCloneItemStack(l, state, pos);
        }
        return new ItemStack(this);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        Direction right = facing.getClockWise();
        BlockPos footLeft = context.getClickedPos();
        BlockPos footRight = footLeft.relative(right);
        BlockPos headLeft = footLeft.relative(facing);
        BlockPos headRight = footRight.relative(facing);
        Level level = context.getLevel();
        for (BlockPos p : new BlockPos[] {footLeft, footRight, headLeft, headRight}) {
            if (!level.getWorldBorder().isWithinBounds(p)) {
                return null;
            }
            if (!level.getBlockState(p).canBeReplaced(context)) {
                return null;
            }
        }
        BlockPos below = footLeft.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return null;
        }
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, BedPart.FOOT)
                .setValue(SIDE, BedPlateSide.LEFT)
                .setValue(OCCUPIED, false);
    }

    @Override
    public void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (state.getValue(PART) != BedPart.FOOT || state.getValue(SIDE) != BedPlateSide.LEFT) {
            return;
        }
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();
        // 不调用 super：原版只铺一格床头；此处铺满 2×2
        level.setBlock(
                pos.relative(right),
                state.setValue(SIDE, BedPlateSide.RIGHT).setValue(PART, BedPart.FOOT),
                Block.UPDATE_ALL);
        level.setBlock(
                pos.relative(facing),
                state.setValue(SIDE, BedPlateSide.LEFT).setValue(PART, BedPart.HEAD),
                Block.UPDATE_ALL);
        level.setBlock(
                pos.relative(facing).relative(right),
                state.setValue(SIDE, BedPlateSide.RIGHT).setValue(PART, BedPart.HEAD),
                Block.UPDATE_ALL);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return isRenderAnchor(state) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 与幻想工作台相同：先落点击格（床尾左）只认地面；其余格依附床尾左。
        // 若要求先有床尾右，BlockItem 在 setPlacedBy 之前会因 canSurvive 失败而放不下。
        if (state.getValue(PART) == BedPart.FOOT && state.getValue(SIDE) == BedPlateSide.LEFT) {
            BlockPos below = pos.below();
            return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
        }
        BlockPos footLeft = footLeftPos(state, pos);
        BlockState atFootLeft = level.getBlockState(footLeft);
        return atFootLeft.is(state.getBlock())
                && atFootLeft.getValue(FACING) == state.getValue(FACING)
                && atFootLeft.getValue(PART) == BedPart.FOOT
                && atFootLeft.getValue(SIDE) == BedPlateSide.LEFT;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of();
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        // 拆卸手套：按准心层卸被套或床单（卸床单连带被套）
        if (hand == InteractionHand.MAIN_HAND
                && player.getItemInHand(hand).getItem() instanceof BedPlate6DisassemblyGloveItem) {
            BedPlate1BlockEntity plate = decorEntity(level, state, pos);
            if (plate == null || !plate.hasDuvet()) {
                return InteractionResult.PASS;
            }
            BedPlate1CollisionShapes.PickedLayer layer =
                    BedPlate1CollisionShapes.pickLayer(
                            state, true, plate.hasCover(), plate.sheetPillows(), hit.getLocation(), pos);
            if (layer == BedPlate1CollisionShapes.PickedLayer.BODY
                    || layer == BedPlate1CollisionShapes.PickedLayer.LARGE_1
                    || layer == BedPlate1CollisionShapes.PickedLayer.LARGE_2
                    || layer == BedPlate1CollisionShapes.PickedLayer.MEDIUM
                    || layer == BedPlate1CollisionShapes.PickedLayer.SMALL) {
                return InteractionResult.PASS;
            }
            if (layer == BedPlate1CollisionShapes.PickedLayer.DUVET_COVER) {
                if (!plate.hasCover()) {
                    return InteractionResult.PASS;
                }
                int coverMat = plate.getCoverMaterialId();
                clearAllCovers(level, state, pos);
                if (!level.isClientSide) {
                    BedPlate6DecorStorage.giveOrDropToPlayer(
                            player, BedPlate6DuvetCoverItem.stackForRegistry(coverMat));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            /* 床单层：连带被套 */
            int duvetMat = plate.getDuvetMaterialId();
            int coverMat = plate.hasCover() ? plate.getCoverMaterialId() : 0;
            clearAllDuvets(level, state, pos);
            if (!level.isClientSide) {
                BedPlate6DecorStorage.giveOrDropToPlayer(
                        player, BedPlate6DuvetItem.stackForRegistry(duvetMat));
                if (coverMat != 0) {
                    BedPlate6DecorStorage.giveOrDropToPlayer(
                            player, BedPlate6DuvetCoverItem.stackForRegistry(coverMat));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
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
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6SmallPillowItem) {
            InteractionResult small = BedPlate6SmallPillowItem.applyToBed(level, pos, state, player, hand);
            if (small != InteractionResult.PASS) {
                return small;
            }
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6DuvetCoverItem) {
            InteractionResult cover = BedPlate6DuvetCoverItem.applyToBed(level, pos, state, player, hand);
            if (cover.consumesAction() || cover == InteractionResult.FAIL) {
                return cover;
            }
        }
        if (player.getItemInHand(hand).getItem() instanceof BedPlate6DuvetItem) {
            InteractionResult duvet = BedPlate6DuvetItem.applyToBed(level, pos, state, player, hand);
            if (duvet.consumesAction() || duvet == InteractionResult.FAIL) {
                return duvet;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos footLeft = footLeftPos(state, pos);
            BedPlate1BlockEntity decor = decorEntity(level, state, pos);
            if (decor != null && BedPlate1DecorStorage.hasStoredDecor(decor)) {
                if (player.getAbilities().instabuild) {
                    BedPlate1DecorStorage.clearAllStoredDecor(decor);
                } else {
                    BedPlate1DecorStorage.spillAllAsWorldDrops(level, footLeft, decor);
                }
            }
            destroySiblings(level, pos, state);
            if (!player.getAbilities().instabuild) {
                Block.popResource(level, footLeft, new ItemStack(this));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    /** 清掉 2×2 上所有格残留的床单（及连带被套）数据。 */
    private static void clearAllDuvets(Level level, BlockState state, BlockPos anyPartPos) {
        forEachDecorPlate(level, state, anyPartPos, BedPlate1BlockEntity::clearDuvet);
    }

    private static void clearAllCovers(Level level, BlockState state, BlockPos anyPartPos) {
        forEachDecorPlate(
                level,
                state,
                anyPartPos,
                plate -> {
                    if (plate.hasCover()) {
                        plate.clearCover();
                    }
                });
    }

    private static void forEachDecorPlate(
            Level level,
            BlockState state,
            BlockPos anyPartPos,
            java.util.function.Consumer<BedPlate1BlockEntity> action) {
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();
        BlockPos footLeft = footLeftPos(state, anyPartPos);
        BlockPos[] cells =
                new BlockPos[] {
                    footLeft,
                    footLeft.relative(right),
                    footLeft.relative(facing),
                    footLeft.relative(facing).relative(right)
                };
        for (BlockPos cell : cells) {
            if (level.getBlockEntity(cell) instanceof BedPlate1BlockEntity plate) {
                action.accept(plate);
            }
        }
    }

    private static void destroySiblings(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();
        BlockPos footLeft = footLeftPos(state, pos);
        BlockPos[] cells =
                new BlockPos[] {
                    footLeft,
                    footLeft.relative(right),
                    footLeft.relative(facing),
                    footLeft.relative(facing).relative(right)
                };
        for (BlockPos cell : cells) {
            if (cell.equals(pos)) {
                continue;
            }
            BlockState cellState = level.getBlockState(cell);
            if (cellState.is(state.getBlock()) && cellState.getValue(FACING) == facing) {
                level.removeBlock(cell, false);
            }
        }
    }

    @Override
    protected boolean enablesSoftLanding(BlockGetter level, BlockState state, BlockPos pos) {
        BedPlate1BlockEntity plate = decorEntity(level, state, pos);
        return plate != null && plate.hasDuvet();
    }
}
