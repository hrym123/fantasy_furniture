package org.lanye.fantasy_furniture.block.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.lanye.fantasy_furniture.FantasyFurniture;
import org.lanye.fantasy_furniture.block.registry.ModBlocks;
import org.lanye.fantasy_furniture.entity.SweeperRobotEntity;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 机仓方块实体：提供停靠位与附近容器检索；贴图条带随绑定机器人血量变化。 */
public class SweeperDockBlockEntity extends BlockEntity implements GeoBlockEntity {

    /**
     * 与 {@link net.minecraft.world.level.block.entity.HopperBlockEntity#addItem} 配合：目标容器及从容器外侧指向方块内的插入面（与漏斗推入箱子的方向语义一致）。
     */
    public record AdjacentStorage(Container container, Direction insertFace) {}

    private static final String NBT_TEX = "DockTex";
    private static final String NBT_CHG = "DockChg";

    /** 客户端用世界时间切换贴图，约每半周期换一次（与下一档材质交替）。 */
    private static final long CHARGE_TEXTURE_HALF_PERIOD_TICKS = 15L;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** 0..4 → {@code textures/block/sweeper_dock_0.png} … {@code _4}，由绑定机器人血量推导。 */
    private byte syncedTextureBand = 4;

    /** 同步到客户端：绑定机器人在仓内且未满血，用于充电闪烁（具体相位由 {@link Level#getGameTime()} 在客户端算）。 */
    private boolean syncedCharging;

    public SweeperDockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SWEEPER_DOCK.blockEntityType().get(), pos, state);
    }

    /** 将机器人血量（1~20）映射到贴图条带索引（0~4）。 */
    public static int healthToTextureBand(int health) {
        int h = Mth.clamp(health, 1, 20);
        if (h == 1) {
            return 0;
        }
        if (h <= 7) {
            return 1;
        }
        if (h <= 12) {
            return 2;
        }
        if (h <= 17) {
            return 3;
        }
        return 4;
    }

    public ResourceLocation getTextureLocation() {
        int base = Mth.clamp(syncedTextureBand, 0, 4);
        int next = Math.min(4, base + 1);
        int i = base;
        if (syncedCharging && next > base) {
            Level level = getLevel();
            if (level != null) {
                long t = level.getGameTime();
                if ((t / CHARGE_TEXTURE_HALF_PERIOD_TICKS) % 2L == 1L) {
                    i = next;
                }
            }
        }
        return ResourceLocation.fromNamespaceAndPath(
                FantasyFurniture.MODID, "textures/block/sweeper_dock_" + i + ".png");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SweeperDockBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel) || be.isRemoved()) {
            return;
        }
        AABB box = new AABB(pos).inflate(24.0);
        List<SweeperRobotEntity> robots =
                serverLevel.getEntitiesOfClass(SweeperRobotEntity.class, box, r -> pos.equals(r.getDockPos()));
        int health = resolveBoundRobotHealth(robots);
        byte band = (byte) healthToTextureBand(health);
        boolean charging = !robots.isEmpty() && robots.get(0).isDockedChargingForDisplay();
        boolean dirty = false;
        if (band != be.syncedTextureBand) {
            be.syncedTextureBand = band;
            dirty = true;
        }
        if (charging != be.syncedCharging) {
            be.syncedCharging = charging;
            dirty = true;
        }
        if (dirty) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static int resolveBoundRobotHealth(List<SweeperRobotEntity> robots) {
        if (robots.isEmpty()) {
            return 20;
        }
        return Mth.clamp(Mth.floor(robots.get(0).getHealth()), 1, 20);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, animState -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /**
     * 查询机仓曼哈顿距离 1 内的存储容器（不含机仓自身），并计算漏斗式插入所用的面朝向。
     *
     * <p>插入面为：从容器方块指向机仓的方向——即物品从机仓侧进入容器时所对的容器外表面（与原版漏斗语义一致）。
     */
    @Nullable
    public AdjacentStorage findAdjacentStorageForHopper() {
        Level level = getLevel();
        if (level == null) {
            return null;
        }
        BlockPos center = getBlockPos();
        for (BlockPos pos : BlockPos.withinManhattan(center, 1, 1, 1)) {
            if (pos.equals(center)) {
                continue;
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container container) {
                int dx = pos.getX() - center.getX();
                int dy = pos.getY() - center.getY();
                int dz = pos.getZ() - center.getZ();
                Direction fromDockToContainer = Direction.getNearest(dx, dy, dz);
                Direction insertFace = fromDockToContainer.getOpposite();
                return new AdjacentStorage(container, insertFace);
            }
        }
        return null;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1.0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte(NBT_TEX, syncedTextureBand);
        tag.putBoolean(NBT_CHG, syncedCharging);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(NBT_TEX, Tag.TAG_BYTE)) {
            syncedTextureBand = tag.getByte(NBT_TEX);
        }
        if (tag.contains(NBT_CHG, Tag.TAG_BYTE)) {
            syncedCharging = tag.getBoolean(NBT_CHG);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putByte(NBT_TEX, syncedTextureBand);
        tag.putBoolean(NBT_CHG, syncedCharging);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains(NBT_TEX, Tag.TAG_BYTE)) {
            syncedTextureBand = tag.getByte(NBT_TEX);
        }
        if (tag.contains(NBT_CHG, Tag.TAG_BYTE)) {
            syncedCharging = tag.getBoolean(NBT_CHG);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static BlockEntityType<SweeperDockBlockEntity> type() {
        return ModBlocks.SWEEPER_DOCK.blockEntityType().get();
    }
}
