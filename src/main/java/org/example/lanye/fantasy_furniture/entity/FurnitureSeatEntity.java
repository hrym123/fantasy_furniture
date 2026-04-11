package org.example.lanye.fantasy_furniture.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.example.lanye.fantasy_furniture.common.seat.SeatConfig;
import org.example.lanye.fantasy_furniture.common.seat.SeatCooldown;
import org.example.lanye.fantasy_furniture.common.seat.SeatRegistry;
import org.example.lanye.fantasy_furniture.registry.ModEntities;

/**
 * 通用不可见坐骑：由 {@link SeatConfig} 决定位置与朝向；潜行下马（原版骑乘逻辑）；下马落点优先为锚点
 * 「朝向正前方一格」上的安全站立位置（{@link DismountHelper#findSafeDismountLocation}，与床/载具一致），再尝试
 * 锚点平面其余三侧；锚点处方块不再满足 {@link SeatConfig#blockValid()} 时逐出乘客并移除实体。
 */
public class FurnitureSeatEntity extends Entity {

    private static final String TAG_ANCHOR = "Anchor";
    private static final String TAG_CONFIG = "SeatConfigId";

    private BlockPos anchorPos = BlockPos.ZERO;
    private String configId = "";

    public FurnitureSeatEntity(EntityType<? extends FurnitureSeatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
        setInvisible(true);
    }

    public static FurnitureSeatEntity create(ServerLevel level, BlockPos anchor, BlockState state, String configId) {
        SeatConfig cfg = SeatRegistry.get(configId);
        if (cfg == null) {
            throw new IllegalArgumentException("unknown seat config: " + configId);
        }
        FurnitureSeatEntity seat = ModEntities.FURNITURE_SEAT.get().create(level);
        seat.anchorPos = anchor.immutable();
        seat.configId = configId;
        Vec3 p = cfg.seatWorldPosition(anchor);
        float yaw = cfg.yawDegrees().apply(state);
        seat.setPos(p.x, p.y, p.z);
        seat.setYRot(yaw);
        seat.setYHeadRot(yaw);
        return seat;
    }

    public BlockPos getAnchorPos() {
        return anchorPos;
    }

    public String getConfigId() {
        return configId;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(TAG_ANCHOR)) {
            anchorPos = BlockPos.of(tag.getLong(TAG_ANCHOR));
        }
        if (tag.contains(TAG_CONFIG)) {
            configId = tag.getString(TAG_CONFIG);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong(TAG_ANCHOR, anchorPos.asLong());
        tag.putString(TAG_CONFIG, configId);
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 safe = findSafeDismountNear(passenger);
        if (safe != null) {
            return safe;
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    /**
     * 与床醒来类似：在若干候选格上寻找可站立位置；优先坐骑朝向正前方一格（卡座「前面一格」）。
     */
    @Nullable
    private Vec3 findSafeDismountNear(LivingEntity passenger) {
        // Level 已是 CollisionGetter 子类型，不可用 instanceof CollisionGetter 模式匹配（编译器报错）
        CollisionGetter collision = level();
        Direction forward = Direction.fromYRot(getYRot());
        BlockPos front = anchorPos.relative(forward);
        Vec3 v = DismountHelper.findSafeDismountLocation(passenger.getType(), collision, front, false);
        if (v != null) {
            return v;
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (d == forward) {
                continue;
            }
            v = DismountHelper.findSafeDismountLocation(
                    passenger.getType(), collision, anchorPos.relative(d), false);
            if (v != null) {
                return v;
            }
        }
        v = DismountHelper.findSafeDismountLocation(passenger.getType(), collision, front, true);
        if (v != null) {
            return v;
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (d == forward) {
                continue;
            }
            v = DismountHelper.findSafeDismountLocation(
                    passenger.getType(), collision, anchorPos.relative(d), true);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (passenger instanceof ServerPlayer sp && !level().isClientSide()) {
            SeatCooldown.setCooldownUntil(
                    sp, level().getGameTime() + SeatCooldown.DEFAULT_COOLDOWN_TICKS);
        }
        if (!level().isClientSide() && getPassengers().isEmpty() && isAlive()) {
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || !(level() instanceof ServerLevel)) {
            return;
        }
        SeatConfig cfg = SeatRegistry.get(configId);
        if (cfg == null || !cfg.blockValid().test(level().getBlockState(anchorPos))) {
            ejectPassengers();
            if (isAlive()) {
                discard();
            }
        }
    }
}
