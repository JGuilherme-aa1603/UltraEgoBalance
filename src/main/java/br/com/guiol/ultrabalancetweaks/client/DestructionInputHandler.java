package br.com.guiol.ultrabalancetweaks.client;

import br.com.guiol.ultrabalancetweaks.DestructionAbility;
import br.com.guiol.ultrabalancetweaks.UltraBalanceTweaks;
import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltraBalanceTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DestructionInputHandler {
    private DestructionInputHandler() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientDestructionState.tick();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        while (DestructionKeybinds.HAKAI.consumeClick()) {
            BalanceNetwork.requestAbility(DestructionAbility.HAKAI);
        }
        while (DestructionKeybinds.SPHERE.consumeClick()) {
            BalanceNetwork.requestAbility(DestructionAbility.SPHERE);
        }
    }
}
