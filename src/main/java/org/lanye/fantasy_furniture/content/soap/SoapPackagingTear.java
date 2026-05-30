package org.lanye.fantasy_furniture.content.soap;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;

/** 撕开包装：套袋皂两阶段去袋；空袋 / 空盒第二次右键还原完整态。 */
public final class SoapPackagingTear {

    private SoapPackagingTear() {}

    /** 套袋皂：进入撕开态（仍保留袋体 geo，换为 torn）。 */
    public static void beginTearSoapBar(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof SoapBarBlock block) || !state.getValue(block.PACKAGED)) {
            return;
        }
        if (state.getValue(block.PACKAGING_TORN)) {
            return;
        }
        level.setBlock(pos, state.setValue(block.PACKAGING_TORN, true), Block.UPDATE_ALL);
        playTearSound(level, pos);
    }

    /** 套袋皂：撕开态完成后去除袋体。 */
    public static void finishTearSoapBar(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof SoapBarBlock block) || !state.getValue(block.PACKAGED)) {
            return;
        }
        level.setBlock(
                pos,
                state.setValue(block.PACKAGED, false)
                        .setValue(block.BAG_MATERIAL, 0)
                        .setValue(block.PACKAGING_TORN, false),
                Block.UPDATE_ALL);
        playTearSound(level, pos);
    }

    /** 单层袋 / 盒摞：进入撕开态。 */
    public static void beginTearSingleLayerStack(
            Level level, BlockPos pos, BlockState state, BooleanProperty tornProperty) {
        if (state.getValue(tornProperty)) {
            return;
        }
        level.setBlock(pos, state.setValue(tornProperty, true), Block.UPDATE_ALL);
        playTearSound(level, pos);
    }

    /** 单层盒摞：撕开态还原为完整包装 geo。 */
    public static void restoreTornSingleLayerStack(
            Level level, BlockPos pos, BlockState state, BooleanProperty tornProperty) {
        if (!state.getValue(tornProperty)) {
            return;
        }
        level.setBlock(pos, state.setValue(tornProperty, false), Block.UPDATE_ALL);
        playTearSound(level, pos);
    }

    private static void playTearSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.8f, 1.05f);
    }
}
