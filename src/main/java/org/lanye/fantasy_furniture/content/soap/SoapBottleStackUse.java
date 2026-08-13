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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.lanye.fantasy_furniture.content.tool.BrushRecolor;
import org.lanye.reverie_core.composite.CompositePartId;
import org.lanye.reverie_core.composite.PartHitHelpers;
import org.lanye.reverie_core.util.PartialBlockInteractionFx;

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

        // 可叠优先：架/盒 / 瓶（与是否潜行无关；对齐独立皂架「潜行未取件时仍可入皂」）
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
            PartialBlockInteractionFx.playPlaceSound(level, pos, state);
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
            PartialBlockInteractionFx.playPlaceSound(level, pos, state);
            syncState(level, pos, state, stack, layersProperty, materialProperty);
            return InteractionResult.CONSUME;
        }

        // 持皂：对齐独立架/盒「点方块即入皂/开盖」，不依赖准心落在载体体积内
        if (stack.hasCarrier()
                && held.is(org.lanye.fantasy_furniture.bootstrap.block.ModBlocks.SOAP_BAR.item().get())) {
            InteractionResult soapOnCarrier =
                    interactCarrier(
                            level, pos, state, player, held, holder, stack, layersProperty, materialProperty);
            if (soapOnCarrier != InteractionResult.PASS) {
                return soapOnCarrier;
            }
        }

        CompositePartId part =
                PartHitHelpers.resolveHitPart(hit, facing, SoapBottlePartPicks.entries(stack));
        if (part == null) {
            part = sneaking ? fallbackTopOrCarrier(stack) : null;
        }

        // 命中载体：开合/入皂/取皂；无动作则放行（hitOrTopLayerIs 已排除载体误播泵动画）
        if (part != null && SoapBottleParts.isCarrier(part)) {
            InteractionResult carrierResult =
                    interactCarrier(
                            level, pos, state, player, held, holder, stack, layersProperty, materialProperty);
            if (carrierResult != InteractionResult.PASS) {
                return carrierResult;
            }
        }

        if (sneaking) {
            if (!held.isEmpty()
                    && !SoapBottleKind.isSoapBottleItem(held)
                    && SoapStackCarrierKind.fromItem(held) == null) {
                return InteractionResult.PASS;
            }
            if (part == null) {
                part = fallbackTopOrCarrier(stack);
            }
            return popHitPart(level, pos, state, player, holder, stack, part, layersProperty, materialProperty);
        }

        // 瓶泵头等：放行给方块后续动画
        return InteractionResult.PASS;
    }

    /** 组合态架/盒：对齐独立件——盒先开盖再入皂；架直接入皂；潜行取皂。 */
    private static InteractionResult interactCarrier(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player,
            ItemStack held,
            Holder holder,
            SoapBottleStackData stack,
            IntegerProperty layersProperty,
            IntegerProperty materialProperty) {
        SoapStackCarrierKind kind = stack.carrier();
        if (kind == null) {
            return InteractionResult.FAIL;
        }
        boolean sneaking = player.isShiftKeyDown();

        if (kind == SoapStackCarrierKind.BOX) {
            // 对齐 SoapBoxBlock#onUseServer
            if (!stack.carrierBoxOpen() && !stack.carrierHasSoap()) {
                stack.setCarrierBoxOpen(true);
                holder.markStackChanged();
                syncState(level, pos, state, stack, layersProperty, materialProperty);
                return InteractionResult.CONSUME;
            }
            if (stack.carrierBoxOpen() && !stack.carrierHasSoap()) {
                if (held.isEmpty()) {
                    stack.setCarrierBoxOpen(false);
                    holder.markStackChanged();
                    syncState(level, pos, state, stack, layersProperty, materialProperty);
                    return InteractionResult.CONSUME;
                }
                if (held.is(org.lanye.fantasy_furniture.bootstrap.block.ModBlocks.SOAP_BAR.item().get())) {
                    SoapBarAppearance soap = SoapBarAppearance.fromStack(held);
                    if (!soap.isFull()) {
                        return InteractionResult.FAIL;
                    }
                    stack.setCarrierSoap(soap);
                    holder.markStackChanged();
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                    syncState(level, pos, state, stack, layersProperty, materialProperty);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            }
            if (stack.carrierBoxOpen() && stack.carrierHasSoap() && sneaking) {
                SoapBarAppearance soap = stack.popCarrierSoap();
                if (soap == null) {
                    return InteractionResult.FAIL;
                }
                ItemStack drop =
                        org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem.stackWithAppearance(
                                org.lanye.fantasy_furniture.bootstrap.block.ModBlocks.SOAP_BAR.item().get(),
                                soap);
                if (!player.getInventory().add(drop)) {
                    player.drop(drop, false);
                }
                holder.markStackChanged();
                syncState(level, pos, state, stack, layersProperty, materialProperty);
                return InteractionResult.CONSUME;
            }
            if (stack.carrierHasSoap() && !sneaking) {
                stack.setCarrierBoxOpen(!stack.carrierBoxOpen());
                holder.markStackChanged();
                syncState(level, pos, state, stack, layersProperty, materialProperty);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // 架
        if (sneaking && stack.carrierHasSoap()) {
            SoapBarAppearance soap = stack.popCarrierSoap();
            if (soap == null) {
                return InteractionResult.FAIL;
            }
            ItemStack drop =
                    org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem.stackWithAppearance(
                            org.lanye.fantasy_furniture.bootstrap.block.ModBlocks.SOAP_BAR.item().get(),
                            soap);
            if (!player.getInventory().add(drop)) {
                player.drop(drop, false);
            }
            holder.markStackChanged();
            syncState(level, pos, state, stack, layersProperty, materialProperty);
            return InteractionResult.CONSUME;
        }
        if (!stack.carrierHasSoap()
                && held.is(org.lanye.fantasy_furniture.bootstrap.block.ModBlocks.SOAP_BAR.item().get())) {
            SoapBarAppearance soap = SoapBarAppearance.fromStack(held);
            if (!soap.isFull()) {
                return InteractionResult.FAIL;
            }
            stack.setCarrierSoap(soap);
            holder.markStackChanged();
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            syncState(level, pos, state, stack, layersProperty, materialProperty);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /**
     * 破坏只掉命中件；若仍有剩余部件则保留方块（返回 {@code false} 表示未摧毁）。
     *
     * <p>客户端残留约定见 {@link PartialBlockInteractionFx#clientKeepBlockWhen}。
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
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof Holder holder)) {
            return true;
        }
        SoapBottleStackData stack = holder.stackData();
        boolean willRemain = stack.layerCount() > 1 || stack.hasCarrier();
        if (!willRemain) {
            return true;
        }
        Boolean clientKeep = PartialBlockInteractionFx.clientKeepBlockWhen(level, true);
        if (clientKeep != null) {
            return clientKeep;
        }
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        double reach = player.getBlockReach();
        CompositePartId part =
                PartHitHelpers.resolveHitPartFromPlayer(
                        player, pos, facing, SoapBottlePartPicks.entries(stack), reach);
        if (part == null) {
            part = fallbackTopOrCarrier(stack);
        }
        if (part != null && SoapBottleParts.isCarrier(part)) {
            dropCarrierSoapIfAny(level, pos, player, stack);
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
        PartialBlockInteractionFx.playDestroyEffects(level, pos, state);
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
        if (SoapBottleParts.isCarrier(part)) {
            dropCarrierSoapIfAny(level, pos, player, stack);
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
            // 潜行右键收回：静默同步；破碎音/粒子仅留给左键部分拆除
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

    /** 取载体前若有内皂，先掉落肥皂。 */
    private static void dropCarrierSoapIfAny(
            Level level, BlockPos pos, Player player, SoapBottleStackData stack) {
        if (!stack.carrierHasSoap()) {
            return;
        }
        SoapBarAppearance soap = stack.popCarrierSoap();
        if (soap == null) {
            return;
        }
        ItemStack drop =
                org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem.stackWithAppearance(
                        org.lanye.fantasy_furniture.bootstrap.block.ModBlocks.SOAP_BAR.item().get(),
                        soap);
        if (!player.getInventory().add(drop)) {
            Block.popResource(level, pos, drop);
        }
    }

    @Nullable
    private static CompositePartId fallbackTopOrCarrier(SoapBottleStackData stack) {
        if (stack.hasCarrier()) {
            return SoapBottleParts.CARRIER;
        }
        int highest = stack.highestOccupiedSlot();
        if (highest < 0) {
            return null;
        }
        // 定长可空槽：须用最高占用槽号，不能用 layerCount()-1
        return SoapBottleParts.bottle(highest);
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
        for (int i = 0; i < SoapBottleStackData.MAX_SLOTS; i++) {
            SoapBottleLayer layer = stack.slotAt(i);
            if (layer != null) {
                drops.add(SoapBottleKind.stackWithMaterial(layer.kind(), layer.materialId()));
            }
        }
        if (stack.hasCarrier()) {
            if (stack.carrierHasSoap() && stack.carrierSoap() != null) {
                drops.add(
                        org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem
                                .stackWithAppearance(
                                        org.lanye.fantasy_furniture.bootstrap.block.ModBlocks.SOAP_BAR
                                                .item()
                                                .get(),
                                        stack.carrierSoap()));
            }
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
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            be.setChanged();
        }
        int layers = Math.max(1, stack.layerCount());
        // MATERIAL 镜像首个占用槽，避免残留瓶被默认材质盖成蓝
        BlockState updated =
                state.setValue(layersProperty, layers)
                        .setValue(materialProperty, stack.displayMaterial());
        level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
        if (be != null) {
            level.blockEntityChanged(pos);
            level.sendBlockUpdated(pos, state, updated, Block.UPDATE_CLIENTS);
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
                SoapBottleLayer layer = stack.slotAt(idx);
                return layer != null && layer.kind() == kind;
            }
            // 命中载体等非瓶件：不得回落到顶层瓶（否则点架/盒也会播泵动画）
            return false;
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
