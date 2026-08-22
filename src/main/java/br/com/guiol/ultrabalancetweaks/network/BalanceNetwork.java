package br.com.guiol.ultrabalancetweaks.network;

import br.com.guiol.ultrabalancetweaks.UltraBalanceTweaks;
import br.com.guiol.ultrabalancetweaks.BalanceConfig;
import br.com.guiol.ultrabalancetweaks.DestructionAbility;
import br.com.guiol.ultrabalancetweaks.DestructionData;
import br.com.guiol.ultrabalancetweaks.InstinctTechnique;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BalanceNetwork {
    private static final String PROTOCOL = "3";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(UltraBalanceTweaks.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private BalanceNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, EgoSyncPacket.class,
                EgoSyncPacket::encode, EgoSyncPacket::decode, EgoSyncPacket::handle);
        CHANNEL.registerMessage(1, AbilityRequestPacket.class,
                AbilityRequestPacket::encode, AbilityRequestPacket::decode, AbilityRequestPacket::handle);
        CHANNEL.registerMessage(2, DestructionSyncPacket.class,
                DestructionSyncPacket::encode, DestructionSyncPacket::decode, DestructionSyncPacket::handle);
        CHANNEL.registerMessage(3, InstinctTechniqueRequestPacket.class,
                InstinctTechniqueRequestPacket::encode, InstinctTechniqueRequestPacket::decode,
                InstinctTechniqueRequestPacket::handle);
    }

    public static void syncEgo(ServerPlayer player, boolean active, float gauge) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EgoSyncPacket(active, gauge));
    }

    public static void requestAbility(DestructionAbility ability) {
        CHANNEL.sendToServer(new AbilityRequestPacket(ability));
    }

    public static void toggleInstinctTechnique() {
        CHANNEL.sendToServer(new InstinctTechniqueRequestPacket());
    }

    public static void syncDestruction(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DestructionSyncPacket(
                DestructionData.cooldown(player, DestructionAbility.HAKAI),
                DestructionData.cooldown(player, DestructionAbility.SPHERE),
                BalanceConfig.HAKAI_REQUIRED_EGO.get().floatValue(),
                BalanceConfig.SPHERE_REQUIRED_EGO.get().floatValue(),
                BalanceConfig.AURA_REQUIRED_EGO.get().floatValue(),
                InstinctTechnique.destructionUnlocked(player),
                InstinctTechnique.unlocked(player),
                InstinctTechnique.isActive(player)));
    }
}
