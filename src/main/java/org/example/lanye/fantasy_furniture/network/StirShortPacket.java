package org.example.lanye.fantasy_furniture.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.example.lanye.fantasy_furniture.block.entity.MixingBowlBlockEntity;

import java.util.function.Supplier;

/** C2S：短按松开时播放一次搅拌动画。 */
public final class StirShortPacket {

    private final BlockPos pos;

    public StirShortPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(StirShortPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static StirShortPacket decode(FriendlyByteBuf buf) {
        return new StirShortPacket(buf.readBlockPos());
    }

    public static void handle(StirShortPacket msg, Supplier<NetworkEvent.Context> ctx) {
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
                                bowl.onServerShortStir();
                            }
                        });
        ctx.get().setPacketHandled(true);
    }
}
