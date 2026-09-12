package org.lanye.fantasy_furniture.content.furniture.cabinet;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.fantasy_furniture.content.furniture.cabinet.blockentity.CabinetBlockEntity;
import org.lanye.reverie_core.util.VoxelShapeRotation;

/**
 * 柜内展品点选：北向局部 AABB（原点底心）与射线求交，命中则展品优先于柜体。
 */
public final class CabinetItemPicks {

    public static final float MIN_EXTENT = 0.05f;
    private static final AABB UNIT = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private static final float MODEL_CONTENT_PAD = 1.12f;

    public record Size(float width, float height, float depth) {
        public Size atLeast(float min) {
            return new Size(Math.max(min, width), Math.max(min, height), Math.max(min, depth));
        }
    }

    @FunctionalInterface
    public interface SlotBox {
        @Nullable
        AABB northLocal(int slot);
    }

    private CabinetItemPicks() {}

    /**
     * 服务端 / 无模型时的占位尺寸（方块用碰撞外接，其余按 0.5 立方只缩小）。
     */
    public static Size estimateSize(ItemStack stack, CabinetKind.CavityFit fit) {
        if (stack == null || stack.isEmpty()) {
            return new Size(0f, 0f, 0f);
        }
        AABB bounds;
        if (stack.getItem() instanceof BlockItem blockItem) {
            bounds = occupancyBounds(blockItem.getBlock().defaultBlockState());
        } else {
            bounds = new AABB(0.0, 0.0, 0.0, 0.5, 0.5, 0.5);
        }
        float scale = uniformScaleToFit(
                (float) bounds.getXsize() * MODEL_CONTENT_PAD,
                (float) bounds.getYsize() * MODEL_CONTENT_PAD,
                (float) bounds.getZsize() * MODEL_CONTENT_PAD,
                fit.width(),
                fit.height(),
                fit.depth());
        return new Size(
                (float) bounds.getXsize() * scale,
                (float) bounds.getYsize() * scale,
                (float) bounds.getZsize() * scale);
    }

    public static float uniformScaleToFit(
            float sizeX, float sizeY, float sizeZ, float fitW, float fitH, float fitD) {
        float sx = fitW / Math.max(0.01f, sizeX);
        float sy = fitH / Math.max(0.01f, sizeY);
        float sz = fitD / Math.max(0.01f, sizeZ);
        return Math.min(1.0f, Math.min(sx, Math.min(sy, sz)));
    }

    /** 用占位高度堆叠后的射线拾取；未命中展品返回 -1。 */
    public static int pickOccupiedSlot(
            CabinetBlockEntity be, Direction facing, Vec3 eyeWorld, Vec3 lookWorld) {
        return pickNearestOccupied(
                be,
                facing,
                eyeWorld,
                lookWorld,
                slot -> estimatedNorthLocal(be, slot));
    }

    public static int pickNearestOccupied(
            CabinetBlockEntity be,
            Direction facing,
            Vec3 eyeWorld,
            Vec3 lookWorld,
            SlotBox boxes) {
        BlockPos master = be.getBlockPos();
        Vec3 eye = CabinetKind.toNorthLocal(master, facing, eyeWorld);
        Vec3 look = CabinetKind.lookToNorth(facing, lookWorld);
        double lenSq = look.lengthSqr();
        if (lenSq < 1.0e-8) {
            return -1;
        }
        Vec3 dir = look.scale(1.0 / Math.sqrt(lenSq));
        Vec3 end = eye.add(dir.scale(12.0));
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        int n = be.storageSlotCount();
        for (int i = 0; i < n; i++) {
            if (be.isEmpty(i)) {
                continue;
            }
            AABB box = boxes.northLocal(i);
            if (box == null || box.getXsize() <= 1.0e-4 || box.getYsize() <= 1.0e-4) {
                continue;
            }
            Optional<Vec3> pt = box.clip(eye, end);
            if (pt.isEmpty()) {
                continue;
            }
            double d = eye.distanceToSqr(pt.get());
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    @Nullable
    public static AABB estimatedNorthLocal(CabinetBlockEntity be, int slot) {
        ItemStack stack = be.getItem(slot);
        if (stack.isEmpty()) {
            return null;
        }
        CabinetStackFloors.SlotPose pose = CabinetStackFloors.pose(be, slot, CabinetItemPicks::estimatedHeight);
        if (pose.renderedH() <= 1.0e-4f) {
            return null;
        }
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, be.storageSlotCount());
        CabinetKind.CavityFit base = kind.cavityFit(i);
        Size size = estimateSize(stack, new CabinetKind.CavityFit(base.width(), pose.fitH(), base.depth()))
                .atLeast(MIN_EXTENT);
        return northLocalAabb(kind, i, pose.floorY(), size, be.itemYaw(i));
    }

    public static AABB northLocalAabb(
            CabinetKind kind, int slot, float floorY, Size size, int yawSteps) {
        float cx = kind.itemX(slot);
        float cz = kind.itemZ();
        float hw = size.width() * 0.5f;
        float hd = size.depth() * 0.5f;
        AABB box = new AABB(cx - hw, floorY, cz - hd, cx + hw, floorY + size.height(), cz + hd);
        return rotateY(box, cx, cz, CabinetYaw.degrees(yawSteps));
    }

    /** 北向局部盒（底心原点）转为方块体素并按柜朝向旋转。 */
    public static VoxelShape orientedOutline(AABB northLocal, Direction facing) {
        VoxelShape north = Shapes.create(
                northLocal.minX + 0.5,
                northLocal.minY,
                northLocal.minZ + 0.5,
                northLocal.maxX + 0.5,
                northLocal.maxY,
                northLocal.maxZ + 0.5);
        return VoxelShapeRotation.rotateYFromNorthLikeGeckoBlockRenderer(north, facing);
    }

    public static AABB rotateY(AABB box, double cx, double cz, float degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        // 0° / 180°：绕竖直轴对称，轴对齐盒不变
        if (Math.abs(sin) < 1.0e-8) {
            return box;
        }
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            double x = (i & 1) == 0 ? box.minX : box.maxX;
            double z = (i & 2) == 0 ? box.minZ : box.maxZ;
            double dx = x - cx;
            double dz = z - cz;
            double nx = cx + dx * cos - dz * sin;
            double nz = cz + dx * sin + dz * cos;
            minX = Math.min(minX, nx);
            maxX = Math.max(maxX, nx);
            minZ = Math.min(minZ, nz);
            maxZ = Math.max(maxZ, nz);
        }
        return new AABB(minX, box.minY, minZ, maxX, box.maxY, maxZ);
    }

    private static float estimatedHeight(CabinetBlockEntity be, int slot, float fitH) {
        ItemStack stack = be.getItem(slot);
        if (stack.isEmpty()) {
            return 0f;
        }
        CabinetKind kind = be.kind();
        int i = CabinetSlot.clampIndex(slot, be.storageSlotCount());
        CabinetKind.CavityFit base = kind.cavityFit(i);
        return estimateSize(stack, new CabinetKind.CavityFit(base.width(), fitH, base.depth())).height();
    }

    private static AABB occupancyBounds(BlockState state) {
        BlockGetter getter = EmptyBlockGetter.INSTANCE;
        VoxelShape shape = state.getShape(getter, BlockPos.ZERO, CollisionContext.empty());
        if (shape.isEmpty()) {
            return UNIT;
        }
        return shape.bounds();
    }
}
