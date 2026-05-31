package org.lanye.fantasy_furniture.content.soap.blockentity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.block.SoapMoldBlock;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldContents;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldIngredientSlot;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldIngredients;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldLiquidKind;
import org.lanye.fantasy_furniture.content.soap.mold.SoapMoldPhase;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 肥皂模具：装料、混合、凝固与取皂（见 {@code soap_mold.gameplay.md}）。 */
public class SoapMoldBlockEntity extends BlockEntity implements GeoBlockEntity {

    /** 与 {@code animation.soap_mold.animation} 的 {@code animation_length}（约 4.71s）一致。 */
    public static final int CURE_TICKS = 94;

    public static final String MAIN_CONTROLLER = "main";

    private static final String TRIGGER_CURE = "cure";

    private static final RawAnimation CURING =
            RawAnimation.begin().then("animation.soap_mold.animation", Animation.LoopType.PLAY_ONCE);

    private static final String TAG_PHASE = "Phase";
    private static final String TAG_LIQ_KIND = "LiqKind";
    private static final String TAG_LIQ_MAT = "LiqMat";
    private static final String TAG_HONEY = "Honey";
    private static final String TAG_WATER = "Water";
    private static final String TAG_PIGMENT = "PigmentMat";
    private static final String TAG_INSERT = "InsertOrd";
    private static final String TAG_CURE_END = "CureEnd";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Deque<SoapMoldIngredientSlot> insertOrder = new ArrayDeque<>();

    private SoapMoldContents contents = SoapMoldContents.empty();

