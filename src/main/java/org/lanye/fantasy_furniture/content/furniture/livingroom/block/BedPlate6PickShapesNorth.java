package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lanye.reverie_core.util.VoxelShapeRotation;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6LargePillowStyles;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;

/**
 * 床板 6 床品/枕头在「北向基准 geo」下的选取用 {@link VoxelShape}（与 {@link BedPlate6Block#getShape} 合并）。
 *
 * <p>体素由 {@code tools/bed6/bed_plate6_voxel_pick_from_geo.py} 导出（每 geo 文件对应床上一只枕/叠放，勿对单枕 geo 做 mirror 双盒）。
 * <p>枕类用列向+3D；被套/被单仅用 3D，避免列向与传单薄层重叠时在边界闪烁。
 *
 * @see org.lanye.reverie_core.geolib.client.BedPlateGeoBlockRenderer#rotateBlock
 */
public final class BedPlate6PickShapesNorth {

  /** 仅用于浮点边界上的命中稳定，不改变 geo 盒尺寸。 */
  private static final double PICK_BOUNDARY_EPS = 1.0 / 256.0;

  /**
   * 列向选取竖直下限（<b>方块内 0～1 坐标</b>，与 {@link AABB} / {@code toAabbs()} 一致）：
   * 脚本被单 {@code y=5} → {@code 5/16}；勿与 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6ComponentPick} 的 {@code ly}（1/16 格）混用。
   */
  private static final double PICK_COLUMN_MIN_Y_BLOCKS = 5.0 / 16.0;

  private static final int TIER_SMALL = 0;
  private static final int TIER_LARGE = 1;
  private static final int TIER_MEDIUM_FRONT = 2;
  private static final int TIER_MEDIUM_REAR = 3;
  private static final int TIER_MEDIUM_SOLO = 2;
  private static final int TIER_COVER = 4;
  private static final int TIER_DUVET = 5;

  private BedPlate6PickShapesNorth() {}

  /** 与 {@link BedPlate6Block} 中床品体素朝向一致。 */
  public static VoxelShape orientForBedFacing(VoxelShape northShape, Direction facing) {
    VoxelShape r = VoxelShapeRotation.rotateYFromNorth(northShape, facing);
    if (facing.getAxis() != Direction.Axis.Y) {
      r = VoxelShapeRotation.rotate(r, Rotation.CLOCKWISE_180);
    }
    return r;
  }

  /**
   * 列向命中：准心射线常先打在薄被单顶面（{@code py≈6~7}），高于枕头盒 {@code minY} 但仍落在其 (x,z) 投影内时，
   * 仍应选中该枕/被套（tier 优先于被单），避免与大枕/被单在边界上来回切换。
   */
  public static boolean columnMatchesFootLocal(VoxelShape orientedShape, Vec3 hitWorld, BlockPos foot) {
    Vec3 corner = Vec3.atLowerCornerOf(foot);
    double px = hitWorld.x - corner.x;
    double py = hitWorld.y - corner.y;
    double pz = hitWorld.z - corner.z;
    for (AABB box : orientedShape.toAabbs()) {
      if (columnMatchesAabb(box, px, py, pz)) {
        return true;
      }
    }
    return false;
  }

  private static boolean columnMatchesAabb(AABB box, double px, double py, double pz) {
    double eps = PICK_BOUNDARY_EPS;
    if (px < box.minX - eps || px > box.maxX + eps) {
      return false;
    }
    if (pz < box.minZ - eps || pz > box.maxZ + eps) {
      return false;
    }
    if (py > box.maxY + eps) {
      return false;
    }
    return py >= PICK_COLUMN_MIN_Y_BLOCKS - eps;
  }

