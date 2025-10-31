package net.theblackcat.endurance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import net.theblackcat.endurance.Endurance;
import net.theblackcat.endurance.interfaces.IPlayerEntity;
import net.theblackcat.endurance.status_effects.ModStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void RecoverDeepWound(float amount, CallbackInfo info) {
        if (GetSelf() instanceof IPlayerEntity player && player.HasDeepWound()) {
            player.AddMendingProgress(amount * player.GetHealRate());
            info.cancel();
        }
    }

    @Inject(method = "onStatusEffectsRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/attribute/AttributeContainer;)V", shift = At.Shift.AFTER))
    private void RemoveDeepWound(Collection<StatusEffectInstance> effects, CallbackInfo info, @Local StatusEffectInstance instance) {
        if (GetSelf() instanceof IPlayerEntity player && instance.getEffectType().matchesId(Identifier.of(ModStatusEffects.DEEP_WOUND.getIdAsString()))) {
            player.OnRemovedDeepWound();
        }
    }

    @Inject(method = "tryUseDeathProtector", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setHealth(F)V"))
    private void Test(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
    }

    @Unique
    private LivingEntity GetSelf() {
        return (LivingEntity) (Object)this;
    }
}
