package org.lanye.fantasy_furniture.content.soap.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapRackBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 肥皂架：单 id；架上有无皂为方块状态，皂数据在方块实体（见设计书 {@code 12-肥皂架}）。
 */
public class SoapRackBlock extends GeolibFacingEntityBlockWithFactory<SoapRackBlockEntity> {

    public static final BooleanProperty HAS_SOAP = BooleanProperty.create("has_soap");

    private static final VoxelShape SHAPE_NORTH = Block.box(4.0, 0.0, 5.5, 12.0, 1.0, 10.5);

    public SoapRackBlock(BlockBehaviour.Properties properties) {
        super(properties, SoapRackBlockEntity::new);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_SOAP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_SOAP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return VoxelShapeRotation.rotateYFromNorth(SHAPE_NORTH, state.getValue(FACING));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of(new ItemStack(asItem()));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && state.getValue(HAS_SOAP)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SoapRackBlockEntity rack) {
                Block.popResource(
                        level,
                        pos,
                        SoapBarBlockItem.stackWithAppearance(
                                ModBlocks.SOAP_BAR.item().get(), rack.containedSoap()));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult onUseServer(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean hasSoap = state.getValue(HAS_SOAP);
        boolean sneaking = player.isShiftKeyDown();

        if (!hasSoap && held.is(ModBlocks.SOAP_BAR.item().get())) {
            SoapRackBlockEntity be = blockEntity(level, pos);
            if (be == null) {
                return InteractionResult.FAIL;
            }
            be.setContainedSoap(SoapBarAppearance.fromStack(held));
            level.setBlock(pos, state.setValue(HAS_SOAP, true), Block.UPDATE_ALL);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.CONSUME;
        }

        if (hasSoap && sneaking) {
            SoapRackBlockEntity be = blockEntity(level, pos);
            if (be == null) {
                return InteractionResult.FAIL;
            }
            ItemStack soap =
                    SoapBarBlockItem.stackWithAppearance(
                            ModBlocks.SOAP_BAR.item().get(), be.containedSoap());
            if (!player.getInventory().add(soap)) {
                player.drop(soap, false);
            }
            be.clearContainedSoap();
            level.setBlock(pos, state.setValue(HAS_SOAP, false), Block.UPDATE_ALL);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @javax.annotation.Nullable
    private static SoapRackBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SoapRackBlockEntity rack ? rack : null;
    }
}
