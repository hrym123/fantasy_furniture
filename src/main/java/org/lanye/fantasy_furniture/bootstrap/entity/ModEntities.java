package org.lanye.fantasy_furniture.bootstrap.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.content.seat.entity.FurnitureSeatEntity;
import org.lanye.fantasy_furniture.content.sweeper.entity.SweeperRobotEntity;

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

    /** 碰撞：{@code python tools/geo_collision_box.py geo/entity/sweeper_robot.geo.json --entity-hitbox}（静态 cube 并集，16=1 格）。 */
    public static final RegistryObject<EntityType<SweeperRobotEntity>> SWEEPER_ROBOT =
            ENTITY_TYPES.register(
                    "sweeper_robot",
                    () ->
                            EntityType.Builder.of(SweeperRobotEntity::new, MobCategory.CREATURE)
                                    .sized(0.604F, 0.25F)
                                    .clientTrackingRange(12)
                                    .updateInterval(1)
                                    .build(FantasyFurniture.MODID + ":sweeper_robot"));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
