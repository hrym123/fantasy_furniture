package org.lanye.fantasy_furniture.content.furniture.livingroom.block;

import java.util.Optional;
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
 * <p>体素由 {@code tools/collision/voxel_pick_from_geo.py --manifest bed_plate6} 导出（每 geo 文件对应床上一只枕/叠放）。
 * <p>枕类用列向+3D；被套/被单仅用 3D，避免列向与传单薄层重叠时在边界闪烁。
 *
 * @see VoxelShapeRotation#rotateYFromNorth
 */
public final class BedPlate6PickShapesNorth {

  /** 准心体素命中对应的可拆卸床品层（与 {@link #pickStackByVoxelHit} 同源）。 */
  public enum PickedDecorLayer {
    NONE,
    SMALL_PILLOW,
    MEDIUM_REAR,
    MEDIUM_FRONT,
    MEDIUM_SOLO,
    LARGE_PILLOW,
    DUVET_COVER,
    DUVET
  }

  /** 仅用于浮点边界上的命中稳定，不改变 geo 盒尺寸。 */
  private static final double PICK_BOUNDARY_EPS = 1.0 / 256.0;

  private static final int TIER_SMALL = 0;
  private static final int TIER_LARGE = 1;
  private static final int TIER_MEDIUM_FRONT = 2;
  private static final int TIER_MEDIUM_REAR = 3;
  private static final int TIER_MEDIUM_SOLO = 2;
  private static final int TIER_COVER = 4;
  private static final int TIER_DUVET = 5;

  /**
   * 双中号时并集裁剪命中点常落在大号/小号/后枕柱内；固定 tier 会使大号(1)压过前枕(2)。
   * 双中号组合下将前/后中号提为最高枕 tier，大号/小号降为 3/4，不改变单中号或「大+单中」语义。
   */
  private record PillowPickTiers(
      int small, int large, int mediumFront, int mediumRear, int mediumSolo, int cover, int duvet) {

    static PillowPickTiers forPlate(BedPlate6BlockEntity plate) {
      if (plate.getMediumPillowCount() == 2) {
        return new PillowPickTiers(4, 3, 0, 1, TIER_MEDIUM_SOLO, 5, 6);
      }
      return new PillowPickTiers(
          TIER_SMALL,
          TIER_LARGE,
          TIER_MEDIUM_FRONT,
          TIER_MEDIUM_REAR,
          TIER_MEDIUM_SOLO,
          TIER_COVER,
          TIER_DUVET);
    }
  }

  private BedPlate6PickShapesNorth() {}

  /**
   * 与 {@link BedPlate6Block} / 床尾 Geo 渲染链一致：北向体素（z∈[0,32]、脚→头）先按 {@link BedBlock#FACING}
   * 旋转，再绕 Y 补 180°，映射到床尾格 MC 局部（+Z 为南）；与 export Y180 + manifest z-flip 成对，勿删第二步。
   */
  public static VoxelShape orientForBedFacing(VoxelShape northShape, Direction facing) {
    VoxelShape byFacing = VoxelShapeRotation.rotateYFromNorth(northShape, facing);
    return VoxelShapeRotation.rotate(byFacing, Rotation.CLOCKWISE_180);
  }

