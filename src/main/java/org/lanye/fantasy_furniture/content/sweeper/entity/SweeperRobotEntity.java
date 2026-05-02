package org.lanye.fantasy_furniture.content.sweeper.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.lanye.fantasy_furniture.Config;
import org.lanye.fantasy_furniture.content.sweeper.blockentity.SweeperDockBlockEntity;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.sweeper.ai.InternalPathTuning;
import org.lanye.fantasy_furniture.core.geolib.GeolibFacingEntityBlock;
import org.lanye.fantasy_furniture.content.sweeper.menu.SweeperRobotMenu;
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
    private static final EntityDataAccessor<Boolean> DATA_WALL_CLIMBING =
            SynchedEntityData.defineId(SweeperRobotEntity.class, EntityDataSerializers.BOOLEAN);
    /** 墙面朝向：从墙块指向实体的水平方向（与 {@link Direction#get3DDataValue()} 一致）。 */
    private static final EntityDataAccessor<Byte> DATA_WALL_FACE =
            SynchedEntityData.defineId(SweeperRobotEntity.class, EntityDataSerializers.BYTE);
    private static final double DOCKED_RADIUS_SQR = 0.2025D; // 0.45 block
    /** 到达「机仓前一格」中心点的水平距离阈值（平方）。 */
    private static final double STAGING_ARRIVE_RADIUS_SQR = 0.07D;
    /**
     * 攀附时与墙面工作点沿法线的距离允许区间（业务脱墙）；同类蜘蛛仍受重力，由 {@link #horizontalCollision}
     * 刷新攀墙位，此处防止沿几何平面滑太远。
     */
    private static final double WALL_CLIMB_DIST_ALONG_MIN = 0.04D;
    private static final double WALL_CLIMB_DIST_ALONG_MAX = 1.15D;

    /**
     * 高处失去贴墙判定时：先顺墙、切向绕行若干 tick，再允许 {@link #exitWallClimb()} 自由跌落（仅巡逻/收集且脚下有坠落风险）。
     */
    private static final int WALL_DESCEND_DEFER_MAX_TICKS = 80;

    /** 脚下方连续非碰撞格达到此深度，才认为「可能摔落」，需要延迟脱墙逻辑。 */
    private static final int WALL_DESCEND_MIN_AIR_BELOW = 2;

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

    /** 发现掉落物：相对机仓「巡逻半径」内，或相对机器人水平约此半径内（方块）。 */
    private static final double COLLECT_VICINITY_AROUND_ROBOT_BLOCKS = 5.0D;

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

    /** 服务端：当前攀附的阻挡格（用于校验墙面仍存在）。 */
    @Nullable private BlockPos wallClimbAnchor;
    /** 连续多少 tick 未检测到“贴墙阻挡列”（用于过滤单帧误判）。 */
    private int wallNoColumnTicks;

    /** 即将从高处脱墙：已用顺墙/绕行缓冲的 tick（见 {@link #maybeDeferWallExitBeforeFall()}）。 */
    private int wallDescendDeferTicks;

    /**
     * 巡逻态：连续「机头大致正对阻挡 + 水平碰撞」的 tick，达到 {@link #PATROL_HEAD_ON_WALL_CLIMB_TICKS} 后才允许入攀，
     * 避免擦边经过墙面就进入攀爬。
     */
    private int patrolHeadOnWallTicks;

    private static final int PATROL_HEAD_ON_WALL_CLIMB_TICKS = 12;

    /**
     * 地面收集：{@link net.minecraft.world.entity.Mob#getNavigation()} 计算的到达掉落物所在格的缓存路径（攀墙时不使用）。
     */
    @Nullable private net.minecraft.world.level.pathfinder.Path collectGroundPath;

    private int collectPathCursor;
    /** 上次成功进入收集路径重算流程的游戏刻（tick），用于与 {@link #COLLECT_PATH_RECOMPUTE_INTERVAL} 比较。 */
    private long collectPathRecomputeGameTime = Long.MIN_VALUE;
    /**
     * 当前 {@link #collectGroundPath} 所指向的方块格（通常与掉落物 {@link ItemEntity#blockPosition()} 一致）；
     * 与下一次求路时的目标格比较，决定能否复用缓存路径。
     */
    @Nullable private BlockPos collectPathGoalBlock;
    private final Map<UUID, Long> collectIgnoredTargetsUntil = new HashMap<>();
    private int collectStuckTicks;
    private double collectLastX = Double.NaN;
    private double collectLastZ = Double.NaN;
    @Nullable private BlockPos collectLastStuckGoalBlock;

    @Nullable private net.minecraft.world.level.pathfinder.Path reenterGroundPath;
    private int reenterPathCursor;
    private long reenterPathRecomputeGameTime = Long.MIN_VALUE;
    @Nullable private BlockPos reenterPathGoalBlock;
    private int returningStuckTicks;
    private double returningLastX = Double.NaN;
    private double returningLastZ = Double.NaN;

    private static final long COLLECT_PATH_RECOMPUTE_INTERVAL = 20L;
    private static final long COLLECT_UNREACHABLE_RETRY_COOLDOWN_TICKS = 20L * 8L;
    private static final double COLLECT_WAYPOINT_ARRIVE_SQR = 0.55D * 0.55D;

    private static final long REENTER_PATH_RECOMPUTE_INTERVAL = 25L;

    /** 卸货到机仓邻接容器：与漏斗相同的 8 tick 成功传输间隔（见 {@link HopperBlockEntity#MOVE_ITEM_SPEED}）。 */
    private int sweeperUnloadCooldownTicks;

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
    protected PathNavigation createNavigation(Level level) {
        return new SweeperGroundNavigation(this, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_STATE, SweeperState.IDLE.ordinal());
        entityData.define(DATA_WALL_CLIMBING, false);
        entityData.define(DATA_WALL_FACE, (byte) Direction.NORTH.get3DDataValue());
    }

    public boolean isWallClimbing() {
        return entityData.get(DATA_WALL_CLIMBING);
    }

    /**
     * 机仓碰撞豁免：仅停靠、出库、入库时与机仓 AABB 重叠；巡逻/拾取时与机仓正常碰撞以便绕开。
     */
    public boolean ignoresDockBlockCollision() {
        SweeperState s = getSweeperState();
        return s == SweeperState.DOCKED || s == SweeperState.EXITING_DOCK || s == SweeperState.RETURNING;
    }

    @Nullable
    public Direction getWallClimbFacing() {
        if (!isWallClimbing()) {
            return null;
        }
        return Direction.from3DDataValue(entityData.get(DATA_WALL_FACE) & 0xFF);
    }

    /**
     * 与原版蜘蛛一致：{@link LivingEntity#travel} 等逻辑以 {@link #onClimbable()} 判定攀爬（梯子与本 mod 竖直墙攀附）。
     */
    @Override
    public boolean onClimbable() {
        return isWallClimbing() || super.onClimbable();
    }

    /**
     * 与 {@link org.lanye.fantasy_furniture.content.sweeper.client.renderer.SweeperRobotRenderer} 贴墙绕水平轴 90° 一致：轴对齐包络上交换宽、高（原 {@code .sized(0.604, 0.25)} → 攀墙时约 0.25×0.604）。
     */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (pose == Pose.SLEEPING) {
            return super.getDimensions(pose);
        }
        EntityDimensions base = super.getDimensions(pose);
        if (!isWallClimbing()) {
            return base;
        }
        return EntityDimensions.scalable(base.height, base.width);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (DATA_WALL_CLIMBING.equals(data)) {
            refreshDimensions();
        }
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
        tickWallClimbSpiderStyle();
        long gameTime = level().getGameTime();
        if (isDocked()) {
            if (gameTime - lastHealGameTime >= Config.sweeperHealIntervalTicks()) {
                heal(1f);
                lastHealGameTime = gameTime;
            }
            tryHopperUnloadCachedItemsWhenDocked();
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
        } else if (shouldReturnToUnloadBecauseCacheFull()) {
            setSweeperState(SweeperState.RETURNING);
        } else if (hasCollectTargetInRange()
                && getSweeperState() != SweeperState.DOCKED
                && getSweeperState() != SweeperState.EXITING_DOCK
                && getSweeperState() != SweeperState.RETURNING) {
            setSweeperState(SweeperState.COLLECTING);
        } else if (isOutsidePatrolRadiusAroundDock()
                && getSweeperState() != SweeperState.DOCKED
                && getSweeperState() != SweeperState.EXITING_DOCK
                && getSweeperState() != SweeperState.RETURNING) {
            setSweeperState(SweeperState.REENTERING_PATROL);
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
            case REENTERING_PATROL -> tickReenteringPatrol();
            case PATROLLING -> tickPatrolling();
            case IDLE -> stopHorizontalMovement();
        }
        tryVacuumItemEntitiesSharingOccupiedCell();
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
        if (tickWallHugDetourActive()) {
            return;
        }
        if (position().distanceToSqr(staging) <= STAGING_ARRIVE_RADIUS_SQR) {
            stopHorizontalMovement();
            resetYawSteer();
            exitDockPostYaw = Mth.wrapDegrees(getYRot() + 90f);
            return;
        }
        driveToward(staging, Config.sweeperMoveSpeed());
        if (horizontalCollision) {
            handleGoalSeekCollision();
            return;
        }
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
            if (tickReturningStuckWatch()) {
            }
            if (tickWallHugDetourActive()) {
                return;
            }
            if (position().distanceToSqr(staging) <= STAGING_ARRIVE_RADIUS_SQR) {
                stopHorizontalMovement();
                dockApproachPhase = 1;
                returningStuckTicks = 0;
                returningLastX = Double.NaN;
                returningLastZ = Double.NaN;
                resetYawSteer();
                resetDriveSteer();
                return;
            }
            driveToward(staging, Config.sweeperMoveSpeed());
            if (horizontalCollision) {
                handleGoalSeekCollision();
            }
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

    /**
     * 是否已超出机仓巡逻半径（与 {@link #tickPatrolling} 首段一致）；机仓无效时不视为越界。
     */
    private boolean isOutsidePatrolRadiusAroundDock() {
        if (!isDockValid()) {
            return false;
        }
        double r = Config.sweeperPatrolRadius();
        return position().distanceToSqr(dockCenter()) > r * r;
    }

    /**
     * 沿法线朝墙内（-n）多深度、略沿切向偏移采样；必要时回退到 {@link #wallClimbAnchor} 竖列。减少单点落在缝里导致一进攀附就 exit。
     */
    private boolean hasBlockingColumnIntoWall(Vec3 n) {
        AABB box = getBoundingBox();
        double midX = (box.minX + box.maxX) * 0.5D;
        double midZ = (box.minZ + box.maxZ) * 0.5D;
        double tx = -n.z;
        double tz = n.x;
        int y0 = Mth.floor(box.minY + 0.12D);
        int y1 = Mth.ceil(box.maxY - 0.02D);
        if (y1 < y0) {
            y1 = y0;
        }
        Level lvl = level();
        double[] depths = {0.2D, 0.33D, 0.48D, 0.62D};
        double[] sides = {0.0D, 0.12D, -0.12D};
        for (double d : depths) {
            for (double s : sides) {
                double cx = midX - n.x * d + tx * s;
                double cz = midZ - n.z * d + tz * s;
                for (int y = y0; y <= y1; y++) {
                    if (lvl.getBlockState(BlockPos.containing(cx, y, cz)).blocksMotion()) {
                        return true;
                    }
                }
            }
        }
        if (wallClimbAnchor != null) {
            int ax = wallClimbAnchor.getX();
            int az = wallClimbAnchor.getZ();
            for (int y = y0; y <= y1; y++) {
                if (lvl.getBlockState(new BlockPos(ax, y, az)).blocksMotion()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 脚下方连续无实心支撑的深度 ≥ {@link #WALL_DESCEND_MIN_AIR_BELOW} 时视为有摔落风险，优先顺墙/绕行再脱墙。
     */
    private boolean isElevatedWithFallRisk() {
        BlockPos feet = blockPosition();
        int air = 0;
        for (int i = 1; i <= 12; i++) {
            BlockPos p = feet.below(i);
            if (p.getY() < level().getMinBuildHeight()) {
                break;
            }
            if (!level().getBlockState(p).blocksMotion()) {
                air++;
            } else {
                break;
            }
        }
        return air >= WALL_DESCEND_MIN_AIR_BELOW;
    }

    private boolean shouldDeferWallFallForPatrolCollect() {
        SweeperState st = getSweeperState();
        return st == SweeperState.PATROLLING && getHealth() > 1f && isElevatedWithFallRisk();
    }

    /**
     * @return true 表示本 tick 不 {@link #exitWallClimb()}，由巡逻逻辑沿墙转向并向前试探以重新贴墙或寻角下落。
     */
    private boolean maybeDeferWallExitBeforeFall() {
        if (!shouldDeferWallFallForPatrolCollect()) {
            wallDescendDeferTicks = 0;
            return false;
        }
        if (wallDescendDeferTicks >= WALL_DESCEND_DEFER_MAX_TICKS) {
            wallDescendDeferTicks = 0;
            return false;
        }
        wallDescendDeferTicks++;
        Direction wf = getWallClimbFacing();
        if (wf == null || !wf.getAxis().isHorizontal()) {
            return false;
        }
        Vec3 n = new Vec3(wf.getStepX(), 0.0D, wf.getStepZ());
        double tx = -n.z;
        double tz = n.x;
        float yawAlongA = Mth.wrapDegrees((float) (Mth.atan2(-tx, tz) * (180.0 / Math.PI)));
        float yawAlongB = Mth.wrapDegrees(yawAlongA + 180.0F);
        int band = (wallDescendDeferTicks - 1) / 20;
        int mode = band % 4;
        float pick =
                mode == 0
                        ? yawAlongA
                        : mode == 1
                                ? yawAlongB
                                : mode == 2 && isDockValid()
                                        ? yawTowardHorizontal(dockCenter())
                                        : yawAlongB;
        if (wallDescendDeferTicks == 1 || (wallDescendDeferTicks - 1) % 20 == 0) {
            patrolSteerTargetYaw = pick;
        }
        patrolWallHugPhase = 0;
        return true;
    }

    private float yawTowardHorizontal(Vec3 goal) {
        Vec3 pos = position();
        double dx = goal.x - pos.x;
        double dz = goal.z - pos.z;
        return Mth.wrapDegrees((float) (Mth.atan2(-dx, dz) * (180.0 / Math.PI)));
    }

    /**
     * 巡逻/取物/出入库共用的局部绕行：沿障碍切向转向并微移，避免直线追目标被单格障碍永久卡住。
     */
    private boolean tickWallHugDetourActive() {
        if (patrolWallHugPhase == 2) {
            double nudgeBlocks = Math.max(WALL_HUG_NUDGE_BLOCKS, robotCollisionRadius() * 1.9D);
            double dx = patrolWallNudgeX * nudgeBlocks;
            double dz = patrolWallNudgeZ * nudgeBlocks;
            AABB shifted = getBoundingBox().move(dx, 0.0D, dz);
            if (level().noCollision(this, shifted)) {
                setPos(getX() + dx, getY(), getZ() + dz);
            } else {
                double dxHalf = patrolWallNudgeX * (nudgeBlocks * 0.5D);
                double dzHalf = patrolWallNudgeZ * (nudgeBlocks * 0.5D);
                AABB shiftedHalf = getBoundingBox().move(dxHalf, 0.0D, dzHalf);
                if (level().noCollision(this, shiftedHalf)) {
                    setPos(getX() + dxHalf, getY(), getZ() + dzHalf);
                }
            }
            patrolWallHugPhase = 0;
            stopHorizontalMovement();
            syncBodyHeadYaw();
            return true;
        }
        if (!Float.isNaN(patrolSteerTargetYaw)) {
            if (tickYawSteerWithPauses(patrolSteerTargetYaw)) {
                patrolSteerTargetYaw = Float.NaN;
                if (patrolWallHugPhase == 1) {
                    patrolWallHugPhase = 2;
                }
            }
            return true;
        }
        return false;
    }

    private boolean tickReturningStuckWatch() {
        if (Double.isNaN(returningLastX) || Double.isNaN(returningLastZ)) {
            returningLastX = getX();
            returningLastZ = getZ();
            returningStuckTicks = 0;
            return false;
        }
        double dx = getX() - returningLastX;
        double dz = getZ() - returningLastZ;
        double movedSqr = dx * dx + dz * dz;
        returningLastX = getX();
        returningLastZ = getZ();
        if (movedSqr < 0.0025D) {
            returningStuckTicks++;
        } else {
            returningStuckTicks = 0;
        }
        return returningStuckTicks >= 30;
    }

    private boolean tryPlanarBypassFromHit(@Nullable BlockPos hit) {
        if (hit == null || !level().getBlockState(hit).blocksMotion()) {
            return false;
        }
        Vec3 c = Vec3.atCenterOf(hit);
        Vec3 p = position();
        double nx = c.x - p.x;
        double nz = c.z - p.z;
        double len = Math.sqrt(nx * nx + nz * nz);
        if (len <= 1.0e-4D) {
            return false;
        }
        nx /= len;
        nz /= len;
        patrolWallNudgeX = nx;
        patrolWallNudgeZ = nz;
        float alongYaw = computeWallHugYawAlongWall(nx, nz);
        patrolWallHugPhase = 1;
        patrolSteerTargetYaw = alongYaw;
        return true;
    }

    private void applyStuckFallbackTurn() {
        patrolWallHugPhase = 0;
        patrolBaseYaw = Mth.wrapDegrees(getYRot() + 90f);
        patrolSteerTargetYaw = patrolBaseYaw;
    }

    /** 撞停时优先 {@link #tryPlanarBypassFromHit} / 备选转向；巡逻不再 defer 绕行。蜘蛛攀附由 {@link #tickWallClimbSpiderStyle} 单独门控。 */
    private void handleGoalSeekCollision() {
        BlockPos hit = findLowestBlockingInForwardColumn();
        stopHorizontalMovement();
        if (tryPlanarBypassFromHit(hit)) {
            resetDriveSteer();
            resetYawSteer();
            return;
        }
        applyStuckFallbackTurn();
        resetDriveSteer();
        resetYawSteer();
    }

    /**
     * 仿照原版“接触即拾取”：当机器人与掉落物碰撞箱相交时可吸入。
     */
    private boolean canVacuumItemNow(ItemEntity item) {
        if (!item.isAlive() || item.getItem().isEmpty()) {
            return false;
        }
        return this.getBoundingBox().intersects(item.getBoundingBox());
    }

    /** 路过同格时发现可吸入掉落物即可收取（不靠机头朝向）；停泊在仓内不处理。 */
    private void tryVacuumItemEntitiesSharingOccupiedCell() {
        if (level().isClientSide() || getSweeperState() == SweeperState.DOCKED) {
            return;
        }
        BlockPos cell = blockPosition();
        var drops =
                level()
                        .getEntitiesOfClass(
                                ItemEntity.class,
                                new AABB(cell),
                                e ->
                                        e.isAlive()
                                                && !e.getItem().isEmpty()
                                                && cell.equals(e.blockPosition())
                                                && isItemDiscoverableForSweep(e)
                                                && !isCollectTargetTemporarilyIgnored(e));
        if (drops.isEmpty()) {
            return;
        }
        var sorted = new ArrayList<ItemEntity>(drops);
        sorted.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (ItemEntity e : sorted) {
            if (!canVacuumItemNow(e)) {
                continue;
            }
            cacheFrom(e);
        }
    }

    private void resetCollectGroundPath() {
        collectGroundPath = null;
        collectPathCursor = 0;
        collectPathRecomputeGameTime = Long.MIN_VALUE;
        collectPathGoalBlock = null;
        collectStuckTicks = 0;
        collectLastStuckGoalBlock = null;
        collectLastX = Double.NaN;
        collectLastZ = Double.NaN;
    }

    private void clearCollectUnreachableCooldownIfExpired() {
        if (collectIgnoredTargetsUntil.isEmpty()) {
            return;
        }
        long now = level().getGameTime();
        collectIgnoredTargetsUntil.entrySet().removeIf(e -> now >= e.getValue());
    }

    private boolean isCollectTargetTemporarilyIgnored(ItemEntity item) {
        clearCollectUnreachableCooldownIfExpired();
        Long retryAt = collectIgnoredTargetsUntil.get(item.getUUID());
        if (retryAt == null) {
            return false;
        }
        boolean ignored = level().getGameTime() < retryAt;
        return ignored;
    }

    private void markCollectTargetUnreachable(@Nullable ItemEntity target, String reason) {
        if (target == null) {
            return;
        }
        clearCollectUnreachableCooldownIfExpired();
        long now = level().getGameTime();
        Long existingRetryAt = collectIgnoredTargetsUntil.get(target.getUUID());
        if (existingRetryAt != null && now < existingRetryAt) {
            return;
        }
        long retryAt = now + COLLECT_UNREACHABLE_RETRY_COOLDOWN_TICKS;
        collectIgnoredTargetsUntil.put(target.getUUID(), retryAt);
    }

    private double xzDistSqrTo(Vec3 v) {
        double dx = getX() - v.x;
        double dz = getZ() - v.z;
        return dx * dx + dz * dz;
    }

    private double xzDistSqrTo(Entity e) {
        double dx = getX() - e.getX();
        double dz = getZ() - e.getZ();
        return dx * dx + dz * dz;
    }

    private boolean tickCollectStuckWatch(BlockPos goalBlock) {
        if (collectLastStuckGoalBlock == null || !collectLastStuckGoalBlock.equals(goalBlock)) {
            collectLastStuckGoalBlock = goalBlock.immutable();
            collectStuckTicks = 0;
            collectLastX = getX();
            collectLastZ = getZ();
            return false;
        }
        if (driveSteerPhase != STEER_IDLE || yawSteerPhase != STEER_IDLE) {
            collectStuckTicks = 0;
            collectLastX = getX();
            collectLastZ = getZ();
            return false;
        }
        if (Double.isNaN(collectLastX) || Double.isNaN(collectLastZ)) {
            collectLastX = getX();
            collectLastZ = getZ();
            collectStuckTicks = 0;
            return false;
        }
        double dx = getX() - collectLastX;
        double dz = getZ() - collectLastZ;
        double movedSqr = dx * dx + dz * dz;
        collectLastX = getX();
        collectLastZ = getZ();
        if (movedSqr < 0.0036D) {
            collectStuckTicks++;
        } else {
            collectStuckTicks = 0;
        }
        return collectStuckTicks >= 30;
    }

    private static Vec3 collectWaypointCenter(Node n) {
        return new Vec3((double) n.x + 0.5D, (double) n.y, (double) n.z + 0.5D);
    }

    /**
     * 收集路径航点：{@link SweeperItemGroundPath} 的最后一档使用求路时保存的掉落物 {@link Vec3}，其余节点仍用格点中心。
     */
    private static Vec3 collectGroundPathWaypoint(Path path, int nodeIndex) {
        if (path instanceof SweeperItemGroundPath sip && nodeIndex == path.getNodeCount() - 1) {
            Vec3 p = sip.exactItemPosition();
            return new Vec3(p.x, p.y, p.z);
        }
        return collectWaypointCenter(path.getNode(nodeIndex));
    }

    private double robotCollisionRadius() {
        double configured = InternalPathTuning.Sweeper.COLLISION_RADIUS;
        double auto = getBbWidth() * 0.5D;
        double base = configured > 0.0D ? configured : auto;
        return base + InternalPathTuning.Sweeper.PATH_RADIUS_BONUS;
    }

    private double waypointArriveSqr() {
        double r = robotCollisionRadius();
        double arrive = Math.max(Math.sqrt(COLLECT_WAYPOINT_ARRIVE_SQR), r + 0.20D);
        return arrive * arrive;
    }

    private void advanceCollectPathCursor() {
        if (collectGroundPath == null) {
            return;
        }
        double arriveSqr = waypointArriveSqr();
        while (collectPathCursor < collectGroundPath.getNodeCount() - 1) {
            Vec3 w = collectWaypointCenter(collectGroundPath.getNode(collectPathCursor));
            if (xzDistSqrTo(w) < arriveSqr) {
                collectPathCursor++;
            } else {
                break;
            }
        }
    }

    /**
     * 仿照原版 {@code PathNavigation#createPath(...)} 的入口包装：在基础 accuracy 上叠加机器人碰撞半径影响。
     * <p>
     * 目的：机器人体型较宽时，放宽“到达目标”的容差，减少末端节点过窄导致的贴边卡住。
     *
     * @param goal 目标方块格
     * @param baseAccuracy 业务期望的基础到达容差（单位：格）
     * @return 导航路径；在明显无效场景（如低于世界最低高度）返回 null
     */
    @Nullable
    private Path createCollectPathWithCollisionRadius(
            ItemEntity target, BlockPos goal, int baseAccuracy) {
        PathNavigation navigation = getNavigation();
        if (navigation instanceof SweeperGroundNavigation sweeperNavigation) {
            // 收集路径优先使用掉落物精确坐标入口，减少“到格不贴物”的末段偏差。
            return sweeperNavigation.createPathToExactPos(target.position());
        }
        // 兜底：非扫地机导航器时仍走原版 BlockPos 入口。
        return navigation.createPath(goal, baseAccuracy);
    }

    /**
     * 为拾取目标刷新「地面」导航路径（节点由 {@link #getNavigation()} 计算；攀墙阶段不走此路径）。
     * <p>
     * 路径目标为掉落物所在方块坐标；与 {@link #collectPathGoalBlock} 一致且未过重算间隔时复用缓存，
     * 否则调用 {@link #createCollectPathWithCollisionRadius(ItemEntity, BlockPos, int)} 重新求路（到达容差会叠加机器人半径影响）。
     *
     * @param target 当前锁定要收集的掉落物实体
     * @return 存在非零节点路径时为 true；{@code null} 或 {@link net.minecraft.world.level.pathfinder.Path#getNodeCount()} 为 0 时返回
     *     false，调用方应放弃该目标
     */
    private boolean ensureCollectGroundPath(ItemEntity target) {
        BlockPos goal = target.blockPosition();
        long gameTime = level().getGameTime();
        // 无缓存、目标格变化、或每隔 COLLECT_PATH_RECOMPUTE_INTERVAL tick 触发重算意图（应对地形/障碍变化）
        if (collectGroundPath == null) {
            // 首次进入收集流程或缓存已被清空：当前没有可跟随的路径，只能重新求路。
        } else if (collectPathGoalBlock == null) {
            // 存在路径但缺少其对应的目标格元数据：无法确认路径是否仍指向当前目标，按失效处理并重算。
        } else if (!collectPathGoalBlock.equals(goal)) {
            // 掉落物所在格变化（滚动/被推挤/目标切换）：旧路径终点已过期，需要改算到新目标格。
        } else if (gameTime - collectPathRecomputeGameTime >= COLLECT_PATH_RECOMPUTE_INTERVAL) {
            // 周期到期：若目标格未变且仍有有效缓存，则不再整段重算，避免两节点直线路径「起点节点」随位移刷新导致约每秒一次的中途拐向。
            if (collectGroundPath.getNodeCount() > 0) {
                collectPathRecomputeGameTime = gameTime;
                return true;
            }
            // 否则继续向下完整重算
        } else {
            return collectGroundPath.getNodeCount() > 0;
        }

        collectPathRecomputeGameTime = gameTime;
        collectPathGoalBlock = goal.immutable();
        Path path = createCollectPathWithCollisionRadius(target, goal, 0);
        collectGroundPath = path;
        collectPathCursor = 0;
        if (path == null || path.getNodeCount() == 0) {
            return false;
        }

        advanceCollectPathCursor();
        return true;
    }

    private void abandonUnreachableCollectTarget(@Nullable ItemEntity target, String reason) {
        markCollectTargetUnreachable(target, reason);
        targetItemUuid = null;
        resetCollectGroundPath();
        getNavigation().stop();
        setSweeperState(SweeperState.PATROLLING);
    }

    private void resetReenterGroundPath() {
        reenterGroundPath = null;
        reenterPathCursor = 0;
        reenterPathRecomputeGameTime = Long.MIN_VALUE;
        reenterPathGoalBlock = null;
    }

    private void advanceReenterPathCursor() {
        if (reenterGroundPath == null) {
            return;
        }
        double arriveSqr = waypointArriveSqr();
        while (reenterPathCursor < reenterGroundPath.getNodeCount() - 1) {
            Vec3 w = collectWaypointCenter(reenterGroundPath.getNode(reenterPathCursor));
            if (xzDistSqrTo(w) < arriveSqr) {
                reenterPathCursor++;
            } else {
                break;
            }
        }
    }

    private boolean ensureReenterGroundPath(BlockPos goal) {
        long gameTime = level().getGameTime();
        boolean needNew =
                reenterGroundPath == null
                        || reenterPathGoalBlock == null
                        || !reenterPathGoalBlock.equals(goal)
                        || gameTime - reenterPathRecomputeGameTime >= REENTER_PATH_RECOMPUTE_INTERVAL;
        if (!needNew) {
            return reenterGroundPath != null
                    && reenterGroundPath.canReach()
                    && reenterGroundPath.getNodeCount() > 0;
        }
        reenterPathRecomputeGameTime = gameTime;
        reenterPathGoalBlock = goal.immutable();
        net.minecraft.world.level.pathfinder.Path path = getNavigation().createPath(goal, 1);
        reenterGroundPath = path;
        reenterPathCursor = 0;
        if (path == null || !path.canReach() || path.getNodeCount() == 0) {
            return false;
        }
        advanceReenterPathCursor();
        return true;
    }

    /**
     * 巡逻柱面内、距机仓水平面中心约 {@link Config#sweeperPatrolRadius()} 处、贴近机器人的目标格（用于寻路回到巡逻区）。
     */
    private BlockPos computeReenterPatrolGoalBlock() {
        Vec3 dc = dockCenter();
        Vec3 p = position();
        double dx = p.x - dc.x;
        double dz = p.z - dc.z;
        double lenH = Math.sqrt(dx * dx + dz * dz);
        double r = Math.max(1.5D, Config.sweeperPatrolRadius() - 0.75D);
        if (lenH <= r) {
            return blockPosition();
        }
        double nx = dx / lenH;
        double nz = dz / lenH;
        Vec3 goal = new Vec3(dc.x + nx * r, p.y, dc.z + nz * r);
        return BlockPos.containing(goal);
    }

    private AABB collectDiscoveryQueryAabb() {
        Vec3 dc = dockCenter();
        Vec3 rp = position();
        double pr = Config.sweeperPatrolRadius();
        double vic = COLLECT_VICINITY_AROUND_ROBOT_BLOCKS;
        double minX = Math.min(dc.x - pr, rp.x - vic) - 2.0D;
        double maxX = Math.max(dc.x + pr, rp.x + vic) + 2.0D;
        double minZ = Math.min(dc.z - pr, rp.z - vic) - 2.0D;
        double maxZ = Math.max(dc.z + pr, rp.z + vic) + 2.0D;
        return new AABB(
                minX,
                level().getMinBuildHeight(),
                minZ,
                maxX,
                level().getMaxBuildHeight(),
                maxZ);
    }

    /** 机仓巡逻半径球内，或机器人水平约 {@link #COLLECT_VICINITY_AROUND_ROBOT_BLOCKS} 格内。 */
    private boolean isItemDiscoverableForSweep(ItemEntity e) {
        if (!e.isAlive() || e.getItem().isEmpty() || !isDockValid()) {
            return false;
        }
        double pr = Config.sweeperPatrolRadius();
        if (e.position().distanceToSqr(dockCenter()) <= pr * pr) {
            return true;
        }
        double v = COLLECT_VICINITY_AROUND_ROBOT_BLOCKS;
        return xzDistSqrTo(e) <= v * v;
    }

    private void tickReenteringPatrol() {
        if (!isOutsidePatrolRadiusAroundDock()) {
            resetReenterGroundPath();
            getNavigation().stop();
            if (hasCollectTargetInRange()) {
                setSweeperState(SweeperState.COLLECTING);
            } else {
                setSweeperState(SweeperState.PATROLLING);
            }
            return;
        }
        if (isWallClimbing()) {
            resetReenterGroundPath();
            float yRad = getYRot() * Mth.DEG_TO_RAD;
            Vec3 forward = new Vec3(-Mth.sin(yRad), 0.0, Mth.cos(yRad));
            driveToward(position().add(forward.scale(1.2)), Config.sweeperMoveSpeed());
            return;
        }
        if (!Float.isNaN(patrolSteerTargetYaw)) {
            if (tickYawSteerWithPauses(patrolSteerTargetYaw)) {
                patrolSteerTargetYaw = Float.NaN;
            }
            float yRad0 = getYRot() * Mth.DEG_TO_RAD;
            Vec3 forward0 = new Vec3(-Mth.sin(yRad0), 0.0, Mth.cos(yRad0));
            driveToward(position().add(forward0.scale(0.85)), Config.sweeperMoveSpeed());
            return;
        }
        if (tickWallHugDetourActive()) {
            return;
        }

        BlockPos goalBlock = computeReenterPatrolGoalBlock();
        if (!ensureReenterGroundPath(goalBlock)) {
            Vec3 dc = dockCenter();
            driveToward(new Vec3(dc.x, getY(), dc.z), Config.sweeperMoveSpeed());
            if (horizontalCollision) {
                handleGoalSeekCollision();
            }
            return;
        }
        net.minecraft.world.level.pathfinder.Path path = reenterGroundPath;
        if (path == null) {
            Vec3 dc = dockCenter();
            driveToward(new Vec3(dc.x, getY(), dc.z), Config.sweeperMoveSpeed());
            return;
        }

        int ry = blockPosition().getY();
        int gy = goalBlock.getY();
        boolean layerOk = Mth.abs(ry - gy) <= 1;
        if (!layerOk) {
            advanceReenterPathCursor();
            if (reenterPathCursor >= path.getNodeCount()) {
                int ry2 = blockPosition().getY();
                int gy2 = goalBlock.getY();
                if (Mth.abs(ry2 - gy2) > 1) {
                    resetReenterGroundPath();
                }
                layerOk = true;
            }
        }
        if (!layerOk) {
            Vec3 w = collectWaypointCenter(path.getNode(reenterPathCursor));
            driveToward(w, Config.sweeperMoveSpeed());
            if (horizontalCollision) {
                handleGoalSeekCollision();
            }
            return;
        }

        advanceReenterPathCursor();
        int reenterLast = path.getNodeCount() - 1;
        if (reenterPathCursor < reenterLast) {
            Vec3 w = collectWaypointCenter(path.getNode(reenterPathCursor));
            driveToward(w, Config.sweeperMoveSpeed());
            if (horizontalCollision) {
                handleGoalSeekCollision();
            }
            return;
        }
        Vec3 reenterLastCenter = collectWaypointCenter(path.getNode(reenterLast));
        if (xzDistSqrTo(reenterLastCenter) > waypointArriveSqr()) {
            driveToward(reenterLastCenter, Config.sweeperMoveSpeed());
            if (horizontalCollision) {
                handleGoalSeekCollision();
            }
            return;
        }

        Vec3 goalCenter = Vec3.atCenterOf(goalBlock);
        driveToward(new Vec3(goalCenter.x, getY(), goalCenter.z), Config.sweeperMoveSpeed());
        if (horizontalCollision) {
            handleGoalSeekCollision();
        }
    }

    /**
     * 与目标格 {@code |ΔY|<=1} 视为同一层高，可直接贴目标；否则先沿路节点移动直至层高对齐，再逼近掉落物实体。
     */
    private void tickCollecting() {
        // 分支A：贴墙攀爬中，不走地面路径，改用近距离朝向驱动贴墙接近目标。
        if (isWallClimbing()) {
            resetCollectGroundPath();
            ItemEntity target = getOrFindCollectTarget();
            // A1：目标丢失，退出收集，回到巡逻。
            if (target == null) {
                setSweeperState(SweeperState.PATROLLING);
                return;
            }
            // A2：已到可吸入范围，直接收集并结束本次收集状态。
            if (canVacuumItemNow(target)) {
                cacheFrom(target);
                targetItemUuid = null;
                setSweeperState(SweeperState.PATROLLING);
                return;
            }
            // A3：仍在执行贴墙转向节拍，先转向再做小步前推，避免横摆过大。
            if (!Float.isNaN(patrolSteerTargetYaw)) {
                if (tickYawSteerWithPauses(patrolSteerTargetYaw)) {
                    patrolSteerTargetYaw = Float.NaN;
                }
                float yR0 = getYRot() * Mth.DEG_TO_RAD;
                Vec3 f0 = new Vec3(-Mth.sin(yR0), 0.0, Mth.cos(yR0));
                driveToward(position().add(f0.scale(0.85)), Config.sweeperMoveSpeed());
                return;
            }
            // A4：脱墙延迟窗口内，继续沿当前朝向小步前进，避免立刻跌落/反复切换状态。
            if (wallDescendDeferTicks > 0) {
                float yRd = getYRot() * Mth.DEG_TO_RAD;
                Vec3 fD = new Vec3(-Mth.sin(yRd), 0.0, Mth.cos(yRd));
                driveToward(position().add(fD.scale(0.85)), Config.sweeperMoveSpeed());
                return;
            }
            // A5：贴墙稳定阶段，直接朝掉落物实体逼近。
            driveToward(target.position(), Config.sweeperMoveSpeed());
            return;
        }
        // 分支B：地面收集模式，依赖缓存路径与节点跟随。
        ItemEntity target = getOrFindCollectTarget();
        // B1：目标丢失，清路径并回巡逻。
        if (target == null) {
            resetCollectGroundPath();
            setSweeperState(SweeperState.PATROLLING);
            return;
        }
        // B2：目标处于短期忽略名单（例如刚判定不可达），暂不重试，回巡逻等待冷却。
        if (isCollectTargetTemporarilyIgnored(target)) {
            targetItemUuid = null;
            resetCollectGroundPath();
            setSweeperState(SweeperState.PATROLLING);
            return;
        }
        // B3：无法获得可用路径，标记当前目标不可达并放弃。
        if (!ensureCollectGroundPath(target)) {
            abandonUnreachableCollectTarget(target, "path_unreachable");
            return;
        }
        net.minecraft.world.level.pathfinder.Path path = collectGroundPath;
        // B4：理论上不应出现（ensure 成功但缓存为空），按异常兜底处理为不可达。
        if (path == null) {
            abandonUnreachableCollectTarget(target, "path_null_after_ensure");
            return;
        }
        // B5：持续位移不足，判定卡住，主动放弃该目标避免死循环。
        if (tickCollectStuckWatch(target.blockPosition())) {
            abandonUnreachableCollectTarget(target, "stuck_loop");
            return;
        }

        int ry = blockPosition().getY();
        int ty = target.blockPosition().getY();
        boolean layerOk = Mth.abs(ry - ty) <= 1;

        // B6：正在执行贴墙绕障分离流程，本 tick 交给该流程接管移动。
        if (tickWallHugDetourActive()) {
            return;
        }

        // B7：与目标层高不一致时，先沿路径推进，争取先对齐层高。
        if (!layerOk) {
            advanceCollectPathCursor();
            if (collectPathCursor >= path.getNodeCount()) {
                int ry2 = blockPosition().getY();
                int ty2 = target.blockPosition().getY();
                // 路径走尽仍未对齐层高，视为当前收集几何条件不可满足。
                if (Mth.abs(ry2 - ty2) > 1) {
                    abandonUnreachableCollectTarget(target, "layer_mismatch_after_path_end");
                    return;
                }
                layerOk = true;
            }
        }

        // B8：尚未层高对齐，继续按当前路径节点行进。
        if (!layerOk) {
            Vec3 w = collectGroundPathWaypoint(path, collectPathCursor);
            driveToward(w, Config.sweeperMoveSpeed());
            if (horizontalCollision) {
                handleGoalSeekCollision();
            }
            return;
        }

        advanceCollectPathCursor();
        int collectLast = path.getNodeCount() - 1;
        // B9：还在中间节点阶段，继续逐节点跟随。
        if (collectPathCursor < collectLast) {
            Vec3 w = collectGroundPathWaypoint(path, collectPathCursor);
            driveToward(w, Config.sweeperMoveSpeed());
            if (horizontalCollision) {
                handleGoalSeekCollision();
            }
            return;
        }
        Vec3 collectLastCenter = collectGroundPathWaypoint(path, collectLast);
        // B10：已到最后节点阶段但未贴近末档航点（扫地路径末档为掉落物精确坐标），先收敛。
        if (xzDistSqrTo(collectLastCenter) > waypointArriveSqr()) {
            driveToward(collectLastCenter, Config.sweeperMoveSpeed());
            if (horizontalCollision) {
                handleGoalSeekCollision();
            }
            return;
        }

        // B11：末档航点（精确坐标意义下）已达成；能吸则收。
        if (canVacuumItemNow(target)) {
            cacheFrom(target);
            targetItemUuid = null;
            resetCollectGroundPath();
            setSweeperState(SweeperState.PATROLLING);
            return;
        }
        // B12：已到路径终点仍吸不到：先对掉落物实体做短距直驱，避免“前一点点停住”；
        // 若末段仍失败，再清路径重算，兼顾贴近能力与避免长时间顶死。
        Vec3 targetPos = target.position();
        if (xzDistSqrTo(targetPos) > 0.01D) {
            driveToward(new Vec3(targetPos.x, getY(), targetPos.z), Config.sweeperMoveSpeed());
            if (horizontalCollision) {
                handleGoalSeekCollision();
            }
            return;
        }
        stopHorizontalMovement();
        resetCollectGroundPath();
    }

    private void tickPatrolling() {
        if (isWallClimbing()) {
            if (!Float.isNaN(patrolSteerTargetYaw)) {
                if (tickYawSteerWithPauses(patrolSteerTargetYaw)) {
                    patrolSteerTargetYaw = Float.NaN;
                }
                float yRad0 = getYRot() * Mth.DEG_TO_RAD;
                Vec3 forward0 = new Vec3(-Mth.sin(yRad0), 0.0, Mth.cos(yRad0));
                driveToward(position().add(forward0.scale(0.85)), Config.sweeperMoveSpeed());
                return;
            }
            if (wallDescendDeferTicks > 0) {
                float yRd = getYRot() * Mth.DEG_TO_RAD;
                Vec3 fwdD = new Vec3(-Mth.sin(yRd), 0.0, Mth.cos(yRd));
                driveToward(position().add(fwdD.scale(0.85)), Config.sweeperMoveSpeed());
                return;
            }
            float yRad = getYRot() * Mth.DEG_TO_RAD;
            Vec3 forward = new Vec3(-Mth.sin(yRad), 0.0, Mth.cos(yRad));
            Vec3 target = position().add(forward.scale(2.0));
            driveToward(target, Config.sweeperMoveSpeed());
            return;
        }
        if (tickWallHugDetourActive()) {
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
            handleGoalSeekCollision();
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
        double rx = fz;
        double rz = -fx;
        Vec3 pos = position();
        double radius = robotCollisionRadius();
        double front = Math.max(0.70D, radius + 0.45D);
        double side = Math.max(0.30D, radius * 1.1D);
        double[] sideOffsets = new double[] {-side, 0.0D, side};
        int minY = Mth.floor(getBoundingBox().minY);
        int maxY = Mth.ceil(getBoundingBox().maxY);
        Level lvl = level();
        for (double s : sideOffsets) {
            int ix = Mth.floor(pos.x + fx * front + rx * s);
            int iz = Mth.floor(pos.z + fz * front + rz * s);
            for (int y = minY; y <= maxY + 1; y++) {
                BlockPos bp = new BlockPos(ix, y, iz);
                if (lvl.getBlockState(bp).blocksMotion()) {
                    return bp;
                }
            }
        }
        double ring = radius + 0.35D;
        int ringBlock = Math.max(1, Mth.floor(ring));
        int[] ox = new int[] {
            ringBlock, ringBlock, 0, -ringBlock, -ringBlock, -ringBlock, 0, ringBlock
        };
        int[] oz = new int[] {
            0, ringBlock, ringBlock, ringBlock, 0, -ringBlock, -ringBlock, -ringBlock
        };
        BlockPos nearest = null;
        double bestSqr = Double.MAX_VALUE;
        int cx = Mth.floor(pos.x);
        int cz = Mth.floor(pos.z);
        for (int i = 0; i < ox.length; i++) {
            int ix = cx + ox[i];
            int iz = cz + oz[i];
            for (int y = minY; y <= maxY + 1; y++) {
                BlockPos bp = new BlockPos(ix, y, iz);
                if (!lvl.getBlockState(bp).blocksMotion()) {
                    continue;
                }
                Vec3 c = Vec3.atCenterOf(bp);
                double ds = (c.x - pos.x) * (c.x - pos.x) + (c.z - pos.z) * (c.z - pos.z);
                if (ds < bestSqr) {
                    bestSqr = ds;
                    nearest = bp;
                }
            }
        }
        if (nearest != null) {
            return nearest;
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

    private void exitWallClimb() {
        if (!entityData.get(DATA_WALL_CLIMBING)) {
            wallClimbAnchor = null;
            wallNoColumnTicks = 0;
            return;
        }
        entityData.set(DATA_WALL_CLIMBING, false);
        wallClimbAnchor = null;
        wallNoColumnTicks = 0;
        wallDescendDeferTicks = 0;
        setNoGravity(false);
        stopHorizontalMovement();
        refreshDimensions();
    }

    /** 由实体位置与阻挡格中心推断墙面朝向（从墙块指向空气侧/实体侧的法线方向）。 */
    private Direction computeWallFaceFromEntityAndHit(BlockPos hit) {
        Vec3 c = Vec3.atCenterOf(hit);
        Vec3 p = position();
        double dx = p.x - c.x;
        double dz = p.z - c.z;
        double adx = Math.abs(dx);
        double adz = Math.abs(dz);
        if (adx > adz) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    /** 阻挡格中心相对实体是否落在机头朝向前方扇形内（用于区分正对撞墙与侧向擦墙）。 */
    private boolean isHeadOnWallRoughlyAhead(BlockPos hit) {
        Vec3 hc = Vec3.atCenterOf(hit);
        double vx = hc.x - getX();
        double vz = hc.z - getZ();
        double len = Math.sqrt(vx * vx + vz * vz);
        if (len < 1.0e-4D) {
            return true;
        }
        vx /= len;
        vz /= len;
        float yRad = getYRot() * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yRad);
        double fz = Mth.cos(yRad);
        return fx * vx + fz * vz >= 0.45D;
    }

    /**
     * 类原版蜘蛛：在 {@code super.tick()} 之后根据 {@link #horizontalCollision} 刷新攀墙同步位；
     * 移动交给 {@link #onClimbable()} 与 {@link LivingEntity#travel}。仍保留巡逻半径 / 锚点 / 墙柱等业务脱墙。
     */
    private void tickWallClimbSpiderStyle() {
        if (level().isClientSide) {
            return;
        }
        SweeperState st = getSweeperState();
        // 允许在巡逻与收集态使用蜘蛛式攀墙；回仓/出库仍保持地面行为，避免入库流程被攀墙打断。
        boolean allowSpiderClimb =
                Config.sweeperEnableWallClimb()
                        && (st == SweeperState.PATROLLING || st == SweeperState.COLLECTING)
                        && getHealth() > 1f;

        if (isWallClimbing()) {
            if (isOutsidePatrolRadiusAroundDock()) {
                exitWallClimb();
                if (getHealth() < Config.sweeperReturnHealthThreshold() && !isDocked()) {
                    setSweeperState(SweeperState.RETURNING);
                } else {
                    setSweeperState(SweeperState.REENTERING_PATROL);
                }
                return;
            }
            Direction wallFace = getWallClimbFacing();
            if (wallFace == null || !wallFace.getAxis().isHorizontal()) {
                exitWallClimb();
                return;
            }
            if (wallClimbAnchor == null || !level().getBlockState(wallClimbAnchor).blocksMotion()) {
                exitWallClimb();
                return;
            }
            Vec3 faceCenter =
                    Vec3.atCenterOf(wallClimbAnchor)
                            .add(
                                    new Vec3(wallFace.getStepX(), wallFace.getStepY(), wallFace.getStepZ())
                                            .scale(0.5));
            Vec3 n = new Vec3(wallFace.getStepX(), wallFace.getStepY(), wallFace.getStepZ());
            double distAlong = position().subtract(faceCenter).dot(n);
            if (distAlong < WALL_CLIMB_DIST_ALONG_MIN || distAlong > WALL_CLIMB_DIST_ALONG_MAX) {
                if (maybeDeferWallExitBeforeFall()) {
                    return;
                }
                exitWallClimb();
                return;
            }
            boolean hasColumn = hasBlockingColumnIntoWall(n);
            if (!hasColumn) {
                if (maybeDeferWallExitBeforeFall()) {
                    return;
                }
                wallNoColumnTicks++;
                if (wallNoColumnTicks < 8) {
                    return;
                }
                exitWallClimb();
                return;
            }
            wallNoColumnTicks = 0;
            wallDescendDeferTicks = 0;
        }

        if (!allowSpiderClimb) {
            if (isWallClimbing()) {
                exitWallClimb();
            }
            return;
        }

        BlockPos hit = findLowestBlockingInForwardColumn();
        boolean hitBlocks = hit != null && level().getBlockState(hit).blocksMotion();
        int collectDy = 0;
        double collectNearSqr = Double.NaN;
        boolean collectClimbAssist = false;
        if (st == SweeperState.COLLECTING && targetItemUuid != null && level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(targetItemUuid);
            if (e instanceof ItemEntity item && item.isAlive() && !item.getItem().isEmpty()) {
                collectDy = Mth.abs(blockPosition().getY() - item.blockPosition().getY());
                collectNearSqr = xzDistSqrTo(item);
                collectClimbAssist = collectDy > 1 && collectNearSqr <= 4.0D;
            }
        }

        boolean shouldHug;
        if (st == SweeperState.PATROLLING) {
            if (hitBlocks
                    && horizontalCollision
                    && hit != null
                    && isHeadOnWallRoughlyAhead(hit)) {
                patrolHeadOnWallTicks = Math.min(patrolHeadOnWallTicks + 1, PATROL_HEAD_ON_WALL_CLIMB_TICKS + 30);
            } else {
                patrolHeadOnWallTicks = 0;
            }
            shouldHug =
                    hitBlocks
                            && horizontalCollision
                            && patrolHeadOnWallTicks >= PATROL_HEAD_ON_WALL_CLIMB_TICKS;
        } else if (st == SweeperState.COLLECTING) {
            patrolHeadOnWallTicks = 0;
            boolean fullReachableGroundPath =
                    collectGroundPath != null
                            && collectGroundPath.getNodeCount() > 0
                            && collectGroundPath.canReach();
            if (fullReachableGroundPath && !collectClimbAssist) {
                // 已有完整地面路径且目标不需要“高差辅助攀爬”：禁止靠擦墙入攀，交给路径与平面绕行。
                shouldHug = false;
            } else if (fullReachableGroundPath) {
                shouldHug = hitBlocks && collectClimbAssist;
            } else {
                shouldHug = hitBlocks && (horizontalCollision || collectClimbAssist);
            }
        } else {
            patrolHeadOnWallTicks = 0;
            shouldHug = false;
        }

        if (!isWallClimbing() && shouldHug) {
            Direction wf = computeWallFaceFromEntityAndHit(hit);
            if (!wf.getAxis().isHorizontal()) {
                if (isWallClimbing()) {
                    exitWallClimb();
                }
                return;
            }
            wallClimbAnchor = hit.immutable();
            wallNoColumnTicks = 0;
            entityData.set(DATA_WALL_FACE, (byte) wf.get3DDataValue());
            entityData.set(DATA_WALL_CLIMBING, true);
            refreshDimensions();
        } else if (!isWallClimbing() && !shouldHug && level().getGameTime() % 20L == 0L) {
        }
    }

    @Nullable
    private ItemEntity getOrFindCollectTarget() {
        clearCollectUnreachableCooldownIfExpired();
        if (targetItemUuid != null && level() instanceof ServerLevel serverLevel) {
            var entity = serverLevel.getEntity(targetItemUuid);
            if (entity instanceof ItemEntity target && target.isAlive() && !target.getItem().isEmpty()) {
                if (isCollectTargetTemporarilyIgnored(target)) {
                    targetItemUuid = null;
                } else {
                return target;
                }
            }
        }
        targetItemUuid = null;
        return findNearestCollectTarget().orElse(null);
    }

    private boolean hasCollectTargetInRange() {
        Optional<ItemEntity> candidate = findNearestCollectTarget();
        return candidate.isPresent();
    }

    private Optional<ItemEntity> findNearestCollectTarget() {
        AABB box = collectDiscoveryQueryAabb();
        var candidates = level()
                .getEntitiesOfClass(
                        ItemEntity.class, box, e -> isItemDiscoverableForSweep(e) && !isCollectTargetTemporarilyIgnored(e));
        return candidates.stream()
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
        if (!canAcceptItemInCache(stack)) {
            return;
        }
        mergeIntoCache(stack);
        if (!stack.isEmpty()) {
            itemEntity.setItem(stack);
            return;
        }
        itemEntity.discard();
        if (isDocked()) {
            sweeperUnloadCooldownTicks = 0;
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

    private boolean isInternalCacheFullForConfiguredSlots() {
        int slotLimit = Mth.clamp(Config.sweeperCacheSlots(), 1, BACKPACK_SLOTS);
        for (int i = 0; i < slotLimit; i++) {
            if (cachedItems.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldReturnToUnloadBecauseCacheFull() {
        if (!isInternalCacheFullForConfiguredSlots()) {
            return false;
        }
        if (getHealth() <= 1f || getHealth() < Config.sweeperReturnHealthThreshold()) {
            return false;
        }
        SweeperState st = getSweeperState();
        if (st == SweeperState.DOCKED || st == SweeperState.EXITING_DOCK || st == SweeperState.RETURNING) {
            return false;
        }
        return !isDocked();
    }

    private boolean canAcceptItemInCache(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack sim = stack.copy();
        for (int i = 0; i < BACKPACK_SLOTS && !sim.isEmpty(); i++) {
            ItemStack cacheStack = cachedItems.get(i);
            if (ItemStack.isSameItemSameTags(cacheStack, sim) && cacheStack.getCount() < cacheStack.getMaxStackSize()) {
                int canMove = Math.min(sim.getCount(), cacheStack.getMaxStackSize() - cacheStack.getCount());
                sim.shrink(canMove);
            }
        }
        if (sim.isEmpty()) {
            return true;
        }
        int slotLimit = Mth.clamp(Config.sweeperCacheSlots(), 1, BACKPACK_SLOTS);
        for (int i = 0; i < slotLimit; i++) {
            if (cachedItems.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在仓内时按漏斗节奏向机仓邻接容器卸货：每成功移动 1 个物品后间隔 {@link HopperBlockEntity#MOVE_ITEM_SPEED} tick。
     */
    private void tryHopperUnloadCachedItemsWhenDocked() {
        SweeperDockBlockEntity dock = getDockEntity();
        if (dock == null) {
            sweeperUnloadCooldownTicks = 0;
            return;
        }
        SweeperDockBlockEntity.AdjacentStorage nearby = dock.findAdjacentStorageForHopper();
        if (nearby == null) {
            sweeperUnloadCooldownTicks = 0;
            return;
        }
        if (sweeperUnloadCooldownTicks > 0) {
            sweeperUnloadCooldownTicks--;
            return;
        }
        if (tryEjectOneItemUsingHopperLogic(nearby)) {
            sweeperUnloadCooldownTicks = HopperBlockEntity.MOVE_ITEM_SPEED;
        }
    }

    /**
     * 将缓存中从高索引到低索引第一个非空槽取出 1 个物品，用 {@link HopperBlockEntity#addItem} 写入邻接容器（尊重 {@link
     * net.minecraft.world.WorldlyContainer} 与槽位规则）。
     */
    private boolean tryEjectOneItemUsingHopperLogic(SweeperDockBlockEntity.AdjacentStorage nearby) {
        Container dest = nearby.container();
        Direction insertFace = nearby.insertFace();
        for (int slot = BACKPACK_SLOTS - 1; slot >= 0; slot--) {
            ItemStack inSlot = getItem(slot);
            if (inSlot.isEmpty()) {
                continue;
            }
            ItemStack one = inSlot.split(1);
            ItemStack leftover = HopperBlockEntity.addItem(this, dest, one, insertFace);
            if (!leftover.isEmpty()) {
                inSlot.grow(leftover.getCount());
                continue;
            }
            return true;
        }
        return false;
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
        if (state == SweeperState.DOCKED
                || state == SweeperState.RETURNING
                || state == SweeperState.EXITING_DOCK) {
            if (isWallClimbing()) {
                exitWallClimb();
            }
        }
        if (state == SweeperState.RETURNING && prev != SweeperState.RETURNING) {
            dockApproachPhase = 0;
            returningStuckTicks = 0;
            returningLastX = Double.NaN;
            returningLastZ = Double.NaN;
            resetYawSteer();
            resetDriveSteer();
            patrolSteerTargetYaw = Float.NaN;
            patrolWallHugPhase = 0;
            exitDockPostYaw = Float.NaN;
        }
        if (prev == SweeperState.RETURNING && state != SweeperState.RETURNING) {
            returningStuckTicks = 0;
            returningLastX = Double.NaN;
            returningLastZ = Double.NaN;
        }
        if (state == SweeperState.COLLECTING && prev != SweeperState.COLLECTING) {
            if (isWallClimbing()) {
                exitWallClimb();
            }
            patrolSteerTargetYaw = Float.NaN;
            resetYawSteer();
            resetDriveSteer();
            patrolWallHugPhase = 0;
            exitDockPostYaw = Float.NaN;
            resetCollectGroundPath();
            resetReenterGroundPath();
        }
        if (prev == SweeperState.COLLECTING && state != SweeperState.COLLECTING) {
            resetCollectGroundPath();
            getNavigation().stop();
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
        if (state == SweeperState.REENTERING_PATROL && prev != SweeperState.REENTERING_PATROL) {
            if (isWallClimbing()) {
                exitWallClimb();
            }
            resetReenterGroundPath();
            resetYawSteer();
            resetDriveSteer();
            patrolWallHugPhase = 0;
            exitDockPostYaw = Float.NaN;
            patrolSteerTargetYaw = Float.NaN;
        }
        if (prev == SweeperState.REENTERING_PATROL && state != SweeperState.REENTERING_PATROL) {
            resetReenterGroundPath();
            getNavigation().stop();
        }
        if (state == SweeperState.DOCKED && prev != SweeperState.DOCKED) {
            sweeperUnloadCooldownTicks = 0;
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
        EXITING_DOCK,
        /** 在巡逻半径外时，寻路回到巡逻区内（优先级在收集与闲逛巡逻之间）。 */
        REENTERING_PATROL;

        public static SweeperState byOrdinal(int ordinal) {
            SweeperState[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return IDLE;
            }
            return values[ordinal];
        }
    }
}
