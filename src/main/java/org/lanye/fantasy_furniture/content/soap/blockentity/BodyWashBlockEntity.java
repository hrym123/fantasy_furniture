package org.lanye.fantasy_furniture.content.soap.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.BodyWashAssets;
import org.lanye.fantasy_furniture.content.soap.BodyWashMaterials;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 沐浴露摞：最多 {@link BodyWashAssets#MAX_STACK} 瓶，LIFO；每层独立颜料。 */
public final class BodyWashBlockEntity extends BlockEntity implements GeoBlockEntity {

    public static final String MAIN_CONTROLLER = "main";

    private static final String TRIGGER_USE = "use";
    private static final String TRIGGER_USE2 = "use2";
    private static final String TRIGGER_USE3 = "use3";
    private static final String TRIGGER_USE4 = "use4";

    /** 源自 {@code 沐浴露_默认.bbmodel} · {@code animation}（泵头 {@code bone}）。 */
    private static final RawAnimation USE_SINGLE =
            RawAnimation.begin().then("animation.body_wash.use", Animation.LoopType.PLAY_ONCE);

    /** 源自 {@code 沐浴露_堆叠_x4.bbmodel} · {@code animation2}…{@code animation4}。 */
    private static final RawAnimation USE_STACK2 =
            RawAnimation.begin().then("animation.body_wash_stack.use2", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK3 =
            RawAnimation.begin().then("animation.body_wash_stack.use3", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK4 =
            RawAnimation.begin().then("animation.body_wash_stack.use4", Animation.LoopType.PLAY_ONCE);

    private static final String TAG_LAYER_MATS = "LayerMats";
    private static final String TAG_LAYERS_LEGACY = "Layers";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<Integer> layerMaterials = new ArrayList<>();

    public BodyWashBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BODY_WASH.blockEntityType().get(), pos, state);
    }

    public int layerCount() {
        return layerMaterials.size();
    }

    public int materialAtLayer(int indexFromBottom) {
        if (indexFromBottom < 0 || indexFromBottom >= layerMaterials.size()) {
            return BodyWashMaterials.DEFAULT;
        }
        return layerMaterials.get(indexFromBottom);
    }

    public int topMaterial() {
        if (layerMaterials.isEmpty()) {
            return BodyWashMaterials.DEFAULT;
        }
        return layerMaterials.get(layerMaterials.size() - 1);
    }

    public List<Integer> layerMaterialsView() {
        return Collections.unmodifiableList(layerMaterials);
    }

    public void setSingleLayer(int materialId) {
        layerMaterials.clear();
        layerMaterials.add(materialId);
        setChanged();
    }

    public boolean pushLayer(int materialId) {
        if (layerMaterials.size() >= BodyWashAssets.MAX_STACK) {
            return false;
        }
        layerMaterials.add(materialId);
        setChanged();
        return true;
    }

    public boolean replaceTopMaterial(int materialId) {
        if (layerMaterials.isEmpty()) {
            return false;
        }
        layerMaterials.set(layerMaterials.size() - 1, materialId);
        setChanged();
        return true;
    }

    @Nullable
    public Integer popTopLayer() {
        if (layerMaterials.isEmpty()) {
            return null;
        }
        int removed = layerMaterials.remove(layerMaterials.size() - 1);
        setChanged();
        return removed;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (int mat : layerMaterials) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Mat", mat);
            list.add(entry);
        }
        tag.put(TAG_LAYER_MATS, list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        layerMaterials.clear();
        if (tag.contains(TAG_LAYER_MATS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_LAYER_MATS, Tag.TAG_COMPOUND);
            for (Tag entryTag : list) {
                CompoundTag entry = (CompoundTag) entryTag;
                int mat = entry.getInt("Mat");
                if (BodyWashMaterials.isValid(mat)) {
                    layerMaterials.add(mat);
                }
            }
            if (!layerMaterials.isEmpty()) {
                return;
            }
        }
        int legacy = tag.contains(TAG_LAYERS_LEGACY) ? tag.getInt(TAG_LAYERS_LEGACY) : 1;
        int count = Math.max(1, Math.min(BodyWashAssets.MAX_STACK, legacy));
        for (int i = 0; i < count; i++) {
            layerMaterials.add(BodyWashMaterials.DEFAULT);
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

    /** 空手右键：按压泵头动画（顶层瓶；单瓶用默认 geo，多瓶用堆叠 geo 对应 {@code boneN}）。 */
    public void onServerUseAnim() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        int layers = Math.max(1, layerCount());
        String trigger =
                switch (layers) {
                    case 1 -> TRIGGER_USE;
                    case 2 -> TRIGGER_USE2;
                    case 3 -> TRIGGER_USE3;
                    default -> TRIGGER_USE4;
                };
        triggerAnim(MAIN_CONTROLLER, trigger);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, MAIN_CONTROLLER, 0, state -> PlayState.STOP)
                        .triggerableAnim(TRIGGER_USE, USE_SINGLE)
                        .triggerableAnim(TRIGGER_USE2, USE_STACK2)
                        .triggerableAnim(TRIGGER_USE3, USE_STACK3)
                        .triggerableAnim(TRIGGER_USE4, USE_STACK4));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
