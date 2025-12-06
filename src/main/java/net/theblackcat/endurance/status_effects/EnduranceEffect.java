package net.theblackcat.endurance.status_effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class EnduranceEffect extends StatusEffect {
    protected EnduranceEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFFFB30F);
    }
}
