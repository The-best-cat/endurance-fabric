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
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity instanceof IPlayerEntity player) {
            player.SetTemporaryHealth(player.GetTemporaryHealth() - 1);
            return true;
        }
        return false;
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        if (entity instanceof IPlayerEntity player) {
            player.OnAppliedDeepWound();
        }
    }

    @Override
    public void onRemoved(AttributeContainer attributeContainer) {
        super.onRemoved(attributeContainer);

    }
}
