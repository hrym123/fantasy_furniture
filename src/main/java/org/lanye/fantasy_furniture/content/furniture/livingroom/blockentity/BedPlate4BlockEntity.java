package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/** 床板4型：共用床单 / 被套（床单世界贴图暂借板3六色，见台账说明）。 */
public final class BedPlate4BlockEntity extends BedPlateBaseBlockEntity {

    private static final String NBT_DUVET = "DuvetMat";
    private static final String NBT_COVER = "CoverMat";

    private int duvetMaterialId;
    private int coverMaterialId;

    public BedPlate4BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BED_PLATE4.blockEntityType().get(), pos, state);
    }

    public int getDuvetMaterialId() {
        return duvetMaterialId;
    }

    public boolean hasDuvet() {
        return BedPlate6DuvetMaterials.isValid(duvetMaterialId);
    }

    public boolean canAddDuvet() {
        return !hasDuvet();
    }

    public int getCoverMaterialId() {
        return coverMaterialId;
    }

    public boolean hasCover() {
        return BedPlate6DuvetCoverMaterials.isValid(coverMaterialId);
    }

    public boolean canAddCover() {
        return hasDuvet() && !hasCover();
    }

    public void setDuvetMaterialId(int materialId) {
        if (materialId != 0 && !BedPlate6DuvetMaterials.isSupportedOnBedPlate4(materialId)) {
            return;
        }
        if (materialId != 0 && !canAddDuvet()) {
            return;
        }
        this.duvetMaterialId = materialId;
        if (materialId == 0) {
            this.coverMaterialId = 0;
        }
        syncClients();
    }

    public void setCoverMaterialId(int materialId) {
        if (materialId != 0 && !BedPlate6DuvetCoverMaterials.isValid(materialId)) {
            return;
        }
        if (materialId != 0 && !canAddCover()) {
            return;
        }
        this.coverMaterialId = materialId;
        syncClients();
    }

    public void clearBedding() {
        duvetMaterialId = 0;
        coverMaterialId = 0;
        syncClients();
    }

    private void syncClients() {
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
        if (duvetMaterialId != 0) {
            tag.putInt(NBT_DUVET, duvetMaterialId);
        }
        if (coverMaterialId != 0) {
            tag.putInt(NBT_COVER, coverMaterialId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        duvetMaterialId = tag.getInt(NBT_DUVET);
        if (!BedPlate6DuvetMaterials.isSupportedOnBedPlate4(duvetMaterialId)) {
            duvetMaterialId = 0;
        }
        coverMaterialId = tag.getInt(NBT_COVER);
        if (!BedPlate6DuvetCoverMaterials.isValid(coverMaterialId) || !hasDuvet()) {
            coverMaterialId = 0;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (duvetMaterialId != 0) {
            tag.putInt(NBT_DUVET, duvetMaterialId);
        }
        if (coverMaterialId != 0) {
            tag.putInt(NBT_COVER, coverMaterialId);
        }
        return tag;
    }
}
