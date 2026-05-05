package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/**
 * 床板 6：可选床单外观（仅方块实体数据，无独立方块）。材质编号 {@code 1..7} 对应七种贴图/物品。
 *
 * @see BedPlate6DuvetItem
 * @see org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem
 */
public final class BedPlate6BlockEntity extends BedPlateBaseBlockEntity {

    /** 当前材质 1..7；0 表示无床单。 */
    private static final String NBT_MAT = "DuvetMat";

    /** 被套材质 1..7；0 表示无被套（仅当有床单时可存在）。 */
    private static final String NBT_COVER = "CoverMat";

    /** 旧版布尔被单标记（仅加载兼容）。 */
    private static final String NBT_LEGACY_BOOL = "Duvet6";

    private int duvetMaterialId;
    private int coverMaterialId;

    public BedPlate6BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BED_PLATE6.blockEntityType().get(), pos, state);
    }

    /** 0 表示无床单，否则为 1..{@link BedPlate6DuvetMaterials#COUNT}。 */
    public int getDuvetMaterialId() {
        return duvetMaterialId;
    }

    public boolean hasDuvet() {
        return BedPlate6DuvetMaterials.isValid(duvetMaterialId);
    }

    /** 0 表示无被套，否则为 1..{@link BedPlate6DuvetMaterials#COUNT}。 */
    public int getCoverMaterialId() {
        return coverMaterialId;
    }

    public boolean hasCover() {
        return BedPlate6DuvetMaterials.isValid(coverMaterialId);
    }

    /** 服务端：{@code 0} 卸下被套；{@code 1..7} 铺上（仅当已有床单时由物品逻辑调用）。 */
    public void setCoverMaterialId(int materialId) {
        if (materialId != 0 && !BedPlate6DuvetMaterials.isValid(materialId)) {
            return;
        }
        if (materialId != 0 && !hasDuvet()) {
            return;
        }
        this.coverMaterialId = materialId;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }

    /** 服务端：{@code 0} 卸下，{@code 1..7} 铺上对应材质。卸下床单时同时卸下被套。 */
    public void setDuvetMaterialId(int materialId) {
        if (materialId != 0 && !BedPlate6DuvetMaterials.isValid(materialId)) {
            return;
        }
        this.duvetMaterialId = materialId;
        if (materialId == 0) {
            this.coverMaterialId = 0;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte(NBT_MAT, (byte) duvetMaterialId);
        tag.putByte(NBT_COVER, (byte) coverMaterialId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(NBT_MAT, Tag.TAG_BYTE)) {
            int v = tag.getByte(NBT_MAT);
            this.duvetMaterialId = (v >= 0 && v <= BedPlate6DuvetMaterials.COUNT) ? v : 0;
        } else if (tag.contains(NBT_LEGACY_BOOL, Tag.TAG_BYTE)) {
            this.duvetMaterialId = tag.getBoolean(NBT_LEGACY_BOOL) ? 1 : 0;
        } else {
            this.duvetMaterialId = 0;
        }
        if (tag.contains(NBT_COVER, Tag.TAG_BYTE)) {
            int c = tag.getByte(NBT_COVER);
            this.coverMaterialId = (c >= 0 && c <= BedPlate6DuvetMaterials.COUNT) ? c : 0;
        } else {
            this.coverMaterialId = 0;
        }
        if (this.coverMaterialId != 0 && !BedPlate6DuvetMaterials.isValid(this.duvetMaterialId)) {
            this.coverMaterialId = 0;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putByte(NBT_MAT, (byte) duvetMaterialId);
        tag.putByte(NBT_COVER, (byte) coverMaterialId);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
        }
    }
}
