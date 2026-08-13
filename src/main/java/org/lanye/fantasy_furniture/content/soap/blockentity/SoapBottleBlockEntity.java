package org.lanye.fantasy_furniture.content.soap.blockentity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.soap.SoapBottleCarrierCollisionShapes;
import org.lanye.fantasy_furniture.content.soap.SoapBottleKind;
import org.lanye.fantasy_furniture.content.soap.SoapBottleLayer;
import org.lanye.fantasy_furniture.content.soap.SoapBottleMixedCollisionShapes;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackData;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackRules;
import org.lanye.fantasy_furniture.content.soap.SoapBottleStackUse;
import org.lanye.fantasy_furniture.content.soap.SoapStackCarrierKind;
import org.lanye.reverie_core.util.VoxelShapeRotation;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;

/** 沐浴露 / 洗发露 / 乳霜摞的共用 BE 基类（层列表 + 混合摞 NBT）。 */
public abstract class SoapBottleBlockEntity extends BlockEntity
        implements GeoBlockEntity, SoapBottleStackUse.Holder {

    private final SoapBottleStackData stack;
    /** 混合摞北向碰撞缓存；层序变更时清空（与材质无关）。 */
    @Nullable private VoxelShape cachedMixedNorth;
    /** 当前朝向旋转后的混合碰撞；与 {@link #cachedMixedFacing} 成对。 */
    @Nullable private VoxelShape cachedMixedFaced;
    @Nullable private Direction cachedMixedFacing;

    protected SoapBottleBlockEntity(
            BlockEntityType<?> type, BlockPos pos, BlockState state, SoapBottleKind hostKind) {
        super(type, pos, state);
        this.stack = new SoapBottleStackData(hostKind);
    }

    protected SoapBottleStackData stack() {
        return stack;
    }

    @Override
    public SoapBottleStackData stackData() {
        return stack;
    }

    @Override
    public void markStackChanged() {
        invalidateMixedCollisionCache();
        setChanged();
    }

    /**
     * 瓶罐摞北向体素（按层合并）并含架/盒载体；完成态第 3 位乳霜用组合乳霜外接盒。
     * 结果缓存至层序 / 载体变更。
     */
    public VoxelShape mixedCollisionNorth() {
        if (cachedMixedNorth == null) {
            List<SoapBottleLayer> layers = layersView();
            VoxelShape shape;
            if (SoapBottleStackRules.isCarrierCompleted(stack)
                    && layers.size() >= 3
                    && layers.get(2).kind() == SoapBottleKind.BODY_CREAM) {
                shape = SoapBottleMixedCollisionShapes.northExcludingSlot(layers, 3);
                shape = Shapes.or(shape, SoapBottleCarrierCollisionShapes.COMBO_CREAM);
            } else {
                shape = SoapBottleMixedCollisionShapes.north(layers);
            }
            SoapStackCarrierKind carrierKind = stack.carrier();
            if (carrierKind != null) {
                shape =
                        Shapes.or(
                                shape,
                                SoapBottleCarrierCollisionShapes.carrierNorth(
                                        carrierKind, stack.carrierIntermediate()));
            }
            cachedMixedNorth = shape;
        }
        return cachedMixedNorth;
    }

    /** 瓶罐摞碰撞（含朝向旋转与载体）；层序 / 载体 / 朝向变更前复用。 */
    public VoxelShape mixedCollisionShape(Direction facing) {
        if (cachedMixedFaced == null || cachedMixedFacing != facing) {
            cachedMixedFaced =
                    VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(
                            mixedCollisionNorth(), facing);
            cachedMixedFacing = facing;
        }
        return cachedMixedFaced;
    }

    public boolean isCarrierCompleted() {
        return SoapBottleStackRules.isCarrierCompleted(stack);
    }

    protected void invalidateMixedCollisionCache() {
        cachedMixedNorth = null;
        cachedMixedFaced = null;
        cachedMixedFacing = null;
    }

    public int layerCount() {
        return stack.layerCount();
    }

    public int materialAtLayer(int indexFromBottom) {
        return stack.layerAt(indexFromBottom).materialId();
    }

    public SoapBottleKind kindAtLayer(int indexFromBottom) {
        return stack.layerAt(indexFromBottom).kind();
    }

    public int topMaterial() {
        return stack.topMaterial();
    }

    public List<SoapBottleLayer> layersView() {
        return stack.layersView();
    }

    @Nullable
    public SoapStackCarrierKind carrier() {
        return stack.carrier();
    }

    public boolean carrierIntermediate() {
        return stack.carrierIntermediate();
    }

    public int carrierBoxMaterialId() {
        return stack.carrierBoxMaterialId();
    }

    public boolean hasCarrier() {
        return stack.hasCarrier();
    }

    public void setSingleLayer(SoapBottleKind kind, int materialId) {
        stack.setSingleLayer(kind, materialId);
        invalidateMixedCollisionCache();
        setChanged();
    }

    public boolean replaceTopMaterial(int materialId) {
        SoapBottleLayer top = stack.topLayer();
        if (top == null || !top.kind().isValidMaterial(materialId)) {
            return false;
        }
        // 原地替换，避免有 carrier 时走 pushLayer 接受规则
        if (!stack.replaceTopMaterial(materialId)) {
            return false;
        }
        invalidateMixedCollisionCache();
        setChanged();
        return true;
    }

    protected void loadStack(CompoundTag tag, int legacyMaxStack) {
        stack.load(tag, legacyMaxStack);
        invalidateMixedCollisionCache();
    }

    protected void saveStack(CompoundTag tag) {
        stack.save(tag);
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

    protected abstract AnimatableInstanceCache animatableCache();
}