  /** 严格 3D 体内命中（床尾格局部 0～1 坐标）；与准心黑框 {@link #northOutlinePieceNorth} 同源。 */
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
   * 按体素命中解析应对应床品层：在<strong>所有</strong>命中的候选中取 tier 最小者（枕 &gt; 被套 &gt; 被单）。
   */
  public static PickedDecorLayer pickLayerByVoxelHit(
      BedPlate6BlockEntity plate, Vec3 hitWorld, BlockPos foot, Direction facing) {
    if (!plate.hasDuvet()) {
      return PickedDecorLayer.NONE;
    }
    PillowPickTiers tiers = PillowPickTiers.forPlate(plate);
    VoxelPickState pick = new VoxelPickState(hitWorld, foot, facing);
    if (plate.hasSmallPillow()) {
      pick.tryTier(tiers.small(), pillowSmallStackNorth(), PickedDecorLayer.SMALL_PILLOW);
    }
    if (plate.hasLargePillow()) {
      pick.tryTier(
          tiers.large(),
          largePillowNorth(plate.getLargePillowStyleId()),
          PickedDecorLayer.LARGE_PILLOW);
    }
    int n = plate.getMediumPillowCount();
    boolean large = plate.hasLargePillow();
    if (n == 2) {
      pick.tryTier(
          tiers.mediumRear(), pillowMediumPairRearNorth(), PickedDecorLayer.MEDIUM_REAR);
      pick.tryTier(
          tiers.mediumFront(), pillowMediumPairFrontNorth(), PickedDecorLayer.MEDIUM_FRONT);
    } else if (n == 1 && large) {
      pick.tryTier(
          tiers.mediumFront(), pillowMediumPairFrontNorth(), PickedDecorLayer.MEDIUM_FRONT);
    } else if (n == 1) {
      pick.tryTier(tiers.mediumSolo(), pillowMediumSoloNorth(), PickedDecorLayer.MEDIUM_SOLO);
    }
    if (plate.hasCover()) {
      pick.tryTier(tiers.cover(), duvetCoverNorth(), PickedDecorLayer.DUVET_COVER);
    }
    pick.tryTier(tiers.duvet(), duvetNorth(), PickedDecorLayer.DUVET);
    return pick.bestLayer != null ? pick.bestLayer : PickedDecorLayer.NONE;
  }

  /**
   * 按体素命中解析应对应床品：在<strong>所有</strong>命中的候选中取 tier 最小者（枕 &gt; 被套 &gt; 被单）；无匹配返回 {@code null}。
   */
  @Nullable
  public static ItemStack pickStackByVoxelHit(
      BedPlate6BlockEntity plate, Vec3 hitWorld, BlockPos foot, Direction facing) {
    PickedDecorLayer layer = pickLayerByVoxelHit(plate, hitWorld, foot, facing);
    return stackForPickedLayer(plate, layer);
  }

  @Nullable
  private static ItemStack stackForPickedLayer(BedPlate6BlockEntity plate, PickedDecorLayer layer) {
    return switch (layer) {
      case SMALL_PILLOW ->
          BedPlate6SmallPillowItem.stackForRegistry(plate.getSmallPillowMat());
      case LARGE_PILLOW ->
          BedPlate6LargePillowItem.stackForRegistry(
              plate.getLargePillowStyleId(), plate.getLargePillowMaterialId());
      case MEDIUM_REAR, MEDIUM_FRONT, MEDIUM_SOLO -> {
        int mat =
            layer == PickedDecorLayer.MEDIUM_FRONT && plate.getMediumPillowCount() == 2
                ? plate.getMediumPillowMatSecond()
                : plate.getMediumPillowMatFirst();
        yield BedPlate6MediumPillowItem.stackForRegistry(mat);
      }
      case DUVET_COVER -> BedPlate6DuvetCoverItem.stackForRegistry(plate.getCoverMaterialId());
      case DUVET -> BedPlate6DuvetItem.stackForRegistry(plate.getDuvetMaterialId());
      case NONE -> null;
    };
  }

  private static final class VoxelPickState {
    private final Vec3 hitWorld;
    private final BlockPos foot;
    private final Direction facing;
    private int bestTier = Integer.MAX_VALUE;
    @Nullable private PickedDecorLayer bestLayer;

    private VoxelPickState(Vec3 hitWorld, BlockPos foot, Direction facing) {
      this.hitWorld = hitWorld;
      this.foot = foot;
      this.facing = facing;
    }

    private void tryTier(int tier, VoxelShape north, PickedDecorLayer layer) {
      if (tier >= bestTier) {
        return;
      }
      if (matchesPickShape(north, hitWorld, foot, facing)) {
        bestTier = tier;
        bestLayer = layer;
      }
    }
  }

  private static boolean matchesPickShape(
      VoxelShape north, Vec3 hitWorld, BlockPos foot, Direction facing) {
    return containsFootLocal(orientForBedFacing(north, facing), hitWorld, foot);
  }

