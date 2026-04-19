package org.lanye.fantasy_furniture.entity;

import com.google.gson.Gson;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.lanye.fantasy_furniture.block.facing.BanquetteBlock;
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

    // #region agent log
    private static final Gson AGENT_GSON = new Gson();
    private static final String AGENT_INGEST =
            "http://127.0.0.1:7482/ingest/e4fd49ed-fe10-4617-802c-ce4c6b004423";

    private static Path resolveAgentDebugLog() {
        Path cwd = Path.of("").toAbsolutePath();
        if ("run".equalsIgnoreCase(String.valueOf(cwd.getFileName()))) {
            return cwd.getParent().resolve("debug-34ccf7.log");
        }
        return cwd.resolve("debug-34ccf7.log");
    }

    private static List<Path> agentDebugLogCandidates() {
        Set<Path> set = new LinkedHashSet<>();
        set.add(resolveAgentDebugLog());
        Path cwd = Path.of("").toAbsolutePath();
        set.add(cwd.resolve("debug-34ccf7.log"));
        Path parent = cwd.getParent();
        if (parent != null) {
            set.add(parent.resolve("debug-34ccf7.log"));
        }
        set.add(Path.of(System.getProperty("user.home"), "fantasy_furniture_debug-34ccf7.log"));
        return new ArrayList<>(set);
    }

    private static void agentLog(String hypothesisId, String location, String message, Map<String, Object> data) {
        Map<String, Object> line = new HashMap<>();
        line.put("sessionId", "34ccf7");
        line.put("runId", "post-fix");
        line.put("hypothesisId", hypothesisId);
        line.put("location", location);
        line.put("message", message);
        line.put("data", data);
        line.put("timestamp", System.currentTimeMillis());
        String payload = AGENT_GSON.toJson(line);
        for (Path p : agentDebugLogCandidates()) {
            try {
                Files.writeString(
                        p,
                        payload + "\n",
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } catch (Throwable ignored) {
            }
        }
        Thread t =
                new Thread(
                        () -> {
                            try {
                                HttpURLConnection c =
                                        (HttpURLConnection) new URL(AGENT_INGEST).openConnection();
                                c.setRequestMethod("POST");
                                c.setRequestProperty("Content-Type", "application/json");
                                c.setRequestProperty("X-Debug-Session-Id", "34ccf7");
                                c.setDoOutput(true);
                                c.setConnectTimeout(400);
                                c.setReadTimeout(400);
                                try (OutputStream os = c.getOutputStream()) {
                                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                                }
                                c.getResponseCode();
                                c.disconnect();
                            } catch (Throwable ignored) {
                            }
                        },
                        "fantasy-furniture-agent-log");
        t.setDaemon(true);
        t.start();
    }
    // #endregion

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

        // #region agent log
        {
            Map<String, Object> d = new HashMap<>();
            d.put("configId", configId);
            d.put("anchorPos", anchorPos.toString());
            d.put("blockValid", blockValid);
            d.put("anchorState", anchorState.toString());
            d.put("entityYaw", getYRot());
            d.put("forward", forward.getName());
            d.put("frontColumn", frontColumn.toString());
            if (anchorState.hasProperty(BanquetteBlock.FACING)) {
                Direction bf = anchorState.getValue(BanquetteBlock.FACING);
                d.put("banquetteFacing", bf.getName());
                d.put("banquetteFacingOpposite", bf.getOpposite().getName());
            }
            agentLog("H2", "FurnitureSeatEntity.java:findSafeDismountNear", "forward computed", d);
        }
        // #endregion

        Vec3 v = findSafeOnColumn(passenger.getType(), collision, frontColumn);
        if (v != null) {
            // #region agent log
            {
                Map<String, Object> d = new HashMap<>();
                d.put("branch", "frontColumn");
                d.put("result", v.toString());
                agentLog("H3", "FurnitureSeatEntity.java:findSafeDismountNear", "findSafeOnColumn front ok", d);
            }
            // #endregion
            return v;
        }

        // #region agent log
        agentLog(
                "H3",
                "FurnitureSeatEntity.java:findSafeDismountNear",
                "front column unsafe, trying neighbors",
                Map.of(
                        "frontColumn",
                        frontColumn.toString(),
                        "fallbackOrder",
                        "sidesThenBack"));
        // #endregion

        // Plane.HORIZONTAL 的枚举顺序会在「前方格」不可用时先尝试到「正后方」邻格；改为先两侧再背后，更符合「尽量朝前」。
        for (Direction dir : horizontalFallbackDirections(forward)) {
            v = findSafeOnColumn(passenger.getType(), collision, anchorPos.relative(dir));
            if (v != null) {
                // #region agent log
                {
                    Map<String, Object> d = new HashMap<>();
                    d.put("branch", "fallbackNeighbor");
                    d.put("dir", dir.getName());
                    d.put("result", v.toString());
                    agentLog("H3", "FurnitureSeatEntity.java:findSafeDismountNear", "fallback column ok", d);
                }
                // #endregion
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
