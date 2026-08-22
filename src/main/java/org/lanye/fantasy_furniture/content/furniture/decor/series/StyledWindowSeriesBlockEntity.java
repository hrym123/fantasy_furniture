package org.lanye.fantasy_furniture.content.furniture.decor.series;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lanye.fantasy_furniture.bootstrap.block.StyledWindowSeriesRegistration;
import org.lanye.reverie_core.geolib.multiblock.WallPlaneFootprint;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class StyledWindowSeriesBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final StyledWindowSeriesId seriesId;

    public StyledWindowSeriesBlockEntity(StyledWindowSeriesId seriesId, BlockPos pos, BlockState state) {
        super(StyledWindowSeriesRegistration.blockEntityType(seriesId).get(), pos, state);
        this.seriesId = seriesId;
    }

    public StyledWindowSeriesId seriesId() {
        return seriesId;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AABB getRenderBoundingBox() {
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof StyledWindowSeriesBlock)) {
            return super.getRenderBoundingBox();
        }
        StyledWindowSeriesSpec spec = StyledWindowSeriesCatalog.get(seriesId);
        WallPlaneFootprint fp = spec.footprint();
        Direction facing = state.getValue(StyledWindowSeriesBlock.FACING);
        BlockPos origin = getBlockPos();
        AABB box = null;
        for (BlockPos p : fp.allCells(origin, facing)) {
            AABB cell = new AABB(p);
            box = box == null ? cell : box.minmax(cell);
        }
        return box != null ? box.inflate(0.25D) : super.getRenderBoundingBox();
    }
}
