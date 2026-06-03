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
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.BodyWashAppearance;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAppearance;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetAssets;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetBottleKind;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetCollisionShapes;
import org.lanye.fantasy_furniture.content.soap.DisplayCabinetStoredBottle;
import org.lanye.fantasy_furniture.content.soap.ShampooAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapPaperBoxMaterials;
import org.lanye.fantasy_furniture.content.soap.blockentity.DisplayCabinetBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.BodyWashBlockItem;
import org.lanye.fantasy_furniture.content.soap.item.ShampooBlockItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.geolib.GeolibFacingEntityBlockWithFactory;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/** 陈列柜：空柜仅渲染柜体；打开时可放入沐浴露 / 洗发露（合计最多 2 瓶，仅此二类）。 */
public final class DisplayCabinetBlock extends GeolibFacingEntityBlockWithFactory<DisplayCabinetBlockEntity> {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final IntegerProperty MATERIAL =
            IntegerProperty.create("material", 1, SoapPaperBoxMaterials.COUNT);

    public DisplayCabinetBlock(BlockBehaviour.Properties properties) {
        super(properties, DisplayCabinetBlockEntity::new);
        registerDefaultState(
                stateDefinition
                        .any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(OPEN, false)
                        .setValue(MATERIAL, SoapPaperBoxMaterials.DEFAULT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN, MATERIAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return outlineShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return outlineShape(state);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return outlineShape(state);
    }

    /** 关盒全量外形；打开态排除 {@code door} 骨骼区域。 */
    private static VoxelShape outlineShape(BlockState state) {
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                DisplayCabinetCollisionShapes.northForOpen(state.getValue(OPEN)),
                state.getValue(FACING));
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack) {
        DisplayCabinetAppearance appearance = DisplayCabinetAppearance.fromStack(stack);
        BlockState placed = state.setValue(MATERIAL, appearance.materialId());
        level.setBlock(pos, placed, Block.UPDATE_ALL);
        super.setPlacedBy(level, pos, placed, placer, stack);
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
        DisplayCabinetBlockEntity be = blockEntity(level, pos);
        if (be == null) {
            return InteractionResult.FAIL;
        }
        ItemStack held = player.getItemInHand(hand);

        if (state.getValue(OPEN)) {
            if (held.is(ModBlocks.BODY_WASH.item().get())) {
                return tryInsertBottle(
                        level,
                        pos,
                        state,
                        be,
                        player,
                        held,
                        new DisplayCabinetStoredBottle(
                                DisplayCabinetBottleKind.BODY_WASH,
                                BodyWashAppearance.fromStack(held).materialId()));
            }
            if (held.is(ModBlocks.SHAMPOO.item().get())) {
                return tryInsertBottle(
                        level,
                        pos,
                        state,
                        be,
                        player,
                        held,
                        new DisplayCabinetStoredBottle(
                                DisplayCabinetBottleKind.SHAMPOO,
                                ShampooAppearance.fromStack(held).materialId()));
            }
            if (held.isEmpty()) {
                if (player.isShiftKeyDown() && be.bottleCount() > 0) {
                    return tryPopBottle(level, pos, state, be, player);
                }
                toggleOpen(level, pos, state, false);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        if (!held.isEmpty()) {
            return InteractionResult.PASS;
        }
        toggleOpen(level, pos, state, true);
        return InteractionResult.CONSUME;
    }

    private static InteractionResult tryInsertBottle(
            Level level,
            BlockPos pos,
            BlockState state,
            DisplayCabinetBlockEntity be,
            Player player,
            ItemStack held,
            DisplayCabinetStoredBottle bottle) {
        if (be.bottleCount() >= DisplayCabinetAssets.MAX_BOTTLES) {
            return InteractionResult.FAIL;
        }
        if (!be.pushBottle(bottle)) {
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        be.setChanged();
        level.blockEntityChanged(pos);
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        level.playSound(
                null,
                pos,
                SoundEvents.BOTTLE_FILL,
                SoundSource.BLOCKS,
                0.8f,
                1.0f);
        return InteractionResult.CONSUME;
    }

    private static InteractionResult tryPopBottle(
            Level level,
            BlockPos pos,
            BlockState state,
            DisplayCabinetBlockEntity be,
            Player player) {
        DisplayCabinetStoredBottle bottle = be.popBottle();
        if (bottle == null) {
            return InteractionResult.FAIL;
        }
        ItemStack stack =
                switch (bottle.kind()) {
                    case BODY_WASH ->
                            BodyWashBlockItem.stackWithMaterial(
                                    ModBlocks.BODY_WASH.item().get(), bottle.materialId());
                    case SHAMPOO ->
                            ShampooBlockItem.stackWithMaterial(
                                    ModBlocks.SHAMPOO.item().get(), bottle.materialId());
                };
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        be.setChanged();
        level.blockEntityChanged(pos);
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        level.playSound(
                null,
                pos,
                SoundEvents.BOTTLE_EMPTY,
                SoundSource.BLOCKS,
                0.8f,
                1.0f);
        return InteractionResult.CONSUME;
    }

    private static void toggleOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        if (state.getValue(OPEN) == open) {
            return;
        }
        level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
        level.playSound(
                null,
                pos,
                open ? SoundEvents.WET_GRASS_BREAK : SoundEvents.WET_GRASS_PLACE,
                SoundSource.BLOCKS,
                0.8f,
                1.0f);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        ItemStack cabinet = new ItemStack(asItem());
        DisplayCabinetAppearance.writeToStack(
                cabinet, new DisplayCabinetAppearance(state.getValue(MATERIAL)));
        drops.add(cabinet);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        DisplayCabinetBlockEntity be =
                blockEntity instanceof DisplayCabinetBlockEntity entity ? entity : null;
        if (be == null) {
            return drops;
        }
        for (DisplayCabinetStoredBottle bottle : be.bottlesView()) {
            drops.add(
                    switch (bottle.kind()) {
                        case BODY_WASH ->
                                BodyWashBlockItem.stackWithMaterial(
                                        ModBlocks.BODY_WASH.item().get(), bottle.materialId());
                        case SHAMPOO ->
                                ShampooBlockItem.stackWithMaterial(
                                        ModBlocks.SHAMPOO.item().get(), bottle.materialId());
                    });
        }
        return drops;
    }

    @Nullable
    private static DisplayCabinetBlockEntity blockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof DisplayCabinetBlockEntity cabinet ? cabinet : null;
    }
}
