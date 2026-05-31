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
import org.lanye.fantasy_furniture.content.soap.ShampooAssets;
import org.lanye.fantasy_furniture.content.soap.ShampooMaterials;
import org.lanye.fantasy_furniture.content.soap.block.ShampooBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 洗发露摞：最多 {@link ShampooAssets#MAX_STACK} 瓶，LIFO；每层独立颜料。 */
public final class ShampooBlockEntity extends BlockEntity implements GeoBlockEntity {

    public static final String MAIN_CONTROLLER = "main";

    private static final String TRIGGER_USE = "use";
    private static final String TRIGGER_USE2 = "use2";
    private static final String TRIGGER_USE3 = "use3";
    private static final String TRIGGER_USE4 = "use4";

    /** 源自 {@code 洗发露_默认.bbmodel} · {@code animation}（泵头 {@code bone}，与堆叠 geo {@code block1/bone} 一致）。 */
    private static final RawAnimation USE_SINGLE =
            RawAnimation.begin().then("animation.shampoo.use", Animation.LoopType.PLAY_ONCE);

    /** 源自 {@code 洗发露_堆叠_x4.bbmodel} · {@code animation2}…{@code animation4}。 */
    private static final RawAnimation USE_STACK2 =
            RawAnimation.begin().then("animation.shampoo_stack.use2", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK3 =
            RawAnimation.begin().then("animation.shampoo_stack.use3", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation USE_STACK4 =
            RawAnimation.begin().then("animation.shampoo_stack.use4", Animation.LoopType.PLAY_ONCE);

    private static final String TAG_LAYER_MATS = "LayerMats";
    private static final String TAG_LAYERS_LEGACY = "Layers";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<Integer> layerMaterials = new ArrayList<>();

    public ShampooBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SHAMPOO.blockEntityType().get(), pos, state);
    }

    public int layerCount() {
        return layerMaterials.size();
    }

    /** 渲染与泵头动画层数：BE 未同步时回退 blockstate {@link ShampooBlock#LAYERS}。 */
    public int visibleLayerCount() {
        if (!layerMaterials.isEmpty()) {
            return layerMaterials.size();
        }
        BlockState state = getBlockState();
        if (state.hasProperty(ShampooBlock.LAYERS)) {
            return Math.max(1, state.getValue(ShampooBlock.LAYERS));
        }
        return 1;
    }

    public int materialAtLayer(int indexFromBottom) {
        if (indexFromBottom < 0 || indexFromBottom >= layerMaterials.size()) {
            return ShampooMaterials.DEFAULT;
        }
        return layerMaterials.get(indexFromBottom);
    }

    public int topMaterial() {
        if (layerMaterials.isEmpty()) {
            return ShampooMaterials.DEFAULT;
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
        if (layerMaterials.size() >= ShampooAssets.MAX_STACK) {
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
                if (ShampooMaterials.isValid(mat)) {
                    layerMaterials.add(mat);
                }
            }
            if (!layerMaterials.isEmpty()) {
                return;
            }
        }
        int legacy = tag.contains(TAG_LAYERS_LEGACY) ? tag.getInt(TAG_LAYERS_LEGACY) : 1;
        int count = Math.max(1, Math.min(ShampooAssets.MAX_STACK, legacy));
        for (int i = 0; i < count; i++) {
            layerMaterials.add(ShampooMaterials.DEFAULT);
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
    public void onLoad() {
        super.onLoad();
        reconcileLayersFromBlockState();
    }

    /** blockstate 已有层数但 BE 列表为空时（旧存档 / 同步间隙）按 state 补一层。 */
    private void reconcileLayersFromBlockState() {
        if (!layerMaterials.isEmpty()) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(ShampooBlock.LAYERS) || !state.hasProperty(ShampooBlock.MATERIAL)) {
            return;
        }
        int count = Math.max(1, state.getValue(ShampooBlock.LAYERS));
        int mat = state.getValue(ShampooBlock.MATERIAL);
        for (int i = 0; i < count; i++) {
            layerMaterials.add(mat);
        }
    }

    /** 空手右键：按压泵头动画（顶层瓶；单瓶用默认 geo，多瓶用堆叠 geo 对应 {@code boneN}）。 */
    public void triggerUseAnim() {
        int layers = visibleLayerCount();
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
