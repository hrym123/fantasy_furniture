package org.lanye.fantasy_furniture.content.furniture.livingroom.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6PickShapesNorth;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 准心/中键/玉选取：优先按床尾格北向体素盒命中解析子件，供
 * {@link BedPlate6Block#getCloneItemStack} 与 Jade 使用。
 */
public final class BedPlate6ComponentPick {

    private BedPlate6ComponentPick() {}

    public static ItemStack stackForHit(
            BlockGetter level,
            BlockState state,
            BlockPos clickedPos,
            Vec3 hitLocation,
            BlockPos hitBlockPos) {
        BlockPos foot = BedPlate6Block.bedFootWorldPos(state, clickedPos);
        BlockEntity be = level.getBlockEntity(foot);
        if (!(be instanceof BedPlate6BlockEntity plate)) {
            return ItemStack.EMPTY;
        }
        double ly = (hitLocation.y - foot.getY()) * 16.0;
        if (ly < 5.0) {
            return bedPlateStack();
        }
        if (!plate.hasDuvet()) {
            return bedPlateStack();
        }

        Direction towardHead = state.getValue(BedBlock.FACING);

        ItemStack voxel =
                BedPlate6PickShapesNorth.pickStackByVoxelHit(plate, hitLocation, foot, towardHead);
        if (voxel != null && !voxel.isEmpty()) {
            return voxel;
        }

        return bedPlateStack();
    }

    private static ItemStack bedPlateStack() {
        return new ItemStack(ModBlocks.BED_PLATE6.item().get());
    }
}
