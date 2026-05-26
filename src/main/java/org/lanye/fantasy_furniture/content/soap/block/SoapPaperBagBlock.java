package org.lanye.fantasy_furniture.content.soap.block;

import java.util.ArrayList;
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
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagMaterials;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBagBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBagBlockItem;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 空包装袋摞：最多四层，LIFO（见设计书 {@code 13-包装袋}）。 */
public class SoapPaperBagBlock extends GeolibFacingEntityBlockWithFactory<SoapPaperBagBlockEntity> {

    public static final int MAX_LAYERS = 4;

    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, MAX_LAYERS);
    public static final IntegerProperty MATERIAL =
            IntegerProperty.create("material", 1, SoapPaperBagMaterials.COUNT);

    private static final VoxelShape SHAPE_ONE_NORTH = Block.box(3.0, 0.0, 4.0, 13.0, 2.5, 12.0);
    private static final VoxelShape SHAPE_STACK_NORTH = Block.box(2.0, 0.0, 2.0, 14.0, 3.5, 14.0);

    public SoapPaperBagBlock(BlockBehaviour.Properties properties) {
        super(properties, SoapPaperBagBlockEntity::new);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LAYERS, 1)
                        .setValue(MATERIAL, SoapPaperBagAppearance.defaults().bagMaterialId()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYERS, MATERIAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape north = state.getValue(LAYERS) <= 1 ? SHAPE_ONE_NORTH : SHAPE_STACK_NORTH;
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        SoapPaperBagAppearance appearance = SoapPaperBagAppearance.fromStack(stack);
        BlockState placed =
                state.setValue(MATERIAL, appearance.bagMaterialId()).setValue(LAYERS, 1);
        level.setBlock(pos, placed, Block.UPDATE_ALL);
        super.setPlacedBy(level, pos, placed, placer, stack);
        SoapPaperBagBlockEntity be = blockEntity(level, pos);
        if (be != null) {
            be.setSingleLayer(appearance.bagMaterialId());
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        SoapPaperBagAppearance.writeToStack(stack, new SoapPaperBagAppearance(state.getValue(MATERIAL)));
        return stack;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        SoapPaperBagBlockEntity be = blockEntity(builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY));
        if (be == null || be.layerCount() == 0) {
            ItemStack fallback = new ItemStack(asItem());
            SoapPaperBagAppearance.writeToStack(
                    fallback, new SoapPaperBagAppearance(state.getValue(MATERIAL)));
            return List.of(fallback);
        }
        List<ItemStack> drops = new ArrayList<>();
        for (int mat : be.layerMaterialsView()) {
            drops.add(SoapPaperBagBlockItem.stackWithBagMaterial(asItem(), mat));
        }
        return drops;
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
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.PASS;
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        SoapPaperBagBlockEntity be = blockEntity(level, pos);
        if (be == null) {
            return InteractionResult.FAIL;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean sneaking = player.isShiftKeyDown();

        if (sneaking) {
            if (!held.isEmpty() && !held.is(asItem())) {
                return InteractionResult.PASS;
            }
            Integer popped = be.popTopLayer();
            if (popped == null) {
                return InteractionResult.FAIL;
            }
            ItemStack bag = SoapPaperBagBlockItem.stackWithBagMaterial(asItem(), popped);
            if (!player.getInventory().add(bag)) {
                player.drop(bag, false);
            }
            if (be.layerCount() == 0) {
                level.removeBlock(pos, false);
            } else {
                syncStateFromEntity(level, pos, state, be);
            }
            return InteractionResult.CONSUME;
        }

        if (held.is(asItem())) {
            SoapPaperBagAppearance bag = SoapPaperBagAppearance.fromStack(held);
            if (!SoapPaperBagMaterials.isPlayable(bag.bagMaterialId())) {
                return InteractionResult.FAIL;
            }
            if (be.layerCount() >= MAX_LAYERS) {
                return InteractionResult.FAIL;
            }
            if (!be.pushLayer(bag.bagMaterialId())) {
                return InteractionResult.FAIL;
            }
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            syncStateFromEntity(level, pos, state, be);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    static void syncStateFromEntity(Level level, BlockPos pos, BlockState state, SoapPaperBagBlockEntity be) {
        int layers = Math.max(1, be.layerCount());
        level.setBlock(
                pos,
                state.setValue(LAYERS, layers).setValue(MATERIAL, be.topMaterial()),
                Block.UPDATE_ALL);
    }

    @Nullable
    private static SoapPaperBagBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SoapPaperBagBlockEntity stack ? stack : null;
    }

    @Nullable
    private static SoapPaperBagBlockEntity blockEntity(@Nullable BlockEntity be) {
        return be instanceof SoapPaperBagBlockEntity stack ? stack : null;
    }
}
