package org.lanye.fantasy_furniture.content.furniture.livingroom.client;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1CollisionShapes;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate1CollisionShapes.PickedLayer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate1BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;

/** 床板1 客户端准心 / 中键（读 {@link Minecraft#hitResult}，无 Mixin）。 */
@OnlyIn(Dist.CLIENT)
public final class BedPlate1ClientPick {

    private BedPlate1ClientPick() {}

    public static ItemStack resolveCloneItemStack(Level level, BlockState state, BlockPos pos) {
        ItemStack bed = new ItemStack(state.getBlock().asItem());
        HitResult hit = Minecraft.getInstance().hitResult;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
            return bed;
        }
        BlockState hitState = level.getBlockState(bhr.getBlockPos());
        if (!(hitState.getBlock() instanceof BedPlate1Block)) {
            return bed;
        }
        BedPlate1BlockEntity hitDecor = BedPlate1Block.decorEntity(level, hitState, bhr.getBlockPos());
        BedPlate1BlockEntity selfDecor = BedPlate1Block.decorEntity(level, state, pos);
        if (hitDecor == null || hitDecor != selfDecor) {
            return bed;
        }
        PickedLayer layer = resolveLayer(level, hitState, bhr);
        if (layer == PickedLayer.DUVET_COVER && hitDecor.hasCover()) {
            ItemStack cover = BedPlate6DuvetCoverItem.stackForRegistry(hitDecor.getCoverMaterialId());
            if (!cover.isEmpty()) {
                return cover;
            }
        }
        if (layer == PickedLayer.DUVET && hitDecor.hasDuvet()) {
            ItemStack duvet = BedPlate6DuvetItem.stackForRegistry(hitDecor.getDuvetMaterialId());
            if (!duvet.isEmpty()) {
                return duvet;
            }
        }
        return bed;
    }

    public static PickedLayer resolveLayer(Level level, BlockState state, BlockHitResult bhr) {
        BedPlate1BlockEntity plate = BedPlate1Block.decorEntity(level, state, bhr.getBlockPos());
        boolean hasDuvet = plate != null && plate.hasDuvet();
        boolean hasCover = plate != null && plate.hasCover();
        return BedPlate1CollisionShapes.pickLayer(
                state, hasDuvet, hasCover, bhr.getLocation(), bhr.getBlockPos());
    }

    @Nullable
    public static VoxelShape crosshairOutlinePieceShape(
            Level level, BlockState state, BlockPos pos, BlockHitResult bhr) {
        BedPlate1BlockEntity plate = BedPlate1Block.decorEntity(level, state, pos);
        if (plate == null || !plate.hasDuvet()) {
            return null;
        }
        PickedLayer layer = resolveLayer(level, state, bhr);
        return BedPlate1CollisionShapes.outlineShape(
                state, plate.hasDuvet(), plate.hasCover(), layer);
    }
}