  /**
   * 射线与床品子件 3D 盒求交：沿射线取<strong>距眼最近</strong>的交点，同距时 tier 更小者优先（与 {@link #pickLayerByVoxelHit} 一致）。
   */
  @Nullable
  public static Vec3 clipFootLocalPreferDecorTier(
      BedPlate6BlockEntity plate, Direction facing, Vec3 localStart, Vec3 localEnd) {
    if (!plate.hasDuvet()) {
      return null;
    }
    PillowPickTiers tiers = PillowPickTiers.forPlate(plate);
    DecorClipState clip = new DecorClipState(facing, localStart, localEnd);
    if (plate.hasSmallPillow()) {
      clip.tryTier(tiers.small(), pillowSmallStackNorth());
    }
    if (plate.hasLargePillow()) {
      clip.tryTier(tiers.large(), largePillowNorth(plate.getLargePillowStyleId()));
    }
    int n = plate.getMediumPillowCount();
    boolean large = plate.hasLargePillow();
    if (n == 2) {
      clip.tryTier(tiers.mediumRear(), pillowMediumPairRearNorth());
      clip.tryTier(tiers.mediumFront(), pillowMediumPairFrontNorth());
    } else if (n == 1 && large) {
      clip.tryTier(tiers.mediumFront(), pillowMediumPairFrontNorth());
    } else if (n == 1) {
      clip.tryTier(tiers.mediumSolo(), pillowMediumSoloNorth());
    }
    if (plate.hasCover()) {
      clip.tryTier(tiers.cover(), duvetCoverNorth());
    }
    clip.tryTier(tiers.duvet(), duvetNorth());
    return clip.bestLocal;
  }

  private static final class DecorClipState {
    private static final double CLIP_TIER_TIE_EPS = 1.0E-6;

    private final Direction facing;
    private final Vec3 localStart;
    private final Vec3 localEnd;
    private int bestTier = Integer.MAX_VALUE;
    private double bestDist2 = Double.MAX_VALUE;
    @Nullable private Vec3 bestLocal;

    private DecorClipState(Direction facing, Vec3 localStart, Vec3 localEnd) {
      this.facing = facing;
      this.localStart = localStart;
      this.localEnd = localEnd;
    }

    private void tryTier(int tier, VoxelShape north) {
      Vec3 hit = closestClipOnNorthShape(north, facing, localStart, localEnd);
      if (hit == null) {
        return;
      }
      double d2 = localStart.distanceToSqr(hit);
      if (d2 < bestDist2 - CLIP_TIER_TIE_EPS
          || (d2 <= bestDist2 + CLIP_TIER_TIE_EPS && tier < bestTier)) {
        bestDist2 = d2;
        bestTier = tier;
        bestLocal = hit;
      }
    }
  }

  /**
   * 双中号：射线与并集求交时优先前枕盒，避免同色 {@link ItemStack} 无法区分时裁剪点落在后枕。
   */
  @Nullable
  public static Vec3 clipLocalDualMediumPreferFront(
      Direction facing, Vec3 localStart, Vec3 localEnd) {
    Vec3 front =
        closestClipOnNorthShape(pillowMediumPairFrontNorth(), facing, localStart, localEnd);
    if (front != null) {
      return front;
    }
    return closestClipOnNorthShape(pillowMediumPairRearNorth(), facing, localStart, localEnd);
  }

