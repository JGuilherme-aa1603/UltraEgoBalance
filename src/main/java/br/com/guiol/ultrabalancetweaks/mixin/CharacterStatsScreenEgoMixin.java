package br.com.guiol.ultrabalancetweaks.mixin;

import br.com.guiol.ultrabalancetweaks.client.EgoMenuProjection;
import com.dragonminez.client.gui.character.CharacterStatsScreen;
import com.dragonminez.common.stats.StatsData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes the DragonMineZ V menu display the synchronized cumulative Power of
 * Ultra Ego. This is deliberately a screen mixin: server damage remains the
 * single source of truth and cannot be applied twice by this projection.
 */
@Mixin(value = CharacterStatsScreen.class, remap = false)
public abstract class CharacterStatsScreenEgoMixin {
    @Redirect(
            method = {"renderStatisticsInfoList", "renderStatisticsInfoHexagon"},
            at = @At(value = "INVOKE",
                    target = "Lcom/dragonminez/common/stats/StatsData;getKiDamage()D"),
            remap = false)
    private double ultrabalance$showEgoKiDamage(StatsData data) {
        return EgoMenuProjection.kiDamage(data, data.getKiDamage());
    }

    @Redirect(
            method = {"renderStatisticsInfoList", "renderStatisticsInfoHexagon"},
            at = @At(value = "INVOKE",
                    target = "Lcom/dragonminez/common/stats/StatsData;getMaxKiDamage()D"),
            remap = false)
    private double ultrabalance$showMaxEgoKiDamage(StatsData data) {
        return EgoMenuProjection.kiDamage(data, data.getMaxKiDamage());
    }

    @Redirect(
            method = "renderStatsInfo",
            at = @At(value = "INVOKE",
                    target = "Lcom/dragonminez/common/stats/StatsData;getTotalMultiplier(Ljava/lang/String;)D"),
            remap = false)
    private double ultrabalance$showEgoPowerMultiplier(StatsData data, String statName) {
        double nativeValue = data.getTotalMultiplier(statName);
        return EgoMenuProjection.totalMultiplier(data, statName, nativeValue);
    }
}
