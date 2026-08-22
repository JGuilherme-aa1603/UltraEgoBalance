package br.com.guiol.ultrabalancetweaks.network;

import br.com.guiol.ultrabalancetweaks.client.ClientCounterState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CounterSyncPacket(int remainingTicks, float multiplier) {
    static void encode(CounterSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.remainingTicks);
        buffer.writeFloat(packet.multiplier);
    }

    static CounterSyncPacket decode(FriendlyByteBuf buffer) {
        return new CounterSyncPacket(buffer.readVarInt(), buffer.readFloat());
    }

    static void handle(CounterSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientCounterState.update(packet.remainingTicks, packet.multiplier)));
        context.setPacketHandled(true);
    }
}
