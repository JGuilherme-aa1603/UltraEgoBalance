package br.com.guiol.ultrabalancetweaks.mixin;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.unofficial.unofficialdmzaddon.dmz.UltraInstinctCombatHandler", remap = false)
public abstract class OriginalUltraInstinctHandlerMixin {
    @Inject(method = "onLivingAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private void ultrabalance$disableOriginalDodge(LivingAttackEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onLivingHurt", at = @At("HEAD"), cancellable = true, remap = false)
    private void ultrabalance$disableOriginalPrecision(LivingHurtEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
