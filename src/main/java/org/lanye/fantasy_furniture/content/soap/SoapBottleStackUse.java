package org.lanye.fantasy_furniture.content.soap;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;

/** 沐浴露 / 洗发露 / 乳霜混合摞放：服务端交互与掉落。 */
public final class SoapBottleStackUse {

    private SoapBottleStackUse() {}

    public interface Holder {
        SoapBottleStackData stackData();

        void markStackChanged();
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
        boolean sneaking = player.isShiftKeyDown();

        if (sneaking) {
            if (!held.isEmpty() && !SoapBottleKind.isSoapBottleItem(held)) {
                return InteractionResult.PASS;
            }
            SoapBottleLayer popped = stack.popTopLayer();
            if (popped == null) {
                return InteractionResult.FAIL;
            }
            holder.markStackChanged();
            ItemStack drop = SoapBottleKind.stackWithMaterial(popped.kind(), popped.materialId());
            if (!player.getInventory().add(drop)) {
                player.drop(drop, false);
            }
            if (stack.layerCount() == 0) {
                level.removeBlock(pos, false);
            } else {
                syncState(level, pos, state, stack, layersProperty, materialProperty);
            }
            return InteractionResult.CONSUME;
        }

        SoapBottleKind heldKind = SoapBottleKind.fromItem(held);
        if (heldKind != null) {
            int materialId = heldKind.materialFromStack(held);
            if (!stack.pushLayer(new SoapBottleLayer(heldKind, materialId))) {
                return InteractionResult.FAIL;
            }
            holder.markStackChanged();
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            syncState(level, pos, state, stack, layersProperty, materialProperty);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
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
        if (stack.layerCount() == 0) {
            ItemStack fallback = new ItemStack(state.getBlock().asItem());
            writeHostAppearance(fallback, state, materialProperty, stack.hostKind());
            return List.of(fallback);
        }
        List<ItemStack> drops = new ArrayList<>();
        for (SoapBottleLayer layer : stack.layersView()) {
            drops.add(SoapBottleKind.stackWithMaterial(layer.kind(), layer.materialId()));
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
    }

    /** 泵头动画等：仅顶层为该种类时触发。 */
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
