package org.lanye.fantasy_furniture.content.soap;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBagBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapPaperBoxBlock;
import org.lanye.fantasy_furniture.content.soap.block.SoapSeriesWaterloggableBlock;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapBarBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBagBlockEntity;
import org.lanye.fantasy_furniture.content.soap.blockentity.SoapPaperBoxBlockEntity;
import org.lanye.fantasy_furniture.content.soap.item.SoapBarBlockItem;

/** 包装摞：空袋/空盒与带皂包装可混叠，共用摞方块与层数上限。 */
public final class SoapPackagingStackOps {

    private SoapPackagingStackOps() {}

    public static ItemStack packagedSoapStack(SoapBarAppearance appearance) {
        return SoapBarBlockItem.stackWithAppearance(ModBlocks.SOAP_BAR.item().get(), appearance);
    }

    /** 地上带袋皂 + 手持带袋皂 → 转为空袋摞方块（2 层带皂）。 */
    public static boolean beginBagStackFromSoapBars(
            Level level, BlockPos pos, BlockState soapBarState, SoapBarAppearance first, SoapBarAppearance second) {
        if (!first.isBagged() || !second.isBagged() || first.packagingTorn() || second.packagingTorn()) {
            return false;
        }
        Direction facing = soapBarState.getValue(SoapBarBlock.FACING);
        boolean waterlogged =
                soapBarState.hasProperty(SoapSeriesWaterloggableBlock.WATERLOGGED)
                        && soapBarState.getValue(SoapSeriesWaterloggableBlock.WATERLOGGED);
        BlockState bagState =
                ModBlocks.SOAP_PAPER_BAG
                        .block()
                        .get()
                        .defaultBlockState()
                        .setValue(SoapPaperBagBlock.FACING, facing)
                        .setValue(SoapPaperBagBlock.LAYERS, 2)
                        .setValue(SoapPaperBagBlock.MATERIAL, second.bagMaterialId())
                        .setValue(SoapPaperBagBlock.TORN, false)
                        .setValue(SoapSeriesWaterloggableBlock.WATERLOGGED, waterlogged);
        level.setBlock(pos, bagState, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof SoapPaperBagBlockEntity be) {
            be.clearLayers();
            be.pushSoapLayer(first);
            be.pushSoapLayer(second);
            SoapPaperBagBlock.syncStateFromEntity(level, pos, bagState, be);
        }
        return true;
    }

    /** 地上带盒皂 + 手持带盒皂 → 转为空盒摞方块（2 层带皂）。 */
    public static boolean beginBoxStackFromSoapBars(
            Level level, BlockPos pos, BlockState soapBarState, SoapBarAppearance first, SoapBarAppearance second) {
        if (!first.isBoxed() || !second.isBoxed() || first.packagingTorn() || second.packagingTorn()) {
            return false;
        }
        Direction facing = soapBarState.getValue(SoapBarBlock.FACING);
        boolean waterlogged =
                soapBarState.hasProperty(SoapSeriesWaterloggableBlock.WATERLOGGED)
                        && soapBarState.getValue(SoapSeriesWaterloggableBlock.WATERLOGGED);
        BlockState boxState =
                ModBlocks.SOAP_PAPER_BOX
                        .block()
                        .get()
                        .defaultBlockState()
                        .setValue(SoapPaperBoxBlock.FACING, facing)
                        .setValue(SoapPaperBoxBlock.LAYERS, 2)
                        .setValue(SoapPaperBoxBlock.MATERIAL, second.boxMaterialId())
                        .setValue(SoapPaperBoxBlock.TORN, false)
                        .setValue(SoapSeriesWaterloggableBlock.WATERLOGGED, waterlogged);
        level.setBlock(pos, boxState, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof SoapPaperBoxBlockEntity be) {
            be.clearLayers();
            be.pushSoapLayer(first);
            be.pushSoapLayer(second);
            SoapPaperBoxBlock.syncStateFromEntity(level, pos, boxState, be);
        }
        return true;
    }

    /** 地上带袋皂 + 手持空袋 → 袋摞（底层带皂、顶层空袋）。 */
    public static boolean beginBagStackFromSoapAndEmpty(
            Level level,
            BlockPos pos,
            BlockState soapBarState,
            SoapBarAppearance soap,
            int emptyBagMaterialId) {
        if (!soap.isBagged() || soap.packagingTorn() || !SoapPaperBagMaterials.isPlayable(emptyBagMaterialId)) {
            return false;
        }
        Direction facing = soapBarState.getValue(SoapBarBlock.FACING);
        boolean waterlogged =
                soapBarState.hasProperty(SoapSeriesWaterloggableBlock.WATERLOGGED)
                        && soapBarState.getValue(SoapSeriesWaterloggableBlock.WATERLOGGED);
        BlockState bagState =
                ModBlocks.SOAP_PAPER_BAG
                        .block()
                        .get()
                        .defaultBlockState()
                        .setValue(SoapPaperBagBlock.FACING, facing)
                        .setValue(SoapPaperBagBlock.LAYERS, 2)
                        .setValue(SoapPaperBagBlock.MATERIAL, emptyBagMaterialId)
                        .setValue(SoapPaperBagBlock.TORN, false)
                        .setValue(SoapSeriesWaterloggableBlock.WATERLOGGED, waterlogged);
        level.setBlock(pos, bagState, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof SoapPaperBagBlockEntity be) {
            be.clearLayers();
            be.pushSoapLayer(soap);
            be.pushLayer(emptyBagMaterialId);
            SoapPaperBagBlock.syncStateFromEntity(level, pos, bagState, be);
        }
        return true;
    }

