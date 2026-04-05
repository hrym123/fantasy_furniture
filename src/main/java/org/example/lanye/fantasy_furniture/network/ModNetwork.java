package org.example.lanye.fantasy_furniture.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.example.lanye.fantasy_furniture.Fantasy_furniture;

/** C2S：短按一次 / 长按循环起停。 */
public final class ModNetwork {

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    ResourceLocation.fromNamespaceAndPath(Fantasy_furniture.MODID, "main"),
                    () -> PROTOCOL,
                    PROTOCOL::equals,
                    PROTOCOL::equals);

    private static int nextId = 0;

    private ModNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(
                nextId++,
                StirShortPacket.class,
                StirShortPacket::encode,
                StirShortPacket::decode,
                StirShortPacket::handle);
        CHANNEL.registerMessage(
                nextId++,
                StirHoldPacket.class,
                StirHoldPacket::encode,
                StirHoldPacket::decode,
                StirHoldPacket::handle);
    }
}
