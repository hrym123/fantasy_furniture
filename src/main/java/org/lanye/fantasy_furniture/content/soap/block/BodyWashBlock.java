package org.lanye.fantasy_furniture.content.soap.block;

import java.util.List;
import java.util.function.Consumer;
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
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.lanye.fantasy_furniture.content.soap.BodyWashAppearance;
import org.lanye.fantasy_furniture.content.soap.client.SoapColoredBreakClientExtensions;
import org.lanye.fantasy_furniture.content.soap.BodyWashAssets;
import org.lanye.fantasy_furniture.content.soap.BodyWashMaterials;
import org.lanye.fantasy_furniture.content.soap.OrientedVoxelShapes;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackRules;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackUse;
import org.lanye.fantasy_furniture.content.soap.SoapStackCollisionShapes;
import org.lanye.fantasy_furniture.content.soap.blockentity.BodyWashBlockEntity;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;

/** 沐浴露：单瓶用 {@code 沐浴露_默认} geo；2 瓶及以上用 {@code 沐浴露_堆叠_x4}；可与洗发露 / 乳霜混合摞放。 */
public final class BodyWashBlock extends SoapSeriesWaterloggableBlock<BodyWashBlockEntity> {

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
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        SoapColoredBreakClientExtensions.register(consumer);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYERS, MATERIAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int layers = state.getValue(LAYERS);
        BlockEntity raw = level.getBlockEntity(pos);
        if (raw instanceof BodyWashBlockEntity be && be.layerCount() > 0) {
            // 含载体 / 混合摞时须走 BE 合并体素；纯同种无载体仍可用查表，但统一走 BE 以免漏载体碰撞
            if (be.hasCarrier()
                    || SoapBottleStackRules.needsPerLayerStackCollision(
                            be.layersView(), SoapBottleKind.BODY_WASH)) {
                return be.mixedCollisionShape(state.getValue(FACING));
            }
        }
        VoxelShape north = SoapStackCollisionShapes.bodyWashNorth(layers);
        return OrientedVoxelShapes.geckoFromNorth(north, state.getValue(FACING));
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
            BodyWashBlockEntity be = blockEntity(level, pos);
            if (be != null) {
                be.onServerUseAnim();
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private static BodyWashBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof BodyWashBlockEntity stack ? stack : null;
    }
}
