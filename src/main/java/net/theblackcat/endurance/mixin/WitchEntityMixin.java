package net.theblackcat.endurance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.theblackcat.endurance.potions.EndurancePotions;
import net.theblackcat.endurance.status_effects.EnduranceStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitchEntity.class)
public class WitchEntityMixin {
    @Unique
    private WitchEntity self;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void construct(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo info) {
        self = (WitchEntity) (Object)this;
    }

    @ModifyVariable(method = "tickMovement", at = @At("STORE"))
    private RegistryEntry<Potion> pickEndurancePotion(RegistryEntry<Potion> potion) {
        if (self.getRandom().nextFloat() < 0.02f
                && self.getHealth() < self.getMaxHealth() / 2
                && !self.hasStatusEffect(EnduranceStatusEffects.ENDURANCE)
                && !self.hasStatusEffect(EnduranceStatusEffects.DEEP_WOUND)
        ) {
            potion = Registries.POTION.getEntry(EndurancePotions.ENDURANCE_POTION);
        }
        return potion;
    }

    @ModifyVariable(method = "shootAt", at = @At("STORE"))
    private RegistryEntry<Potion> throwBleedingPotion(RegistryEntry<Potion> potion, @Local(ordinal = 3) double g, @Local(argsOnly = true) LivingEntity target) {
        var difficulty = self.getEntityWorld().getDifficulty();
        float chance = difficulty == Difficulty.EASY ? 0.05f : difficulty == Difficulty.NORMAL ? 0.1f : difficulty == Difficulty.HARD ? 0.2f : 0f;
        if (self.getRandom().nextFloat() < chance && g <= 8 && !target.hasStatusEffect(EnduranceStatusEffects.DEEP_WOUND)) {
            potion = Registries.POTION.getEntry(EndurancePotions.BLEEDING_POTION);
        }
        return potion;
    }

    @ModifyVariable(method = "shootAt", at = @At(value = "STORE", ordinal = 2))
    private RegistryEntry<Potion> throwEndurancePotion(RegistryEntry<Potion> potion, @Local(argsOnly = true) LivingEntity target) {
        if (target.getHealth() <= 8) {
            potion = Registries.POTION.getEntry(EndurancePotions.ENDURANCE_POTION);
        }
        return potion;
    }
}
