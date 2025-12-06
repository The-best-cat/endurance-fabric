package net.theblackcat.endurance.status_effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.theblackcat.endurance.Endurance;

public class EnduranceStatusEffects {
    public static final RegistryEntry<StatusEffect> ENDURANCE = register("endurance", new EnduranceEffect());
    public static final RegistryEntry<StatusEffect> DEEP_WOUND = register("deep_wound", new DeepWoundEffect());

    private static RegistryEntry<StatusEffect> register(String id, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Endurance.id(id), effect);
    }

    public static void initialise() {}
}
