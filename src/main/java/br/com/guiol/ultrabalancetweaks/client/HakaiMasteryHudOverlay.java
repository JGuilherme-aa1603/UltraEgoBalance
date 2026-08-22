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

@Mod.EventBusSubscriber(modid = UltraBalanceTweaks.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HakaiMasteryHudOverlay {
    private static final float[] MILESTONES = {10.0f, 25.0f, 50.0f};

    private HakaiMasteryHudOverlay() {
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!BalanceConfig.HAKAI_HUD_ENABLED.get() || !ClientDestructionState.destructionUnlocked()
                || minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }

        float mastery = ClientDestructionState.hakaiMastery();
        int level = ClientDestructionState.hakaiLevel();
        String levelRoman = ClientDestructionState.hakaiLevelRoman();
        String masteryText = formatMastery(mastery);
        String title = Component.translatable("gui.ultrabalancetweaks.hakai_mastery",
                levelRoman, masteryText).getString();

        String footer;
        boolean mastered = level >= 4 && mastery >= 99.95f;
        if (mastered) {
            footer = Component.translatable("gui.ultrabalancetweaks.hakai_mastered",
                    compactPower(ClientDestructionState.battlePower())).getString();
        } else {
            String nextLevel = roman(Math.min(4, level + 1));
            footer = Component.translatable("gui.ultrabalancetweaks.hakai_next",
                    nextLevel,
                    formatMastery(ClientDestructionState.nextHakaiMastery()),
                    compactPower(ClientDestructionState.battlePower()),
                    compactPower(ClientDestructionState.nextHakaiBattlePower())).getString();
        }

        int configuredWidth = BalanceConfig.HUD_WIDTH.get();
        int contentWidth = Math.max(minecraft.font.width(title), minecraft.font.width(footer)) + 10;
        int width = Math.max(configuredWidth, Math.min(280, contentWidth));
        int x = (minecraft.getWindow().getGuiScaledWidth() - width) / 2
                + BalanceConfig.HUD_X_OFFSET.get() + BalanceConfig.HAKAI_HUD_X_OFFSET.get();
        int egoY = minecraft.getWindow().getGuiScaledHeight() - 58 + BalanceConfig.HUD_Y_OFFSET.get();
        int y = egoY - (ClientEgoState.active() ? 45 : 0) + BalanceConfig.HAKAI_HUD_Y_OFFSET.get();
        int barHeight = 7;

        long time = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        float pulse = mastered ? (float) ((Math.sin((time + event.getPartialTick()) * 0.22) + 1.0) * 0.5) : 0.0f;
        int border = mastered ? argb(235, 210 + (int) (45 * pulse), 158 + (int) (65 * pulse), 52)
                : argb(225, 139, 54, 192);

        GuiGraphics graphics = event.getGuiGraphics();
        graphics.fill(x - 3, y - 3, x + width + 3, y + barHeight + 3, argb(150, 6, 2, 12));
        graphics.fill(x - 2, y - 2, x + width + 2, y + barHeight + 2, border);
        graphics.fill(x - 1, y - 1, x + width + 1, y + barHeight + 1, argb(245, 18, 5, 28));
        graphics.fill(x, y, x + width, y + barHeight, argb(225, 31, 10, 44));

        int fillWidth = Math.max(0, Math.min(width, Math.round(width * mastery / 100.0f)));
        for (int column = 0; column < fillWidth; column++) {
            float ratio = width <= 1 ? 1.0f : column / (float) (width - 1);
            int red = 105 + Math.round(135 * ratio);
            int green = 22 + Math.round(36 * ratio);
            int blue = 158 + Math.round(85 * ratio);
            graphics.fill(x + column, y, x + column + 1, y + barHeight,
                    argb(245, red, green, blue));
        }

        if (fillWidth > 3) {
            graphics.fill(x + 1, y + 1, x + fillWidth - 1, y + 2, argb(90, 255, 224, 255));
            int shimmer = (int) ((time * 2L) % Math.max(1, width));
            if (shimmer < fillWidth) {
                graphics.fill(x + shimmer, y + 1, x + Math.min(fillWidth, shimmer + 2),
                        y + barHeight - 1, argb(115, 255, 196, 255));
            }
        }

        for (float milestone : MILESTONES) {
            int markerX = x + Math.round(width * milestone / 100.0f);
            int markerColor = mastery + 1.0E-3f >= milestone ? argb(225, 255, 224, 255)
                    : argb(175, 103, 76, 118);
            graphics.fill(markerX, y - 1, markerX + 1, y + barHeight + 1, markerColor);
        }

        int titleColor = mastered ? 0xFFFFDF75 : 0xFFF0D8FF;
        graphics.drawCenteredString(minecraft.font, title, x + width / 2, y - 12, titleColor);

        boolean powerReady = ClientDestructionState.battlePower() + 1.0E-3
                >= ClientDestructionState.nextHakaiBattlePower();
        boolean masteryReady = mastery + 1.0E-3f >= ClientDestructionState.nextHakaiMastery();
        int footerColor = mastered ? 0xFFFFD86B : powerReady && masteryReady ? 0xFFDAB6F3 : 0xFFB996C8;
        graphics.drawCenteredString(minecraft.font, footer, x + width / 2, y + barHeight + 5, footerColor);
    }

    private static String formatMastery(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String compactPower(double value) {
        if (value >= 1_000_000_000.0) {
            return compact(value / 1_000_000_000.0, "B");
        }
        if (value >= 1_000_000.0) {
            return compact(value / 1_000_000.0, "M");
        }
        if (value >= 1_000.0) {
            return compact(value / 1_000.0, "K");
        }
        return Long.toString(Math.round(value));
    }

    private static String compact(double value, String suffix) {
        if (Math.abs(value - Math.rint(value)) < 0.05) {
            return String.format(Locale.ROOT, "%.0f%s", value, suffix);
        }
        return String.format(Locale.ROOT, "%.1f%s", value, suffix);
    }

    private static String roman(int level) {
        return switch (level) {
            case 4 -> "IV";
            case 3 -> "III";
            case 2 -> "II";
            case 1 -> "I";
            default -> "—";
        };
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
    }
}
