package org.lanye.fantasy_furniture.core.geolib;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

/**
 * {@link AnimatedBlockRegistration} 对单条条目的产出：方块、物品、{@link BlockEntityType} 的 {@link RegistryObject}。
 *
 * @param <BE> 方块实体类型
 */
public record AnimatedBlockEntry<BE extends BlockEntity>(
        RegistryObject<Block> block,
        RegistryObject<Item> item,
        RegistryObject<BlockEntityType<BE>> blockEntityType) {}
