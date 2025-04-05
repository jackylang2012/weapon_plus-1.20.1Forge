package net.lclmod.weapon_plus.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncPlayerVelocityPacket {
    private final Vec3 velocity;

    public SyncPlayerVelocityPacket(Vec3 velocity) {
        this.velocity = velocity;
    }

    public Vec3 getVelocity() {
        return velocity;
    }

    // 编码数据包
    public static void encode(SyncPlayerVelocityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.velocity.x);
        buffer.writeDouble(packet.velocity.y);
        buffer.writeDouble(packet.velocity.z);
    }

    // 解码数据包
    public static SyncPlayerVelocityPacket decode(FriendlyByteBuf buffer) {
        return new SyncPlayerVelocityPacket(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

    // 处理数据包
    public static void handle(SyncPlayerVelocityPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // 在客户端设置玩家速度
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.setDeltaMovement(packet.getVelocity());
            }
        });
        context.get().setPacketHandled(true);
    }
}