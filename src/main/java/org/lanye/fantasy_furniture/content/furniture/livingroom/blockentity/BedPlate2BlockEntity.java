package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6MediumPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6SmallPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/**
 * 床板2型：共用寝具物品；枕头按槽位分色叠层，不再换拼装床体。
 *
 * @see org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate2Block
 */
public final class BedPlate2BlockEntity extends BedPlateBaseBlockEntity {

    private static final String NBT_DUVET = "DuvetMat";
    private static final String NBT_COVER = "CoverMat";
    private static final String NBT_LG1_STYLE = "Lg1Style";
    private static final String NBT_LG1_MAT = "Lg1Mat";
    private static final String NBT_LG2_STYLE = "Lg2Style";
    private static final String NBT_LG2_MAT = "Lg2Mat";
    private static final String NBT_MEDIUM = "MdPillow";
    private static final String NBT_SMALL = "SmPillow";

    private int duvetMaterialId;
    private int coverMaterialId;
    private int large1StyleId;
    private int large1MaterialId;
    private int large2StyleId;
    private int large2MaterialId;
    private int mediumPillowMat;
    private int smallPillowMat;

    public BedPlate2BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BED_PLATE2.blockEntityType().get(), pos, state);
    }

    public int getDuvetMaterialId() {
        return duvetMaterialId;
    }

    public boolean hasDuvet() {
        return BedPlate6DuvetMaterials.isValid(duvetMaterialId);
    }

    public int getCoverMaterialId() {
        return coverMaterialId;
    }

    public boolean hasCover() {
        return BedPlate6DuvetCoverMaterials.isValid(coverMaterialId);
    }

    public int getLargePillowCount() {
        int n = 0;
        if (hasLargePillowSlot(1)) {
            n++;
        }
        if (hasLargePillowSlot(2)) {
            n++;
        }
        return n;
    }

    public boolean hasLargePillowSlot(int index) {
        if (index == 1) {
            return BedPlate6LargePillowStyles.isValid(large1StyleId)
                    && BedPlate6DuvetMaterials.isValid(large1MaterialId);
        }
        if (index == 2) {
            return BedPlate6LargePillowStyles.isValid(large2StyleId)
                    && BedPlate6DuvetMaterials.isValid(large2MaterialId);
        }
        return false;
    }

    public int getLargePillowStyleId(int index) {
        return index == 1 ? large1StyleId : large2StyleId;
    }

    public int getLargePillowMaterialId(int index) {
        return index == 1 ? large1MaterialId : large2MaterialId;
    }

    public boolean hasMediumPillow() {
        return BedPlate6MediumPillowMaterials.isValid(mediumPillowMat);
    }

    public int getMediumPillowMat() {
        return mediumPillowMat;
    }

    public boolean hasSmallPillow() {
        return BedPlate6SmallPillowMaterials.isValid(smallPillowMat);
    }

    /** 给组合枕头绘制 / 选取用的槽位快照。床板2没有左右列，中号固定在 1 槽。 */
    public BedPlateSheetPillowSlots pillowSlots() {
        BedPlateSheetPillowSlots slots = new BedPlateSheetPillowSlots();
        if (hasLargePillowSlot(1)) {
            slots.tryAddLarge(large1StyleId, large1MaterialId);
        }
        if (hasLargePillowSlot(2)) {
            slots.tryAddLarge(large2StyleId, large2MaterialId);
        }
        if (hasMediumPillow()) {
            slots.tryAddMedium(mediumPillowMat);
        }
        if (hasSmallPillow()) {
            slots.tryAddSmall(smallPillowMat);
        }
        return slots;
    }

    public int getSmallPillowMat() {
        return smallPillowMat;
    }

    /**
     * 拼装形态：0=默认床体；1–4 见组合玩法。
     */
    public int getAssemblyId() {
        int largeCount = getLargePillowCount();
        boolean med = hasMediumPillow();
        boolean sm = hasSmallPillow();
        boolean bedding = hasDuvet() || hasCover();
        if (largeCount == 2 && med && sm && !bedding) {
            return 2;
        }
        if (largeCount == 1 && med && sm) {
            return 1;
        }
        if (med && largeCount == 0 && !sm) {
            return 3;
        }
        if (largeCount == 1 && !med && !sm) {
            return 4;
        }
        return 0;
    }

    public boolean canAddDuvet() {
        return !hasDuvet() && getLargePillowCount() < 2;
    }

    public boolean canAddCover() {
        return hasDuvet() && !hasCover();
    }

    /** 第二只大号仅允许在无床单/被套时放置（拼装 2 路径）。 */
    public boolean canAddLargePillow() {
        int count = getLargePillowCount();
        if (count >= 2) {
            return false;
        }
        if (count == 1) {
            return !hasDuvet() && !hasCover();
        }
        return true;
    }

    public boolean canAddMediumPillow() {
        return !hasMediumPillow();
    }

    public boolean canAddSmallPillow() {
        if (hasSmallPillow()) {
            return false;
        }
        int largeCount = getLargePillowCount();
        return (largeCount == 1 || largeCount == 2) && hasMediumPillow();
    }

    public void setDuvetMaterialId(int materialId) {
        if (materialId != 0 && !BedPlate6DuvetMaterials.isValid(materialId)) {
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

    public boolean tryAddLargePillow(int styleId, int materialId) {
        if (!canAddLargePillow()) {
            return false;
        }
        if (!BedPlate6LargePillowStyles.isValid(styleId)
                || !BedPlate6DuvetMaterials.isValid(materialId)
                || BedPlate6LargePillowItem.isUnavailableLargeVariant(styleId, materialId)) {
            return false;
        }
        if (!hasLargePillowSlot(1)) {
            large1StyleId = styleId;
            large1MaterialId = materialId;
        } else {
            large2StyleId = styleId;
            large2MaterialId = materialId;
        }
        syncClients();
        return true;
    }

    public boolean tryAddMediumPillow(int materialId) {
        if (!canAddMediumPillow() || !BedPlate6MediumPillowMaterials.isValid(materialId)) {
            return false;
        }
        this.mediumPillowMat = materialId;
        syncClients();
        return true;
    }

    public boolean tryAddSmallPillow(int materialId) {
        if (!canAddSmallPillow() || !BedPlate6SmallPillowMaterials.isValid(materialId)) {
            return false;
        }
        this.smallPillowMat = materialId;
        syncClients();
        return true;
    }

    public void clearAllBedding() {
        duvetMaterialId = 0;
        coverMaterialId = 0;
        large1StyleId = 0;
        large1MaterialId = 0;
        large2StyleId = 0;
        large2MaterialId = 0;
        mediumPillowMat = 0;
        smallPillowMat = 0;
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
        if (large1StyleId != 0) {
            tag.putInt(NBT_LG1_STYLE, large1StyleId);
            tag.putInt(NBT_LG1_MAT, large1MaterialId);
        }
        if (large2StyleId != 0) {
            tag.putInt(NBT_LG2_STYLE, large2StyleId);
            tag.putInt(NBT_LG2_MAT, large2MaterialId);
        }
        if (mediumPillowMat != 0) {
            tag.putInt(NBT_MEDIUM, mediumPillowMat);
        }
        if (smallPillowMat != 0) {
            tag.putInt(NBT_SMALL, smallPillowMat);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        duvetMaterialId = tag.getInt(NBT_DUVET);
        coverMaterialId = tag.getInt(NBT_COVER);
        large1StyleId = tag.getInt(NBT_LG1_STYLE);
        large1MaterialId = tag.getInt(NBT_LG1_MAT);
        large2StyleId = tag.getInt(NBT_LG2_STYLE);
        large2MaterialId = tag.getInt(NBT_LG2_MAT);
        mediumPillowMat = tag.getInt(NBT_MEDIUM);
        smallPillowMat = tag.getInt(NBT_SMALL);
        if (!BedPlate6DuvetMaterials.isValid(duvetMaterialId)) {
            duvetMaterialId = 0;
        }
        if (!BedPlate6DuvetCoverMaterials.isValid(coverMaterialId)) {
            coverMaterialId = 0;
        }
        if (!BedPlate6MediumPillowMaterials.isValid(mediumPillowMat)) {
            mediumPillowMat = 0;
        }
        if (!BedPlate6SmallPillowMaterials.isValid(smallPillowMat)) {
            smallPillowMat = 0;
        }
        if (large1StyleId != 0
                && (!BedPlate6LargePillowStyles.isValid(large1StyleId)
                        || !BedPlate6DuvetMaterials.isValid(large1MaterialId)
                        || BedPlate6LargePillowItem.isUnavailableLargeVariant(large1StyleId, large1MaterialId))) {
            large1StyleId = 0;
            large1MaterialId = 0;
        }
        if (large2StyleId != 0
                && (!BedPlate6LargePillowStyles.isValid(large2StyleId)
                        || !BedPlate6DuvetMaterials.isValid(large2MaterialId)
                        || BedPlate6LargePillowItem.isUnavailableLargeVariant(large2StyleId, large2MaterialId))) {
            large2StyleId = 0;
            large2MaterialId = 0;
        }
    }

    /** 客户端叠层渲染依赖此包；缺省则服务端已铺、客户端仍无床单/枕头。 */
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (duvetMaterialId != 0) {
            tag.putInt(NBT_DUVET, duvetMaterialId);
        }
        if (coverMaterialId != 0) {
            tag.putInt(NBT_COVER, coverMaterialId);
        }
        if (large1StyleId != 0) {
            tag.putInt(NBT_LG1_STYLE, large1StyleId);
            tag.putInt(NBT_LG1_MAT, large1MaterialId);
        }
        if (large2StyleId != 0) {
            tag.putInt(NBT_LG2_STYLE, large2StyleId);
            tag.putInt(NBT_LG2_MAT, large2MaterialId);
        }
        if (mediumPillowMat != 0) {
            tag.putInt(NBT_MEDIUM, mediumPillowMat);
        }
        if (smallPillowMat != 0) {
            tag.putInt(NBT_SMALL, smallPillowMat);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
        }
    }
}
