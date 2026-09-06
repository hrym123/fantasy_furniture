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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareAppearance;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareCollisionShapes;
import org.lanye.fantasy_furniture.content.furniture.decor.DrinkwareMaterials;
import org.lanye.fantasy_furniture.content.furniture.decor.blockentity.DrinkwareBlockEntity;
import org.lanye.fantasy_furniture.content.furniture.decor.item.DrinkwareBlockItem;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 杯具：四向 + 九色材质档 + 堆叠 1～4。
 * 持同色杯具右击可叠层；刷子换色；柜子走通用槽位。
 */
public final class DrinkwareBlock extends GeolibFacingEntityBlockWithFactory<DrinkwareBlockEntity> {

    public static final IntegerProperty STACK =
            IntegerProperty.create("stack", 1, DrinkwareCollisionShapes.MAX_STACK);
    public static final IntegerProperty MATERIAL =
            IntegerProperty.create("material", 1, DrinkwareMaterials.COUNT);

    public DrinkwareBlock(BlockBehaviour.Properties properties) {
        super(properties, DrinkwareBlockEntity::new);
        registerDefaultState(
                defaultBlockState()
                        .setValue(STACK, 1)
                        .setValue(MATERIAL, DrinkwareMaterials.DEFAULT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STACK, MATERIAL);
    }

    private VoxelShape shape(BlockState state) {
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                DrinkwareCollisionShapes.north(state.getValue(STACK)), state.getValue(FACING));
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        DrinkwareAppearance appearance = DrinkwareAppearance.fromStack(stack);
        level.setBlock(pos, state.setValue(MATERIAL, appearance.materialId()), Block.UPDATE_ALL);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(asItem());
        DrinkwareAppearance.writeToStack(stack, new DrinkwareAppearance(state.getValue(MATERIAL)));
        return stack;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack stack = new ItemStack(asItem(), state.getValue(STACK));
        DrinkwareAppearance.writeToStack(stack, new DrinkwareAppearance(state.getValue(MATERIAL)));
        return List.of(stack);
    }

    @Override
    protected InteractionResult onUseClient(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.SUCCESS;
        }
        if (canStackOnto(state, player.getItemInHand(hand))) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!canStackOnto(state, held)) {
            return InteractionResult.PASS;
        }
        int next = state.getValue(STACK) + 1;
        level.setBlock(pos, state.setValue(STACK, next), Block.UPDATE_ALL);
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    private static boolean canStackOnto(BlockState state, ItemStack held) {
        if (!(held.getItem() instanceof DrinkwareBlockItem)) {
            return false;
        }
        if (state.getValue(STACK) >= DrinkwareCollisionShapes.MAX_STACK) {
            return false;
        }
        int heldMat = DrinkwareAppearance.fromStack(held).materialId();
        return heldMat == state.getValue(MATERIAL);
    }
}
