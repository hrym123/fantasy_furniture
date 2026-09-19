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
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlateSheetPillowSlots;
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
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;

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
        if (layer == PickedLayer.MEDIUM && hitView.pillows() != null && hitView.pillows().hasPlate2Pose()) {
            int slot =
                    BedPlateSimpleBeddingShapes.mediumSlotAt(
                            plate, hitState, hitView.pillows(), bhr.getLocation(), bhr.getBlockPos());
            ItemStack medium =
                    BedPlate6MediumPillowItem.stackForRegistry(
                            hitView.pillows().mediumMatOnSide(slot == 0 ? 1 : slot));
            return medium.isEmpty() ? bed : medium;
        }
        ItemStack layerStack = stackForLayer(hitView, layer);
        return layerStack.isEmpty() ? bed : layerStack;
    }

    private static ItemStack stackForLayer(BeddingView view, PickedLayer layer) {
        return switch (layer) {
            case DUVET_COVER ->
                    view.hasCover()
                            ? BedPlate6DuvetCoverItem.stackForRegistry(view.coverMat())
                            : ItemStack.EMPTY;
            case DUVET ->
                    view.hasDuvet()
                            ? BedPlate6DuvetItem.stackForRegistry(view.duvetMat())
                            : ItemStack.EMPTY;
            case LARGE -> BedPlate6LargePillowItem.stackForRegistry(view.largeStyle(), view.largeMat());
            case MEDIUM -> BedPlate6MediumPillowItem.stackForRegistry(view.mediumMat());
            case SMALL -> BedPlate6SmallPillowItem.stackForRegistry(view.smallMat());
            case BODY -> ItemStack.EMPTY;
        };
    }

    public static PickedLayer resolveLayer(
            Plate plate, BlockState state, BeddingView view, BlockHitResult bhr) {
        return BedPlateSimpleBeddingShapes.pickLayer(
                plate,
                state,
                view != null && view.hasDuvet(),
                view != null && view.hasCover(),
                view != null ? view.largeStyle() : 0,
                view != null ? view.mediumMat() : 0,
                view != null ? view.smallMat() : 0,
                view != null ? view.pillows() : null,
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
        if (view == null || (!view.hasDuvet() && !view.hasCover() && !view.hasPillow())) {
            return null;
        }
        PickedLayer layer = resolveLayer(plate, state, view, bhr);
        return BedPlateSimpleBeddingShapes.outlineShape(
                plate,
                state,
                view.hasDuvet(),
                view.hasCover(),
                view.largeStyle(),
                view.mediumMat(),
                view.smallMat(),
                view.pillows(),
                layer);
    }

    @Nullable
    public static BeddingView beddingAt(Level level, BlockState state, BlockPos anyPartPos) {
        BlockPos foot = BedPlateBedFootPos.footPos(state, anyPartPos);
        BlockEntity be = level.getBlockEntity(foot);
        if (be instanceof BedPlate2BlockEntity p2) {
            int largeStyle = 0;
            int largeMat = 0;
            if (p2.hasLargePillowSlot(1)) {
                largeStyle = p2.getLargePillowStyleId(1);
                largeMat = p2.getLargePillowMaterialId(1);
            } else if (p2.hasLargePillowSlot(2)) {
                largeStyle = p2.getLargePillowStyleId(2);
                largeMat = p2.getLargePillowMaterialId(2);
            }
            return new BeddingView(
                    foot,
                    p2.hasDuvet(),
                    p2.getDuvetMaterialId(),
                    p2.hasCover(),
                    p2.getCoverMaterialId(),
                    largeStyle,
                    largeMat,
                    p2.getMediumPillowMat(),
                    p2.getSmallPillowMat(),
                    p2.pillowSlots());
        }
        if (be instanceof BedPlate3BlockEntity p3) {
            return viewFromSlots(
                    foot,
                    p3.hasDuvet(),
                    p3.getDuvetMaterialId(),
                    p3.hasCover(),
                    p3.getCoverMaterialId(),
                    p3.sheetPillows());
        }
        if (be instanceof BedPlate4BlockEntity p4) {
            return viewFromSlots(
                    foot,
                    p4.hasDuvet(),
                    p4.getDuvetMaterialId(),
                    p4.hasCover(),
                    p4.getCoverMaterialId(),
                    p4.sheetPillows());
        }
        return null;
    }

    private static BeddingView viewFromSlots(
            BlockPos foot,
            boolean hasDuvet,
            int duvetMat,
            boolean hasCover,
            int coverMat,
            BedPlateSheetPillowSlots slots) {
        return new BeddingView(
                foot,
                hasDuvet,
                duvetMat,
                hasCover,
                coverMat,
                slots.largeStyleId(),
                slots.largeMaterialId(),
                slots.mediumMat(),
                slots.smallMat(),
                slots);
    }

    public record BeddingView(
            BlockPos footPos,
            boolean hasDuvet,
            int duvetMat,
            boolean hasCover,
            int coverMat,
            int largeStyle,
            int largeMat,
            int mediumMat,
            int smallMat,
            BedPlateSheetPillowSlots pillows) {
        boolean hasPillow() {
            return largeStyle != 0 || mediumMat != 0 || smallMat != 0;
        }
    }
}
