package br.com.guiol.ultrabalancetweaks.network;

import br.com.guiol.ultrabalancetweaks.InstinctTechnique;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class InstinctTechniqueRequestPacket {
    static void encode(InstinctTechniqueRequestPacket packet, FriendlyByteBuf buffer) {
    }

    static InstinctTechniqueRequestPacket decode(FriendlyByteBuf buffer) {
        return new InstinctTechniqueRequestPacket();
    }

    static void handle(InstinctTechniqueRequestPacket packet,
                       Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> InstinctTechnique.toggle(sender));
        }
        context.setPacketHandled(true);
    }
}
