package org.lanye.fantasy_furniture.content.furniture.decor.block;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.lanye.fantasy_furniture.content.furniture.common.state.PlainGlassWindowMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowShapes;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.PlainGlassWindowBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.client.PlainGlassWindowBlockClientExtensions;
import org.lanye.fantasy_furniture.content.furniture.decor.item.PlainGlassWindowBlockItem;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 0号窗户：{@link #FACING}×{@link #SHAPE}；颜色由<strong>方块注册 id / {@link #variant}</strong>表达（REG-608，无
 * {@code material} 轴）。右键按 {@link PlainGlassWindowShapes#nextShapeInCycle(int)} 切换造型。
 *
 * <p>光照：与玻璃类方块一致，不挡光。
 *
 * <p>碰撞：北向基准与 {@code tools/collision/geo_collision_box.py} 外接盒一致（多数造型）；斜角 45° 使用整格外接盒。
 */
public class PlainGlassWindowBlock extends GeolibFacingEntityBlockWithFactory<PlainGlassWindowBlockEntity> {

    /**
     * {@link #playerWillDestroy} 在方块被替换前调用，{@link #onRemove} 需知是否为创造玩家以抑制掉落（见 T006）。
     */
    private static final ThreadLocal<Player> BREAKING_PLAYER = new ThreadLocal<>();

    public static final IntegerProperty SHAPE =
            IntegerProperty.create("shape", 0, PlainGlassWindowShapes.COUNT - 1);

    private final PlainGlassWindowMaterialVariant variant;

    private static final VoxelShape[] SHAPES_NORTH = {
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.4D), // straight
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.4D, 16.0D), // 90°
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.3581D, 16.0D), // 22.5°
        Block.box(0.0D, 0.0D, 0.205D, 16.0D, 16.5463D, 16.0D), // 45°
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D), // 斜角 45°
    };

    public PlainGlassWindowBlock(
            BlockBehaviour.Properties properties, PlainGlassWindowMaterialVariant variant) {
        super(properties, PlainGlassWindowBlockEntity::new);
        this.variant = variant;
        registerDefaultState(defaultBlockState().setValue(SHAPE, 0));
    }

    public PlainGlassWindowMaterialVariant variant() {
        return variant;
    }

    /** 从方块实例解析材质索引（REG-608：颜色由 block id 表达）。 */
    public static int materialIndex(BlockState state) {
        if (state.getBlock() instanceof PlainGlassWindowBlock window) {
            return window.variant.ordinal();
        }
        return 0;
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        PlainGlassWindowBlockClientExtensions.register(consumer);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHAPE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state);
    }

    private static VoxelShape shapeFor(BlockState state) {
        int s = Mth.clamp(state.getValue(SHAPE), 0, PlainGlassWindowShapes.COUNT - 1);
        VoxelShape north = SHAPES_NORTH[s];
        Direction dir = state.getValue(FACING);
        return switch (dir) {
            case NORTH, SOUTH, EAST, WEST -> VoxelShapeRotation.rotateYFromNorth(north, dir);
            default -> north;
        };
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of();
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BREAKING_PLAYER.set(player);
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        try {
            if (!state.is(newState.getBlock()) && !level.isClientSide) {
                Player p = BREAKING_PLAYER.get();
                boolean creative = p != null && p.getAbilities().instabuild;
                if (!creative) {
                    Block.popResource(level, pos, defaultDropStack(state));
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        } finally {
            BREAKING_PLAYER.remove();
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return stackForState(state);
    }

    /**
     * 破坏掉落：同名 item（REG-608），造型固定为 0（无 {@link PlainGlassWindowBlockItem#TAG_SHAPE}），见 T006。
     */
    private static ItemStack defaultDropStack(BlockState state) {
        return stackForShape(state, 0);
    }

    /** 中键选取：同名 item + 当前造型。 */
    private static ItemStack stackForState(BlockState state) {
        int shape = Mth.clamp(state.getValue(SHAPE), 0, PlainGlassWindowShapes.COUNT - 1);
        return stackForShape(state, shape);
    }

    private static ItemStack stackForShape(BlockState state, int shape) {
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        if (shape != 0) {
            stack.getOrCreateTag().putInt(PlainGlassWindowBlockItem.TAG_SHAPE, shape);
        }
        return stack;
    }

    @Override
    protected InteractionResult onUseClient(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.PASS;
        }
        int s = PlainGlassWindowShapes.nextShapeInCycle(state.getValue(SHAPE));
        level.setBlock(pos, state.setValue(SHAPE, s), Block.UPDATE_ALL_IMMEDIATE);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
