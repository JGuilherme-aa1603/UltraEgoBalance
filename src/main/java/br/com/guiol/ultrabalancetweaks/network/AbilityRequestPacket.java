package br.com.guiol.ultrabalancetweaks.network;

import br.com.guiol.ultrabalancetweaks.DestructionAbilities;
import br.com.guiol.ultrabalancetweaks.DestructionAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AbilityRequestPacket(DestructionAbility ability) {
    static void encode(AbilityRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.ability);
    }

    static AbilityRequestPacket decode(FriendlyByteBuf buffer) {
        return new AbilityRequestPacket(buffer.readEnum(DestructionAbility.class));
    }

    static void handle(AbilityRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> DestructionAbilities.activate(sender, packet.ability));
        }
        context.setPacketHandled(true);
    }
}