  /** 严格 3D 体内命中（方块内 0～1 坐标），与列向选取互补。 */
  private static boolean containsFootLocal(VoxelShape orientedShape, Vec3 hitWorld, BlockPos foot) {
    Vec3 corner = Vec3.atLowerCornerOf(foot);
    double px = hitWorld.x - corner.x;
    double py = hitWorld.y - corner.y;
    double pz = hitWorld.z - corner.z;
    for (AABB box : orientedShape.toAabbs()) {
      if (box.inflate(PICK_BOUNDARY_EPS).contains(px, py, pz)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 按体素命中解析应对应床品：在<strong>所有</strong>命中的候选中取 tier 最小者（枕 &gt; 被套 &gt; 被单）；无匹配返回 {@code null}。
   */
  @Nullable
  public static ItemStack pickStackByVoxelHit(
      BedPlate6BlockEntity plate, Vec3 hitWorld, BlockPos foot, Direction facing) {
    if (!plate.hasDuvet()) {
      return null;
    }
    VoxelPickState pick = new VoxelPickState(hitWorld, foot, facing);
    if (plate.hasSmallPillow()) {
      pick.tryTier(
          TIER_SMALL,
          pillowSmallStackNorth(),
          BedPlate6SmallPillowItem.stackForRegistry(plate.getSmallPillowMat()),
          false);
    }
    if (plate.hasLargePillow()) {
      pick.tryTier(
          TIER_LARGE,
          largePillowNorth(plate.getLargePillowStyleId()),
          BedPlate6LargePillowItem.stackForRegistry(
              plate.getLargePillowStyleId(), plate.getLargePillowMaterialId()),
          false);
    }
    int n = plate.getMediumPillowCount();
    boolean large = plate.hasLargePillow();
    if (n == 2) {
      pick.tryTier(
          TIER_MEDIUM_REAR,
          pillowMediumPairRearNorth(),
          BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst()),
          false);
      pick.tryTier(
          TIER_MEDIUM_FRONT,
          pillowMediumPairFrontNorth(),
          BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatSecond()),
          false);
    } else if (n == 1 && large) {
      pick.tryTier(
          TIER_MEDIUM_FRONT,
          pillowMediumPairFrontNorth(),
          BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst()),
          false);
    } else if (n == 1) {
      pick.tryTier(
          TIER_MEDIUM_SOLO,
          pillowMediumSoloNorth(),
          BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst()),
          false);
    }
    if (plate.hasCover()) {
      pick.tryTier(
          TIER_COVER,
          duvetCoverNorth(),
          BedPlate6DuvetCoverItem.stackForRegistry(plate.getCoverMaterialId()),
          true);
    }
    pick.tryTier(
        TIER_DUVET,
        duvetNorth(),
        BedPlate6DuvetItem.stackForRegistry(plate.getDuvetMaterialId()),
        true);
    return pick.best;
  }

  private static final class VoxelPickState {
    private final Vec3 hitWorld;
    private final BlockPos foot;
    private final Direction facing;
    private int bestTier = Integer.MAX_VALUE;
    @Nullable private ItemStack best;

    private VoxelPickState(Vec3 hitWorld, BlockPos foot, Direction facing) {
      this.hitWorld = hitWorld;
      this.foot = foot;
      this.facing = facing;
    }

    private void tryTier(int tier, VoxelShape north, ItemStack candidate, boolean beddingLayer) {
      if (tier >= bestTier) {
        return;
      }
      if (matchesPickShape(north, hitWorld, foot, facing, beddingLayer)) {
        bestTier = tier;
        best = candidate;
      }
    }
  }

  private static boolean matchesPickShape(
      VoxelShape north, Vec3 hitWorld, BlockPos foot, Direction facing, boolean beddingLayer) {
    VoxelShape oriented = orientForBedFacing(north, facing);
    if (beddingLayer) {
      return containsFootLocal(oriented, hitWorld, foot);
    }
    return columnMatchesFootLocal(oriented, hitWorld, foot)
        || containsFootLocal(oriented, hitWorld, foot);
  }

  /** 有床单时并入的北向选取体（与被套/枕头并集）。 */
  public static VoxelShape unionNorthForPick(BedPlate6BlockEntity plate) {
    if (!plate.hasDuvet()) {
      return Shapes.empty();
    }
    VoxelShape s = duvetNorth();
    if (plate.hasCover()) {
      s = Shapes.or(s, duvetCoverNorth());
    }
    if (plate.hasLargePillow()) {
      s = Shapes.or(s, largePillowNorth(plate.getLargePillowStyleId()));
    }
    int n = plate.getMediumPillowCount();
    boolean large = plate.hasLargePillow();
    if (n == 2) {
      s = Shapes.or(s, pillowMediumPairRearNorth());
      s = Shapes.or(s, pillowMediumPairFrontNorth());
    } else if (n == 1 && large) {
      s = Shapes.or(s, pillowMediumPairFrontNorth());
    } else if (n == 1) {
      s = Shapes.or(s, pillowMediumSoloNorth());
    }
    if (plate.hasSmallPillow()) {
      s = Shapes.or(s, pillowSmallStackNorth());
    }
    return s;
  }

  /**
   * 客户端描边：与 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6ComponentPick#stackForHit} 解析结果对应的北向体素，
   * 不含床垫。返回 {@code null} 表示仍使用 {@link #unionNorthForPick} 整床并集。
   */
  @Nullable
  public static VoxelShape northOutlinePieceNorth(BedPlate6BlockEntity plate, ItemStack resolved) {
    if (resolved == null || resolved.isEmpty() || resolved.is(ModBlocks.BED_PLATE6.item().get())) {
      return null;
    }
    if (!plate.hasDuvet()) {
      return null;
    }
    if (ItemStack.isSameItemSameTags(resolved, BedPlate6DuvetItem.stackForRegistry(plate.getDuvetMaterialId()))) {
      return duvetNorth();
    }
    if (plate.hasCover()
        && ItemStack.isSameItemSameTags(
            resolved, BedPlate6DuvetCoverItem.stackForRegistry(plate.getCoverMaterialId()))) {
      return duvetCoverNorth();
    }
    if (plate.hasSmallPillow()
        && ItemStack.isSameItemSameTags(
            resolved, BedPlate6SmallPillowItem.stackForRegistry(plate.getSmallPillowMat()))) {
      return pillowSmallStackNorth();
    }
    int n = plate.getMediumPillowCount();
    boolean large = plate.hasLargePillow();
    if (n == 2) {
      if (ItemStack.isSameItemSameTags(
          resolved, BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst()))) {
        return pillowMediumPairRearNorth();
      }
      if (ItemStack.isSameItemSameTags(
          resolved, BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatSecond()))) {
        return pillowMediumPairFrontNorth();
      }
    } else if (n == 1 && large) {
      if (ItemStack.isSameItemSameTags(
          resolved, BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst()))) {
        return pillowMediumPairFrontNorth();
      }
    } else if (n == 1) {
      if (ItemStack.isSameItemSameTags(
          resolved, BedPlate6MediumPillowItem.stackForRegistry(plate.getMediumPillowMatFirst()))) {
        return pillowMediumSoloNorth();
      }
    }
    if (plate.hasLargePillow()
        && ItemStack.isSameItemSameTags(
            resolved,
            BedPlate6LargePillowItem.stackForRegistry(
                plate.getLargePillowStyleId(), plate.getLargePillowMaterialId()))) {
      return largePillowNorth(plate.getLargePillowStyleId());
    }
    return null;
  }

  private static VoxelShape largePillowNorth(int styleId) {
    if (!BedPlate6LargePillowStyles.isValid(styleId)) {
      return Shapes.empty();
    }
    return switch (styleId) {
      case 1 -> pillowLargeStripedNorth();
      case 2 -> pillowLargePlainNorth();
      case 3 -> pillowLargePlaidNorth();
      default -> Shapes.empty();
    };
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=6de1eece2265 bed_plate6_duvet.geo.json
  private static VoxelShape duvetNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(0.0, 5.0, 1.0, 16.0, 7.0, 31.0));
    return s;
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=e4fa448485f0 bed_plate6_duvet_cover.geo.json
  private static VoxelShape duvetCoverNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(0.8, 6.4, 18.0, 15.2, 9.4, 24.0));
    s = Shapes.or(s, Block.box(0.3, 6.4, 0.0, 15.7, 8.4, 18.0));
    s = Shapes.or(s, Block.box(0.0, 3.5522, 0.0, 16.0, 6.5522, 24.0));
    s = Shapes.or(s, Block.box(14.6176, 6.5522, 0.0, 16.0, 9.1654, 18.0));
    s = Shapes.or(s, Block.box(14.5522, 6.4, 18.0, 16.0, 9.937, 24.0));
    s = Shapes.or(s, Block.box(0.0, 6.5522, 0.0, 1.3824, 9.1654, 18.0));
    s = Shapes.or(s, Block.box(0.0, 6.4, 18.0, 1.4478, 9.937, 24.0));
    return s;
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=8a9a8b6a3cdb bed_plate6_pillow_large_striped.geo.json
  private static VoxelShape pillowLargeStripedNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(1.5, 7.0, 29.75, 14.5, 14.0, 30.25));
    s = Shapes.or(s, Block.box(2.5, 7.0, 29.0, 13.5, 14.0, 31.0));
    return s;
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=576bab387250 bed_plate6_pillow_large_plain.geo.json
  private static VoxelShape pillowLargePlainNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(1.5, 7.0, 29.75, 14.5, 14.0, 30.25));
    s = Shapes.or(s, Block.box(2.5, 7.0, 29.0, 13.5, 14.0, 31.0));
    return s;
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=7dc26a739cda bed_plate6_pillow_large_plaid.geo.json
  private static VoxelShape pillowLargePlaidNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(1.5, 7.0, 29.75, 14.5, 14.0, 30.25));
    s = Shapes.or(s, Block.box(2.5, 7.0, 29.0, 13.5, 14.0, 31.0));
    return s;
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=522ae4609dda bed_plate6_pillow_medium_solo.geo.json
  private static VoxelShape pillowMediumSoloNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(3.0, 7.0, 24.0, 13.0, 9.0, 31.0));
    return s;
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=eb7dc3507626 bed_plate6_pillow_medium_pair_front.geo.json
  private static VoxelShape pillowMediumPairFrontNorth() {
    return Block.box(5.0, 6.2284, 24.7042, 15.0, 13.4609, 29.2307);
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=285c9348ed54 bed_plate6_pillow_medium_pair_rear.geo.json
  private static VoxelShape pillowMediumPairRearNorth() {
    return Block.box(1.0, 7.06, 28.7, 11.0, 14.06, 30.7);
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=9a35b8057aa9 bed_plate6_pillow_small_stack.geo.json
  private static VoxelShape pillowSmallStackNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(0.4, 7.3085, 27.2706, 4.4, 11.0422, 28.8937));
    s = Shapes.or(s, Block.box(0.9, 7.6173, 27.0, 3.9, 10.7716, 29.0719));
    return s;
  }
}
