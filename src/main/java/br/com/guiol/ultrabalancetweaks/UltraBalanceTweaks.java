package br.com.guiol.ultrabalancetweaks;

import com.mojang.logging.LogUtils;
import br.com.guiol.ultrabalancetweaks.network.BalanceNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(UltraBalanceTweaks.MOD_ID)
public final class UltraBalanceTweaks {
    public static final String MOD_ID = "ultrabalancetweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public UltraBalanceTweaks(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, BalanceConfig.COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, BalanceConfig.CLIENT_SPEC);
        BalanceNetwork.register();
        MinecraftForge.EVENT_BUS.register(new BalanceEvents());
        LOGGER.info("Ultra Balance Tweaks loaded: cumulative Ultra Ego, Destruction techniques and Ki-scaled Ultra Instinct enabled");
    }
}
