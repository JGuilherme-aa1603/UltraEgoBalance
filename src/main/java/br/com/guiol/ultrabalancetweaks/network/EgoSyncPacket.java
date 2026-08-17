package br.com.guiol.ultrabalancetweaks.network;

import br.com.guiol.ultrabalancetweaks.client.ClientEgoState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EgoSyncPacket(boolean active, float gauge) {
    static void encode(EgoSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
        buffer.writeFloat(packet.gauge);
    }

    static EgoSyncPacket decode(FriendlyByteBuf buffer) {
        return new EgoSyncPacket(buffer.readBoolean(), buffer.readFloat());
    }

    static void handle(EgoSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientEgoState.update(packet.active, packet.gauge)));
        context.setPacketHandled(true);
    }
}
