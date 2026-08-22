package br.com.guiol.ultrabalancetweaks.network;

import br.com.guiol.ultrabalancetweaks.client.ClientDestructionState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DestructionSyncPacket(int hakaiCooldown, int sphereCooldown,
                                    float hakaiRequirement, float sphereRequirement, float auraRequirement,
                                    boolean destructionUnlocked, boolean instinctTechniqueUnlocked,
                                    boolean instinctTechniqueActive) {
    static void encode(DestructionSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.hakaiCooldown);
        buffer.writeVarInt(packet.sphereCooldown);
        buffer.writeFloat(packet.hakaiRequirement);
        buffer.writeFloat(packet.sphereRequirement);
        buffer.writeFloat(packet.auraRequirement);
        buffer.writeBoolean(packet.destructionUnlocked);
        buffer.writeBoolean(packet.instinctTechniqueUnlocked);
        buffer.writeBoolean(packet.instinctTechniqueActive);
    }

    static DestructionSyncPacket decode(FriendlyByteBuf buffer) {
        return new DestructionSyncPacket(buffer.readVarInt(), buffer.readVarInt(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean());
    }

    static void handle(DestructionSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientDestructionState.update(packet.hakaiCooldown, packet.sphereCooldown,
                        packet.hakaiRequirement, packet.sphereRequirement, packet.auraRequirement,
                        packet.destructionUnlocked, packet.instinctTechniqueUnlocked,
                        packet.instinctTechniqueActive)));
        context.setPacketHandled(true);
    }
}
