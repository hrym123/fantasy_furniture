package org.lanye.fantasy_furniture.content.furniture.cabinet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 柜内实体类展品：船 / 矿车 / 盔甲架，以及盔甲（穿在隐形盔甲架上的穿戴模型）。
 */
@OnlyIn(Dist.CLIENT)
final class CabinetDisplayEntities {

    private static final Map<Item, Entity> VEHICLE_CACHE = new IdentityHashMap<>();

    /** 专用于展示盔甲穿戴态；每帧换装，不按 Item 缓存。 */
    @Nullable
    private static ArmorStand armorMannequin;

    /** Tiny lift only; must not push into shelf. */
    private static final float FLOOR_ZFIGHT_LIFT = 0.001f;

    private CabinetDisplayEntities() {}

    static boolean tryDraw(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            float fitW,
            float fitH,
            float fitD) {
        if (tryDrawWornArmor(poseStack, bufferSource, light, stack, level, fitW, fitH, fitD)) {
            return true;
        }
        Entity entity = getOrCreateVehicle(stack, level);
        if (entity == null) {
            return false;
        }
        renderEntity(poseStack, bufferSource, light, entity, fitW, fitH, fitD);
        return true;
    }

    /**
     * Scaled content height matching {@link #tryDraw} / armor / vehicle paths.
     * @return height, or {@code -1} if this stack is not drawn as a display entity
     */
    static float measureRenderedHeight(
            ItemStack stack, Level level, float fitW, float fitH, float fitD) {
        EquipmentSlot armorSlot = armorEquipmentSlot(stack);
        if (armorSlot != null) {
            float[] ySpan = armorLocalY(armorSlot);
            float contentH = Math.max(0.05f, ySpan[1] - ySpan[0]);
            float contentW = armorLocalWidth(armorSlot);
            float contentD = contentW * 0.75f;
            final float ARMOR_CONTENT_PAD = 1.12f;
            float scale = CabinetModelBounds.uniformScaleToFit(
                    contentW * ARMOR_CONTENT_PAD,
                    contentH * ARMOR_CONTENT_PAD,
                    contentD * ARMOR_CONTENT_PAD,
                    fitW,
                    fitH,
                    fitD);
            return contentH * scale;
        }
        Entity entity = getOrCreateVehicle(stack, level);
        if (entity == null) {
            return -1f;
        }
        float w = Math.max(0.01f, entity.getBbWidth());
        float h = Math.max(0.01f, entity.getBbHeight());
        float scale = CabinetModelBounds.uniformScaleToFit(w, h, w, fitW, fitH, fitD);
        return h * scale;
    }

    /** 盔甲 / 鞘翅：穿在隐形盔甲架上；按该槽位盔甲外接缩放约 fit，底边贴层板。 */
    private static boolean tryDrawWornArmor(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            ItemStack stack,
            Level level,
            float fitW,
            float fitH,
            float fitD) {
        EquipmentSlot slot = armorEquipmentSlot(stack);
        if (slot == null) {
            return false;
        }
        ArmorStand stand = mannequin(level);
        if (stand == null) {
            return false;
        }
        clearArmor(stand);
        stand.setItemSlot(slot, stack.copy());

        // 人体盔甲在盔甲架上的大致局部外接（脚底=0）；偏大估计以免缩放过猛撑破空腔
        float[] ySpan = armorLocalY(slot);
        float contentH = Math.max(0.05f, ySpan[1] - ySpan[0]);
        float contentW = armorLocalWidth(slot);
        float contentD = contentW * 0.75f;
        // Mesh AABB estimates run small → pad content so uniformScale cannot overflow cubby.
        // Mild pad only (keep armor size similar to approved look; no aggressive shrink).
        final float ARMOR_CONTENT_PAD = 1.12f;
        float padW = contentW * ARMOR_CONTENT_PAD;
        float padH = contentH * ARMOR_CONTENT_PAD;
        float padD = contentD * ARMOR_CONTENT_PAD;
        float scale = CabinetModelBounds.uniformScaleToFit(padW, padH, padD, fitW, fitH, fitD);

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        // 把该件盔甲底边落到层板（略多压一点，避免头盔等看起来悬在腔中）
        poseStack.translate(0.0, -ySpan[0] + FLOOR_ZFIGHT_LIFT, 0.0);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        try {
            dispatcher.render(stand, 0.0, 0.0, 0.0, 0.0f, 0.0f, poseStack, bufferSource, light);
        } finally {
            dispatcher.setRenderShadow(true);
        }
        poseStack.popPose();
        return true;
    }

