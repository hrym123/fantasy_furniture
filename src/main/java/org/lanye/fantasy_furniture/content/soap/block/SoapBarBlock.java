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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapBarMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapBarWear;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBagMaterials;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 肥皂：可放置于地面；磨损档决定碰撞与 geo，颜料档决定贴图（见设计书 {@code 02-肥皂与包装}）。
 */
public class SoapBarBlock extends GeolibFacingEntityBlockWithFactory<SoapBarBlockEntity> {

    public static final IntegerProperty WEAR = IntegerProperty.create("wear", 0, 2);
    public static final IntegerProperty MATERIAL = IntegerProperty.create("material", 1, SoapBarMaterials.COUNT);
    public static final BooleanProperty PACKAGED = BooleanProperty.create("packaged");
    public static final IntegerProperty BAG_MATERIAL =
            IntegerProperty.create("bag_material", 0, SoapBarMaterials.COUNT);

    private static final VoxelShape SHAPE_FULL_NORTH = Block.box(5.0, 0.0, 6.3, 11.0, 2.0, 9.7);
    private static final VoxelShape SHAPE_USED_ONCE_NORTH = Block.box(6.0, 0.0, 6.8, 10.0, 1.4, 9.2);
    private static final VoxelShape SHAPE_USED_TWICE_NORTH = Block.box(7.0, 0.0, 7.3, 9.0, 0.8, 8.7);

    public SoapBarBlock(BlockBehaviour.Properties properties) {
        super(properties, SoapBarBlockEntity::new);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(WEAR, SoapBarAppearance.DEFAULT_WEAR)
                        .setValue(MATERIAL, SoapBarAppearance.DEFAULT_MATERIAL)
                        .setValue(PACKAGED, false)
                        .setValue(BAG_MATERIAL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WEAR, MATERIAL, PACKAGED, BAG_MATERIAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape north =
                switch (SoapBarWear.clamp(state.getValue(WEAR))) {
                    case 1 -> SHAPE_USED_ONCE_NORTH;
                    case 2 -> SHAPE_USED_TWICE_NORTH;
                    default -> SHAPE_FULL_NORTH;
                };
        return VoxelShapeRotation.rotateYFromNorth(north, state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        SoapBarAppearance appearance = SoapBarAppearance.fromStack(stack);
        BlockState placed =
                state.setValue(WEAR, appearance.wear())
                        .setValue(MATERIAL, appearance.materialId())
                        .setValue(PACKAGED, appearance.isPackaged())
                        .setValue(BAG_MATERIAL, appearance.bagMaterialId());
        level.setBlock(pos, placed, Block.UPDATE_ALL);
        super.setPlacedBy(level, pos, placed, placer, stack);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        SoapBarAppearance.writeToStack(stack, SoapBarAppearance.fromState(state));
        return stack;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(asItem());
        SoapBarAppearance.writeToStack(stack, SoapBarAppearance.fromState(state));
        return List.of(stack);
    }

    @Override
    protected InteractionResult onUseClient(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
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
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean packaged = state.getValue(PACKAGED);

        if (!packaged && held.is(ModBlocks.SOAP_PAPER_BAG.item().get())) {
            SoapPaperBagAppearance bag = SoapPaperBagAppearance.fromStack(held);
            if (!SoapPaperBagMaterials.isPlayable(bag.bagMaterialId())) {
                return InteractionResult.FAIL;
            }
            BlockState wrapped =
                    state.setValue(PACKAGED, true).setValue(BAG_MATERIAL, bag.bagMaterialId());
            level.setBlock(pos, wrapped, Block.UPDATE_ALL);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.CONSUME;
        }

        if (packaged && held.isEmpty()) {
            ItemStack soap =
                    SoapBarBlockItem.stackWithAppearance(
                            ModBlocks.SOAP_BAR.item().get(), SoapBarAppearance.fromState(state));
            if (!player.getInventory().add(soap)) {
                player.drop(soap, false);
            }
            level.removeBlock(pos, false);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    /**
     * 入水消耗等玩法推进磨损；颜料不变。磨损为 {@code 2} 后再调用则移除方块。
     */
    public static void advanceWear(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof SoapBarBlock)) {
            return;
        }
        int wear = state.getValue(WEAR);
        if (wear < 2) {
            level.setBlock(pos, state.setValue(WEAR, wear + 1), Block.UPDATE_ALL);
        } else {
            level.removeBlock(pos, false);
        }
    }
}
