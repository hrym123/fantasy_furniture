package org.lanye.fantasy_furniture.content.soap.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.soap.ShampooAppearance;
import org.lanye.fantasy_furniture.content.soap.ShampooAssets;
import org.lanye.fantasy_furniture.content.soap.ShampooMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackRules;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackUse;
import org.lanye.fantasy_furniture.content.soap.SoapStackCollisionShapes;
import org.lanye.fantasy_furniture.content.soap.blockentity.ShampooBlockEntity;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 洗发露：单瓶用 {@code 洗发露_默认} geo；2 瓶及以上用 {@code 洗发露_堆叠_x4}；可与沐浴露 / 乳霜混合摞放。 */
public final class ShampooBlock extends GeolibFacingEntityBlockWithFactory<ShampooBlockEntity> {

    public static final IntegerProperty LAYERS =
            IntegerProperty.create("layers", 1, ShampooAssets.MAX_STACK);
    public static final IntegerProperty MATERIAL =
            IntegerProperty.create("material", 1, ShampooMaterials.COUNT);

    public ShampooBlock(BlockBehaviour.Properties properties) {
        super(properties, ShampooBlockEntity::new);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LAYERS, 1)
                        .setValue(MATERIAL, ShampooMaterials.DEFAULT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYERS, MATERIAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int layers = state.getValue(LAYERS);
        VoxelShape north;
        BlockEntity raw = level.getBlockEntity(pos);
        if (raw instanceof ShampooBlockEntity be
                && be.layerCount() > 0
                && SoapBottleStackRules.isMixed(be.layersView())) {
            north = SoapStackCollisionShapes.bodyWashNorth(layers);
        } else {
            north = SoapStackCollisionShapes.shampooNorth(layers);
        }
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        ShampooAppearance appearance = ShampooAppearance.fromStack(stack);
        BlockState placed =
                state.setValue(MATERIAL, appearance.materialId()).setValue(LAYERS, 1);
        level.setBlock(pos, placed, Block.UPDATE_ALL);
        super.setPlacedBy(level, pos, placed, placer, stack);
        ShampooBlockEntity be = blockEntity(level, pos);
        if (be != null) {
            be.setSingleLayer(appearance.materialId());
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        ShampooAppearance.writeToStack(stack, new ShampooAppearance(state.getValue(MATERIAL)));
        return stack;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return SoapBottleStackUse.getDrops(state, builder, MATERIAL, builder.getOptionalParameter(
                net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY));
    }

    @Override
    protected InteractionResult onUseClient(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.PASS;
        }
        if (hand == InteractionHand.MAIN_HAND
                && player.getItemInHand(hand).isEmpty()
                && !player.isShiftKeyDown()) {
            ShampooBlockEntity be = blockEntity(level, pos);
            if (be != null) {
                be.triggerUseAnim();
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        InteractionResult stackResult =
                SoapBottleStackUse.onUseServer(
                        state, level, pos, player, hand, hit, this, LAYERS, MATERIAL, level.getBlockEntity(pos));
        if (stackResult != InteractionResult.PASS) {
            return stackResult;
        }
        if (hand == InteractionHand.MAIN_HAND
                && player.getItemInHand(hand).isEmpty()
                && !player.isShiftKeyDown()) {
            ShampooBlockEntity be = blockEntity(level, pos);
            if (be != null) {
                be.triggerUseAnim();
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private static ShampooBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof ShampooBlockEntity stack ? stack : null;
    }
}
