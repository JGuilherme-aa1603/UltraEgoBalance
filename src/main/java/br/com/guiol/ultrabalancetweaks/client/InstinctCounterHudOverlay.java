package br.com.guiol.ultrabalancetweaks.client;

import br.com.guiol.ultrabalancetweaks.UltraBalanceTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = UltraBalanceTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InstinctCounterHudOverlay {
    private InstinctCounterHudOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ClientCounterState.active() || minecraft.player == null || minecraft.options.hideGui
                || minecraft.screen != null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
        int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;
        String amount = String.format(Locale.ROOT, "x%.2f", ClientCounterState.multiplier());
        String text = Component.translatable("gui.ultrabalancetweaks.counter_ready", amount).getString();
        int width = minecraft.font.width(text) + 12;
        int x = centerX - width / 2;
        int y = centerY + 15;

        graphics.fill(x - 1, y - 1, x + width + 1, y + 11, 0xA0071119);
        graphics.fill(x, y, x + width, y + 10, 0xD0152632);
        int fill = Math.max(1, Math.round((width - 2) * ClientCounterState.progress()));
        graphics.fill(x + 1, y + 8, x + 1 + fill, y + 9, 0xE9C9F4FF);
        graphics.drawCenteredString(minecraft.font, text, centerX, y + 1, 0xE9F7FDFF);

        int color = 0xE9D9F8FF;
        graphics.fill(centerX - 8, centerY - 8, centerX - 6, centerY - 2, color);
        graphics.fill(centerX + 6, centerY - 8, centerX + 8, centerY - 2, color);
        graphics.fill(centerX - 8, centerY + 2, centerX - 6, centerY + 8, color);
        graphics.fill(centerX + 6, centerY + 2, centerX + 8, centerY + 8, color);
    }
}
