package net.theblackcat.endurance.status_effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import net.theblackcat.endurance.interfaces.IPlayerEntity;

public class DeepWoundEffect extends StatusEffect {
    protected DeepWoundEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFF7D4343);
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        if (entity instanceof IPlayerEntity player) {
            player.onAppliedDeepWound();
        }
    }
}
