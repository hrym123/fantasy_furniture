package org.lanye.fantasy_furniture.content.furniture.decor.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.decor.ComputerAppearance;
import org.lanye.fantasy_furniture.content.furniture.decor.ComputerCollisionShapes;
import org.lanye.fantasy_furniture.content.furniture.decor.ComputerMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.ComputerBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.item.ComputerBlockItem;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 电脑（1 型 / 2 型）：四向 + 开合 + 六色材质档；空手右键开合，刷子换色。
 * 打开态发出微光（光照等级 {@link #OPEN_LIGHT_LEVEL}）。
 */
public final class ComputerBlock extends GeolibFacingEntityBlockWithFactory<ComputerBlockEntity> {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final IntegerProperty MATERIAL =
            IntegerProperty.create("material", 1, ComputerMaterials.COUNT);

    /** 打开态方块光照等级（微光）。 */
    public static final int OPEN_LIGHT_LEVEL = 9;

    private final String closedAssetId;

    public ComputerBlock(BlockBehaviour.Properties properties, String closedAssetId) {
        super(properties, ComputerBlockEntity::new);
        this.closedAssetId = closedAssetId;
        registerDefaultState(
                defaultBlockState()
                        .setValue(OPEN, false)
                        .setValue(MATERIAL, ComputerMaterials.DEFAULT));
    }

    /** 关闭态资源 stem（如 {@code computer_1}）；打开态为 {@code stem_open}。 */
    public String closedAssetId() {
        return closedAssetId;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN, MATERIAL);
    }

    private VoxelShape shape(BlockState state) {
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                ComputerCollisionShapes.north(closedAssetId, state.getValue(OPEN)), state.getValue(FACING));
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
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(OPEN) ? OPEN_LIGHT_LEVEL : 0;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        ComputerAppearance appearance = ComputerAppearance.fromStack(stack);
        level.setBlock(pos, state.setValue(MATERIAL, appearance.materialId()), Block.UPDATE_ALL);
    }

    /** 中键选取：同色物品（材质档写入 NBT）。 */
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        ComputerAppearance.writeToStack(stack, new ComputerAppearance(state.getValue(MATERIAL)));
        return stack;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        for (ItemStack stack : drops) {
            if (stack.getItem() instanceof ComputerBlockItem) {
                ComputerAppearance.writeToStack(
                        stack, new ComputerAppearance(state.getValue(MATERIAL)));
            }
        }
        return drops;
    }

    @Override
    protected InteractionResult onUseClient(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.PASS;
        }
        if (!player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.PASS;
        }
        level.setBlock(pos, state.setValue(OPEN, !state.getValue(OPEN)), Block.UPDATE_ALL);
        return InteractionResult.CONSUME;
    }
}
