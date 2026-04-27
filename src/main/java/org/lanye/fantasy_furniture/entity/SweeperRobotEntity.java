package org.lanye.fantasy_furniture.entity;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.lanye.fantasy_furniture.Config;
import org.lanye.fantasy_furniture.block.entity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.block.registry.ModBlocks;
import org.lanye.fantasy_furniture.geolib.GeolibFacingEntityBlock;
import org.lanye.fantasy_furniture.world.inventory.SweeperRobotMenu;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 扫地机器人：低血回仓、巡逻、拾取掉落物并回仓卸货。潜行右键打开 9×3 背包（与小箱子相同 GUI）。 */
public class SweeperRobotEntity extends PathfinderMob implements GeoEntity, MenuProvider, Container {

    /** 小箱子：27 格（与 {@link net.minecraft.world.inventory.MenuType#GENERIC_9x3} 一致）。 */
    public static final int BACKPACK_SLOTS = 27;

    public static final String NBT_DOCK_POS = "DockPos";
    public static final String NBT_STATE = "SweeperState";
    public static final String NBT_LAST_HEAL = "LastHealTime";
    public static final String NBT_LAST_DECAY = "LastDecayTime";
    public static final String NBT_TARGET_ITEM = "TargetItem";
    public static final String NBT_CACHE = "CachedItems";
    public static final String NBT_DOCK_APPROACH_PHASE = "DockApproachPhase";

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(SweeperRobotEntity.class, EntityDataSerializers.INT);
    private static final double DOCKED_RADIUS_SQR = 0.2025D; // 0.45 block
    /** 到达「机仓前一格」中心点的水平距离阈值（平方）。 */
    private static final double STAGING_ARRIVE_RADIUS_SQR = 0.07D;
    /** 未满血在机仓内充电时，相对 {@link #dockCenter()} 竖直向下平移（世界坐标 Y 减小）。 */
    private static final double CHARGE_DOCK_Y_OFFSET = 0.2D;

    private static final RawAnimation MOVE_ANIM =
            RawAnimation.begin().then("animation.sweeper_robot.move", Animation.LoopType.LOOP);
    private static final RawAnimation IDLE_ANIM =
            RawAnimation.begin().then("animation.sweeper_robot.idle", Animation.LoopType.LOOP);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final NonNullList<ItemStack> cachedItems = NonNullList.withSize(BACKPACK_SLOTS, ItemStack.EMPTY);
    @Nullable private BlockPos dockPos;
    @Nullable private UUID targetItemUuid;
    private long lastHealGameTime;
    private long lastDecayGameTime;
    private long lastPatrolRandomTurnGameTime = Long.MIN_VALUE;
    private float patrolBaseYaw;
    /**
     * 入库子阶段：0→驶向机仓正前方一格（前一格）中心；1→原地转向，使机头与 {@link #getDockFacing()} 一致（朝外，车尾对机仓）；
     * 2→保持该朝向仅倒车入仓直至 {@link #isDocked()}。
     */
    private int dockApproachPhase;

    private static final int STEER_IDLE = 0;
    private static final int STEER_PRE = 1;
    private static final int STEER_TURN = 2;
    private static final int STEER_POST = 3;

    private int driveSteerPhase;
    private int driveSteerTicks;
    private double driveSteerGx = Double.NaN;
    private double driveSteerGz = Double.NaN;

    private int yawSteerPhase;
    private int yawSteerTicks;
    private float yawSteerLastTarget = Float.NaN;

    /** 巡逻待转目标偏航；{@link Float#NaN} 表示无。 */
    private float patrolSteerTargetYaw = Float.NaN;

    /**
     * 巡逻贴墙：0 平常；1 已撞墙，正用 {@link #patrolSteerTargetYaw} 转到沿墙方向；2 下一 tick 沿法线微移贴紧墙面。
     */
    private int patrolWallHugPhase;

    /** 贴墙微移方向（水平单位向量，指向墙内 / 障碍中心）。 */
    private double patrolWallNudgeX;

    private double patrolWallNudgeZ;

    private static final double WALL_HUG_NUDGE_BLOCKS = 0.07D;

    /**
     * 出库停泊点到达后、切入巡逻/收集前的「再转一次」目标偏航；{@link Float#NaN} 表示未在进行。
     */
    private float exitDockPostYaw = Float.NaN;

