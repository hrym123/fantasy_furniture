package org.lanye.fantasy_furniture.content.soap.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapMoldBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldIngredients;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldPhase;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 肥皂模具：逐项右击装料、混合凝固与取皂（见 {@code soap_mold.gameplay.md}）。 */
public class SoapMoldBlock extends GeolibFacingEntityBlockWithFactory<SoapMoldBlockEntity> {

    public static final IntegerProperty FILL_LEVEL = IntegerProperty.create("fill_level", 0, 4);

    /** geo/block/soap_mold.geo.json --gecko-block --exclude-bones group3（盆体 group2，不含动画压杆） */
    public static final VoxelShape SHAPE_NORTH = Block.box(4.2, 0.0, 7.0, 11.8, 4.0, 12.0);

    public SoapMoldBlock(BlockBehaviour.Properties properties) {
        super(properties, SoapMoldBlockEntity::new);
        registerDefaultState(stateDefinition.any().setValue(FILL_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FILL_LEVEL);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : BaseEntityBlock.createTickerHelper(
                        type, ModBlocks.SOAP_MOLD.blockEntityType().get(), SoapMoldBlock::serverTick);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(SHAPE_NORTH, state.getValue(FACING));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of(new ItemStack(asItem()));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            SoapMoldBlockEntity be = blockEntity(level, pos);
            if (be != null) {
                if (be.contents().phase() == SoapMoldPhase.READY) {
                    ItemStack soap = be.createSoapStack();
                    if (!soap.isEmpty()) {
                        Block.popResource(level, pos, soap);
                    }
                } else {
                    for (ItemStack drop : be.buildIngredientDrops()) {
                        Block.popResource(level, pos, drop);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
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

        SoapMoldBlockEntity be = blockEntity(level, pos);
        if (be == null) {
            return InteractionResult.FAIL;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean sneaking = player.isShiftKeyDown();
        boolean liquidVessel = SoapMoldIngredients.isLiquidVessel(held);
        boolean insertable = SoapMoldIngredients.matchInsert(held).isPresent();
        SoapMoldPhase phase = be.contents().phase();

        if (sneaking && (held.isEmpty() || (!insertable && !liquidVessel))) {
            ItemStack popped = be.tryPopLastIngredient();
            if (popped != null) {
                giveOrDrop(player, popped);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }

        if (liquidVessel && be.contents().hasLiquid()) {
            ItemStack liquid = be.tryExtractLiquidIngredient();
            if (liquid != null) {
                giveOrDrop(player, liquid);
                return InteractionResult.CONSUME;
            }
        }

        if (held.is(Items.BUCKET) && be.contents().hasWater() && !be.contents().hasLiquid()) {
            ItemStack water = be.tryExtractWater();
            if (water != null) {
                giveOrDrop(player, water);
                return InteractionResult.CONSUME;
            }
        }

        if (phase == SoapMoldPhase.READY) {
            SoapBarAppearance soap = be.tryTakeSoap();
            if (soap != null) {
                giveOrDrop(
                        player,
                        SoapBarBlockItem.stackWithAppearance(ModBlocks.SOAP_BAR.item().get(), soap));
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }

        if (phase == SoapMoldPhase.READY_TO_MIX && held.isEmpty() && !sneaking) {
            if (be.tryStartMixing(level.getGameTime())) {
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }

        if (phase == SoapMoldPhase.CURING) {
            return InteractionResult.PASS;
        }

        if (phase == SoapMoldPhase.EMPTY || phase == SoapMoldPhase.FILLING) {
            return SoapMoldIngredients.matchInsert(held)
                    .filter(match -> be.tryInsert(match))
                    .map(
                            match -> {
                                if (!player.getAbilities().instabuild) {
                                    held.shrink(1);
                                }
                                if (match.slot() == org.lanye.fantasy_furniture.content.soap.mold.SoapMoldIngredientSlot.WATER) {
                                    giveOrDrop(player, new ItemStack(Items.BUCKET));
                                }
                                return InteractionResult.CONSUME;
                            })
                    .orElse(InteractionResult.FAIL);
        }

        return InteractionResult.FAIL;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    @Nullable
    private static SoapMoldBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SoapMoldBlockEntity mold ? mold : null;
    }

    /** {@link SoapMoldBlockEntity#serverTick} 服务端 tick 入口。 */
    public static void serverTick(Level level, BlockPos pos, BlockState state, SoapMoldBlockEntity be) {
        if (level instanceof ServerLevel serverLevel) {
            be.serverTick(serverLevel);
        }
    }
}
