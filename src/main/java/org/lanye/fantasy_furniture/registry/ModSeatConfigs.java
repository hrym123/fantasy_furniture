package org.lanye.fantasy_furniture.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lanye.fantasy_furniture.block.registry.ModBlocks;
import org.lanye.fantasy_furniture.block.facing.BanquetteBlock;
import org.lanye.fantasy_furniture.common.seat.SeatConfig;
import org.lanye.fantasy_furniture.common.seat.SeatRegistry;
import org.lanye.fantasy_furniture.entity.FurnitureSeatEntity;

/**
 * 模组内各可坐家具的 {@link SeatConfig}，在 {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} 中注册。
 *
 * <h2>新增一种可坐方块（固定模板）</h2>
 * <ol>
 *   <li><strong>方块 id</strong>：在 {@code assets/…/blockstates}、{@code models} 等处使用的注册名；代码里用
 *       {@link ModBlocks} 或领域注册类取到 {@link Block} 实例，勿手写字符串 id。</li>
 *   <li><strong>配置 id 常量</strong>：在本类增加 {@code public static final String YOUR_SEAT_ID = "your_seat";}，
 *       字符串即为 {@link SeatRegistry#register(String, SeatConfig)} 的第一个参数；须与
 *       {@link FurnitureSeatEntity#create} 传入的 id、以及坐骑实体 NBT
 *       {@link FurnitureSeatEntity#NBT_SEAT_CONFIG_ID} 中保存的值<strong>完全一致</strong>（单一来源：先定常量，再在
 *       {@link #register()} 里使用）。</li>
 *   <li><strong>{@link SeatConfig}</strong>
 *       <ul>
 *         <li>{@code blockValid}：通常 {@code state -> state.is(你的方块)}，与入座匹配一致；</li>
 *         <li>{@code sitRangeBlockRelative}：相对方块最小角的入座检测 AABB（0–1 比例，与 {@link net.minecraft.world.level.block.Block#box}
 *             同坐标系），应覆盖玩家应能触发入座的站立区域，与方块<strong>碰撞箱</strong>对齐或略保守；</li>
 *         <li>{@code seatEntityOffsetFromBlockMin}：坐骑实体中心在锚点方块内的位置（格），宜与模型坐垫对齐，避免魔法数散落——在
 *             本类为每种家具设 {@code private static final double …} 并注释与 geo/碰撞的对应关系；</li>
 *         <li>{@code yawDegrees}：从 {@link net.minecraft.world.level.block.state.BlockState} 读朝向等，返回坐骑
 *             {@link net.minecraft.world.entity.Entity#getYRot()} 所用角度（度）。有 {@code FACING} 时常见为
 *             {@code state.getValue(XXX.FACING).getOpposite().toYRot()}，须与方块状态定义一致。</li>
 *       </ul>
 *   </li>
 *   <li><strong>方块侧</strong>：右键调用 {@link org.lanye.fantasy_furniture.common.seat.SeatInteraction#trySitFromBlockUse}；
 *       {@link SeatRegistry} 按注册顺序匹配第一条规则，若多家具范围重叠注意顺序或收紧 {@code sitRangeBlockRelative}。</li>
 *   <li><strong>NBT 约定</strong>：勿在业务代码中手写 {@code "Anchor"} / {@code "SeatConfigId"}，一律使用
 *       {@link FurnitureSeatEntity#NBT_ANCHOR_POS}、{@link FurnitureSeatEntity#NBT_SEAT_CONFIG_ID}。</li>
 * </ol>
 */
public final class ModSeatConfigs {

    /**
     * 与 {@link SeatRegistry}、{@link FurnitureSeatEntity} 存档字段 {@link FurnitureSeatEntity#NBT_SEAT_CONFIG_ID} 一致。
     */
    public static final String BANQUETTE_ID = "banquette";

    /** 与 {@link BanquetteBlock} 模型坐垫高度对齐：在基准偏移上再向下 5/16 格。 */
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
