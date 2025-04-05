package net.lclmod.weapon_plus.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("weapon_plus", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // 注册数据包
    public static void register() {
        CHANNEL.registerMessage(0, SyncPlayerVelocityPacket.class, SyncPlayerVelocityPacket::encode, SyncPlayerVelocityPacket::decode, SyncPlayerVelocityPacket::handle);
    }

    // 发送数据包给客户端
    public static void sendToClient(SyncPlayerVelocityPacket packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}