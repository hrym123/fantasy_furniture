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
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;

/**
 * 床板 6 准心/中键/玉选取：按击中高度 +（必要时）床头格 / 沿床深度，在「被单 / 被套 / 大中小枕」间映射，供
 * {@link BedPlate6Block#getCloneItemStack} 与 Jade {@code usePickedResult} 使用。
 *
 * <p>枕头由 Geo 绘制且若未并入 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block#getShape}，射线常先落在床垫/被单
 * 薄层（局部 y≈5～7），仅靠高度会误判为被单；故用 {@code hitBlockPos} 与沿 {@link BedBlock#FACING} 的深度作补救。
 */
public final class BedPlate6ComponentPick {

    /** 自床尾格原点沿床头方向超过此距离（方块）且 y 仍落在被单带时，优先按枕头解析（与模型枕头靠床头一侧一致）。 */
    private static final double LOW_RAY_PILLOW_ALONG_BLOCKS = 14.0 / 16.0;

    private BedPlate6ComponentPick() {}

    /**
     * 根据射线击中点（世界坐标）返回应对应的床品物品或床方块物品；无 BE 或无法解析时返回空栈（由调用方回退）。
     *
     * @param hitBlockPos 准心方块命中的格子（与 {@link net.minecraft.world.phys.BlockHitResult#getBlockPos()} 一致）
     */
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
        BlockPos head = foot.relative(towardHead);
        boolean onHeadCell = hitBlockPos.equals(head);
        double alongBedBlocks = alongTowardHeadBlocks(hitLocation, foot, towardHead);

        boolean pillowZoneLowRay =
                ly < 7.0
                        && ly >= 5.0
                        && (onHeadCell || alongBedBlocks >= LOW_RAY_PILLOW_ALONG_BLOCKS)
                        && (plate.hasSmallPillow()
                                || plate.getMediumPillowCount() > 0
                                || plate.hasLargePillow());
        if (pillowZoneLowRay) {
            if (plate.hasSmallPillow()) {
                return BedPlate6SmallPillowItem.stackForRegistry(plate.getSmallPillowMat());
            }
            if (plate.getMediumPillowCount() > 0) {
                if (plate.getMediumPillowCount() == 2) {
                    return alongBedBlocks >= 18.0 / 16.0
                            ? BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatSecond())
                            : BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst());
                }
                return BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst());
            }
            if (plate.hasLargePillow()) {
                return BedPlate6LargePillowItem.stackForRegistry(
                        plate.getLargePillowStyleId(), plate.getLargePillowMaterialId());
            }
        }

        if (plate.hasSmallPillow() && ly >= 11.5) {
            return BedPlate6SmallPillowItem.stackForRegistry(plate.getSmallPillowMat());
        }
        if (plate.getMediumPillowCount() > 0 && ly >= 9.5) {
            if (plate.getMediumPillowCount() == 2) {
                return ly >= 10.5
                        ? BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatSecond())
                        : BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst());
            }
            return BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst());
        }
        if (plate.hasLargePillow() && ly >= 7.5) {
            return BedPlate6LargePillowItem.stackForRegistry(
                    plate.getLargePillowStyleId(), plate.getLargePillowMaterialId());
        }
        if (plate.hasCover() && ly >= 6.0) {
            return BedPlate6DuvetCoverItem.stackForRegistry(plate.getCoverMaterialId());
        }
        if (plate.hasDuvet() && ly >= 5.0) {
            return BedPlate6DuvetItem.stackForRegistry(plate.getDuvetMaterialId());
        }
        return bedPlateStack();
    }

    /**
     * 击中点相对床尾格 {@link Vec3#atLowerCornerOf(BlockPos) 角点}、沿床头方向（{@link BedBlock#FACING}）的投影长度，单位：方块（约 0～2 为整床范围）。
     */
    private static double alongTowardHeadBlocks(Vec3 hit, BlockPos foot, Direction towardHead) {
        Vec3 corner = Vec3.atLowerCornerOf(foot);
        Vec3 d = hit.subtract(corner);
        return d.x() * towardHead.getStepX()
                + d.y() * towardHead.getStepY()
                + d.z() * towardHead.getStepZ();
    }

    private static ItemStack bedPlateStack() {
        return new ItemStack(ModBlocks.BED_PLATE6.item().get());
    }
}
