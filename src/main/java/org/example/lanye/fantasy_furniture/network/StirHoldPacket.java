package org.example.lanye.fantasy_furniture.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.example.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;

import java.util.function.Supplier;

/** C2S：长按达到阈值后开始循环（true），松开右键或准心离开则立即停止（false）。 */
public final class StirHoldPacket {

    private final BlockPos pos;
    private final boolean active;

    public StirHoldPacket(BlockPos pos, boolean active) {
        this.pos = pos;
        this.active = active;
    }

    public static void encode(StirHoldPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.active);
    }

    public static StirHoldPacket decode(FriendlyByteBuf buf) {
        return new StirHoldPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(StirHoldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get()
                .enqueueWork(
                        () -> {
                            ServerPlayer player = ctx.get().getSender();
                            if (player == null) {
                                return;
                            }
                            var level = player.serverLevel();
                            if (!level.isLoaded(msg.pos)) {
                                return;
                            }
                            if (player.distanceToSqr(Vec3.atCenterOf(msg.pos)) > 10.0 * 10.0) {
                                return;
                            }
                            BlockEntity be = level.getBlockEntity(msg.pos);
                            if (be instanceof MixingBowlBlockEntity bowl) {
                                bowl.onServerHoldStir(msg.active);
                            }
                        });
        ctx.get().setPacketHandled(true);
    }
}
