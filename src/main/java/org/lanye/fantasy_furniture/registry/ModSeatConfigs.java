package org.lanye.fantasy_furniture.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lanye.fantasy_furniture.block.registry.ModBlocks;
import org.lanye.fantasy_furniture.block.facing.BanquetteBlock;
import org.lanye.fantasy_furniture.common.seat.SeatConfig;
import org.lanye.fantasy_furniture.common.seat.SeatRegistry;

/** 模组内各可坐家具的 {@link SeatConfig}，在 {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} 中注册。 */
public final class ModSeatConfigs {

    /** 与 {@link SeatRegistry}、{@link org.lanye.fantasy_furniture.entity.FurnitureSeatEntity} NBT 一致。 */
    public static final String BANQUETTE_ID = "banquette";

    /** 坐骑实体相对方块原点的 Y：在原先基础上再向下 5/16 格（约 0.3125）。 */
    private static final double BANQUETTE_SEAT_Y = 0.35 - 5.0 / 16.0;

    private ModSeatConfigs() {}

    public static void register() {
        Block banquette = ModBlocks.BANQUETTE.block().get();
        SeatRegistry.register(
                BANQUETTE_ID,
                new SeatConfig(
                        state -> state.is(banquette),
                        new AABB(0, 0, 0, 1, 1, 1),
                        new Vec3(0.5, BANQUETTE_SEAT_Y, 0.5),
                        state -> state.getValue(BanquetteBlock.FACING).getOpposite().toYRot()));
    }
}
