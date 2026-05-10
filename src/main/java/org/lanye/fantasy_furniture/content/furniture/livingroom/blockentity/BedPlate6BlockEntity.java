package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6MediumPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6SmallPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/**
 * 床板 6：可选床单外观（仅方块实体数据，无独立方块）。材质编号 {@code 1..7} 对应床单贴图；被套为 {@code 1..6}。
 *
 * <p>枕头合法组合：大；大中；大中小；中；中中；中中小。有大号时至多一只中号；有小号时仅「大+一中」或「二中」。
 *
 * @see BedPlate6DuvetItem
 * @see org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem
 * @see org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem
 * @see org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem
 * @see org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem
 */
public final class BedPlate6BlockEntity extends BedPlateBaseBlockEntity {

    /** 当前材质 1..7；0 表示无床单。 */
    private static final String NBT_MAT = "DuvetMat";

    /** 被套材质 1..6；0 表示无被套（仅当有床单时可存在）。 */
    private static final String NBT_COVER = "CoverMat";

    /** 大号枕头款式 1..3；0 表示无（须先有床单）。 */
    private static final String NBT_LG_PILLOW_STYLE = "LgPillowStyle";

    /** 大号枕头材质 1..7（仅当款式非 0 时有效）。 */
    private static final String NBT_LG_PILLOW_MAT = "LgPillowMat";

    /** 中号枕头槽 0：先放置的一只；0 表示空。 */
    private static final String NBT_MD_PILLOW_0 = "MdPillow0";

    /** 中号枕头槽 1：后放置的一只；须槽 0 非空时才有意义。 */
    private static final String NBT_MD_PILLOW_1 = "MdPillow1";

    /** 小号枕头材质 1..6；0 表示无。 */
    private static final String NBT_SM_PILLOW = "SmPillow";

    /** 旧版布尔被单标记（仅加载兼容）。 */
    private static final String NBT_LEGACY_BOOL = "Duvet6";

    private int duvetMaterialId;
    private int coverMaterialId;
    private int largePillowStyleId;
    private int largePillowMaterialId;
    private int mediumPillowMatFirst;
    private int mediumPillowMatSecond;
    private int smallPillowMat;

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

    /** 0 表示无被套，否则为 1..{@link BedPlate6DuvetCoverMaterials#COUNT}。 */
    public int getCoverMaterialId() {
        return coverMaterialId;
    }

    public boolean hasCover() {
        return BedPlate6DuvetCoverMaterials.isValid(coverMaterialId);
    }

    /** 0 表示无大号枕头，否则为 1..{@link BedPlate6LargePillowStyles#COUNT}。 */
    public int getLargePillowStyleId() {
        return largePillowStyleId;
    }

    /** 1..{@link BedPlate6DuvetMaterials#COUNT}，仅当 {@link #hasLargePillow()} 时为有效编号。 */
    public int getLargePillowMaterialId() {
        return largePillowMaterialId;
    }

    public boolean hasLargePillow() {
        return BedPlate6LargePillowStyles.isValid(largePillowStyleId)
                && BedPlate6DuvetMaterials.isValid(largePillowMaterialId);
    }

    /** 先放的中号枕头材质 {@code 1..6}；0 表示该槽空。 */
    public int getMediumPillowMatFirst() {
        return mediumPillowMatFirst;
    }

    /** 后放的中号枕头材质；仅当 {@link #getMediumPillowCount()} 为 2 时非 0。 */
    public int getMediumPillowMatSecond() {
        return mediumPillowMatSecond;
    }

    /** {@code 0}、{@code 1} 或 {@code 2}。 */
    public int getMediumPillowCount() {
        if (!BedPlate6MediumPillowMaterials.isValid(mediumPillowMatFirst)) {
            return 0;
        }
        return BedPlate6MediumPillowMaterials.isValid(mediumPillowMatSecond) ? 2 : 1;
    }

    /** 小号枕头材质 {@code 1..6}；0 表示无。 */
    public int getSmallPillowMat() {
        return smallPillowMat;
    }

    public boolean hasSmallPillow() {
        return BedPlate6SmallPillowMaterials.isValid(smallPillowMat);
    }

