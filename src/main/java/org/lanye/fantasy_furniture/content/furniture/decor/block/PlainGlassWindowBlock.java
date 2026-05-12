package org.lanye.fantasy_furniture.content.furniture.decor.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.common.state.PlainGlassWindowMaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.PlainGlassWindowShapes;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.PlainGlassWindowBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.item.PlainGlassWindowBlockItem;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;
import org.jetbrains.annotations.Nullable;

/**
 * 普通玻璃窗：{@link #FACING}×{@link #SHAPE} 必选；{@link #MATERIAL}（颜色名枚举）仅在
 * {@link PlainGlassWindowMaterials#count()} &gt; 1 时存在于 BlockState。贴图按材质序数选用。右键按
 * {@link PlainGlassWindowShapes#nextShapeInCycle(int)} 顺序切换造型。
 *
 * <p>光照：与玻璃类方块一致，不挡光（{@link #getLightBlock} 为 0、允许天光竖直向下传播）。
 *
 * <p>碰撞：北向基准与 {@code tools/collision/geo_collision_box.py} 外接盒一致（多数造型）；斜角 45° 使用<strong>整格外接盒</strong>
 * {@code Block.box(0,0,0,16,16,16)}，与单格线框及「占满一格」的交互预期一致。随 {@link #FACING} 经
 * {@link VoxelShapeRotation#rotateYFromNorth} 旋转。
 */
public class PlainGlassWindowBlock extends GeolibFacingEntityBlockWithFactory<PlainGlassWindowBlockEntity> {

    /**
     * {@link #playerWillDestroy} 在方块被替换前调用，{@link #onRemove} 需知是否为创造玩家以抑制掉落（见 T006）。
     * 非玩家破坏（爆炸等）下为 {@code null}，仍应掉落默认物品。
     */
    private static final ThreadLocal<Player> BREAKING_PLAYER = new ThreadLocal<>();

    public static final IntegerProperty SHAPE =
            IntegerProperty.create("shape", 0, PlainGlassWindowShapes.COUNT - 1);

    /**
     * 多材质套时注册；仅一种材质时为 {@code null}（世界中恒视为材质索引 0）。
     */
    @Nullable
    public static final EnumProperty<PlainGlassWindowMaterialVariant> MATERIAL = createMaterialProperty();

    @Nullable
    private static EnumProperty<PlainGlassWindowMaterialVariant> createMaterialProperty() {
        return PlainGlassWindowMaterials.count() > 1
                ? EnumProperty.create("material", PlainGlassWindowMaterialVariant.class)
                : null;
    }

    /** 从方块状态解析材质索引；单材质且无 {@link #MATERIAL} 属性时恒为 0。 */
    public static int materialIndex(BlockState state) {
        return MATERIAL != null ? state.getValue(MATERIAL).ordinal() : 0;
    }

    /**
     * 北向基准碰撞，索引与 {@link PlainGlassWindowShapes#geoBasename(int)} 一致。
     * 造型 0～3 与 {@code tools/collision/geo_collision_box.py} 外接盒一致；斜角 45°（索引 4）为整格 {@code 16³} 外接盒。
     */
    private static final VoxelShape[] SHAPES_NORTH = {
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.4D), // straight
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.4D, 16.0D), // 90°
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.3819D, 16.0D), // 22.5°
        Block.box(0.0D, 0.205D, 0.0D, 16.0D, 17.2463D, 16.0D), // 45°
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D), // 斜角 45°：整格外接盒（单格立方）
    };

    public PlainGlassWindowBlock(BlockBehaviour.Properties properties) {
        super(properties, PlainGlassWindowBlockEntity::new);
        BlockState def = defaultBlockState().setValue(SHAPE, 0);
        if (MATERIAL != null) {
            def = def.setValue(MATERIAL, PlainGlassWindowMaterialVariant.WHITE);
        }
        registerDefaultState(def);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHAPE);
        if (MATERIAL != null) {
            builder.add(MATERIAL);
        }
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
     * 破坏、爆炸等非创造掉落：保留被拆方块 {@linkplain #materialIndex 材质}，造型固定为 0（默认模型、无
     * {@link PlainGlassWindowBlockItem#TAG_SHAPE}），与 T006 口径一致。
     */
    private static ItemStack defaultDropStack(BlockState state) {
        return stackForMaterialAndShape(materialIndex(state), 0);
    }

    /** 选取方块（Ctrl 中键等）仍反映当前材质与造型。 */
    private static ItemStack stackForState(BlockState state) {
        int shape = Mth.clamp(state.getValue(SHAPE), 0, PlainGlassWindowShapes.COUNT - 1);
        return stackForMaterialAndShape(materialIndex(state), shape);
    }

    private static ItemStack stackForMaterialAndShape(int materialIndex, int shape) {
        Item item =
                ForgeRegistries.ITEMS.getValue(
                        ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID,
                                "plain_glass_window_" + PlainGlassWindowMaterials.itemSuffix(materialIndex)));
        if (item == null) {
            item = ModBlocks.PLAIN_GLASS_WINDOW.item().get();
        }
        ItemStack stack = new ItemStack(item);
        // 与创造栏默认物品一致：造型 0 不写 NBT，否则无法与无标签堆叠
        if (shape != 0) {
            stack.getOrCreateTag().putInt(PlainGlassWindowBlockItem.TAG_SHAPE, shape);
        }
        return stack;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int s = PlainGlassWindowShapes.nextShapeInCycle(state.getValue(SHAPE));
        level.setBlock(pos, state.setValue(SHAPE, s), Block.UPDATE_ALL_IMMEDIATE);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
