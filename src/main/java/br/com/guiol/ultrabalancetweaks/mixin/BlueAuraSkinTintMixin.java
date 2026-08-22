package br.com.guiol.ultrabalancetweaks.mixin;

import com.dragonminez.client.render.layer.AuraTintTracker;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

/**
 * DragonMineZ fades an aura-colour tint over every body layer while Ki is being
 * charged. On Blue's saturated cyan aura this washes the skin out even when the
 * form's explicit tintIntensity is zero. Keep the native aura itself and remove
 * only that body/hair/race-part tint for Blue and Blue Evolved.
 */
@Mixin(value = AuraTintTracker.class, remap = false)
public abstract class BlueAuraSkinTintMixin {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ultrabalance$keepBlueSkinColor(int entityId, long gameTime,
                                                       boolean shouldFadeIn,
                                                       CallbackInfoReturnable<Float> cir) {
        if (isBlueForm(entityId)) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ultrabalance$keepCachedBlueSkinColor(int entityId,
                                                             CallbackInfoReturnable<Float> cir) {
        if (isBlueForm(entityId)) {
            cir.setReturnValue(0.0f);
        }
    }

    private static boolean isBlueForm(int entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return false;
        }
        Entity entity = minecraft.level.getEntity(entityId);
        if (!(entity instanceof AbstractClientPlayer player)) {
            return false;
        }
        return StatsProvider.get(StatsCapability.INSTANCE, player)
                .map(BlueAuraSkinTintMixin::hasBlueForm)
                .orElse(false);
    }

    private static boolean hasBlueForm(StatsData data) {
        if (data == null || data.getCharacter() == null) {
            return false;
        }
        String group = normalize(data.getCharacter().getActiveFormGroup());
        String form = normalize(data.getCharacter().getActiveForm());
        return "godforms".equals(group) && ("super_saiyan_blue".equals(form)
                || "super_saiyan_blue_evolved".equals(form));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
