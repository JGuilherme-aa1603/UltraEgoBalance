package br.com.guiol.ultrabalancetweaks.network;

import br.com.guiol.ultrabalancetweaks.InstinctCounterData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class CounterRequestPacket {
    static void encode(CounterRequestPacket packet, FriendlyByteBuf buffer) {
    }

    static CounterRequestPacket decode(FriendlyByteBuf buffer) {
        return new CounterRequestPacket();
    }

    static void handle(CounterRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getSender() != null) {
            context.enqueueWork(() -> InstinctCounterData.execute(context.getSender()));
        }
        context.setPacketHandled(true);
    }
}
