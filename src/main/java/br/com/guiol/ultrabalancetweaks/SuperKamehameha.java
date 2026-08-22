package br.com.guiol.ultrabalancetweaks;

import com.dragonminez.common.config.ConfigManager;
import com.dragonminez.common.config.SkillsConfig;
import com.dragonminez.common.stats.techniques.KiAttackData;
import com.dragonminez.common.stats.techniques.PredefinedTechniques;

import java.util.ArrayList;
import java.util.List;

public final class SuperKamehameha {
    public static final String ID = "super_kamehameha";

    private SuperKamehameha() {
    }

    public static boolean install() {
        KiAttackData kamehameha = PredefinedTechniques.REGISTRY.get("kamehameha");
        SkillsConfig skills = ConfigManager.getSkillsConfig();
        if (kamehameha == null || skills == null) {
            return false;
        }

        KiAttackData technique = new KiAttackData();
        technique.load(kamehameha.save());
        technique.setId(ID);
        technique.setName("technique.ultrabalancetweaks.super_kamehameha");
        technique.setAuthor("Goku");
        technique.setDamageMultiplier(2.5f);
        technique.setSize(3.0f);
        technique.setSpeed(1.05f);
        technique.setCastTime(120);
        technique.setCooldown(40);
        technique.calculateDerivedValues();
        PredefinedTechniques.REGISTRY.put(ID, technique);

        if (!skills.getKiSkills().contains(ID)) {
            skills.getKiSkills().add(ID);
        }
        skills.getSkills().putIfAbsent(ID,
                new SkillsConfig.SkillCosts(new ArrayList<>(List.of(6000)), new ArrayList<>()));
        List<String> goku = skills.getSkillOfferings().computeIfAbsent("goku", ignored -> new ArrayList<>());
        if (!goku.contains(ID)) {
            int kameIndex = goku.indexOf("kamehameha");
            goku.add(kameIndex < 0 ? goku.size() : kameIndex + 1, ID);
        }
        return true;
    }
}
