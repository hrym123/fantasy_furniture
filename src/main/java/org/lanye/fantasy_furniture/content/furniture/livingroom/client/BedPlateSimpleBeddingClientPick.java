package org.lanye.fantasy_furniture.content.furniture.livingroom.client;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateBedFootPos;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate2Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate3Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate4Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes.PickedLayer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlateSimpleBeddingShapes.Plate;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate2BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate3BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate4BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;

/** 床板2/3/4：准心 / 中键按独立床单·被套层选取（读 {@link Minecraft#hitResult}）。 */
@OnlyIn(Dist.CLIENT)
public final class BedPlateSimpleBeddingClientPick {

    private BedPlateSimpleBeddingClientPick() {}

    @Nullable
    public static Plate plateOf(BlockState state) {
        if (state.getBlock() instanceof BedPlate2Block) {
            return Plate.PLATE2;
        }
        if (state.getBlock() instanceof BedPlate3Block) {
            return Plate.PLATE3;
        }
        if (state.getBlock() instanceof BedPlate4Block) {
            return Plate.PLATE4;
        }
        return null;
    }

    public static ItemStack resolveCloneItemStack(Level level, BlockState state, BlockPos pos) {
        ItemStack bed = new ItemStack(state.getBlock().asItem());
        Plate plate = plateOf(state);
        if (plate == null) {
            return bed;
        }
        HitResult hit = Minecraft.getInstance().hitResult;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
            return bed;
        }
        BlockState hitState = level.getBlockState(bhr.getBlockPos());
        if (plateOf(hitState) != plate) {
            return bed;
        }
        BeddingView hitView = beddingAt(level, hitState, bhr.getBlockPos());
        BeddingView selfView = beddingAt(level, state, pos);
        if (hitView == null || selfView == null || !hitView.footPos().equals(selfView.footPos())) {
            return bed;
        }
        PickedLayer layer = resolveLayer(plate, hitState, hitView, bhr);
        if (layer == PickedLayer.DUVET_COVER && hitView.hasCover()) {
            ItemStack cover = BedPlate6DuvetCoverItem.stackForRegistry(hitView.coverMat());
            if (!cover.isEmpty()) {
                return cover;
            }
        }
        if (layer == PickedLayer.DUVET && hitView.hasDuvet()) {
            ItemStack duvet = BedPlate6DuvetItem.stackForRegistry(hitView.duvetMat());
            if (!duvet.isEmpty()) {
                return duvet;
            }
        }
        return bed;
    }

    public static PickedLayer resolveLayer(
            Plate plate, BlockState state, BeddingView view, BlockHitResult bhr) {
        return BedPlateSimpleBeddingShapes.pickLayer(
                plate,
                state,
                view != null && view.hasDuvet(),
                view != null && view.hasCover(),
                bhr.getLocation(),
                bhr.getBlockPos());
    }

    @Nullable
    public static VoxelShape crosshairOutlinePieceShape(
            Level level, BlockState state, BlockPos pos, BlockHitResult bhr) {
        Plate plate = plateOf(state);
        if (plate == null) {
            return null;
        }
        BeddingView view = beddingAt(level, state, pos);
        if (view == null || !view.hasDuvet()) {
            return null;
        }
        PickedLayer layer = resolveLayer(plate, state, view, bhr);
        return BedPlateSimpleBeddingShapes.outlineShape(
                plate, state, view.hasDuvet(), view.hasCover(), layer);
    }

    @Nullable
    public static BeddingView beddingAt(Level level, BlockState state, BlockPos anyPartPos) {
        BlockPos foot = BedPlateBedFootPos.footPos(state, anyPartPos);
        BlockEntity be = level.getBlockEntity(foot);
        if (be instanceof BedPlate2BlockEntity p2) {
            return new BeddingView(foot, p2.hasDuvet(), p2.getDuvetMaterialId(), p2.hasCover(), p2.getCoverMaterialId());
        }
        if (be instanceof BedPlate3BlockEntity p3) {
            return new BeddingView(foot, p3.hasDuvet(), p3.getDuvetMaterialId(), p3.hasCover(), p3.getCoverMaterialId());
        }
        if (be instanceof BedPlate4BlockEntity p4) {
            return new BeddingView(foot, p4.hasDuvet(), p4.getDuvetMaterialId(), p4.hasCover(), p4.getCoverMaterialId());
        }
        return null;
    }

    public record BeddingView(
            BlockPos footPos, boolean hasDuvet, int duvetMat, boolean hasCover, int coverMat) {}
}
