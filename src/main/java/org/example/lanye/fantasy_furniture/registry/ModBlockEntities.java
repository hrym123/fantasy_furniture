package org.example.lanye.fantasy_furniture.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;

/**
 * 本模组 {@link BlockEntityType} 注册。
 * <p>
 * 带方块实体的家具由 {@link org.example.lanye.fantasy_furniture.geolib.AnimatedBlockRegistration} 写入同一
 * {@link #BLOCK_ENTITY_TYPES}，具体条目见 {@link org.example.lanye.fantasy_furniture.block.ModBlocks} 中的
 * {@link org.example.lanye.fantasy_furniture.geolib.AnimatedBlockEntry} 字段。
 */
public final class ModBlockEntities {

    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Fantasy_furniture.MODID);

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
