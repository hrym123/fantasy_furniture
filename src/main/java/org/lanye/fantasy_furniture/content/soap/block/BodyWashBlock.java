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
import org.lanye.fantasy_furniture.content.soap.BodyWashAppearance;
import org.lanye.fantasy_furniture.content.soap.BodyWashAssets;
import org.lanye.fantasy_furniture.content.soap.BodyWashMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapStackCollisionShapes;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyWashBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.BodyWashBlockItem;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 沐浴露：单瓶用 {@code 沐浴露_默认} geo；2 瓶及以上用 {@code 沐浴露_堆叠_x4} 按位显示。 */
public final class BodyWashBlock extends GeolibFacingEntityBlockWithFactory<BodyWashBlockEntity> {

    public static final IntegerProperty LAYERS =
            IntegerProperty.create("layers", 1, BodyWashAssets.MAX_STACK);
    public static final IntegerProperty MATERIAL =
            IntegerProperty.create("material", 1, BodyWashMaterials.COUNT);

    public BodyWashBlock(BlockBehaviour.Properties properties) {
        super(properties, BodyWashBlockEntity::new);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LAYERS, 1)
                        .setValue(MATERIAL, BodyWashMaterials.DEFAULT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYERS, MATERIAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape north = SoapStackCollisionShapes.bodyWashNorth(state.getValue(LAYERS));
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        BodyWashAppearance appearance = BodyWashAppearance.fromStack(stack);
        BlockState placed =
                state.setValue(MATERIAL, appearance.materialId()).setValue(LAYERS, 1);
        level.setBlock(pos, placed, Block.UPDATE_ALL);
        super.setPlacedBy(level, pos, placed, placer, stack);
        BodyWashBlockEntity be = blockEntity(level, pos);
        if (be != null) {
            be.setSingleLayer(appearance.materialId());
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        BodyWashAppearance.writeToStack(stack, new BodyWashAppearance(state.getValue(MATERIAL)));
        return stack;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BodyWashBlockEntity be = blockEntity(builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY));
        if (be == null || be.layerCount() == 0) {
            ItemStack fallback = new ItemStack(asItem());
            BodyWashAppearance.writeToStack(
                    fallback, new BodyWashAppearance(state.getValue(MATERIAL)));
            return List.of(fallback);
        }
        List<ItemStack> drops = new ArrayList<>();
        for (int mat : be.layerMaterialsView()) {
            drops.add(BodyWashBlockItem.stackWithMaterial(asItem(), mat));
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
        BodyWashBlockEntity be = blockEntity(level, pos);
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
            ItemStack wash = BodyWashBlockItem.stackWithMaterial(asItem(), popped);
            if (!player.getInventory().add(wash)) {
                player.drop(wash, false);
            }
            if (be.layerCount() == 0) {
                level.removeBlock(pos, false);
            } else {
                syncStateFromEntity(level, pos, state, be);
            }
            return InteractionResult.CONSUME;
        }

        if (held.is(asItem())) {
            BodyWashAppearance wash = BodyWashAppearance.fromStack(held);
            if (be.layerCount() >= BodyWashAssets.MAX_STACK) {
                return InteractionResult.FAIL;
            }
            if (!be.pushLayer(wash.materialId())) {
                return InteractionResult.FAIL;
            }
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            syncStateFromEntity(level, pos, state, be);
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty() && !sneaking) {
            be.onServerUseAnim();
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    static void syncStateFromEntity(Level level, BlockPos pos, BlockState state, BodyWashBlockEntity be) {
        int layers = Math.max(1, be.layerCount());
        level.setBlock(
                pos,
                state.setValue(LAYERS, layers).setValue(MATERIAL, be.topMaterial()),
                Block.UPDATE_ALL);
    }

    @Nullable
    private static BodyWashBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof BodyWashBlockEntity stack ? stack : null;
    }

    @Nullable
    private static BodyWashBlockEntity blockEntity(@Nullable BlockEntity be) {
        return be instanceof BodyWashBlockEntity stack ? stack : null;
    }
}
