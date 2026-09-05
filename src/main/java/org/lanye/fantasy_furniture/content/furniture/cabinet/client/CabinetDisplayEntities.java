package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * 柜内「实体类物品」展示：船 / 矿车 / 盔甲架等用对应实体渲染，而非扁平物品图标。
 */
@OnlyIn(Dist.CLIENT)
final class CabinetDisplayEntities {

    private static final Map<Item, Entity> CACHE = new IdentityHashMap<>();

    private CabinetDisplayEntities() {}

    static boolean tryDraw(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            float fit) {
        Entity entity = getOrCreate(stack, level);
        if (entity == null) {
            return false;
        }
        float w = Math.max(0.01f, entity.getBbWidth());
        float h = Math.max(0.01f, entity.getBbHeight());
        float scale = fit / Math.max(w, h);

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        // 实体原点在脚底；上移半高使包围盒中心对齐槽位中心
        poseStack.translate(0.0, -h * 0.5, 0.0);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        try {
            dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 0.0f, poseStack, bufferSource, light);
        } finally {
            dispatcher.setRenderShadow(true);
        }
        poseStack.popPose();
        return true;
    }

    @Nullable
    private static Entity getOrCreate(ItemStack stack, Level level) {
        Item item = stack.getItem();
        if (!(item instanceof BoatItem
                || item instanceof MinecartItem
                || item instanceof ArmorStandItem)) {
            return null;
        }
        Entity cached = CACHE.get(item);
        if (cached != null && cached.level() == level) {
            return cached;
        }
        Entity created = create(item, level);
        if (created != null) {
            created.setYRot(0.0f);
            created.setXRot(0.0f);
            created.yRotO = 0.0f;
            created.xRotO = 0.0f;
            CACHE.put(item, created);
        }
        return created;
    }

    @Nullable
    private static Entity create(Item item, Level level) {
        if (item instanceof ArmorStandItem) {
            ArmorStand stand = EntityType.ARMOR_STAND.create(level);
            if (stand != null) {
                stand.setShowArms(true);
                stand.setNoGravity(true);
            }
            return stand;
        }
        if (item instanceof MinecartItem) {
            return AbstractMinecart.createMinecart(level, 0.0, 0.0, 0.0, minecartType(item));
        }
        if (item instanceof BoatItem) {
            Boat.Type type = boatType(item);
            Boat boat = isChestBoat(item) ? new ChestBoat(level, 0.0, 0.0, 0.0) : new Boat(level, 0.0, 0.0, 0.0);
            boat.setVariant(type);
            return boat;
        }
        return null;
    }

    private static boolean isChestBoat(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        return path.contains("chest_boat") || path.contains("chest_raft");
    }

    private static Boat.Type boatType(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        for (Boat.Type type : Boat.Type.values()) {
            String name = type.getName();
            if (path.equals(name + "_boat")
                    || path.equals(name + "_chest_boat")
                    || path.equals(name + "_raft")
                    || path.equals(name + "_chest_raft")) {
                return type;
            }
        }
        return Boat.Type.OAK;
    }

    private static AbstractMinecart.Type minecartType(Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        return switch (path) {
            case "chest_minecart" -> AbstractMinecart.Type.CHEST;
            case "furnace_minecart" -> AbstractMinecart.Type.FURNACE;
            case "tnt_minecart" -> AbstractMinecart.Type.TNT;
            case "hopper_minecart" -> AbstractMinecart.Type.HOPPER;
            case "command_block_minecart" -> AbstractMinecart.Type.COMMAND_BLOCK;
            default -> AbstractMinecart.Type.RIDEABLE;
        };
    }
}
