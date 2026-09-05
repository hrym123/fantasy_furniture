package org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.lanye.fantasy_furniture.bootstrap.block.BedPlate1Registration;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate1MaterialVariant;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate1Materials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1Block;
import org.lanye.reverie_core.geolib.bed.BedPlateBaseBlockEntity;

/**
 * 床板1型：床体贴图随材质档；寝具数据写在<strong>床尾左</strong>（放置格）BE，渲染仍在床尾右。
 */
public final class BedPlate1BlockEntity extends BedPlateBaseBlockEntity {

    private static final String NBT_DUVET = "DuvetMat";
    private static final String NBT_COVER = "CoverMat";

    private int duvetMaterialId;
    private int coverMaterialId;

    public BedPlate1BlockEntity(BlockPos pos, BlockState state) {
        super(BedPlate1Registration.blockEntityType().get(), pos, state);
    }

    public ResourceLocation getTextureLocation() {
        Block block = getBlockState().getBlock();
        if (block instanceof BedPlate1Block plate) {
            return BedPlate1Materials.texture(plate.variant());
        }
        return BedPlate1Materials.texture(BedPlate1MaterialVariant.DEFAULT);
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
        if (materialId != 0 && !BedPlate6DuvetMaterials.isSupportedOnBedPlate1(materialId)) {
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

    public void clearDuvet() {
        this.duvetMaterialId = 0;
        this.coverMaterialId = 0;
        syncClients();
    }

    public void setCoverMaterialId(int materialId) {
        if (materialId != 0 && !BedPlate6DuvetCoverMaterials.isSupportedOnBedPlate1(materialId)) {
            return;
        }
        if (materialId != 0 && !canAddCover()) {
            return;
        }
        this.coverMaterialId = materialId;
        syncClients();
    }

    public void clearCover() {
        this.coverMaterialId = 0;
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
        } else if (level != null && level.isClientSide) {
            requestModelDataUpdate();
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (level == null) {
            return super.getRenderBoundingBox();
        }
        BlockState st = getBlockState();
        if (!(st.getBlock() instanceof BedPlate1Block)) {
            return super.getRenderBoundingBox();
        }
        BlockPos footLeft = BedPlate1Block.footLeftPos(st, worldPosition);
        Direction facing = st.getValue(BedBlock.FACING);
        Direction right = facing.getClockWise();
        BlockPos footRight = footLeft.relative(right);
        BlockPos headLeft = footLeft.relative(facing);
        BlockPos headRight = footRight.relative(facing);
        return new AABB(footLeft)
                .minmax(new AABB(footRight))
                .minmax(new AABB(headLeft))
                .minmax(new AABB(headRight));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(NBT_DUVET, duvetMaterialId);
        tag.putInt(NBT_COVER, coverMaterialId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        duvetMaterialId = tag.getInt(NBT_DUVET);
        if (!BedPlate6DuvetMaterials.isValid(duvetMaterialId)) {
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
        tag.putInt(NBT_DUVET, duvetMaterialId);
        tag.putInt(NBT_COVER, coverMaterialId);
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
