package org.lanye.fantasy_furniture.content.furniture.livingroom;

import javax.annotation.Nullable;
import net.minecraft.world.phys.HitResult;

/**
 * 客户端每帧由 Mixin 写入当前准心 {@link HitResult}，供 {@link org.lanye.fantasy_furniture.content.furniture.livingroom.block.BedPlate6Block#getCloneItemStack(net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState)}
 * 在无五参数 API 时仍能按击中高度解析床品子件。服务端不写入，{@link #peek()} 恒为 {@code null}。
 */
public final class BedPlate6CrosshairPick {

    private static final ThreadLocal<HitResult> CURRENT = new ThreadLocal<>();

    private BedPlate6CrosshairPick() {}

    /** 仅客户端主线程在 {@code Minecraft#tick} 末尾调用。 */
    public static void setClientCrosshairHit(@Nullable HitResult hit) {
        if (hit == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(hit);
        }
    }

    @Nullable
    public static HitResult peek() {
        return CURRENT.get();
    }
}