    /** 盔甲架局部空间中该槽位盔甲大致 Y 范围 [min, max]。 */
    private static float[] armorLocalY(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> new float[] {1.15f, 2.25f};
            case CHEST -> new float[] {0.45f, 1.85f};
            case LEGS -> new float[] {0.05f, 1.15f};
            case FEET -> new float[] {0.00f, 0.65f};
            default -> new float[] {0.00f, 2.00f};
        };
    }

    private static float armorLocalWidth(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0.70f;
            case CHEST -> 0.70f;
            case LEGS -> 0.55f;
            case FEET -> 0.78f;
            default -> 0.60f;
        };
    }

    @Nullable
    private static EquipmentSlot armorEquipmentSlot(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        // 盔甲架物品本身走实体展柜，不在此穿戴
        if (stack.getItem() instanceof ArmorStandItem) {
            return null;
        }
        Equipable equipable = Equipable.get(stack);
        if (equipable == null) {
            return null;
        }
        EquipmentSlot slot = equipable.getEquipmentSlot();
        // HEAD/CHEST/LEGS/FEET（含鞘翅 CHEST）
        return slot.getType() == EquipmentSlot.Type.ARMOR ? slot : null;
    }

    @Nullable
    private static ArmorStand mannequin(Level level) {
        if (armorMannequin == null || armorMannequin.level() != level) {
            armorMannequin = EntityType.ARMOR_STAND.create(level);
            if (armorMannequin == null) {
                return null;
            }
            armorMannequin.setInvisible(true);
            armorMannequin.setNoBasePlate(true);
            armorMannequin.setShowArms(true);
            armorMannequin.setNoGravity(true);
            armorMannequin.setYRot(0.0f);
            armorMannequin.setXRot(0.0f);
            armorMannequin.yRotO = 0.0f;
            armorMannequin.xRotO = 0.0f;
            armorMannequin.yBodyRot = 0.0f;
            armorMannequin.yBodyRotO = 0.0f;
            armorMannequin.yHeadRot = 0.0f;
            armorMannequin.yHeadRotO = 0.0f;
        }
        return armorMannequin;
    }

    private static void clearArmor(ArmorStand stand) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                stand.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    /** BoatRenderer 在模型前上抬 0.375，柜内需抵消才能贴层板。 */
    private static final float BOAT_RENDER_Y_LIFT = 0.375f;

    private static void renderEntity(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            Entity entity,
            float fitW,
            float fitH,
            float fitD) {
        float w = Math.max(0.01f, entity.getBbWidth());
        float h = Math.max(0.01f, entity.getBbHeight());
        float scale = CabinetModelBounds.uniformScaleToFit(w, h, w, fitW, fitH, fitD);

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        if (entity instanceof Boat) {
            poseStack.translate(0.0, -BOAT_RENDER_Y_LIFT + FLOOR_ZFIGHT_LIFT, 0.0);
        } else {
            poseStack.translate(0.0, FLOOR_ZFIGHT_LIFT, 0.0);
        }

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        try {
            dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 0.0f, poseStack, bufferSource, light);
        } finally {
            dispatcher.setRenderShadow(true);
        }
        poseStack.popPose();
    }

    @Nullable
    private static Entity getOrCreateVehicle(ItemStack stack, Level level) {
        Item item = stack.getItem();
        if (!(item instanceof BoatItem || item instanceof MinecartItem || item instanceof ArmorStandItem)) {
            return null;
        }
        Entity cached = VEHICLE_CACHE.get(item);
        if (cached != null && cached.level() == level) {
            return cached;
        }
        Entity created = createVehicle(item, level);
        if (created != null) {
            created.setYRot(0.0f);
            created.setXRot(0.0f);
            created.yRotO = 0.0f;
            created.xRotO = 0.0f;
            VEHICLE_CACHE.put(item, created);
        }
        return created;
    }

    @Nullable
    private static Entity createVehicle(Item item, Level level) {
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
