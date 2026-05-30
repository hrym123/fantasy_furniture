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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxAssets;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapPackagingTear;
import org.lanye.fantasy_furniture.content.soap.SoapStackCollisionShapes;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapPaperBoxBlockItem;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 空包装盒摞：最多七层，LIFO；堆叠样式 1 / 2（默认 1）。 */
public final class SoapPaperBoxBlock extends GeolibFacingEntityBlockWithFactory<SoapPaperBoxBlockEntity> {

    public static final IntegerProperty LAYERS =
            IntegerProperty.create("layers", 1, SoapPaperBoxAssets.MAX_STACK);
    public static final IntegerProperty MATERIAL =
            IntegerProperty.create("material", 1, SoapPaperBoxMaterials.COUNT);
    public static final IntegerProperty STACK_STYLE =
            IntegerProperty.create("stack_style", 1, SoapPaperBoxAssets.STACK_STYLE_COUNT);
    public static final BooleanProperty TORN = BooleanProperty.create("torn");

    public SoapPaperBoxBlock(BlockBehaviour.Properties properties) {
        super(properties, SoapPaperBoxBlockEntity::new);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LAYERS, 1)
                        .setValue(MATERIAL, SoapPaperBoxMaterials.DEFAULT)
                        .setValue(STACK_STYLE, SoapPaperBoxAssets.DEFAULT_STACK_STYLE)
                        .setValue(TORN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYERS, MATERIAL, STACK_STYLE, TORN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape north =
                SoapStackCollisionShapes.soapPaperBoxNorth(
                        state.getValue(LAYERS), state.getValue(STACK_STYLE));
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        SoapPaperBoxAppearance appearance = SoapPaperBoxAppearance.fromStack(stack);
        BlockState placed =
                state.setValue(MATERIAL, appearance.materialId()).setValue(LAYERS, 1).setValue(TORN, false);
        level.setBlock(pos, placed, Block.UPDATE_ALL);
        super.setPlacedBy(level, pos, placed, placer, stack);
        SoapPaperBoxBlockEntity be = blockEntity(level, pos);
        if (be != null) {
            be.setSingleLayer(appearance.materialId());
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        SoapPaperBoxAppearance.writeToStack(stack, new SoapPaperBoxAppearance(state.getValue(MATERIAL)));
        return stack;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        SoapPaperBoxBlockEntity be = blockEntity(builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY));
        if (be == null || be.layerCount() == 0) {
            ItemStack fallback = new ItemStack(asItem());
            SoapPaperBoxAppearance.writeToStack(
                    fallback, new SoapPaperBoxAppearance(state.getValue(MATERIAL)));
            return List.of(fallback);
        }
        List<ItemStack> drops = new ArrayList<>();
        for (int mat : be.layerMaterialsView()) {
            drops.add(SoapPaperBoxBlockItem.stackWithMaterial(asItem(), mat));
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
        SoapPaperBoxBlockEntity be = blockEntity(level, pos);
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
            ItemStack box = SoapPaperBoxBlockItem.stackWithMaterial(asItem(), popped);
            if (!player.getInventory().add(box)) {
                player.drop(box, false);
            }
            if (be.layerCount() == 0) {
                level.removeBlock(pos, false);
            } else {
                syncStateFromEntity(level, pos, state, be);
            }
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty() && be.layerCount() == 1) {
            if (state.getValue(TORN)) {
                SoapPackagingTear.restoreTornSingleLayerStack(level, pos, state, TORN);
            } else {
                SoapPackagingTear.beginTearSingleLayerStack(level, pos, state, TORN);
            }
            return InteractionResult.CONSUME;
        }

        if (held.is(asItem())) {
            SoapPaperBoxAppearance box = SoapPaperBoxAppearance.fromStack(held);
            if (be.layerCount() >= SoapPaperBoxAssets.MAX_STACK) {
                return InteractionResult.FAIL;
            }
            if (!be.pushLayer(box.materialId())) {
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

    static void syncStateFromEntity(Level level, BlockPos pos, BlockState state, SoapPaperBoxBlockEntity be) {
        int layers = Math.max(1, be.layerCount());
        BlockState next = state.setValue(LAYERS, layers).setValue(MATERIAL, be.topMaterial());
        if (layers != 1) {
            next = next.setValue(TORN, false);
        }
        level.setBlock(pos, next, Block.UPDATE_ALL);
    }

    @Nullable
    private static SoapPaperBoxBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SoapPaperBoxBlockEntity stack ? stack : null;
    }

    @Nullable
    private static SoapPaperBoxBlockEntity blockEntity(@Nullable BlockEntity be) {
        return be instanceof SoapPaperBoxBlockEntity stack ? stack : null;
    }
}
