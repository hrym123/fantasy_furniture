package org.lanye.fantasy_furniture.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.entity.FurnitureSeatEntity;

public final class ModEntities {

    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FantasyFurniture.MODID);

    public static final RegistryObject<EntityType<FurnitureSeatEntity>> FURNITURE_SEAT =
            ENTITY_TYPES.register(
                    "furniture_seat",
                    () ->
                            EntityType.Builder.of(FurnitureSeatEntity::new, MobCategory.MISC)
                                    .sized(0.01F, 0.01F)
                                    .clientTrackingRange(10)
                                    .updateInterval(1)
                                    .build(FantasyFurniture.MODID + ":furniture_seat"));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