    /**
     * 可新放小号：已有床单、尚无小号，且（大号且恰好一只中号）或（无大号且恰好两只中号）——即「两只枕头」后再叠小号。
     */
    public boolean canAddSmallPillow() {
        if (!hasDuvet() || hasSmallPillow()) {
            return false;
        }
        if (hasLargePillow()) {
            return getMediumPillowCount() == 1;
        }
        return getMediumPillowCount() == 2;
    }

    /** 当前小号是否与底枕组合一致（用于读档或改槽后校正）。 */
    public boolean smallPillowCombinationValid() {
        if (!hasSmallPillow()) {
            return true;
        }
        return (hasLargePillow() && getMediumPillowCount() == 1)
                || (!hasLargePillow() && getMediumPillowCount() == 2);
    }

    /**
     * 服务端：设置两只中号槽；{@code first==0} 时清空两只；否则 {@code first} 须为合法材质，{@code second} 为 0 或合法材质。
     */
    public void setMediumPillowSlots(int first, int second) {
        if (first == 0) {
            this.mediumPillowMatFirst = 0;
            this.mediumPillowMatSecond = 0;
        } else {
            if (!hasDuvet() || !BedPlate6MediumPillowMaterials.isValid(first)) {
                return;
            }
            if (second != 0 && !BedPlate6MediumPillowMaterials.isValid(second)) {
                return;
            }
            this.mediumPillowMatFirst = first;
            this.mediumPillowMatSecond = second;
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

    /** 服务端：小号材质；{@code 0} 卸下。 */
    public void setSmallPillowMat(int materialId) {
        if (materialId != 0 && !BedPlate6SmallPillowMaterials.isValid(materialId)) {
            return;
        }
        if (materialId != 0 && !canAddSmallPillow()) {
            return;
        }
        this.smallPillowMat = materialId;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        }
    }

    /**
     * 服务端：{@code styleId == 0} 卸下大号枕头；否则须已有床单，且款式、材质在合法范围内。
     * 卸下大号时不清小号（由物品逻辑在返还小号物品后调用 {@link #setSmallPillowMat(int)}）。
     */
    public void setLargePillow(int styleId, int materialId) {
        if (styleId == 0) {
            this.largePillowStyleId = 0;
            this.largePillowMaterialId = 0;
        } else {
            if (!hasDuvet()) {
                return;
            }
            if (!BedPlate6LargePillowStyles.isValid(styleId)
                    || !BedPlate6DuvetMaterials.isValid(materialId)
                    || BedPlate6LargePillowItem.isUnavailableLargeVariant(styleId, materialId)) {
                return;
            }
            this.largePillowStyleId = styleId;
            this.largePillowMaterialId = materialId;
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

    /** 组合不合法时卸下小号（不掉落，用于读档校正；掉落由物品逻辑处理）。 */
    public void clearSmallIfCombinationInvalid() {
        if (hasSmallPillow() && !smallPillowCombinationValid()) {
            this.smallPillowMat = 0;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(
                        worldPosition,
                        getBlockState(),
                        getBlockState(),
                        Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
            }
        }
    }

    /** 服务端：{@code 0} 卸下被套；{@code 1..6} 铺上（仅当已有床单时由物品逻辑调用）。 */
    public void setCoverMaterialId(int materialId) {
        if (materialId != 0 && !BedPlate6DuvetCoverMaterials.isValid(materialId)) {
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

    /** 服务端：{@code 0} 卸下，{@code 1..7} 铺上对应材质。卸下床单时同时卸下被套与全部枕头。 */
    public void setDuvetMaterialId(int materialId) {
        if (materialId != 0 && !BedPlate6DuvetMaterials.isValid(materialId)) {
            return;
        }
        this.duvetMaterialId = materialId;
        if (materialId == 0) {
            this.coverMaterialId = 0;
            this.largePillowStyleId = 0;
            this.largePillowMaterialId = 0;
            this.mediumPillowMatFirst = 0;
            this.mediumPillowMatSecond = 0;
            this.smallPillowMat = 0;
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
        tag.putByte(NBT_LG_PILLOW_STYLE, (byte) largePillowStyleId);
        tag.putByte(NBT_LG_PILLOW_MAT, (byte) largePillowMaterialId);
        tag.putByte(NBT_MD_PILLOW_0, (byte) mediumPillowMatFirst);
        tag.putByte(NBT_MD_PILLOW_1, (byte) mediumPillowMatSecond);
        tag.putByte(NBT_SM_PILLOW, (byte) smallPillowMat);
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
        if (this.coverMaterialId == 7) {
            this.coverMaterialId = 6;
        }
        if (this.coverMaterialId != 0 && !BedPlate6DuvetCoverMaterials.isValid(this.coverMaterialId)) {
            this.coverMaterialId = 0;
        }
        if (this.coverMaterialId != 0 && !BedPlate6DuvetMaterials.isValid(this.duvetMaterialId)) {
            this.coverMaterialId = 0;
        }
        if (tag.contains(NBT_LG_PILLOW_STYLE, Tag.TAG_BYTE)) {
            int ps = tag.getByte(NBT_LG_PILLOW_STYLE);
            this.largePillowStyleId = (ps >= 0 && ps <= BedPlate6LargePillowStyles.COUNT) ? ps : 0;
        } else {
            this.largePillowStyleId = 0;
        }
        if (tag.contains(NBT_LG_PILLOW_MAT, Tag.TAG_BYTE)) {
            int pm = tag.getByte(NBT_LG_PILLOW_MAT);
            this.largePillowMaterialId = (pm >= 0 && pm <= BedPlate6DuvetMaterials.COUNT) ? pm : 0;
        } else {
            this.largePillowMaterialId = 0;
        }
        if (this.largePillowStyleId != 0
                && (!BedPlate6LargePillowStyles.isValid(this.largePillowStyleId)
                        || !BedPlate6DuvetMaterials.isValid(this.largePillowMaterialId)
                        || !BedPlate6DuvetMaterials.isValid(this.duvetMaterialId)
                        || BedPlate6LargePillowItem.isUnavailableLargeVariant(
                                this.largePillowStyleId, this.largePillowMaterialId))) {
            this.largePillowStyleId = 0;
            this.largePillowMaterialId = 0;
        }
        if (tag.contains(NBT_MD_PILLOW_0, Tag.TAG_BYTE)) {
            int p0 = tag.getByte(NBT_MD_PILLOW_0);
            this.mediumPillowMatFirst =
                    (p0 >= 0 && p0 <= BedPlate6MediumPillowMaterials.COUNT) ? p0 : 0;
        } else {
            this.mediumPillowMatFirst = 0;
        }
        if (tag.contains(NBT_MD_PILLOW_1, Tag.TAG_BYTE)) {
            int p1 = tag.getByte(NBT_MD_PILLOW_1);
            this.mediumPillowMatSecond =
                    (p1 >= 0 && p1 <= BedPlate6MediumPillowMaterials.COUNT) ? p1 : 0;
        } else {
            this.mediumPillowMatSecond = 0;
        }
        if (!BedPlate6MediumPillowMaterials.isValid(this.mediumPillowMatFirst)) {
            this.mediumPillowMatFirst = 0;
            this.mediumPillowMatSecond = 0;
        } else if (!BedPlate6MediumPillowMaterials.isValid(this.mediumPillowMatSecond)) {
            this.mediumPillowMatSecond = 0;
        }
        if (tag.contains(NBT_SM_PILLOW, Tag.TAG_BYTE)) {
            int s = tag.getByte(NBT_SM_PILLOW);
            this.smallPillowMat =
                    (s >= 0 && s <= BedPlate6SmallPillowMaterials.COUNT) ? s : 0;
        } else {
            this.smallPillowMat = 0;
        }
        if (!BedPlate6SmallPillowMaterials.isValid(this.smallPillowMat)) {
            this.smallPillowMat = 0;
        }
        if (!BedPlate6DuvetMaterials.isValid(this.duvetMaterialId)) {
            this.mediumPillowMatFirst = 0;
            this.mediumPillowMatSecond = 0;
            this.smallPillowMat = 0;
        }
        clearSmallIfCombinationInvalid();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putByte(NBT_MAT, (byte) duvetMaterialId);
        tag.putByte(NBT_COVER, (byte) coverMaterialId);
        tag.putByte(NBT_LG_PILLOW_STYLE, (byte) largePillowStyleId);
        tag.putByte(NBT_LG_PILLOW_MAT, (byte) largePillowMaterialId);
        tag.putByte(NBT_MD_PILLOW_0, (byte) mediumPillowMatFirst);
        tag.putByte(NBT_MD_PILLOW_1, (byte) mediumPillowMatSecond);
        tag.putByte(NBT_SM_PILLOW, (byte) smallPillowMat);
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