    /** 地上带盒皂 + 手持空盒 → 盒摞（底层带皂、顶层空盒）。 */
    public static boolean beginBoxStackFromSoapAndEmpty(
            Level level,
            BlockPos pos,
            BlockState soapBarState,
            SoapBarAppearance soap,
            int emptyBoxMaterialId) {
        if (!soap.isBoxed() || soap.packagingTorn() || !SoapPaperBoxMaterials.isValid(emptyBoxMaterialId)) {
            return false;
        }
        Direction facing = soapBarState.getValue(SoapBarBlock.FACING);
        boolean waterlogged =
                soapBarState.hasProperty(SoapSeriesWaterloggableBlock.WATERLOGGED)
                        && soapBarState.getValue(SoapSeriesWaterloggableBlock.WATERLOGGED);
        BlockState boxState =
                ModBlocks.SOAP_PAPER_BOX
                        .block()
                        .get()
                        .defaultBlockState()
                        .setValue(SoapPaperBoxBlock.FACING, facing)
                        .setValue(SoapPaperBoxBlock.LAYERS, 2)
                        .setValue(SoapPaperBoxBlock.MATERIAL, emptyBoxMaterialId)
                        .setValue(SoapPaperBoxBlock.TORN, false)
                        .setValue(SoapSeriesWaterloggableBlock.WATERLOGGED, waterlogged);
        level.setBlock(pos, boxState, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof SoapPaperBoxBlockEntity be) {
            be.clearLayers();
            be.pushSoapLayer(soap);
            be.pushLayer(emptyBoxMaterialId);
            SoapPaperBoxBlock.syncStateFromEntity(level, pos, boxState, be);
        }
        return true;
    }

    /** 带皂袋摞只剩 1 层时还原为地上带袋皂（可开合模型）。 */
    public static void collapseBagSoapStackToSoapBar(Level level, BlockPos pos, BlockState bagState) {
        if (!(level.getBlockEntity(pos) instanceof SoapPaperBagBlockEntity be) || be.layerCount() != 1) {
            return;
        }
        SoapBarAppearance soap = be.packagedSoapAt(0);
        if (soap == null) {
            return;
        }
        placeSoapBar(level, pos, bagState, soap);
    }

    /** 带皂盒摞只剩 1 层时还原为地上带盒皂。 */
    public static void collapseBoxSoapStackToSoapBar(Level level, BlockPos pos, BlockState boxState) {
        if (!(level.getBlockEntity(pos) instanceof SoapPaperBoxBlockEntity be) || be.layerCount() != 1) {
            return;
        }
        SoapBarAppearance soap = be.packagedSoapAt(0);
        if (soap == null) {
            return;
        }
        placeSoapBar(level, pos, boxState, soap);
    }

    private static void placeSoapBar(
            Level level, BlockPos pos, BlockState fromState, SoapBarAppearance appearance) {
        Direction facing =
                fromState.hasProperty(SoapBarBlock.FACING)
                        ? fromState.getValue(SoapBarBlock.FACING)
                        : Direction.NORTH;
        boolean waterlogged =
                fromState.hasProperty(SoapSeriesWaterloggableBlock.WATERLOGGED)
                        && fromState.getValue(SoapSeriesWaterloggableBlock.WATERLOGGED);
        BlockState soapState =
                ModBlocks.SOAP_BAR
                        .block()
                        .get()
                        .defaultBlockState()
                        .setValue(SoapBarBlock.FACING, facing)
                        .setValue(SoapBarBlock.DURABILITY, appearance.durability())
                        .setValue(SoapBarBlock.MATERIAL, appearance.materialId())
                        .setValue(SoapBarBlock.PACKAGED, true)
                        .setValue(SoapBarBlock.BOXED, appearance.isBoxed())
                        .setValue(SoapBarBlock.BAG_MATERIAL, appearance.packagingMaterialId())
                        .setValue(SoapBarBlock.PACKAGING_TORN, appearance.packagingTorn())
                        .setValue(SoapSeriesWaterloggableBlock.WATERLOGGED, waterlogged);
        level.setBlock(pos, soapState, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof SoapBarBlockEntity soapBe) {
            soapBe.setParticleMat(appearance.particleMatId(), false);
        }
    }

    public static void writeSoapBodyToLayerTag(CompoundTag entry, SoapBarAppearance appearance) {
        entry.putBoolean("HasSoap", true);
        entry.putInt("SoapDurability", appearance.durability());
        entry.putInt("SoapMat", appearance.materialId());
        entry.putInt("PartMat", appearance.particleMatId());
    }

    @Nullable
    public static SoapBarAppearance readSoapBodyFromLayerTag(CompoundTag entry) {
        if (!entry.getBoolean("HasSoap")) {
            return null;
        }
        int durability = entry.contains("SoapDurability")
                ? entry.getInt("SoapDurability")
                : entry.contains("SoapWear")
                        ? SoapBarDurability.fromLegacyWear(entry.getInt("SoapWear"))
                        : SoapBarAppearance.DEFAULT_DURABILITY;
        int mat = entry.contains("SoapMat") ? entry.getInt("SoapMat") : SoapBarAppearance.DEFAULT_MATERIAL;
        int part =
                entry.contains("PartMat") ? entry.getInt("PartMat") : SoapBarAppearance.pigmentToParticleMat(mat);
        return new SoapBarAppearance(durability, mat, 0, false, part, 0);
    }

    public static SoapBarAppearance baggedFromLayer(int bagMat, SoapBarAppearance body) {
        return new SoapBarAppearance(
                body.durability(), body.materialId(), bagMat, false, body.particleMatId(), 0);
    }

    public static SoapBarAppearance boxedFromLayer(int boxMat, SoapBarAppearance body) {
        return SoapBarAppearance.withBox(
                body.durability(), body.materialId(), boxMat, false, body.particleMatId());
    }
}
