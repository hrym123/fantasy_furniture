package org.lanye.fantasy_furniture.content.furniture.decor.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.nbt.CompoundTag;
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
 * {@link PlainGlassWindowMaterials#count()} &gt; 1 时存在于 BlockState。贴图按材质序数选用。右键循环切换造型。
 *
 * <p>光照：与玻璃类方块一致，不挡光（{@link #getLightBlock} 为 0、允许天光竖直向下传播）。
 *
 * <p>碰撞：各 {@link #SHAPE} 对应 geo 在单格内裁切后的<strong>最小外接轴对齐盒</strong>（北向基准，与
 * {@code tools/geo_collision_box.py} 默认输出一致；改模后请对
 * {@code assets/.../geo/block/plain_glass_window_shape_*.geo.json} 重跑脚本更新数值）。随 {@link #FACING} 经
 * {@link VoxelShapeRotation#rotateYFromNorth} 旋转。
 */
public class PlainGlassWindowBlock extends GeolibFacingEntityBlockWithFactory<PlainGlassWindowBlockEntity> {

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
     * 北向基准外接盒，索引与 {@link PlainGlassWindowShapes#geoBasename(int)} 顺序一致。
     * 由 {@code python tools/geo_collision_box.py src/.../geo/block/<basename>.geo.json} 生成。
     */
    private static final VoxelShape[] SHAPES_NORTH = {
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.4D), // straight
        Block.box(0.0D, 0.7D, 0.0D, 16.0D, 16.7D, 0.7D), // 90°
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 17.6D, 0.4D), // 22.5°
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 22.7D, 1.4D), // 45°（模型超高，y 穿出单格）
        Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.4D), // diag45
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            popResource(level, pos, stackForState(state));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return stackForState(state);
    }

    private static ItemStack stackForState(BlockState state) {
        int m = materialIndex(state);
        Item item =
                ForgeRegistries.ITEMS.getValue(
                        ResourceLocation.fromNamespaceAndPath(
                                FantasyFurniture.MODID,
                                "plain_glass_window_" + PlainGlassWindowMaterials.itemSuffix(m)));
        if (item == null) {
            item = ModBlocks.PLAIN_GLASS_WINDOW.item().get();
        }
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(PlainGlassWindowBlockItem.TAG_SHAPE, state.getValue(SHAPE));
        return stack;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int s = (state.getValue(SHAPE) + 1) % PlainGlassWindowShapes.COUNT;
        level.setBlock(pos, state.setValue(SHAPE, s), Block.UPDATE_ALL_IMMEDIATE);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
