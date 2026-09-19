package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate3Registration;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate3MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate3Materials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowHost;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate3Block;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/** 床板3型：材质档贴图 + 共用床单 / 被套 / 枕头。 */
public final class BedPlate3BlockEntity extends BedPlateBaseBlockEntity implements BedPlateSheetPillowHost {

    private static final String NBT_DUVET = "DuvetMat";
    private static final String NBT_COVER = "CoverMat";

    private int duvetMaterialId;
    private int coverMaterialId;
    private final BedPlateSheetPillowSlots sheetPillows = new BedPlateSheetPillowSlots();

    public BedPlate3BlockEntity(BlockPos pos, BlockState state) {
        super(BedPlate3Registration.blockEntityType().get(), pos, state);
    }

    public ResourceLocation getTextureLocation() {
        Block block = getBlockState().getBlock();
        if (block instanceof BedPlate3Block plate) {
            return BedPlate3Materials.texture(plate.variant());
        }
        return BedPlate3Materials.texture(BedPlate3MaterialVariant.DEFAULT);
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

    @Override
    public BedPlateSheetPillowSlots sheetPillows() {
        return sheetPillows;
    }

    @Override
    public void syncSheetPillows() {
        syncClients();
    }

    public void setDuvetMaterialId(int materialId) {
        if (materialId != 0 && !BedPlate6DuvetMaterials.isSupportedOnBedPlate3(materialId)) {
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
        sheetPillows.write(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        duvetMaterialId = tag.getInt(NBT_DUVET);
        if (!BedPlate6DuvetMaterials.isSupportedOnBedPlate3(duvetMaterialId)) {
            duvetMaterialId = 0;
        }
        coverMaterialId = tag.getInt(NBT_COVER);
        if (!BedPlate6DuvetCoverMaterials.isValid(coverMaterialId) || !hasDuvet()) {
            coverMaterialId = 0;
        }
        sheetPillows.read(tag);
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
        sheetPillows.write(tag);
        return tag;
    }
}
