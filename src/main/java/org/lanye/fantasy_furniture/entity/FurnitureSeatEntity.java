package org.lanye.fantasy_furniture.entity;

import java.util.List;
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
import org.lanye.fantasy_furniture.common.seat.SeatConfig;
import org.lanye.fantasy_furniture.common.seat.SeatCooldown;
import org.lanye.fantasy_furniture.common.seat.SeatRegistry;
import org.lanye.fantasy_furniture.registry.ModEntities;

/**
 * 通用不可见坐骑：由 {@link SeatConfig} 决定位置与朝向；潜行下马（原版骑乘逻辑）。
 * 下马落点优先为 {@link SeatConfig#dismountStepDirectionFromAnchor()} 所指座椅正前方邻格（{@link DismountHelper#findSafeDismountLocation}），
 * 并沿该柱上下与略向下搜索；再尝试锚点平面其余水平邻格。
 * 锚点处方块不再满足 {@link SeatConfig#blockValid()} 时逐出乘客并移除实体。
 * <p>
 * <strong>存档 / 同步 NBT（键名以此类常量为唯一来源）</strong>：
 * <ul>
 *   <li>{@link #NBT_ANCHOR_POS}：锚点方块坐标，{@link BlockPos#asLong()}；</li>
 *   <li>{@link #NBT_SEAT_CONFIG_ID}：与 {@link org.lanye.fantasy_furniture.registry.ModSeatConfigs} 中注册的 id
 *       字符串一致（如 {@link org.lanye.fantasy_furniture.registry.ModSeatConfigs#BANQUETTE_ID}）。</li>
 * </ul>
 *
 * @see org.lanye.fantasy_furniture.registry.ModSeatConfigs 各可坐方块的配置 id 与 {@link SeatConfig} 登记
 */
public class FurnitureSeatEntity extends Entity {

    /** 锚点方块位置（NBT long，与 {@link BlockPos#of(long)} 互转）。 */
    public static final String NBT_ANCHOR_POS = "Anchor";

    /** 与 {@link org.lanye.fantasy_furniture.common.seat.SeatRegistry} / {@link org.lanye.fantasy_furniture.registry.ModSeatConfigs} 中 id 一致。 */
    public static final String NBT_SEAT_CONFIG_ID = "SeatConfigId";

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
        if (tag.contains(NBT_ANCHOR_POS)) {
            anchorPos = BlockPos.of(tag.getLong(NBT_ANCHOR_POS));
        }
        if (tag.contains(NBT_SEAT_CONFIG_ID)) {
            configId = tag.getString(NBT_SEAT_CONFIG_ID);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong(NBT_ANCHOR_POS, anchorPos.asLong());
        tag.putString(NBT_SEAT_CONFIG_ID, configId);
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
     * 优先 {@link SeatConfig#dismountStepDirectionFromAnchor()} 对应的正前方邻格（及上下邻格、向下搜索），找不到再试其余水平邻格。
     */
    @Nullable
    private Vec3 findSafeDismountNear(LivingEntity passenger) {
        CollisionGetter collision = level();
        SeatConfig cfg = SeatRegistry.get(configId);
        if (cfg == null) {
            return null;
        }
        BlockState anchorState = collision.getBlockState(anchorPos);
        // 勿在 blockValid 为 false 时直接 return null：客户端预测 / 逐出竞态下锚点可能瞬时非座席方块，会导致本方法恒为 null、
        // 与 super 组合后落点异常；无法从状态读「正前方」时回退为坐骑朝向（与旧版 fromYRot 一致）。
        boolean blockValid = cfg.blockValid().test(anchorState);
        Direction forward =
                blockValid
                        ? cfg.dismountStepDirectionFromAnchor().apply(anchorState)
                        : Direction.fromYRot(getYRot());
        BlockPos frontColumn = anchorPos.relative(forward);

        Vec3 v = findSafeOnColumn(passenger.getType(), collision, frontColumn);
        if (v != null) {
            return v;
        }

        // Plane.HORIZONTAL 的枚举顺序会在「前方格」不可用时先尝试到「正后方」邻格；改为先两侧再背后，更符合「尽量朝前」。
        for (Direction dir : horizontalFallbackDirections(forward)) {
            v = findSafeOnColumn(passenger.getType(), collision, anchorPos.relative(dir));
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    /** 与 {@code forward} 不同的三个水平方向：先左右邻格，最后背后（避免 Plane.HORIZONTAL 固定顺序先踩到背后）。 */
    private static Iterable<Direction> horizontalFallbackDirections(Direction forward) {
        return List.of(
                forward.getClockWise(),
                forward.getCounterClockWise(),
                forward.getOpposite());
    }

    /** 在同一 (x,z) 柱上从 {@code base} 起向上 1 格再向下最多 5 格尝试安全落点。 */
    @Nullable
    private static Vec3 findSafeOnColumn(
            EntityType<?> type, CollisionGetter collision, BlockPos base) {
        Vec3 v = DismountHelper.findSafeDismountLocation(type, collision, base, false);
        if (v != null) {
            return v;
        }
        v = DismountHelper.findSafeDismountLocation(type, collision, base, true);
        if (v != null) {
            return v;
        }
        BlockPos up = base.above();
        v = DismountHelper.findSafeDismountLocation(type, collision, up, false);
        if (v != null) {
            return v;
        }
        v = DismountHelper.findSafeDismountLocation(type, collision, up, true);
        if (v != null) {
            return v;
        }
        BlockPos down = base;
        for (int i = 0; i < 5; i++) {
            down = down.below();
            v = DismountHelper.findSafeDismountLocation(type, collision, down, false);
            if (v != null) {
                return v;
            }
            v = DismountHelper.findSafeDismountLocation(type, collision, down, true);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!level().isClientSide() && passenger instanceof LivingEntity living) {
            Vec3 safe = findSafeDismountNear(living);
            if (safe != null) {
                if (passenger instanceof ServerPlayer sp) {
                    sp.connection.teleport(safe.x, safe.y, safe.z, sp.getYRot(), sp.getXRot());
                } else {
                    passenger.teleportTo(safe.x, safe.y, safe.z);
                }
            }
        }
        if (passenger instanceof ServerPlayer sp && !level().isClientSide()) {
            SeatConfig cfg = SeatRegistry.get(configId);
            long cooldownTicks =
                    cfg == null
                            ? SeatCooldown.DEFAULT_COOLDOWN_TICKS
                            : cfg.normalizedDismountCooldownTicks();
            SeatCooldown.setCooldownUntil(
                    sp, level().getGameTime() + cooldownTicks);
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