    public SoapMoldBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SOAP_MOLD.blockEntityType().get(), pos, state);
    }

    public SoapMoldContents contents() {
        return contents;
    }

    public Deque<SoapMoldIngredientSlot> insertOrderView() {
        return insertOrder;
    }

    public boolean tryInsert(SoapMoldIngredients.Match match) {
        if (!contents.canModifyIngredients()) {
            return false;
        }
        if (!slotEmpty(match.slot())) {
            return false;
        }
        contents =
                switch (match.slot()) {
                    case LIQUID ->
                            new SoapMoldContents(
                                    contents.phase(),
                                    match.liquidKind(),
                                    match.liquidMatId(),
                                    contents.hasHoneycomb(),
                                    contents.hasWater(),
                                    contents.pigmentMatId(),
                                    contents.cureFinishGameTime());
                    case HONEY ->
                            new SoapMoldContents(
                                    contents.phase(),
                                    contents.liquidKind(),
                                    contents.liquidMatId(),
                                    true,
                                    contents.hasWater(),
                                    contents.pigmentMatId(),
                                    contents.cureFinishGameTime());
                    case WATER ->
                            new SoapMoldContents(
                                    contents.phase(),
                                    contents.liquidKind(),
                                    contents.liquidMatId(),
                                    contents.hasHoneycomb(),
                                    true,
                                    contents.pigmentMatId(),
                                    contents.cureFinishGameTime());
                    case PIGMENT ->
                            new SoapMoldContents(
                                    contents.phase(),
                                    contents.liquidKind(),
                                    contents.liquidMatId(),
                                    contents.hasHoneycomb(),
                                    contents.hasWater(),
                                    match.pigmentMatId(),
                                    contents.cureFinishGameTime());
                };
        insertOrder.addLast(match.slot());
        contents = contents.recomputeIngredientPhase();
        syncFillLevel();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.blockEntityChanged(worldPosition);
            BlockState current = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, current, current, Block.UPDATE_ALL);
        }
        return true;
    }

    @Nullable
    public ItemStack tryPopLastIngredient() {
        if (!contents.canModifyIngredients() || insertOrder.isEmpty()) {
            return null;
        }
        SoapMoldIngredientSlot slot = insertOrder.removeLast();
        ItemStack stack = SoapMoldIngredients.stackForSlot(contents, slot);
        clearSlot(slot);
        contents = contents.recomputeIngredientPhase();
        syncFillLevel();
        setChanged();
        return stack.isEmpty() ? null : stack;
    }

    @Nullable
    public ItemStack tryExtractLiquidIngredient() {
        if (!contents.canModifyIngredients() || !contents.hasLiquid()) {
            return null;
        }
        ItemStack stack = SoapMoldIngredients.liquidStack(contents.liquidKind(), contents.liquidMatId());
        clearSlot(SoapMoldIngredientSlot.LIQUID);
        insertOrder.remove(SoapMoldIngredientSlot.LIQUID);
        contents = contents.recomputeIngredientPhase();
        syncFillLevel();
        setChanged();
        return stack.isEmpty() ? null : stack;
    }

    @Nullable
    public ItemStack tryExtractWater() {
        if (!contents.canModifyIngredients() || !contents.hasWater()) {
            return null;
        }
        ItemStack stack = new ItemStack(Items.WATER_BUCKET);
        clearSlot(SoapMoldIngredientSlot.WATER);
        insertOrder.remove(SoapMoldIngredientSlot.WATER);
        contents = contents.recomputeIngredientPhase();
        syncFillLevel();
        setChanged();
        return stack;
    }

    public boolean tryStartMixing(long gameTime) {
        if (contents.phase() != SoapMoldPhase.READY_TO_MIX || !contents.isFull()) {
            return false;
        }
        contents =
                new SoapMoldContents(
                        SoapMoldPhase.CURING,
                        contents.liquidKind(),
                        contents.liquidMatId(),
                        contents.hasHoneycomb(),
                        contents.hasWater(),
                        contents.pigmentMatId(),
                        gameTime + CURE_TICKS);
        syncFillLevel();
        setChanged();
        triggerCureAnimation();
        syncToClients();
        return true;
    }

    private void triggerCureAnimation() {
        if (level != null && !level.isClientSide) {
            triggerAnim(MAIN_CONTROLLER, TRIGGER_CURE);
        }
    }

    public SoapBarAppearance pendingSoapAppearance() {
        return new SoapBarAppearance(
                SoapBarAppearance.DEFAULT_WEAR,
                contents.pigmentMatId(),
                0,
                false,
                contents.liquidMatId());
    }

    public ItemStack createSoapStack() {
        if (contents.phase() != SoapMoldPhase.READY) {
            return ItemStack.EMPTY;
        }
        return SoapBarBlockItem.stackWithAppearance(
                ModBlocks.SOAP_BAR.item().get(), pendingSoapAppearance());
    }

    @Nullable
    public SoapBarAppearance tryTakeSoap() {
        if (contents.phase() != SoapMoldPhase.READY) {
            return null;
        }
        SoapBarAppearance soap = pendingSoapAppearance();
        resetAfterCraft();
        syncToClients();
        return soap;
    }

    public void serverTick(ServerLevel level) {
        if (contents.phase() != SoapMoldPhase.CURING) {
            return;
        }
        if (level.getGameTime() >= contents.cureFinishGameTime()) {
            contents = contents.withPhase(SoapMoldPhase.READY);
            syncFillLevel();
            setChanged();
            syncToClients();
        }
    }

    public List<ItemStack> buildIngredientDrops() {
        List<ItemStack> drops = new ArrayList<>();
        if (contents.hasLiquid()) {
            drops.add(SoapMoldIngredients.liquidStack(contents.liquidKind(), contents.liquidMatId()));
        }
        if (contents.hasHoneycomb()) {
            drops.add(new ItemStack(Items.HONEYCOMB));
        }
        // 水视为消耗品，破坏模具时不返还水桶（投水时已返还空桶）
        if (contents.hasPigment()) {
            drops.add(SoapMoldIngredients.dyeStackForPigment(contents.pigmentMatId()));
        }
        return drops;
    }

    private void resetAfterCraft() {
        insertOrder.clear();
        contents = SoapMoldContents.empty();
        syncFillLevel();
        setChanged();
    }

    private void syncToClients() {
        if (level == null || level.isClientSide) {
            return;
        }
        level.blockEntityChanged(worldPosition);
        BlockState current = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, current, current, Block.UPDATE_ALL);
        if (level instanceof ServerLevel server) {
            ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(this);
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5;
            for (ServerPlayer player : server.players()) {
                if (player.distanceToSqr(x, y, z) <= 4096.0) {
                    player.connection.send(packet);
                }
            }
        }
    }

    private void clearSlot(SoapMoldIngredientSlot slot) {
        contents =
                switch (slot) {
                    case LIQUID ->
                            new SoapMoldContents(
                                    contents.phase(),
                                    SoapMoldLiquidKind.NONE,
                                    0,
                                    contents.hasHoneycomb(),
                                    contents.hasWater(),
                                    contents.pigmentMatId(),
                                    contents.cureFinishGameTime());
                    case HONEY ->
                            new SoapMoldContents(
                                    contents.phase(),
                                    contents.liquidKind(),
                                    contents.liquidMatId(),
                                    false,
                                    contents.hasWater(),
                                    contents.pigmentMatId(),
                                    contents.cureFinishGameTime());
                    case WATER ->
                            new SoapMoldContents(
                                    contents.phase(),
                                    contents.liquidKind(),
                                    contents.liquidMatId(),
                                    contents.hasHoneycomb(),
                                    false,
                                    contents.pigmentMatId(),
                                    contents.cureFinishGameTime());
                    case PIGMENT ->
                            new SoapMoldContents(
                                    contents.phase(),
                                    contents.liquidKind(),
                                    contents.liquidMatId(),
                                    contents.hasHoneycomb(),
                                    contents.hasWater(),
                                    0,
                                    contents.cureFinishGameTime());
                };
    }

    private boolean slotEmpty(SoapMoldIngredientSlot slot) {
        return switch (slot) {
            case LIQUID -> !contents.hasLiquid();
            case HONEY -> !contents.hasHoneycomb();
            case WATER -> !contents.hasWater();
            case PIGMENT -> !contents.hasPigment();
        };
    }

    private void syncFillLevel() {
        if (level == null || !(level.getBlockState(worldPosition).getBlock() instanceof SoapMoldBlock block)) {
            return;
        }
        int fill = contents.filledCount();
        BlockState state = level.getBlockState(worldPosition);
        if (state.getValue(block.FILL_LEVEL) != fill) {
            level.setBlock(worldPosition, state.setValue(block.FILL_LEVEL, fill), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_PHASE, contents.phase().ordinal());
        tag.putInt(TAG_LIQ_KIND, contents.liquidKind().ordinal());
        tag.putInt(TAG_LIQ_MAT, contents.liquidMatId());
        tag.putBoolean(TAG_HONEY, contents.hasHoneycomb());
        tag.putBoolean(TAG_WATER, contents.hasWater());
        tag.putInt(TAG_PIGMENT, contents.pigmentMatId());
        tag.putLong(TAG_CURE_END, contents.cureFinishGameTime());
        ListTag order = new ListTag();
        for (SoapMoldIngredientSlot slot : insertOrder) {
            order.add(net.minecraft.nbt.IntTag.valueOf(slot.ordinal()));
        }
        tag.put(TAG_INSERT, order);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        insertOrder.clear();
        if (tag.contains(TAG_INSERT, Tag.TAG_LIST)) {
            ListTag order = tag.getList(TAG_INSERT, Tag.TAG_INT);
            for (Tag entry : order) {
                insertOrder.addLast(SoapMoldIngredientSlot.fromId(((net.minecraft.nbt.IntTag) entry).getAsInt()));
            }
        }
        contents =
                new SoapMoldContents(
                        SoapMoldPhase.fromId(tag.getInt(TAG_PHASE)),
                        SoapMoldLiquidKind.fromId(tag.getInt(TAG_LIQ_KIND)),
                        tag.getInt(TAG_LIQ_MAT),
                        tag.getBoolean(TAG_HONEY),
                        tag.getBoolean(TAG_WATER),
                        tag.getInt(TAG_PIGMENT),
                        tag.getLong(TAG_CURE_END));
        if (level != null && !level.isClientSide) {
            syncFillLevel();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            syncFillLevel();
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        SoapMoldPhase before = contents.phase();
        load(tag);
        if (level != null
                && level.isClientSide
                && before != SoapMoldPhase.CURING
                && contents.phase() == SoapMoldPhase.CURING) {
            triggerAnim(MAIN_CONTROLLER, TRIGGER_CURE);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, MAIN_CONTROLLER, 0, state -> {
                    if (contents.phase() == SoapMoldPhase.CURING) {
                        return state.setAndContinue(CURING);
                    }
                    return PlayState.STOP;
                }).triggerableAnim(TRIGGER_CURE, CURING));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
