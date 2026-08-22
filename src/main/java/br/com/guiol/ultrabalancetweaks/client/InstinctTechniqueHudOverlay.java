package br.com.guiol.ultrabalancetweaks.client;

import br.com.guiol.ultrabalancetweaks.UltraBalanceTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltraBalanceTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InstinctTechniqueHudOverlay {
    private InstinctTechniqueHudOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ClientDestructionState.instinctTechniqueActive() || minecraft.player == null
                || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        Component name = Component.translatable("gui.ultrabalancetweaks.instinct_technique_active");
        String text = DestructionKeybinds.instinctTechniqueKey().getString() + "  " + name.getString();
        int textWidth = minecraft.font.width(text);
        int width = textWidth + 14;
        int x = (minecraft.getWindow().getGuiScaledWidth() - width) / 2;
        int y = minecraft.getWindow().getGuiScaledHeight() - 78;
        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        int pulse = 185 + (int) ((Math.sin((time + event.getPartialTick()) * 0.18) + 1.0) * 25.0);

        graphics.fill(x - 2, y - 2, x + width + 2, y + 13, argb(145, 7, 17, 25));
        graphics.fill(x - 1, y - 1, x + width + 1, y + 12, argb(pulse, 151, 222, 255));
        graphics.fill(x, y, x + width, y + 11, argb(235, 16, 31, 43));
        graphics.fill(x + 2, y + 1, x + width - 2, y + 2, argb(115, 220, 248, 255));
        graphics.drawCenteredString(minecraft.font, text, x + width / 2, y + 2, 0xE9FAFF);
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
    }
}
