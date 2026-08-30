package br.com.guiol.ultrabalancetweaks.client;

import br.com.guiol.ultrabalancetweaks.DestructionAbility;
import br.com.guiol.ultrabalancetweaks.UltraBalanceTweaks;
import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltraBalanceTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DestructionInputHandler {
    private static boolean counterAttackWasDown;

    private DestructionInputHandler() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientDestructionState.tick();
        ClientCounterState.tick();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            counterAttackWasDown = false;
            return;
        }
        boolean counterAttackDown = minecraft.options.keyAttack.isDown();
        if (ClientCounterState.active() && counterAttackDown && !counterAttackWasDown) {
            BalanceNetwork.requestCounter();
        }
        counterAttackWasDown = counterAttackDown;
        while (DestructionKeybinds.HAKAI.consumeClick()) {
            BalanceNetwork.requestAbility(DestructionAbility.HAKAI);
        }
        while (DestructionKeybinds.SPHERE.consumeClick()) {
            BalanceNetwork.requestAbility(DestructionAbility.SPHERE);
        }
        while (DestructionKeybinds.INSTINCT_TECHNIQUE.consumeClick()) {
            BalanceNetwork.toggleInstinctTechnique();
        }
    }

    @SubscribeEvent
    public static void instinctiveCounterClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || !ClientCounterState.active()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        BalanceNetwork.requestCounter();
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientDestructionState.clear();
        ClientCounterState.clear();
        ClientEgoState.clear();
        counterAttackWasDown = false;
    }
}
