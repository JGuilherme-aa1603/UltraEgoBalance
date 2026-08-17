package br.com.guiol.ultrabalancetweaks.network;

import br.com.guiol.ultrabalancetweaks.UltraBalanceTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BalanceNetwork {
    private static final String PROTOCOL = "1";
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
    }

    public static void syncEgo(ServerPlayer player, boolean active, float gauge) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EgoSyncPacket(active, gauge));
    }
}
