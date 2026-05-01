package org.lanye.fantasy_furniture.bootstrap.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.lanye.fantasy_furniture.FantasyFurniture;

/**
 * 本模组 {@link BlockEntityType} 注册。
 * <p>
 * 带方块实体的家具由 {@link org.lanye.fantasy_furniture.core.geolib.AnimatedBlockRegistration} 写入同一
 * {@link #BLOCK_ENTITY_TYPES}，具体条目见 {@link org.lanye.fantasy_furniture.bootstrap.block.FurnitureAnimatedBlocks} /
 * {@link org.lanye.fantasy_furniture.bootstrap.block.ModBlocks} 中的
 * {@link org.lanye.fantasy_furniture.core.geolib.AnimatedBlockEntry} 字段。
 */
public final class ModBlockEntities {

    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FantasyFurniture.MODID);

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