  @Nullable
  private static Vec3 closestClipOnNorthShape(
      VoxelShape north, Direction facing, Vec3 localStart, Vec3 localEnd) {
    VoxelShape oriented = orientForBedFacing(north, facing);
    Vec3 bestLocal = null;
    double bestDist2 = Double.MAX_VALUE;
    for (AABB box : oriented.toAabbs()) {
      Optional<Vec3> hit = box.clip(localStart, localEnd);
      if (hit.isPresent()) {
        double d2 = localStart.distanceToSqr(hit.get());
        if (d2 < bestDist2) {
          bestDist2 = d2;
          bestLocal = hit.get();
        }
      }
    }
    return bestLocal;
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
   * 客户端描边：与 {@link #pickLayerByVoxelHit} 同源，按层返回北向体素（双中号同色时勿用 {@link ItemStack} 比对）。
   */
  @Nullable
  public static VoxelShape northOutlinePieceNorth(
      BedPlate6BlockEntity plate, PickedDecorLayer layer) {
    if (!plate.hasDuvet() || layer == null || layer == PickedDecorLayer.NONE) {
      return null;
    }
    return switch (layer) {
      case SMALL_PILLOW -> pillowSmallStackNorth();
      case LARGE_PILLOW -> largePillowNorth(plate.getLargePillowStyleId());
      case MEDIUM_REAR -> pillowMediumPairRearNorth();
      case MEDIUM_FRONT -> pillowMediumPairFrontNorth();
      case MEDIUM_SOLO -> pillowMediumSoloNorth();
      case DUVET_COVER -> duvetCoverNorth();
      case DUVET -> duvetNorth();
      default -> null;
    };
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

  // voxel_pick_from_geo: sha256[:12]=ade50fbdd891 bed_plate6_duvet.geo.json
  private static VoxelShape duvetNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(0.0, 5.0, 0.0, 16.0, 7.0, 31.0));
    return s;
  }

  // voxel_pick_from_geo: sha256[:12]=811cc52d1691 bed_plate6_duvet_cover.geo.json
  private static VoxelShape duvetCoverNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(0.0, 3.5522, 0.0, 16.0, 9.4, 24.0));
    return s;
  }

  // voxel_pick_from_geo: sha256[:12]=900315c514d5 bed_plate6_pillow_large_striped.geo.json
  private static VoxelShape pillowLargeStripedNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(1.5, 7.0, 29.75, 14.5, 14.0, 30.25));
    s = Shapes.or(s, Block.box(2.5, 7.0, 29.0, 13.5, 14.0, 31.0));
    return s;
  }

  // voxel_pick_from_geo: sha256[:12]=ff701ee47813 bed_plate6_pillow_large_plain.geo.json
  private static VoxelShape pillowLargePlainNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(1.5, 7.0, 29.75, 14.5, 14.0, 30.25));
    s = Shapes.or(s, Block.box(2.5, 7.0, 29.0, 13.5, 14.0, 31.0));
    return s;
  }

  // voxel_pick_from_geo: sha256[:12]=daebd9c1f5b0 bed_plate6_pillow_large_plaid.geo.json
  private static VoxelShape pillowLargePlaidNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(1.5, 7.0, 29.75, 14.5, 14.0, 30.25));
    s = Shapes.or(s, Block.box(2.5, 7.0, 29.0, 13.5, 14.0, 31.0));
    return s;
  }

  // voxel_pick_from_geo: sha256[:12]=be33a8cd1183 bed_plate6_pillow_medium_solo.geo.json
  private static VoxelShape pillowMediumSoloNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(3.0, 7.0, 24.0, 13.0, 9.0, 31.0));
    s = Shapes.or(s, Block.box(2.0, 7.75, 24.0, 14.0, 8.25, 31.0));
    return s;
  }

  // voxel_pick_from_geo: sha256[:12]=43dd3e5fcd65 bed_plate6_pillow_medium_pair_front.geo.json
  private static VoxelShape pillowMediumPairFrontNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(5.0, 6.2284, 24.7042, 15.0, 13.4609, 29.2307));
    s = Shapes.or(s, Block.box(4.0, 6.5728, 25.5357, 16.0, 13.1165, 28.3992));
    return s;
  }

  // voxel_pick_from_geo: sha256[:12]=2db0c84353ad bed_plate6_pillow_medium_pair_rear.geo.json
  private static VoxelShape pillowMediumPairRearNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(1.0, 7.0, 28.7, 11.0, 14.0, 30.7));
    s = Shapes.or(s, Block.box(0.0, 7.0, 29.45, 12.0, 14.0, 29.95));
    return s;
  }

  // voxel_pick_from_geo: sha256[:12]=1a75624e4909 bed_plate6_pillow_small_stack.geo.json
  private static VoxelShape pillowSmallStackNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(0.4, 7.3085, 27.2706, 4.4, 11.0422, 28.8937));
    s = Shapes.or(s, Block.box(0.9, 7.6173, 27.0, 3.9, 10.7716, 29.0719));
    return s;
  }
}
