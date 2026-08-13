package org.lanye.fantasy_furniture.content.soap;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.composite.CompositePartId;
import org.lanye.reverie_core.composite.PartHitHelpers;

/** 沐浴露 / 洗发露 / 乳霜混合摞：分件选取取出 / 破坏；可叠优先；其余走命中件交互。 */
public final class SoapBottleStackUse {

    private SoapBottleStackUse() {}

    public interface Holder {
        SoapBottleStackData stackData();

        void markStackChanged();

        @Nullable
        default SoapStackCarrierKind carrierKind() {
            return stackData().carrier();
        }

        default boolean carrierIntermediate() {
            return stackData().carrierIntermediate();
        }
    }

    public static InteractionResult onUseServer(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            Block hostBlock,
            IntegerProperty layersProperty,
            IntegerProperty materialProperty,
            @Nullable BlockEntity blockEntity) {
        if (BrushRecolor.defersBlockUse(player, hand, state)) {
            return InteractionResult.PASS;
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!(blockEntity instanceof Holder holder)) {
            return InteractionResult.FAIL;
        }
        SoapBottleStackData stack = holder.stackData();
        ItemStack held = player.getItemInHand(hand);
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        boolean sneaking = player.isShiftKeyDown();

        if (sneaking) {
            if (!held.isEmpty()
                    && !SoapBottleKind.isSoapBottleItem(held)
                    && SoapStackCarrierKind.fromItem(held) == null) {
                return InteractionResult.PASS;
            }
            CompositePartId part =
                    PartHitHelpers.resolveHitPart(hit, facing, SoapBottlePartPicks.entries(stack));
            if (part == null) {
                part = fallbackTopOrCarrier(stack);
            }
            return popHitPart(level, pos, state, player, holder, stack, part, layersProperty, materialProperty);
        }

        // 可叠优先：架/盒 / 瓶
        SoapStackCarrierKind heldCarrier = SoapStackCarrierKind.fromItem(held);
        if (heldCarrier != null && SoapBottleStackRules.canAcceptCarrier(stack, heldCarrier)) {
            int boxMat =
                    heldCarrier == SoapStackCarrierKind.BOX
                            ? SoapBoxAppearance.fromStack(held).boxMaterialId()
                            : SoapBoxAppearance.DEFAULT_MATERIAL;
            if (!stack.tryPushCarrier(heldCarrier, boxMat)) {
                return InteractionResult.FAIL;
            }
            holder.markStackChanged();
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            syncState(level, pos, state, stack, layersProperty, materialProperty);
            return InteractionResult.CONSUME;
        }

        SoapBottleKind heldKind = SoapBottleKind.fromItem(held);
        if (heldKind != null) {
            int materialId = heldKind.materialFromStack(held);
            if (heldKind == SoapBottleKind.BODY_CREAM
                    && stack.hasCarrier()
                    && stack.carrierIntermediate()) {
                if (!stack.tryPushCreamAfterCarrier(materialId)) {
                    return InteractionResult.FAIL;
                }
            } else if (!stack.pushLayer(new SoapBottleLayer(heldKind, materialId))) {
                return InteractionResult.FAIL;
            }
            holder.markStackChanged();
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            syncState(level, pos, state, stack, layersProperty, materialProperty);
            return InteractionResult.CONSUME;
        }

        // 命中件自身交互（泵头等）：空手 / 非可叠物品
        CompositePartId part =
                PartHitHelpers.resolveHitPart(hit, facing, SoapBottlePartPicks.entries(stack));
        if (part == null) {
            return InteractionResult.PASS;
        }
        int bottleIdx = SoapBottleParts.bottleIndex(part);
        if (bottleIdx >= 0) {
            // 泵头由宿主 Block 播动画；此处放行
            return InteractionResult.PASS;
        }
        // 载体：摞内暂无入皂/开盖 NBT
        return InteractionResult.PASS;
    }

