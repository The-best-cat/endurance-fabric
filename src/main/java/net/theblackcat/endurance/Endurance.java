package net.theblackcat.endurance;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.theblackcat.endurance.damage_types.EnduranceDamageTypes;
import net.theblackcat.endurance.data_components.EnduranceDataComponents;
import net.theblackcat.endurance.enchantments.effects.EnduranceEnchantmentEffects;
import net.theblackcat.endurance.interfaces.IPlayerEntity;
import net.theblackcat.endurance.potions.EndurancePotions;
import net.theblackcat.endurance.status_effects.EnduranceStatusEffects;
import net.theblackcat.endurance.tracked_data.ModTrackedDataHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endurance implements ModInitializer {
	public static final String MOD_ID = "endurance";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        EnduranceStatusEffects.Initialise();
        EnduranceDamageTypes.Initialise();
        EnduranceEnchantmentEffects.Initialise();
        EnduranceDataComponents.Initialise();
        EndurancePotions.Initialise();

        ModTrackedDataHandlers.Register();

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof PlayerEntity player)) return true;

            var endurance = player.getStatusEffect(EnduranceStatusEffects.ENDURANCE);
            if (endurance == null || player.hasStatusEffect(EnduranceStatusEffects.DEEP_WOUND)) return true;

            if (endurance.isInfinite()) {
                var chest = player.getEquippedStack(EquipmentSlot.CHEST);
                if (!chest.isEmpty()) {
                    int level = EnduranceEnchantmentEffects.GetLevel(player.getWorld(), EnduranceEnchantmentEffects.UNDYING, chest);
                    if (level > 0) {
                        int cooldown = 2400 - (level - 1) * 600;
                        chest.set(EnduranceDataComponents.UNDYING_COOLDOWN_TYPE, cooldown);
                    }
                }
            }

            player.setHealth(1f);
            player.removeStatusEffect(EnduranceStatusEffects.ENDURANCE);
            player.addStatusEffect(new StatusEffectInstance(EnduranceStatusEffects.DEEP_WOUND, -1, endurance.getAmplifier(), false, true, true));

            ((IPlayerEntity)player).OnEndured();
            return false;
        });

        FabricBrewingRecipeRegistryBuilder.BUILD.register(EndurancePotions::BuildRecipe);
	}

    public static Identifier Id(String id) {
        return Identifier.of(MOD_ID, id);
    }
}