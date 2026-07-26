package org.lanye.fantasy_furniture.content.tool;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import org.lanye.fantasy_furniture.bootstrap.tag.ModTags;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetCoverMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6DuvetMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6MediumPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6SmallPillowMaterials;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6PickShapesNorth;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6PickShapesNorth.PickedDecorLayer;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetCoverItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6DuvetItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6LargePillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6MediumPillowItem;
import org.lanye.fantasy_furniture.content.furniture.livingroom.item.BedPlate6SmallPillowItem;
import org.lanye.reverie_core.tool.SpoolRecolorHandlers;

/** 线轴对 {@link ModTags#COIL_RECOLORABLE_BLOCKS} 上已铺床品循环换色（准心体素与拆卸手套同源）。 */
public final class CoilRecolor {

    private CoilRecolor() {}

    /**
     * 主手线轴对可换色目标右击时，方块自身 {@code use} 应让出（由 reverie_core {@code fantasy_spool} 换色）。
     */
    public static boolean defersBlockUse(Player player, InteractionHand hand, BlockState state) {
        return SpoolRecolorHandlers.defersBlockUse(player, hand, state);
    }

    public static boolean apply(Level level, BlockState state, BlockPos pos, Vec3 hitLocation) {
        return resolvePlate(level, state, pos, hitLocation)
                .map(resolved -> cycleLayerServer(resolved.plate(), resolved.layer()))
                .orElse(false);
    }

    /** 不改 BE：解析准心层「下一档」对应床品物品，供 HUD。 */
    public static Optional<ItemStack> previewNextStack(
            Level level, BlockState state, BlockPos pos, Vec3 hitLocation) {
        return resolvePlate(level, state, pos, hitLocation).flatMap(CoilRecolor::stackForNext);
    }

    private record Resolved(BedPlate6BlockEntity plate, PickedDecorLayer layer) {}

    private static Optional<Resolved> resolvePlate(
            Level level, BlockState state, BlockPos pos, Vec3 hitLocation) {
        if (!state.is(ModTags.COIL_RECOLORABLE_BLOCKS)) {
            return Optional.empty();
        }
        BlockPos footPos = footPos(state, pos);
        BlockEntity be = level.getBlockEntity(footPos);
        if (!(be instanceof BedPlate6BlockEntity plate) || !plate.hasDuvet()) {
            return Optional.empty();
        }
        double ly = (hitLocation.y - footPos.getY()) * 16.0;
        if (ly < 5.0) {
            return Optional.empty();
        }
        Direction towardHead = state.getValue(BedBlock.FACING);
        PickedDecorLayer layer =
                BedPlate6PickShapesNorth.pickLayerByVoxelHit(plate, hitLocation, footPos, towardHead);
        if (layer == PickedDecorLayer.NONE) {
            return Optional.empty();
        }
        return Optional.of(new Resolved(plate, layer));
    }

    private static Optional<ItemStack> stackForNext(Resolved resolved) {
        BedPlate6BlockEntity plate = resolved.plate();
        return switch (resolved.layer()) {
            case DUVET -> {
                if (!plate.hasDuvet()) {
                    yield Optional.empty();
                }
                int next = nextMaterialId(plate.getDuvetMaterialId(), BedPlate6DuvetMaterials.COUNT);
                yield nonEmpty(BedPlate6DuvetItem.stackForRegistry(next));
            }
            case DUVET_COVER -> {
                if (!plate.hasCover()) {
                    yield Optional.empty();
                }
                int next = nextMaterialId(plate.getCoverMaterialId(), BedPlate6DuvetCoverMaterials.COUNT);
                yield nonEmpty(BedPlate6DuvetCoverItem.stackForRegistry(next));
            }
            case LARGE_PILLOW -> {
                if (!plate.hasLargePillow()) {
                    yield Optional.empty();
                }
                int style = plate.getLargePillowStyleId();
                int current = plate.getLargePillowMaterialId();
                int next = nextLargePillowMaterial(style, current);
                if (next == current) {
                    yield Optional.empty();
                }
                yield nonEmpty(BedPlate6LargePillowItem.stackForRegistry(style, next));
            }
            case MEDIUM_REAR -> {
                if (plate.getMediumPillowCount() != 2) {
                    yield Optional.empty();
                }
                int next =
                        nextMaterialId(plate.getMediumPillowMatFirst(), BedPlate6MediumPillowMaterials.COUNT);
                yield nonEmpty(BedPlate6MediumPillowItem.stackForRegistry(next));
            }
            case MEDIUM_FRONT -> {
                int mc = plate.getMediumPillowCount();
                if (mc == 2) {
                    int next =
                            nextMaterialId(
                                    plate.getMediumPillowMatSecond(), BedPlate6MediumPillowMaterials.COUNT);
                    yield nonEmpty(BedPlate6MediumPillowItem.stackForRegistry(next));
                }
                if (mc == 1 && plate.hasLargePillow()) {
                    int next =
                            nextMaterialId(
                                    plate.getMediumPillowMatFirst(), BedPlate6MediumPillowMaterials.COUNT);
                    yield nonEmpty(BedPlate6MediumPillowItem.stackForRegistry(next));
                }
                yield Optional.empty();
            }
            case MEDIUM_SOLO -> {
                if (plate.getMediumPillowCount() != 1 || plate.hasLargePillow()) {
                    yield Optional.empty();
                }
                int next =
                        nextMaterialId(plate.getMediumPillowMatFirst(), BedPlate6MediumPillowMaterials.COUNT);
                yield nonEmpty(BedPlate6MediumPillowItem.stackForRegistry(next));
            }
            case SMALL_PILLOW -> {
                if (!plate.hasSmallPillow()) {
                    yield Optional.empty();
                }
                int next = nextMaterialId(plate.getSmallPillowMat(), BedPlate6SmallPillowMaterials.COUNT);
                yield nonEmpty(BedPlate6SmallPillowItem.stackForRegistry(next));
            }
            case NONE -> Optional.empty();
        };
    }

