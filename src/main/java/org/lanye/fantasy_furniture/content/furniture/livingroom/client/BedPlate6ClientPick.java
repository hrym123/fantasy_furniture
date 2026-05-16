package org.lanye.fantasy_furniture.content.furniture.livingroom.client;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.furniture.livingroom.BedPlate6CrosshairPick;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block;
import org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6PickShapesNorth;
import org.lanye.fantasy_furniture.content.furniture.livingroom.blockentity.BedPlate6BlockEntity;
import org.lanye.fantasy_furniture.debug.AgentDebugNdjson;
import org.lanye.reverie_core.util.VoxelShapeTranslation;

/** 床板 6 客户端准心 / 描边（读 {@link Minecraft#hitResult}，无 Mixin）。 */
@OnlyIn(Dist.CLIENT)
public final class BedPlate6ClientPick {

    private BedPlate6ClientPick() {}

    @Nullable
    public static HitResult currentCrosshairHit() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        return mc.hitResult;
    }

    public static ItemStack resolveCloneItemStack(Level level, BlockState state, BlockPos pos) {
        HitResult hit = currentCrosshairHit();
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) {
            return new ItemStack(ModBlocks.BED_PLATE6.item().get());
        }
        BlockState hitState = level.getBlockState(bhr.getBlockPos());
        if (!(hitState.getBlock() instanceof BedPlate6Block)) {
            return new ItemStack(ModBlocks.BED_PLATE6.item().get());
        }
        if (!BedPlate6Block.bedFootWorldPos(state, pos)
                .equals(BedPlate6Block.bedFootWorldPos(hitState, bhr.getBlockPos()))) {
            return new ItemStack(ModBlocks.BED_PLATE6.item().get());
        }
        return BedPlate6CrosshairPick.resolveClientPickForOutline(level, state, pos, hit);
    }

    /**
     * 客户端玩家描边：每帧按当前准心即时解析单子件；不回退整床并集（避免多组件同亮）。
     */
    public static VoxelShape clientPlayerOutlineShape(
            Level level,
            BlockState state,
            BlockPos pos,
            VoxelShape base,
            BedPlate6BlockEntity plate,
            Direction facing) {
        ItemStack resolved =
                BedPlate6CrosshairPick.resolveClientPickForOutline(
                        level, state, pos, currentCrosshairHit());

        String outlineMode;
        VoxelShape pieceLocal;
        if (resolved.isEmpty() || resolved.is(ModBlocks.BED_PLATE6.item().get())) {
            outlineMode = "base_only";
            pieceLocal = Shapes.empty();
        } else {
            VoxelShape pieceNorth = BedPlate6PickShapesNorth.northOutlinePieceNorth(plate, resolved);
            if (pieceNorth == null || pieceNorth.isEmpty()) {
                outlineMode = "base_only_unmapped";
                pieceLocal = Shapes.empty();
            } else {
                outlineMode = "single_piece";
                pieceLocal = BedPlate6PickShapesNorth.orientForBedFacing(pieceNorth, facing);
            }
        }

        // #region agent log
        JsonObject d = new JsonObject();
        d.addProperty("outlineMode", outlineMode);
        d.addProperty("atPos", pos.toString());
        if (!resolved.isEmpty()) {
            d.addProperty(
                    "item",
                    BuiltInRegistries.ITEM.getKey(resolved.getItem()).toString());
        }
        AgentDebugNdjson.append(
                "H9",
                "BedPlate6ClientPick:clientPlayerOutlineShape",
                "outline_resolve",
                d);
        // #endregion

        if (state.getValue(BedBlock.PART) != BedPart.FOOT) {
            double tx = -facing.getStepX();
            double ty = -facing.getStepY();
            double tz = -facing.getStepZ();
            VoxelShape inHead = VoxelShapeTranslation.translate(pieceLocal, tx, ty, tz);
            return pieceLocal.isEmpty() ? base : inHead;
        }
        return pieceLocal.isEmpty() ? base : pieceLocal;
    }
}
