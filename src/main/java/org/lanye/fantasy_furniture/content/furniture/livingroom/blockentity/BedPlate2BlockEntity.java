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
    private static final String NBT_MEDIUM_2 = "Md2Pillow";
    private static final String NBT_MEDIUM_3 = "Md3Pillow";
    private static final String NBT_SMALL = "SmPillow";
    private static final String NBT_LG1_GEO = "Lg1Geo";
    private static final String NBT_LG2_GEO = "Lg2Geo";
    private static final String NBT_MD_GEO = "MdGeo";

    private int duvetMaterialId;
    private int coverMaterialId;
    private int large1StyleId;
    private int large1MaterialId;
    private int large2StyleId;
    private int large2MaterialId;
    private int medium1Mat;
    private int medium2Mat;
    private int medium3Mat;
    private int smallPillowMat;
    /** -1 自动；0=p1，1=s1，2=s2。床板2的整套摆放方式不看这三项。 */
    private int large1Geo = -1;
    private int large2Geo = -1;
    private int mediumGeo = -1;
    /** -1 还没点过调试棒；0=P，1=S，2=X。整床枕头共用一个字母。 */
    private int pillowMode = -1;

    private static final String NBT_PILLOW_MODE = "PillowMode";

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
        return getMediumPillowCount() > 0;
    }

    public boolean hasMediumPillowSlot(int slot) {
        return BedPlate6MediumPillowMaterials.isValid(mediumMatRaw(slot));
    }

    public int getMediumPillowCount() {
        return (hasMediumPillowSlot(1) ? 1 : 0)
                + (hasMediumPillowSlot(2) ? 1 : 0)
                + (hasMediumPillowSlot(3) ? 1 : 0);
    }

    /** 第一只。选取门闸只要非 0。 */
    public int getMediumPillowMat() {
        for (int slot = 1; slot <= 3; slot++) {
            if (hasMediumPillowSlot(slot)) {
                return mediumMatRaw(slot);
            }
        }
        return 0;
    }

    public int getMediumPillowMat(int slot) {
        return hasMediumPillowSlot(slot) ? mediumMatRaw(slot) : 0;
    }

    private int mediumMatRaw(int slot) {
        return switch (slot) {
            case 2 -> medium2Mat;
            case 3 -> medium3Mat;
            default -> medium1Mat;
        };
    }

    public boolean hasSmallPillow() {
        return BedPlate6SmallPillowMaterials.isValid(smallPillowMat);
    }

    /** 给组合枕头绘制 / 选取用的槽位快照。中号按放下顺序占 1、2、3。 */
    public BedPlateSheetPillowSlots pillowSlots() {
        BedPlateSheetPillowSlots slots = new BedPlateSheetPillowSlots();
        if (hasLargePillowSlot(1)) {
            slots.tryAddLargeSide(1, large1StyleId, large1MaterialId);
            slots.setLargeGeo(1, large1Geo);
        }
        if (hasLargePillowSlot(2)) {
            slots.tryAddLargeSide(2, large2StyleId, large2MaterialId);
            slots.setLargeGeo(2, large2Geo);
        }
        for (int slot = 1; slot <= 3; slot++) {
            if (hasMediumPillowSlot(slot)) {
                slots.tryAddMediumSide(slot, mediumMatRaw(slot));
            }
        }
        if (hasMediumPillowSlot(1)) {
            slots.setMediumGeo(mediumGeo);
        }
        if (hasSmallPillow()) {
            slots.tryAddSmall(smallPillowMat);
        }
        slots.setPlate2Pose(resolvedPillowMode());
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
        return !hasDuvet();
    }

    public boolean canAddCover() {
        return hasDuvet() && !hasCover();
    }

    /** 大号至多两只。当前字母放不下时，放下后改成第一个放得下的字母（P 只有 1 槽，第二只会改成 S）。 */
    public boolean canAddLargePillow() {
        return getLargePillowCount() < 2;
    }

    public boolean canAddMediumPillow() {
        int next = getMediumPillowCount() + 1;
        if (next > 3) {
            return false;
        }
        int large = getLargePillowCount();
        boolean small = hasSmallPillow();
        if (large <= 1 && next <= 1) {
            return true;
        }
        if (large <= 2 && next <= 2) {
            return true;
        }
        return !small && large <= 3 && next <= 3;
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
        promoteModeForPillowCount();
        syncClients();
        return true;
    }

    public boolean tryAddMediumPillow(int materialId) {
        if (!canAddMediumPillow() || !BedPlate6MediumPillowMaterials.isValid(materialId)) {
            return false;
        }
        if (!hasMediumPillowSlot(1)) {
            medium1Mat = materialId;
        } else if (!hasMediumPillowSlot(2)) {
            medium2Mat = materialId;
        } else {
            medium3Mat = materialId;
        }
        promoteModeForPillowCount();
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

    public void clearLargePillowSlot(int index) {
        if (index == 2) {
            large2StyleId = 0;
            large2MaterialId = 0;
            large2Geo = -1;
        } else {
            large1StyleId = 0;
            large1MaterialId = 0;
            large1Geo = -1;
        }
        syncClients();
    }

    public void clearMediumPillow() {
        medium1Mat = 0;
        medium2Mat = 0;
        medium3Mat = 0;
        mediumGeo = -1;
        syncClients();
    }

    /** 卸下一只后把后面的前移，槽位号保持 1 起连续。 */
    public void clearMediumPillowSlot(int slot) {
        if (slot == 1) {
            medium1Mat = medium2Mat;
            medium2Mat = medium3Mat;
        } else if (slot == 2) {
            medium2Mat = medium3Mat;
        }
        medium3Mat = 0;
        if (!hasMediumPillow()) {
            mediumGeo = -1;
        }
        syncClients();
    }

    public void clearSmallPillow() {
        smallPillowMat = 0;
        syncClients();
    }

    public void clearAllBedding() {
        duvetMaterialId = 0;
        coverMaterialId = 0;
        large1StyleId = 0;
        large1MaterialId = 0;
        large2StyleId = 0;
        large2MaterialId = 0;
        medium1Mat = 0;
        medium2Mat = 0;
        medium3Mat = 0;
        smallPillowMat = 0;
        large1Geo = -1;
        large2Geo = -1;
        mediumGeo = -1;
        pillowMode = -1;
        syncClients();
    }

    /** 当前字母放不下时，改成 P、S、X 里第一个放得下的。 */
    private void promoteModeForPillowCount() {
        int mode = pillowMode >= 0 && pillowMode <= 2 ? pillowMode : resolvedPillowMode();
        if (pillowModeLegal(mode)) {
            return;
        }
        for (int candidate = 0; candidate < 3; candidate++) {
            if (pillowModeLegal(candidate)) {
                pillowMode = candidate;
                return;
            }
        }
    }

    /**
     * 调试棒：整套枕头换字母（P → S → X），槽位号保持 1、2、3。
     * 当前件数放不下的字母会跳过。没有枕头则 null。
     */
    public String cyclePillowPose() {
        if (!hasAnyPillow()) {
            return null;
        }
        int current = resolvedPillowMode();
        int next = current;
        for (int step = 1; step <= 3; step++) {
            int candidate = (current + step) % 3;
            if (pillowModeLegal(candidate)) {
                next = candidate;
                break;
            }
        }
        pillowMode = next;
        syncClients();
        return describePillowPose(next);
    }

    public int displayedPillowMode() {
        return pillowMode >= 0 && pillowMode <= 2 ? pillowMode : resolvedPillowMode();
    }

    private boolean hasAnyPillow() {
        return getLargePillowCount() > 0 || hasMediumPillow() || hasSmallPillow();
    }

    /** 没点过、或存档里的字母已经放不下时，用 P、S、X 里第一个放得下的。 */
    private int resolvedPillowMode() {
        if (pillowMode >= 0 && pillowMode <= 2 && pillowModeLegal(pillowMode)) {
            return pillowMode;
        }
        for (int mode = 0; mode < 3; mode++) {
            if (pillowModeLegal(mode)) {
                return mode;
            }
        }
        return 1;
    }

    /** P 大号只到 1、中号到 1；S 大号到 2；X 大号和中号到 3，小号没有 X。 */
    private boolean pillowModeLegal(int mode) {
        int large = getLargePillowCount();
        int medium = getMediumPillowCount();
        int small = hasSmallPillow() ? 1 : 0;
        if (large + medium + small == 0) {
            return false;
        }
        return switch (mode) {
            case 0 -> large <= 1 && medium <= 1;
            case 1 -> large <= 2 && medium <= 2;
            case 2 -> small == 0 && large <= 3 && medium <= 3;
            default -> false;
        };
    }

    private String describePillowPose(int mode) {
        String letter = switch (mode) {
            case 2 -> "X";
            case 1 -> "S";
            default -> "P";
        };
        StringBuilder text = new StringBuilder();
        int large = getLargePillowCount();
        for (int slot = 1; slot <= large; slot++) {
            appendPose(text, "大" + letter + slot);
        }
        int medium = getMediumPillowCount();
        for (int slot = 1; slot <= medium; slot++) {
            appendPose(text, "中" + letter + slot);
        }
        if (hasSmallPillow()) {
            appendPose(text, "小" + letter + "1");
        }
        return text.toString();
    }

    private static void appendPose(StringBuilder text, String part) {
        if (text.length() > 0) {
            text.append('、');
        }
        text.append(part);
    }

    /** @return 切换后的 p1 / s1 / s2；该槽没有大号则 null */
    public String cycleLargePose(int index) {
        BedPlateSheetPillowSlots slots = pillowSlots();
        String next = slots.cycleLarge(index);
        if (next == null) {
            return null;
        }
        if (index == 2) {
            large2Geo = slots.largeGeo(2);
        } else {
            large1Geo = slots.largeGeo(1);
        }
        syncClients();
        return next;
    }

    /** @return 切换后的 p1 / s1 / s2；没有中号则 null */
    public String cycleMediumPose() {
        BedPlateSheetPillowSlots slots = pillowSlots();
        String next = slots.cycleMedium();
        if (next == null) {
            return null;
        }
        mediumGeo = slots.mediumGeo();
        syncClients();
        return next;
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
        writeMediums(tag);
        if (smallPillowMat != 0) {
            tag.putInt(NBT_SMALL, smallPillowMat);
        }
        if (pillowMode >= 0) {
            tag.putInt(NBT_PILLOW_MODE, pillowMode);
        }
        writeGeo(tag);
    }

    private void writeMediums(CompoundTag tag) {
        if (hasMediumPillowSlot(1)) {
            tag.putInt(NBT_MEDIUM, medium1Mat);
            if (mediumGeo >= 0) {
                tag.putInt(NBT_MD_GEO, mediumGeo);
            }
        }
        if (hasMediumPillowSlot(2)) {
            tag.putInt(NBT_MEDIUM_2, medium2Mat);
        }
        if (hasMediumPillowSlot(3)) {
            tag.putInt(NBT_MEDIUM_3, medium3Mat);
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
        medium1Mat = readMedium(tag, NBT_MEDIUM);
        medium2Mat = readMedium(tag, NBT_MEDIUM_2);
        medium3Mat = readMedium(tag, NBT_MEDIUM_3);
        smallPillowMat = tag.getInt(NBT_SMALL);
        pillowMode = tag.contains(NBT_PILLOW_MODE) ? tag.getInt(NBT_PILLOW_MODE) : -1;
        if (pillowMode < 0 || pillowMode > 2) {
            pillowMode = -1;
        }
        large1Geo = readGeo(tag, NBT_LG1_GEO);
        large2Geo = readGeo(tag, NBT_LG2_GEO);
        mediumGeo = readGeo(tag, NBT_MD_GEO);
        if (!BedPlate6DuvetMaterials.isValid(duvetMaterialId)) {
            duvetMaterialId = 0;
        }
        if (!BedPlate6DuvetCoverMaterials.isValid(coverMaterialId)) {
            coverMaterialId = 0;
        }
        if (!hasMediumPillow()) {
            mediumGeo = -1;
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
            large1Geo = -1;
        }
        if (large2StyleId != 0
                && (!BedPlate6LargePillowStyles.isValid(large2StyleId)
                        || !BedPlate6DuvetMaterials.isValid(large2MaterialId)
                        || BedPlate6LargePillowItem.isUnavailableLargeVariant(large2StyleId, large2MaterialId))) {
            large2StyleId = 0;
            large2MaterialId = 0;
            large2Geo = -1;
        }
    }

    private void writeGeo(CompoundTag tag) {
        if (large1StyleId != 0 && large1Geo >= 0) {
            tag.putInt(NBT_LG1_GEO, large1Geo);
        }
        if (large2StyleId != 0 && large2Geo >= 0) {
            tag.putInt(NBT_LG2_GEO, large2Geo);
        }
    }

    private static int readMedium(CompoundTag tag, String key) {
        int mat = tag.getInt(key);
        return BedPlate6MediumPillowMaterials.isValid(mat) ? mat : 0;
    }

    private static int readGeo(CompoundTag tag, String key) {
        if (!tag.contains(key)) {
            return -1;
        }
        int geo = tag.getInt(key);
        return geo >= 0 && geo <= 2 ? geo : -1;
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
        writeMediums(tag);
        if (smallPillowMat != 0) {
            tag.putInt(NBT_SMALL, smallPillowMat);
        }
        if (pillowMode >= 0) {
            tag.putInt(NBT_PILLOW_MODE, pillowMode);
        }
        writeGeo(tag);
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
