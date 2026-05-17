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
 * <p>体素由 {@code tools/bed6/bed_plate6_voxel_pick_from_geo.py} 导出（每 geo 文件对应床上一只枕/叠放，勿对单枕 geo 做 mirror 双盒）。
 * <p>枕类用列向+3D；被套/被单仅用 3D，避免列向与传单薄层重叠时在边界闪烁。
 *
 * @see org.lanye.reverie_core.geolib.client.BedPlateGeoBlockRenderer#rotateBlock
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
      pick.tryTier(tiers.small(), pillowSmallStackNorth(), PickedDecorLayer.SMALL_PILLOW, false);
    }
    if (plate.hasLargePillow()) {
      pick.tryTier(
          tiers.large(),
          largePillowNorth(plate.getLargePillowStyleId()),
          PickedDecorLayer.LARGE_PILLOW,
          false);
    }
    int n = plate.getMediumPillowCount();
    boolean large = plate.hasLargePillow();
    if (n == 2) {
      pick.tryTier(
          tiers.mediumRear(), pillowMediumPairRearNorth(), PickedDecorLayer.MEDIUM_REAR, false);
      pick.tryTier(
          tiers.mediumFront(), pillowMediumPairFrontNorth(), PickedDecorLayer.MEDIUM_FRONT, false);
    } else if (n == 1 && large) {
      pick.tryTier(
          tiers.mediumFront(), pillowMediumPairFrontNorth(), PickedDecorLayer.MEDIUM_FRONT, false);
    } else if (n == 1) {
      pick.tryTier(tiers.mediumSolo(), pillowMediumSoloNorth(), PickedDecorLayer.MEDIUM_SOLO, false);
    }
    if (plate.hasCover()) {
      pick.tryTier(tiers.cover(), duvetCoverNorth(), PickedDecorLayer.DUVET_COVER, true);
    }
    pick.tryTier(tiers.duvet(), duvetNorth(), PickedDecorLayer.DUVET, true);
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

    private void tryTier(int tier, VoxelShape north, PickedDecorLayer layer, boolean beddingLayer) {
      if (tier >= bestTier) {
        return;
      }
      if (matchesPickShape(north, hitWorld, foot, facing, beddingLayer)) {
        bestTier = tier;
        bestLayer = layer;
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

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=6de1eece2265 bed_plate6_duvet.geo.json
  private static VoxelShape duvetNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(0.0, 5.0, 1.0, 16.0, 7.0, 31.0));
    return s;
  }

  // bed_plate6_voxel_pick_from_geo: sha256[:12]=e4fa448485f0 bed_plate6_duvet_cover.geo.json
  private static VoxelShape duvetCoverNorth() {
    VoxelShape s = Shapes.empty();
    s = Shapes.or(s, Block.box(0.0, 3.5522, 0.0, 16.0, 9.4, 24.0));
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
