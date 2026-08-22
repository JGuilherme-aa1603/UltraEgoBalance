package br.com.guiol.ultrabalancetweaks.mixin;

import com.dragonminez.common.stats.StatsData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.unofficial.unofficialdmzaddon.client.AddonAuraPolicy", remap = false)
public abstract class AddonAuraPolicyMixin {
    @Inject(method = "hasConstantAddonAura", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ultrabalance$respectNativeAuraToggle(StatsData data,
                                                             CallbackInfoReturnable<Boolean> cir) {
        if (isControlledForm(data)) {
            cir.setReturnValue(false);
        }
    }

    private static boolean isControlledForm(StatsData data) {
        if (data == null || data.getCharacter() == null) {
            return false;
        }
        String group = normalize(data.getCharacter().getActiveFormGroup());
        String form = normalize(data.getCharacter().getActiveForm());
        return ("godforms".equals(group) && ("super_saiyan_god".equals(form)
                || "super_saiyan_blue".equals(form) || "super_saiyan_blue_evolved".equals(form)))
                || ("ultrainstinct".equals(group) && "true".equals(form));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(java.util.Locale.ROOT);
    }
}
