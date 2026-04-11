/**
 * 与 <strong>GeckoLib</strong> 相关的<strong>共通逻辑</strong>根包：可复用的基类、注册辅助、与方块实体/实体/物品动画相关的工具（不引用具体家具名称）。
 * 带方块实体的方块注册见 {@link org.lanye.fantasy_furniture.geolib.AnimatedBlockRegistration}、
 * {@link org.lanye.fantasy_furniture.geolib.AnimatedBlockSpec}；仅需「标准 Geo 方块 + Geo 方块物品」时可使用
 * {@link org.lanye.fantasy_furniture.geolib.GeolibAnimatedFactories} 一次生成 {@link AnimatedBlockSpec}。
 * <p>
 * <b>约定</b>
 * <ul>
 *   <li>需要自定义碰撞、交互或同步逻辑的 {@link net.minecraft.world.level.block.entity.BlockEntity} 仍单独实现；仅包装用的
 *   {@link net.minecraft.world.level.block.Block} / {@link net.minecraft.world.item.BlockItem} 不必再为每种家具建子类。</li>
 *   <li>仅客户端可用的类（渲染器、依赖 OpenGL 的辅助）放在子包 {@link org.lanye.fantasy_furniture.geolib.client}。</li>
 * </ul>
 */
package org.lanye.fantasy_furniture.geolib;