    public SweeperRobotEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.patrolBaseYaw = this.getYRot();
        setMaxUpStep(0.6F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        // 行为由 tick 状态机驱动，不使用传统 goalSelector。
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_STATE, SweeperState.IDLE.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains(NBT_DOCK_POS)) {
            dockPos = BlockPos.of(tag.getLong(NBT_DOCK_POS));
        }
        if (tag.contains(NBT_STATE)) {
            setSweeperState(SweeperState.byOrdinal(tag.getInt(NBT_STATE)));
        }
        dockApproachPhase = tag.getInt(NBT_DOCK_APPROACH_PHASE);
        lastHealGameTime = tag.getLong(NBT_LAST_HEAL);
        lastDecayGameTime = tag.getLong(NBT_LAST_DECAY);
        if (tag.hasUUID(NBT_TARGET_ITEM)) {
            targetItemUuid = tag.getUUID(NBT_TARGET_ITEM);
        }
        for (int i = 0; i < BACKPACK_SLOTS; i++) {
            cachedItems.set(i, ItemStack.EMPTY);
        }
        ListTag list = tag.getList(NBT_CACHE, Tag.TAG_COMPOUND);
        for (int i = 0; i < BACKPACK_SLOTS && i < list.size(); i++) {
            cachedItems.set(i, ItemStack.of(list.getCompound(i)));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (dockPos != null) {
            tag.putLong(NBT_DOCK_POS, dockPos.asLong());
        }
        tag.putInt(NBT_STATE, getSweeperState().ordinal());
        tag.putInt(NBT_DOCK_APPROACH_PHASE, dockApproachPhase);
        tag.putLong(NBT_LAST_HEAL, lastHealGameTime);
        tag.putLong(NBT_LAST_DECAY, lastDecayGameTime);
        if (targetItemUuid != null) {
            tag.putUUID(NBT_TARGET_ITEM, targetItemUuid);
        }
        ListTag list = new ListTag();
        for (int i = 0; i < BACKPACK_SLOTS; i++) {
            list.add(cachedItems.get(i).save(new CompoundTag()));
        }
        tag.put(NBT_CACHE, list);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            // 与 GeckoLib：避免 yBodyRot 追赶滞后导致「逻辑转角 ≠ 模型转角」。
            syncBodyHeadYaw();
            return;
        }
        if (getHealth() <= 0f) {
            setHealth(1f);
        }
        if (dockPos == null || !isDockValid()) {
            // 机仓不存在时直接回收机器人，避免无主实体长期滞留。
            discard();
            return;
        }
        long gameTime = level().getGameTime();
        if (isDocked()) {
            if (gameTime - lastHealGameTime >= Config.sweeperHealIntervalTicks()) {
                heal(1f);
                lastHealGameTime = gameTime;
            }
            tryStoreCachedItems();
        } else if (gameTime - lastDecayGameTime >= Config.sweeperDecayIntervalTicks()) {
            setHealth(Math.max(1f, getHealth() - 1f));
            lastDecayGameTime = gameTime;
        }
        updateStateAndAct();
    }

    private void updateStateAndAct() {
        if (getHealth() <= 1f) {
            setSweeperState(isDocked() ? SweeperState.DOCKED : SweeperState.RETURNING);
        } else if (getHealth() < Config.sweeperReturnHealthThreshold()) {
            // 已入仓则保持 DOCKED，否则会每 tick 被强制 RETURNING、阶段 0 又驶向「前一格」，与机仓之间来回拉扯。
            setSweeperState(isDocked() ? SweeperState.DOCKED : SweeperState.RETURNING);
        } else if (hasCollectTargetInRange()
                && getSweeperState() != SweeperState.DOCKED
                && getSweeperState() != SweeperState.EXITING_DOCK
                && getSweeperState() != SweeperState.RETURNING) {
            setSweeperState(SweeperState.COLLECTING);
        } else if (getSweeperState() == SweeperState.IDLE) {
            setSweeperState(SweeperState.PATROLLING);
        }

        switch (getSweeperState()) {
            case DOCKED -> {
                settleIntoDock();
                if (getHealth() >= Config.sweeperReturnHealthThreshold() && getHealth() > 1f) {
                    setSweeperState(SweeperState.EXITING_DOCK);
                }
            }
            case EXITING_DOCK -> tickExitingDock();
            case RETURNING -> tickReturningDockSequence();
            case COLLECTING -> tickCollecting();
            case PATROLLING -> tickPatrolling();
            case IDLE -> stopHorizontalMovement();
        }
    }

    private void resetDriveSteer() {
        driveSteerPhase = STEER_IDLE;
        driveSteerTicks = 0;
        driveSteerGx = Double.NaN;
        driveSteerGz = Double.NaN;
    }

    private void resetYawSteer() {
        yawSteerPhase = STEER_IDLE;
        yawSteerTicks = 0;
        yawSteerLastTarget = Float.NaN;
    }

    private void syncDriveGoal(Vec3 goal) {
        if (Double.isNaN(driveSteerGx)
                || Math.abs(goal.x - driveSteerGx) > 0.2D
                || Math.abs(goal.z - driveSteerGz) > 0.2D) {
            driveSteerPhase = STEER_IDLE;
        }
        driveSteerGx = goal.x;
        driveSteerGz = goal.z;
    }

    private void syncYawSteerTarget(float targetYaw) {
        if (Float.isNaN(yawSteerLastTarget)) {
            yawSteerLastTarget = targetYaw;
            return;
        }
        if (Math.abs(Mth.wrapDegrees(targetYaw - yawSteerLastTarget)) > 3f) {
            yawSteerPhase = STEER_IDLE;
            yawSteerLastTarget = targetYaw;
        }
    }

    /**
     * 原地转向节拍：停 → 转 → 停，再交给调用方前进。
     *
     * @return true 表示已对准且节拍结束，可继续直线移动。
     */
    private boolean tickYawSteerWithPauses(float targetYaw) {
        syncYawSteerTarget(targetYaw);
        int n = Config.sweeperTurnPauseTicks();
        float thresh = Config.sweeperTurnThresholdDegrees();
        float diff = Mth.wrapDegrees(targetYaw - getYRot());

        if (yawSteerPhase == STEER_PRE) {
            stopHorizontalMovement();
            if (--yawSteerTicks <= 0) {
                yawSteerPhase = STEER_TURN;
            }
            return false;
        }
        if (yawSteerPhase == STEER_POST) {
            stopHorizontalMovement();
            if (--yawSteerTicks <= 0) {
                yawSteerPhase = STEER_IDLE;
            }
            return false;
        }

        if (Math.abs(diff) > thresh) {
            if (yawSteerPhase == STEER_IDLE) {
                if (n <= 0) {
                    yawSteerPhase = STEER_TURN;
                } else {
                    yawSteerPhase = STEER_PRE;
                    yawSteerTicks = n;
                    stopHorizontalMovement();
                    return false;
                }
            }
            if (yawSteerPhase == STEER_TURN) {
                float step =
                        Mth.clamp(diff, -Config.sweeperTurnSpeedDegrees(), Config.sweeperTurnSpeedDegrees());
                setYRot(getYRot() + step);
                syncBodyHeadYaw();
                stopHorizontalMovement();
                return false;
            }
        } else {
            setYRot(targetYaw);
            syncBodyHeadYaw();
            if (yawSteerPhase == STEER_TURN) {
                if (n <= 0) {
                    yawSteerPhase = STEER_IDLE;
                    return true;
                }
                yawSteerPhase = STEER_POST;
                yawSteerTicks = n;
                stopHorizontalMovement();
                return false;
            }
            if (yawSteerPhase == STEER_IDLE) {
                return true;
            }
        }
        return false;
    }

    /** 出库：沿机头方向驶到机仓朝向的「前一格」中心，再原地旋转一次，最后进入巡逻/收集。 */
    private void tickExitingDock() {
        if (!isDockValid()) {
            stopHorizontalMovement();
            exitDockPostYaw = Float.NaN;
            setSweeperState(SweeperState.IDLE);
            return;
        }
        Vec3 staging = dockStagingCenter();
        if (!Float.isNaN(exitDockPostYaw)) {
            if (tickYawSteerWithPauses(exitDockPostYaw)) {
                exitDockPostYaw = Float.NaN;
                resetYawSteer();
                if (hasCollectTargetInRange()) {
                    setSweeperState(SweeperState.COLLECTING);
                } else {
                    setSweeperState(SweeperState.PATROLLING);
                }
            }
            return;
        }
        if (position().distanceToSqr(staging) <= STAGING_ARRIVE_RADIUS_SQR) {
            stopHorizontalMovement();
            resetYawSteer();
            exitDockPostYaw = Mth.wrapDegrees(getYRot() + 90f);
            return;
        }
        driveToward(staging, Config.sweeperMoveSpeed());
    }

    /**
     * 入库：前一格中心 → 机头与机仓 FACING 一致（朝外）→ 倒车入仓。
     */
    private void tickReturningDockSequence() {
        if (!isDockValid()) {
            stopHorizontalMovement();
            setSweeperState(SweeperState.IDLE);
            return;
        }
        Vec3 staging = dockStagingCenter();

        if (dockApproachPhase == 0) {
            if (position().distanceToSqr(staging) <= STAGING_ARRIVE_RADIUS_SQR) {
                stopHorizontalMovement();
                dockApproachPhase = 1;
                resetYawSteer();
                resetDriveSteer();
                return;
            }
            driveToward(staging, Config.sweeperMoveSpeed());
            return;
        }
        if (dockApproachPhase == 1) {
            float yawOut = yawDockFacingOutward();
            if (!tickYawSteerWithPauses(yawOut)) {
                return;
            }
            dockApproachPhase = 2;
            resetYawSteer();
            resetDriveSteer();
            return;
        }
        if (isDocked()) {
            dockApproachPhase = 0;
            setSweeperState(SweeperState.DOCKED);
            stopHorizontalMovement();
            return;
        }
        float yawOut = yawDockFacingOutward();
        if (!tickYawSteerWithPauses(yawOut)) {
            return;
        }
        double speed = Config.sweeperMoveSpeed();
        float yRad = getYRot() * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yRad), 0, Mth.cos(yRad));
        setDeltaMovement(-forward.x * speed, getDeltaMovement().y, -forward.z * speed);
    }

    /**
     * 与机仓方块 {@link #getDockFacing()} 一致的偏航：从机仓中心指向「前一格」中心（机头朝外，车尾朝向机仓）。
     * 与 {@link #driveToward} 中 {@code atan2(-dx, dz)} 约定一致。
     */
    private float yawDockFacingOutward() {
        Vec3 dock = dockCenter();
        Vec3 st = dockStagingCenter();
        double dx = st.x - dock.x;
        double dz = st.z - dock.z;
        return Mth.wrapDegrees((float) (Mth.atan2(-dx, dz) * (180.0 / Math.PI)));
    }

    private void tickCollecting() {
        ItemEntity target = getOrFindCollectTarget();
        if (target == null) {
            setSweeperState(SweeperState.PATROLLING);
            return;
        }
        driveToward(target.position(), Config.sweeperMoveSpeed());
        if (distanceToSqr(target) <= 1.6D) {
            cacheFrom(target);
            targetItemUuid = null;
            setSweeperState(SweeperState.PATROLLING);
        }
    }

    private void tickPatrolling() {
        Vec3 dock = dockCenter();
        double radius = Config.sweeperPatrolRadius();
        if (position().distanceToSqr(dock) > radius * radius) {
            setSweeperState(SweeperState.RETURNING);
            return;
        }
        if (patrolWallHugPhase == 2) {
            double dx = patrolWallNudgeX * WALL_HUG_NUDGE_BLOCKS;
            double dz = patrolWallNudgeZ * WALL_HUG_NUDGE_BLOCKS;
            AABB shifted = getBoundingBox().move(dx, 0.0D, dz);
            if (level().noCollision(this, shifted)) {
                setPos(getX() + dx, getY(), getZ() + dz);
            }
            patrolWallHugPhase = 0;
            stopHorizontalMovement();
            syncBodyHeadYaw();
            return;
        }
        if (!Float.isNaN(patrolSteerTargetYaw)) {
            if (tickYawSteerWithPauses(patrolSteerTargetYaw)) {
                patrolSteerTargetYaw = Float.NaN;
                if (patrolWallHugPhase == 1) {
                    patrolWallHugPhase = 2;
                }
            }
            return;
        }
        int randomTurnInterval = Config.sweeperPatrolRandomTurnIntervalTicks();
        if (randomTurnInterval > 0) {
            long gameTime = level().getGameTime();
            if (lastPatrolRandomTurnGameTime == Long.MIN_VALUE) {
                lastPatrolRandomTurnGameTime = gameTime;
            } else if (gameTime - lastPatrolRandomTurnGameTime >= randomTurnInterval) {
                lastPatrolRandomTurnGameTime = gameTime;
                patrolBaseYaw = this.random.nextFloat() * 360f;
                patrolWallHugPhase = 0;
                patrolSteerTargetYaw = patrolBaseYaw;
                stopHorizontalMovement();
                return;
            }
        }
        Vec3 forward =
                new Vec3(
                        -Mth.sin(this.getYRot() * Mth.DEG_TO_RAD),
                        0,
                        Mth.cos(this.getYRot() * Mth.DEG_TO_RAD));
        Vec3 target = position().add(forward.scale(2.0));
        driveToward(target, Config.sweeperMoveSpeed());
        if (horizontalCollision) {
            stopHorizontalMovement();
            BlockPos hit = findLowestBlockingInForwardColumn();
            if (hit != null && level().getBlockState(hit).blocksMotion()) {
                Vec3 c = Vec3.atCenterOf(hit);
                Vec3 p = position();
                double nx = c.x - p.x;
                double nz = c.z - p.z;
                double len = Math.sqrt(nx * nx + nz * nz);
                if (len > 1.0e-4) {
                    nx /= len;
                    nz /= len;
                    patrolWallNudgeX = nx;
                    patrolWallNudgeZ = nz;
                    float alongYaw = computeWallHugYawAlongWall(nx, nz);
                    patrolWallHugPhase = 1;
                    patrolSteerTargetYaw = alongYaw;
                    return;
                }
            }
            patrolWallHugPhase = 0;
            patrolBaseYaw = Mth.wrapDegrees(getYRot() + 90f);
            patrolSteerTargetYaw = patrolBaseYaw;
            return;
        }
    }

    /** 逻辑朝向与渲染用 body/head 一致（GeckoLib 默认参考 yBodyRot）。 */
    private void syncBodyHeadYaw() {
        float y = getYRot();
        setYBodyRot(y);
        setYHeadRot(y);
    }

    private Direction getDockFacing() {
        if (!isDockValid()) {
            return Direction.NORTH;
        }
        return level().getBlockState(dockPos).getValue(GeolibFacingEntityBlock.FACING);
    }

    /** 机仓开口朝 {@link #getDockFacing()}，前一格为沿该方向相邻格的中心（与 {@link #dockCenter()} 同高度）。 */
    private Vec3 dockStagingCenter() {
        BlockPos staging = Objects.requireNonNull(dockPos).relative(getDockFacing());
        Vec3 d = dockCenter();
        return new Vec3(staging.getX() + 0.5, d.y, staging.getZ() + 0.5);
    }

    /**
     * 仅沿机头方向移动：水平速度与 {@link #getYRot()} 共线；机头始终朝向目标再前进（与运动方向一致）。
     * 转向前/后各停顿 {@link Config#sweeperTurnPauseTicks()} tick（可配置为 0 关闭）。
     */
    private void driveToward(Vec3 goal, double speed) {
        syncDriveGoal(goal);
        Vec3 pos = position();
        double dx = goal.x - pos.x;
        double dz = goal.z - pos.z;
        if (dx * dx + dz * dz < 1.0e-6) {
            stopHorizontalMovement();
            return;
        }
        float targetYaw =
                Mth.wrapDegrees((float) (Mth.atan2(-dx, dz) * (180.0 / Math.PI)));
        float current = getYRot();
        float alignDiff = Mth.wrapDegrees(targetYaw - current);
        float thresh = Config.sweeperTurnThresholdDegrees();
        int n = Config.sweeperTurnPauseTicks();

        if (driveSteerPhase == STEER_PRE) {
            stopHorizontalMovement();
            if (--driveSteerTicks <= 0) {
                driveSteerPhase = STEER_TURN;
            }
            return;
        }
        if (driveSteerPhase == STEER_POST) {
            stopHorizontalMovement();
            if (--driveSteerTicks <= 0) {
                driveSteerPhase = STEER_IDLE;
            }
            return;
        }

        if (Math.abs(alignDiff) > thresh) {
            if (driveSteerPhase == STEER_IDLE) {
                if (n <= 0) {
                    driveSteerPhase = STEER_TURN;
                } else {
                    driveSteerPhase = STEER_PRE;
                    driveSteerTicks = n;
                    stopHorizontalMovement();
                    return;
                }
            }
            if (driveSteerPhase == STEER_TURN) {
                float turnStep =
                        Mth.clamp(alignDiff, -Config.sweeperTurnSpeedDegrees(), Config.sweeperTurnSpeedDegrees());
                setYRot(current + turnStep);
                syncBodyHeadYaw();
                stopHorizontalMovement();
                return;
            }
        } else {
            setYRot(targetYaw);
            syncBodyHeadYaw();
            if (driveSteerPhase == STEER_TURN) {
                if (n <= 0) {
                    driveSteerPhase = STEER_IDLE;
                } else {
                    driveSteerPhase = STEER_POST;
                    driveSteerTicks = n;
                    stopHorizontalMovement();
                    return;
                }
            }
            if (driveSteerPhase != STEER_IDLE) {
                stopHorizontalMovement();
                return;
            }

            float yRad = getYRot() * Mth.DEG_TO_RAD;
            Vec3 forward = new Vec3(-Mth.sin(yRad), 0, Mth.cos(yRad));
            Vec3 toN = new Vec3(dx, 0, dz).normalize();
            double along = forward.dot(toN);
            if (Math.abs(along) < 0.03) {
                stopHorizontalMovement();
                return;
            }
            if (along <= 0) {
                stopHorizontalMovement();
                return;
            }
            setDeltaMovement(forward.x * speed, getDeltaMovement().y, forward.z * speed);
        }
    }

    /**
     * 机头前方一列中，与碰撞箱相交高度范围内最低的阻挡格（用于贴墙法线）。
     */
    @Nullable
    private BlockPos findLowestBlockingInForwardColumn() {
        float yRad = getYRot() * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yRad);
        double fz = Mth.cos(yRad);
        Vec3 pos = position();
        int ix = Mth.floor(pos.x + fx * 0.52);
        int iz = Mth.floor(pos.z + fz * 0.52);
        int minY = Mth.floor(getBoundingBox().minY);
        int maxY = Mth.ceil(getBoundingBox().maxY);
        Level lvl = level();
        for (int y = minY; y <= maxY + 1; y++) {
            BlockPos bp = new BlockPos(ix, y, iz);
            if (lvl.getBlockState(bp).blocksMotion()) {
                return bp;
            }
        }
        return null;
    }

    /**
     * 已知指向墙内的水平单位向量 (nx,nz)（实体→障碍），返回机头应朝向的偏航：沿墙行走，取与当前机头方向更一致的切向（通常相对撞墙朝向约 90°）。
     */
    private float computeWallHugYawAlongWall(double nx, double nz) {
        double t1x = -nz;
        double t1z = nx;
        double t2x = nz;
        double t2z = -nx;
        float yRad = getYRot() * Mth.DEG_TO_RAD;
        double hx = -Mth.sin(yRad);
        double hz = Mth.cos(yRad);
        double dot1 = hx * t1x + hz * t1z;
        double dot2 = hx * t2x + hz * t2z;
        double sx = dot1 >= dot2 ? t1x : t2x;
        double sz = dot1 >= dot2 ? t1z : t2z;
        return Mth.wrapDegrees((float) (Mth.atan2(-sx, sz) * (180.0 / Math.PI)));
    }

    @Nullable
    private ItemEntity getOrFindCollectTarget() {
        if (targetItemUuid != null && level() instanceof ServerLevel serverLevel) {
            var entity = serverLevel.getEntity(targetItemUuid);
            if (entity instanceof ItemEntity target && target.isAlive() && !target.getItem().isEmpty()) {
                return target;
            }
        }
        targetItemUuid = null;
        return findNearestCollectTarget().orElse(null);
    }

    private boolean hasCollectTargetInRange() {
        return findNearestCollectTarget().isPresent();
    }

    private Optional<ItemEntity> findNearestCollectTarget() {
        double range = Config.sweeperCollectRange();
        AABB box = new AABB(blockPosition()).inflate(range);
        return level().getEntitiesOfClass(ItemEntity.class, box, e -> e.isAlive() && !e.getItem().isEmpty()).stream()
                .min((a, b) -> Double.compare(distanceToSqr(a), distanceToSqr(b)))
                .map(
                        e -> {
                            targetItemUuid = e.getUUID();
                            return e;
                        });
    }

    private void cacheFrom(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem().copy();
        if (stack.isEmpty()) {
            return;
        }
        mergeIntoCache(stack);
        itemEntity.discard();
        if (isDocked()) {
            tryStoreCachedItems();
        }
    }

    private void mergeIntoCache(ItemStack stack) {
        for (int i = 0; i < BACKPACK_SLOTS && !stack.isEmpty(); i++) {
            ItemStack cacheStack = cachedItems.get(i);
            if (ItemStack.isSameItemSameTags(cacheStack, stack)
                    && cacheStack.getCount() < cacheStack.getMaxStackSize()) {
                int canMove = Math.min(stack.getCount(), cacheStack.getMaxStackSize() - cacheStack.getCount());
                cacheStack.grow(canMove);
                stack.shrink(canMove);
            }
        }
        int slotLimit = Mth.clamp(Config.sweeperCacheSlots(), 1, BACKPACK_SLOTS);
        for (int i = 0; i < slotLimit && !stack.isEmpty(); i++) {
            if (cachedItems.get(i).isEmpty()) {
                int split = Math.min(stack.getMaxStackSize(), stack.getCount());
                cachedItems.set(i, stack.split(split));
            }
        }
    }

    private void tryStoreCachedItems() {
        SweeperDockBlockEntity dock = getDockEntity();
        if (dock == null) {
            return;
        }
        Container container = dock.findNearbyContainer();
        if (container == null) {
            return;
        }
        for (int i = BACKPACK_SLOTS - 1; i >= 0; i--) {
            ItemStack stack = cachedItems.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remaining = moveToContainer(stack, container);
            if (remaining.isEmpty()) {
                cachedItems.set(i, ItemStack.EMPTY);
            } else {
                cachedItems.set(i, remaining);
            }
        }
    }

    private static ItemStack moveToContainer(ItemStack input, Container container) {
        ItemStack remaining = input.copy();
        for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++) {
            ItemStack slot = container.getItem(i);
            if (slot.isEmpty()) {
                container.setItem(i, remaining);
                container.setChanged();
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameTags(slot, remaining) && slot.getCount() < slot.getMaxStackSize()) {
                int move = Math.min(remaining.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(move);
                remaining.shrink(move);
                container.setChanged();
            }
        }
        return remaining;
    }

    private Vec3 dockCenter() {
        Objects.requireNonNull(dockPos);
        // 停靠点放在机仓内部，配合机仓对机器人无碰撞，实现“入仓”效果。
        return new Vec3(dockPos.getX() + 0.5, dockPos.getY() + 0.20, dockPos.getZ() + 0.5);
    }

    /** 吸附目标：充电中略降低，满血或非充电仍回 {@link #dockCenter()}。 */
    private Vec3 dockRestPosition() {
        Vec3 base = dockCenter();
        if (!isDockedChargingForDisplay()) {
            return base;
        }
        return new Vec3(base.x, base.y - CHARGE_DOCK_Y_OFFSET, base.z);
    }

    private boolean isDocked() {
        if (dockPos == null) {
            return false;
        }
        Vec3 dock = dockCenter();
        double dx = position().x - dock.x;
        double dz = position().z - dock.z;
        return dx * dx + dz * dz <= DOCKED_RADIUS_SQR;
    }

    private void settleIntoDock() {
        Vec3 dock = dockRestPosition();
        Vec3 current = position();
        // 轻微插值吸附，避免回仓末端出现抖动/卡边。
        Vec3 next = current.lerp(dock, 0.35D);
        setPos(next.x, next.y, next.z);
        if (next.distanceToSqr(dock) < 0.0004D) {
            setPos(dock.x, dock.y, dock.z);
        }
        stopHorizontalMovement();
    }

    private void stopHorizontalMovement() {
        setDeltaMovement(0.0, getDeltaMovement().y, 0.0);
    }

    public void bindDock(BlockPos pos) {
        this.dockPos = pos.immutable();
        this.lastHealGameTime = level().getGameTime();
        this.lastDecayGameTime = this.lastHealGameTime;
        this.dockApproachPhase = 0;
        setSweeperState(SweeperState.DOCKED);
    }

    public BlockPos getDockPos() {
        return dockPos;
    }

    /**
     * 机仓充电显示：{@link SweeperState#DOCKED}、已在停靠点且未满血（仍会触发回血）时，机仓贴图可在当前条带与下一档之间闪烁。
     */
    public boolean isDockedChargingForDisplay() {
        if (dockPos == null) {
            return false;
        }
        if (entityData.get(DATA_STATE) != SweeperState.DOCKED.ordinal()) {
            return false;
        }
        return isDocked() && getHealth() < getMaxHealth();
    }

    /** 客户端渲染用插值偏航；GeckoLib 需与逻辑朝向一致，且渲染器包无法读取受保护的 yRotO。 */
    public float getVisualYaw(float partialTick) {
        return Mth.rotLerp(partialTick, yRotO, getYRot());
    }

    private boolean isDockValid() {
        return dockPos != null
                && level().isLoaded(dockPos)
                && level().getBlockState(dockPos).is(ModBlocks.SWEEPER_DOCK.block().get());
    }

    @Nullable
    private SweeperDockBlockEntity getDockEntity() {
        if (!isDockValid()) {
            return null;
        }
        if (level().getBlockEntity(dockPos) instanceof SweeperDockBlockEntity dock) {
            return dock;
        }
        return null;
    }

    private SweeperState getSweeperState() {
        return SweeperState.byOrdinal(entityData.get(DATA_STATE));
    }

    private void setSweeperState(SweeperState state) {
        SweeperState prev = getSweeperState();
        entityData.set(DATA_STATE, state.ordinal());
        if (state == SweeperState.RETURNING && prev != SweeperState.RETURNING) {
            dockApproachPhase = 0;
            resetYawSteer();
            resetDriveSteer();
            patrolSteerTargetYaw = Float.NaN;
            patrolWallHugPhase = 0;
            exitDockPostYaw = Float.NaN;
        }
        if (state == SweeperState.COLLECTING && prev != SweeperState.COLLECTING) {
            patrolSteerTargetYaw = Float.NaN;
            resetYawSteer();
            resetDriveSteer();
            patrolWallHugPhase = 0;
            exitDockPostYaw = Float.NaN;
        }
        if (state == SweeperState.EXITING_DOCK && prev != SweeperState.EXITING_DOCK) {
            resetYawSteer();
            resetDriveSteer();
            patrolWallHugPhase = 0;
            exitDockPostYaw = Float.NaN;
        }
        if (state == SweeperState.PATROLLING && prev != SweeperState.PATROLLING) {
            resetDriveSteer();
            patrolSteerTargetYaw = Float.NaN;
            patrolWallHugPhase = 0;
            exitDockPostYaw = Float.NaN;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float hp = getHealth();
        if (amount >= hp) {
            setHealth(1f);
            setSweeperState(SweeperState.RETURNING);
            return false;
        }
        boolean result = super.hurt(source, amount);
        if (getHealth() < 1f) {
            setHealth(1f);
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        setHealth(1f);
        setSweeperState(SweeperState.RETURNING);
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return getHealth() > 1f && super.canBeSeenAsEnemy();
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, this, buf -> buf.writeVarInt(getId()));
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.fantasy_furniture.sweeper_robot");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SweeperRobotMenu(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return BACKPACK_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : cachedItems) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < BACKPACK_SLOTS ? cachedItems.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(cachedItems, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(cachedItems, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < BACKPACK_SLOTS) {
            cachedItems.set(slot, stack);
        }
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive() && !isRemoved() && player.distanceToSqr(this) <= 64.0;
    }

    @Override
    public void clearContent() {
        Collections.fill(cachedItems, ItemStack.EMPTY);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "main",
                        2,
                        state -> state.setAndContinue(this.getDeltaMovement().horizontalDistanceSqr() > 0.0001 ? MOVE_ANIM : IDLE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public enum SweeperState {
        DOCKED,
        RETURNING,
        COLLECTING,
        PATROLLING,
        IDLE,
        /** 出库：先到达机仓前一格中心。 */
        EXITING_DOCK;

        public static SweeperState byOrdinal(int ordinal) {
            SweeperState[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return IDLE;
            }
            return values[ordinal];
        }
    }
}
