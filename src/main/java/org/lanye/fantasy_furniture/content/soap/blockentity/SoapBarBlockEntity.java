package org.lanye.fantasy_furniture.content.soap.blockentity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lanye.fantasy_furniture.bootstrap.block.ModBlocks;
import org.lanye.fantasy_furniture.content.soap.SoapBarAppearance;
import org.lanye.fantasy_furniture.content.soap.SoapFlatLiquidMaterials;
import org.lanye.fantasy_furniture.content.soap.SoapWaterParticles;
import org.lanye.fantasy_furniture.content.soap.block.SoapBarBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 肥皂方块实体：耐久度与颜料由 {@link SoapBarBlock} 方块状态驱动渲染；入水耐久消耗计时与粒子色写在本实体。
 */
public class SoapBarBlockEntity extends BlockEntity implements GeoBlockEntity {

    /** 每消耗 1 点耐久所需连续浸水时长（1 分钟）。 */
    public static final int WATER_WEAR_INTERVAL_TICKS = 20 * 60;

    private static final int AMBIENT_PARTICLE_INTERVAL_TICKS = 12;

    private static final String TAG_WATER_WEAR_TICKS = "WaterWearTicks";
    private static final String TAG_PART_MAT = "PartMat";
    private static final String TAG_PART_FROM_LIQUID = SoapBarAppearance.NBT_PART_FROM_LIQUID;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** 当前耐久档内已累计的浸水 tick；离水不清零。 */
    private int waterWearTicks;

    private boolean wasImmersed;

    /** 粒子贴图 id（1–6）；0 表示未写入。 */
    private int particleMatId = 0;

    /** true：粒子色来自制皂液体，可与方块颜料不同。 */
    private boolean particleFromLiquid;

    public SoapBarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SOAP_BAR.blockEntityType().get(), pos, state);
    }

    public int particleMatId() {
        return particleMatId;
    }

    public boolean particleFromLiquid() {
        return particleFromLiquid;
    }

    public void setParticleMatId(int particleMatId) {
        setParticleMat(particleMatId, this.particleFromLiquid);
    }

    public void setParticleMat(int particleMatId, boolean fromLiquid) {
        if (particleMatId != 0 && !SoapFlatLiquidMaterials.isValid(particleMatId)) {
            particleMatId = 0;
        }
        if (this.particleMatId != particleMatId || this.particleFromLiquid != fromLiquid) {
            this.particleMatId = particleMatId;
            this.particleFromLiquid = fromLiquid && particleMatId > 0;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    public void serverTick(ServerLevel level) {
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof SoapBarBlock)) {
            return;
        }
        boolean immersed = SoapBarBlock.isImmersedInWater(level, worldPosition, state);
        if (!immersed) {
            if (wasImmersed) {
                wasImmersed = false;
                setChanged();
            }
            return;
        }
        wasImmersed = true;
        waterWearTicks++;
        if (waterWearTicks % AMBIENT_PARTICLE_INTERVAL_TICKS == 0) {
            SoapWaterParticles.spawnAmbient(level, worldPosition, state, this);
        }
        if (waterWearTicks % 20 == 0) {
            setChanged();
        }
        if (waterWearTicks < WATER_WEAR_INTERVAL_TICKS) {
            return;
        }
        waterWearTicks = 0;
        setChanged();
        SoapWaterParticles.spawnBurst(level, worldPosition, state, this);
        SoapBarBlock.advanceDurability(level, worldPosition, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (waterWearTicks > 0) {
            tag.putInt(TAG_WATER_WEAR_TICKS, waterWearTicks);
        }
        if (particleMatId > 0) {
            tag.putInt(TAG_PART_MAT, particleMatId);
        }
        if (particleFromLiquid) {
            tag.putBoolean(TAG_PART_FROM_LIQUID, true);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        waterWearTicks = Math.max(0, tag.getInt(TAG_WATER_WEAR_TICKS));
        if (waterWearTicks > WATER_WEAR_INTERVAL_TICKS) {
            waterWearTicks = WATER_WEAR_INTERVAL_TICKS - 1;
        }
        int part = tag.contains(TAG_PART_MAT) ? tag.getInt(TAG_PART_MAT) : 0;
        particleMatId =
                part == 0 || SoapFlatLiquidMaterials.isValid(part) ? part : 0;
        particleFromLiquid = particleMatId > 0 && tag.getBoolean(TAG_PART_FROM_LIQUID);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (particleMatId > 0) {
            tag.putInt(TAG_PART_MAT, particleMatId);
        }
        if (particleFromLiquid) {
            tag.putBoolean(TAG_PART_FROM_LIQUID, true);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
