package br.com.guiol.ultrabalancetweaks.network;

import br.com.guiol.ultrabalancetweaks.client.ClientEgoState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EgoSyncPacket(boolean active, float gauge, float basePowerMultiplier, float maxPowerMultiplier) {
    static void encode(EgoSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
        buffer.writeFloat(packet.gauge);
        buffer.writeFloat(packet.basePowerMultiplier);
        buffer.writeFloat(packet.maxPowerMultiplier);
    }

    static EgoSyncPacket decode(FriendlyByteBuf buffer) {
        return new EgoSyncPacket(buffer.readBoolean(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    static void handle(EgoSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientEgoState.update(packet.active, packet.gauge,
                        packet.basePowerMultiplier, packet.maxPowerMultiplier)));
        context.setPacketHandled(true);
    }
}