    private static Optional<ItemStack> nonEmpty(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack);
    }

    private static boolean cycleLayerServer(BedPlate6BlockEntity plate, PickedDecorLayer layer) {
        return switch (layer) {
            case DUVET -> {
                if (!plate.hasDuvet()) {
                    yield false;
                }
                plate.setDuvetMaterialId(nextMaterialId(plate.getDuvetMaterialId(), BedPlate6DuvetMaterials.COUNT));
                yield true;
            }
            case DUVET_COVER -> {
                if (!plate.hasCover()) {
                    yield false;
                }
                plate.setCoverMaterialId(
                        nextMaterialId(plate.getCoverMaterialId(), BedPlate6DuvetCoverMaterials.COUNT));
                yield true;
            }
            case LARGE_PILLOW -> {
                if (!plate.hasLargePillow()) {
                    yield false;
                }
                int style = plate.getLargePillowStyleId();
                int next = nextLargePillowMaterial(style, plate.getLargePillowMaterialId());
                plate.setLargePillow(style, next);
                yield true;
            }
            case MEDIUM_REAR -> {
                if (plate.getMediumPillowCount() != 2) {
                    yield false;
                }
                int next =
                        nextMaterialId(plate.getMediumPillowMatFirst(), BedPlate6MediumPillowMaterials.COUNT);
                plate.setMediumPillowSlots(next, plate.getMediumPillowMatSecond());
                yield true;
            }
            case MEDIUM_FRONT -> {
                int mc = plate.getMediumPillowCount();
                if (mc == 2) {
                    int next =
                            nextMaterialId(
                                    plate.getMediumPillowMatSecond(), BedPlate6MediumPillowMaterials.COUNT);
                    plate.setMediumPillowSlots(plate.getMediumPillowMatFirst(), next);
                    yield true;
                }
                if (mc == 1 && plate.hasLargePillow()) {
                    int next =
                            nextMaterialId(
                                    plate.getMediumPillowMatFirst(), BedPlate6MediumPillowMaterials.COUNT);
                    plate.setMediumPillowSlots(next, 0);
                    yield true;
                }
                yield false;
            }
            case MEDIUM_SOLO -> {
                if (plate.getMediumPillowCount() != 1 || plate.hasLargePillow()) {
                    yield false;
                }
                int next =
                        nextMaterialId(plate.getMediumPillowMatFirst(), BedPlate6MediumPillowMaterials.COUNT);
                plate.setMediumPillowSlots(next, 0);
                yield true;
            }
            case SMALL_PILLOW -> {
                if (!plate.hasSmallPillow()) {
                    yield false;
                }
                int next = nextMaterialId(plate.getSmallPillowMat(), BedPlate6SmallPillowMaterials.COUNT);
                plate.setSmallPillowMat(next);
                yield true;
            }
            case NONE -> false;
        };
    }

    private static int nextMaterialId(int current, int count) {
        if (current < 1 || current > count) {
            return 1;
        }
        return current % count + 1;
    }

    private static int nextLargePillowMaterial(int styleId, int current) {
        int id = current;
        for (int i = 0; i < BedPlate6DuvetMaterials.COUNT; i++) {
            id = nextMaterialId(id, BedPlate6DuvetMaterials.COUNT);
            if (!BedPlate6LargePillowItem.isUnavailableLargeVariant(styleId, id)) {
                return id;
            }
        }
        return current;
    }

    private static BlockPos footPos(BlockState state, BlockPos pos) {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return pos;
        }
        return pos.relative(state.getValue(BedBlock.FACING).getOpposite());
    }
}
