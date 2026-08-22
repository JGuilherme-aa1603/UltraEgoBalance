package br.com.guiol.ultrabalancetweaks.client;

import br.com.guiol.ultrabalancetweaks.BalanceConfig;
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
public final class EgoHudOverlay {
    private EgoHudOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!BalanceConfig.HUD_ENABLED.get() || !ClientEgoState.active() || minecraft.player == null
                || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }

        float gauge = ClientEgoState.animatedGauge(event.getPartialTick());
        GuiGraphics graphics = event.getGuiGraphics();
        int width = BalanceConfig.HUD_WIDTH.get();
        int x = (minecraft.getWindow().getGuiScaledWidth() - width) / 2 + BalanceConfig.HUD_X_OFFSET.get();
        int y = minecraft.getWindow().getGuiScaledHeight() - 58 + BalanceConfig.HUD_Y_OFFSET.get();
        int barHeight = 9;

        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        float pulse = gauge >= 95.0f ? (float) ((Math.sin((time + event.getPartialTick()) * 0.28) + 1.0) * 0.5) : 0.0f;
        int outerColor = gauge >= 95.0f ? argb(230, 205 + (int) (50 * pulse), 76, 255) : argb(220, 118, 43, 166);

        graphics.fill(x - 3, y - 3, x + width + 3, y + barHeight + 3, argb(150, 8, 2, 13));
        graphics.fill(x - 2, y - 2, x + width + 2, y + barHeight + 2, outerColor);
        graphics.fill(x - 1, y - 1, x + width + 1, y + barHeight + 1, argb(245, 20, 5, 30));
        graphics.fill(x, y, x + width, y + barHeight, argb(225, 31, 11, 42));

        int fillWidth = Math.max(0, Math.min(width, Math.round(width * gauge / 100.0f)));
        for (int column = 0; column < fillWidth; column++) {
            float ratio = width <= 1 ? 1.0f : column / (float) (width - 1);
            int red = 95 + Math.round(145 * ratio);
            int green = 18 + Math.round(35 * ratio);
            int blue = 148 + Math.round(107 * ratio);
            graphics.fill(x + column, y, x + column + 1, y + barHeight, argb(245, red, green, blue));
        }

        if (fillWidth > 3) {
            int shimmer = (int) ((time * 2L) % Math.max(1, width));
            if (shimmer < fillWidth) {
                graphics.fill(x + shimmer, y + 1, x + Math.min(fillWidth, shimmer + 2), y + barHeight - 1,
                        argb(120, 255, 205, 255));
            }
            graphics.fill(x + 1, y + 1, x + fillWidth - 1, y + 2, argb(95, 255, 218, 255));
        }

        for (int segment = 1; segment < 10; segment++) {
            int segmentX = x + Math.round(width * segment / 10.0f);
            graphics.fill(segmentX, y, segmentX + 1, y + barHeight, argb(110, 15, 3, 24));
        }

        if (gauge >= ClientDestructionState.auraRequirement()) {
            Component aura = Component.translatable("gui.ultrabalancetweaks.aura");
            graphics.drawString(minecraft.font, aura, x + width - minecraft.font.width(aura) - 2,
                    y + 1, 0xFFF0B6, true);
        }

        Component title = Component.translatable(gauge >= 99.95f
                ? "gui.ultrabalancetweaks.ego_full" : "gui.ultrabalancetweaks.ego");
        String label = title.getString();
        if (BalanceConfig.HUD_SHOW_NUMERIC_VALUE.get()) {
            label += "  " + Math.round(gauge) + "%";
        }
        int textColor = gauge >= 95.0f ? 0xFFF0B6 : 0xE9C8FF;
        graphics.drawCenteredString(minecraft.font, label, x + width / 2, y - 11, textColor);

        double ratio = Math.max(0.0, Math.min(1.0, gauge / 100.0));
        double basePower = BalanceConfig.ULTRA_EGO_MULTIPLIERS.kiPower().get();
        double currentPower = basePower
                + (BalanceConfig.EGO_MAX_PWR_MULTIPLIER.get() - basePower) * ratio;
        String power = Component.translatable("gui.ultrabalancetweaks.power",
                String.format(Locale.ROOT, "%.1f", currentPower)).getString();
        graphics.drawCenteredString(minecraft.font, power, x + width / 2, y - 21, 0xDAB6F3FF);

        if (BalanceConfig.HUD_SHOW_ABILITIES.get()) {
            int gap = 4;
            int chipWidth = (width - gap) / 2;
            drawAbilityChip(graphics, minecraft, x, y + barHeight + 5, chipWidth,
                    DestructionKeybinds.hakaiKey(), Component.translatable("gui.ultrabalancetweaks.hakai_short"),
                    gauge, ClientDestructionState.hakaiRequirement(), ClientDestructionState.hakaiCooldown());
            drawAbilityChip(graphics, minecraft, x + chipWidth + gap, y + barHeight + 5, chipWidth,
                    DestructionKeybinds.sphereKey(), Component.translatable("gui.ultrabalancetweaks.sphere_short"),
                    gauge, ClientDestructionState.sphereRequirement(), ClientDestructionState.sphereCooldown());
        }
    }

    private static void drawAbilityChip(GuiGraphics graphics, Minecraft minecraft, int x, int y, int width,
                                        Component key, Component name, float gauge, float requirement, int cooldown) {
        boolean unlocked = gauge + 1.0E-3f >= requirement;
        boolean ready = unlocked && cooldown <= 0;
        int border = ready ? argb(220, 215, 72, 255)
                : unlocked ? argb(210, 132, 72, 171) : argb(180, 70, 54, 79);
        int textColor = ready ? 0xFFF0B6 : unlocked ? 0xD9B6E8 : 0x887A91;
        graphics.fill(x, y, x + width, y + 11, argb(155, 8, 2, 13));
        graphics.fill(x + 1, y + 1, x + width - 1, y + 10, border);
        graphics.fill(x + 2, y + 2, x + width - 2, y + 9, argb(235, 24, 7, 34));

        String status;
        if (!unlocked) {
            status = "E" + Math.round(requirement);
        } else if (cooldown > 0) {
            status = (int) Math.ceil(cooldown / 20.0) + "s";
        } else {
            status = Component.translatable("gui.ultrabalancetweaks.ready_short").getString();
        }
        String text = key.getString() + " " + name.getString() + " " + status;
        graphics.drawCenteredString(minecraft.font, text, x + width / 2, y + 2, textColor);
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
    }
}
