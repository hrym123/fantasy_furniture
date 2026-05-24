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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapBoxAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 肥皂盒：单 id；盖态与盒内有无皂为方块状态，盒内皂数据在方块实体（见设计书 {@code 11-肥皂盒}）。
 */
public class SoapBoxBlock extends GeolibFacingEntityBlockWithFactory<SoapBoxBlockEntity> {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final BooleanProperty HAS_SOAP = BooleanProperty.create("has_soap");
    public static final IntegerProperty MATERIAL =
            IntegerProperty.create("material", 1, SoapBarMaterials.COUNT);

    /** 关盖：{@code geo_collision_box.py} 外接盒（{@code soap_box.geo.json}）。 */
    private static final VoxelShape SHAPE_CLOSED_NORTH = Block.box(3.5, 0.0, 5.0, 12.5, 4.0, 11.0);

    /** 开盖：{@code geo_collision_box.py} 外接盒 X 镜像（盖在 −X），与 Gecko FACING=NORTH 渲染一致。 */
    private static final VoxelShape SHAPE_OPEN_NORTH = Block.box(1.96, 0.0, 5.0, 12.5, 7.97, 11.0);

    public SoapBoxBlock(BlockBehaviour.Properties properties) {
        super(properties, SoapBoxBlockEntity::new);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(OPEN, false)
                        .setValue(HAS_SOAP, false)
                        .setValue(MATERIAL, SoapBoxAppearance.DEFAULT_MATERIAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN, HAS_SOAP, MATERIAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape north = state.getValue(OPEN) ? SHAPE_OPEN_NORTH : SHAPE_CLOSED_NORTH;
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        SoapBoxAppearance box = SoapBoxAppearance.fromStack(stack);
        BlockState placed = state.setValue(MATERIAL, box.boxMaterialId());
        level.setBlock(pos, placed, Block.UPDATE_ALL);
        super.setPlacedBy(level, pos, placed, placer, stack);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        SoapBoxAppearance.writeToStack(stack, SoapBoxAppearance.fromState(state));
        return stack;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack box = new ItemStack(asItem());
        SoapBoxAppearance.writeToStack(box, SoapBoxAppearance.fromState(state));
        return List.of(box);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && state.getValue(HAS_SOAP)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SoapBoxBlockEntity soapBox) {
                Block.popResource(
                        level,
                        pos,
                        SoapBarBlockItem.stackWithAppearance(
                                ModBlocks.SOAP_BAR.item().get(), soapBox.containedSoap()));
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
        boolean open = state.getValue(OPEN);
        boolean hasSoap = state.getValue(HAS_SOAP);
        boolean sneaking = player.isShiftKeyDown();

        if (!open && !hasSoap) {
            level.setBlock(pos, state.setValue(OPEN, true), Block.UPDATE_ALL);
            return InteractionResult.CONSUME;
        }

        if (open && !hasSoap) {
            if (held.isEmpty()) {
                level.setBlock(pos, state.setValue(OPEN, false), Block.UPDATE_ALL);
                return InteractionResult.CONSUME;
            }
            if (held.is(ModBlocks.SOAP_BAR.item().get())) {
                SoapBarAppearance soap = SoapBarAppearance.fromStack(held);
                if (!soap.isFull()) {
                    return InteractionResult.FAIL;
                }
                SoapBoxBlockEntity be = blockEntity(level, pos);
                if (be == null) {
                    return InteractionResult.FAIL;
                }
                be.setContainedSoap(soap);
                level.setBlock(pos, state.setValue(HAS_SOAP, true), Block.UPDATE_ALL);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        if (open && hasSoap && sneaking) {
            SoapBoxBlockEntity be = blockEntity(level, pos);
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

        if (hasSoap && !sneaking) {
            level.setBlock(pos, state.setValue(OPEN, !open), Block.UPDATE_ALL);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    private static SoapBoxBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SoapBoxBlockEntity box ? box : null;
    }
}