    /**
     * 破坏只掉命中件；若仍有剩余部件则保留方块（返回 {@code false} 表示未摧毁）。
     */
    public static boolean onDestroyedByPlayer(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            boolean willHarvest,
            FluidState fluid,
            IntegerProperty layersProperty,
            IntegerProperty materialProperty) {
        if (level.isClientSide) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof Holder holder)) {
            return true;
        }
        SoapBottleStackData stack = holder.stackData();
        if (stack.layerCount() <= 1 && !stack.hasCarrier()) {
            return true;
        }
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        double reach = player.getBlockReach();
        CompositePartId part =
                PartHitHelpers.resolveHitPartFromPlayer(
                        player, pos, facing, SoapBottlePartPicks.entries(stack), reach);
        if (part == null) {
            part = fallbackTopOrCarrier(stack);
        }
        ItemStack drop = takeHitPart(stack, part);
        if (drop == null) {
            return true;
        }
        holder.markStackChanged();
        if (willHarvest && !player.getAbilities().instabuild) {
            if (!player.getInventory().add(drop)) {
                player.drop(drop, false);
            }
        } else if (!player.getAbilities().instabuild) {
            Block.popResource(level, pos, drop);
        }
        if (stack.layerCount() == 0 && !stack.hasCarrier()) {
            return true;
        }
        syncState(level, pos, state, stack, layersProperty, materialProperty);
        return false;
    }

    private static InteractionResult popHitPart(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player,
            Holder holder,
            SoapBottleStackData stack,
            @Nullable CompositePartId part,
            IntegerProperty layersProperty,
            IntegerProperty materialProperty) {
        if (part == null) {
            return InteractionResult.FAIL;
        }
        ItemStack drop = takeHitPart(stack, part);
        if (drop == null) {
            return InteractionResult.FAIL;
        }
        holder.markStackChanged();
        if (!player.getInventory().add(drop)) {
            player.drop(drop, false);
        }
        if (stack.layerCount() == 0 && !stack.hasCarrier()) {
            level.removeBlock(pos, false);
        } else {
            syncState(level, pos, state, stack, layersProperty, materialProperty);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    private static ItemStack takeHitPart(SoapBottleStackData stack, CompositePartId part) {
        if (SoapBottleParts.isCarrier(part)) {
            return stack.popCarrierItem();
        }
        int idx = SoapBottleParts.bottleIndex(part);
        if (idx < 0) {
            return null;
        }
        SoapBottleLayer layer = stack.popLayerAt(idx);
        if (layer == null) {
            return null;
        }
        return SoapBottleKind.stackWithMaterial(layer.kind(), layer.materialId());
    }

    @Nullable
    private static CompositePartId fallbackTopOrCarrier(SoapBottleStackData stack) {
        if (stack.hasCarrier()) {
            return SoapBottleParts.CARRIER;
        }
        if (stack.layerCount() == 0) {
            return null;
        }
        return SoapBottleParts.bottle(stack.layerCount() - 1);
    }

    public static List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder builder,
            IntegerProperty materialProperty,
            @Nullable BlockEntity blockEntity) {
        if (!(blockEntity instanceof Holder holder)) {
            ItemStack fallback = new ItemStack(state.getBlock().asItem());
            writeHostAppearance(fallback, state, materialProperty, SoapBottleKind.fromItem(fallback));
            return List.of(fallback);
        }
        SoapBottleStackData stack = holder.stackData();
        if (stack.layerCount() == 0 && !stack.hasCarrier()) {
            ItemStack fallback = new ItemStack(state.getBlock().asItem());
            writeHostAppearance(fallback, state, materialProperty, stack.hostKind());
            return List.of(fallback);
        }
        List<ItemStack> drops = new ArrayList<>();
        for (SoapBottleLayer layer : stack.layersView()) {
            drops.add(SoapBottleKind.stackWithMaterial(layer.kind(), layer.materialId()));
        }
        if (stack.hasCarrier()) {
            drops.add(stack.carrier().toItemStack(stack.carrierBoxMaterialId()));
        }
        return drops;
    }

    public static void syncState(
            Level level,
            BlockPos pos,
            BlockState state,
            SoapBottleStackData stack,
            IntegerProperty layersProperty,
            IntegerProperty materialProperty) {
        int layers = Math.max(1, stack.layerCount());
        level.setBlock(
                pos,
                state.setValue(layersProperty, layers).setValue(materialProperty, stack.topMaterial()),
                Block.UPDATE_ALL);
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, level.getBlockState(pos), Block.UPDATE_CLIENTS);
        }
    }

    /** 泵头动画等：命中层（或顶层回退）为该种类时触发。 */
    public static boolean hitOrTopLayerIs(
            SoapBottleStackData stack,
            SoapBottleKind kind,
            @Nullable CompositePartId hitPart) {
        if (hitPart != null) {
            int idx = SoapBottleParts.bottleIndex(hitPart);
            if (idx >= 0) {
                return stack.layerAt(idx).kind() == kind;
            }
        }
        SoapBottleLayer top = stack.topLayer();
        return top != null && top.kind() == kind;
    }

    public static boolean topLayerIs(SoapBottleStackData stack, SoapBottleKind kind) {
        SoapBottleLayer top = stack.topLayer();
        return top != null && top.kind() == kind;
    }

    private static void writeHostAppearance(
            ItemStack stack,
            BlockState state,
            IntegerProperty materialProperty,
            @Nullable SoapBottleKind kind) {
        if (kind == null) {
            return;
        }
        int mat = state.getValue(materialProperty);
        switch (kind) {
            case BODY_WASH ->
                    BodyWashAppearance.writeToStack(stack, new BodyWashAppearance(mat));
            case SHAMPOO -> ShampooAppearance.writeToStack(stack, new ShampooAppearance(mat));
            case BODY_CREAM ->
                    BodyCreamAppearance.writeToStack(stack, new BodyCreamAppearance(mat));
        }
    }
}
