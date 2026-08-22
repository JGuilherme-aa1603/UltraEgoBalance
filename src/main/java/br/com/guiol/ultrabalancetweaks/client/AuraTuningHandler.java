package br.com.guiol.ultrabalancetweaks.client;

import br.com.guiol.ultrabalancetweaks.FormTuning;
import br.com.guiol.ultrabalancetweaks.SuperKamehameha;
import br.com.guiol.ultrabalancetweaks.UltraBalanceTweaks;
import com.dragonminez.common.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.unofficial.unofficialdmzaddon.client.GodAuraConfigAccess;

@Mod.EventBusSubscriber(modid = UltraBalanceTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AuraTuningHandler {
    private static boolean preferenceMigrated;

    private AuraTuningHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) {
            return;
        }
        if (!preferenceMigrated) {
            Object userConfig = ConfigManager.getUserConfig();
            if (userConfig instanceof GodAuraConfigAccess access) {
                if (access.unofficialdmzaddon$constantGodFormAuras()) {
                    access.unofficialdmzaddon$setConstantGodFormAuras(false);
                    ConfigManager.saveGeneralUserConfig();
                }
                preferenceMigrated = true;
            }
        }
        if (Minecraft.getInstance().player.tickCount == 20
                || Minecraft.getInstance().player.tickCount % 100 == 0) {
            FormTuning.applyRuntimeTuning();
            FormTuning.applyAttributeMultipliers();
            FormTuning.applyAuraVisuals();
            SuperKamehameha.install();
        }
    }
}
