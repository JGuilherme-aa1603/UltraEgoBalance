package br.com.guiol.ultrabalancetweaks.client;

import br.com.guiol.ultrabalancetweaks.UltraBalanceTweaks;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = UltraBalanceTweaks.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DestructionKeybinds {
    public static final String CATEGORY = "key.categories.ultrabalancetweaks";
    public static final KeyMapping HAKAI = new KeyMapping("key.ultrabalancetweaks.hakai",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
    public static final KeyMapping SPHERE = new KeyMapping("key.ultrabalancetweaks.sphere",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
    public static final KeyMapping INSTINCT_TECHNIQUE = new KeyMapping(
            "key.ultrabalancetweaks.instinct_technique",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);

    private DestructionKeybinds() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(HAKAI);
        event.register(SPHERE);
        event.register(INSTINCT_TECHNIQUE);
    }

    public static Component hakaiKey() {
        return HAKAI.getTranslatedKeyMessage();
    }

    public static Component sphereKey() {
        return SPHERE.getTranslatedKeyMessage();
    }

    public static Component instinctTechniqueKey() {
        return INSTINCT_TECHNIQUE.getTranslatedKeyMessage();
    }
}
