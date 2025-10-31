package net.theblackcat.endurance;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.theblackcat.endurance.damage_types.ModDamageTypes;
import net.theblackcat.endurance.data_components.ModDataComponents;
import net.theblackcat.endurance.enchantments.effects.ModEnchantmentEffects;
import net.theblackcat.endurance.interfaces.IPlayerEntity;
import net.theblackcat.endurance.potions.ModPotions;
import net.theblackcat.endurance.status_effects.ModStatusEffects;
import net.theblackcat.endurance.tracked_data.ModTrackedDataHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endurance implements ModInitializer {
	public static final String MOD_ID = "endurance";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModStatusEffects.Initialise();
        ModDamageTypes.Initialise();
        ModEnchantmentEffects.Initialise();
        ModDataComponents.Initialise();
        ModPotions.Initialise();

        ModTrackedDataHandlers.Register();

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof PlayerEntity player)) return true;

            var endurance = player.getStatusEffect(ModStatusEffects.ENDURANCE);
            if (endurance == null || player.hasStatusEffect(ModStatusEffects.DEEP_WOUND)) return true;

            if (endurance.isInfinite()) {
                var chest = player.getEquippedStack(EquipmentSlot.CHEST);
                if (!chest.isEmpty()) {
                    int level = ModEnchantmentEffects.GetLevel(player.getWorld(), ModEnchantmentEffects.UNDYING, chest);
                    if (level > 0) {
                        int cooldown = 2400 - (level - 1) * 600;
                        chest.set(ModDataComponents.UNDYING_COOLDOWN_TYPE, cooldown);
                    }
                }
            }

            player.setHealth(1f);
            player.removeStatusEffect(ModStatusEffects.ENDURANCE);
            player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.DEEP_WOUND, -1, 0, false, true, true));

            ((IPlayerEntity)player).OnEndured();
            return false;
        });

        FabricBrewingRecipeRegistryBuilder.BUILD.register(ModPotions::BuildRecipe);
	}

    public static Identifier Id(String id) {
        return Identifier.of(MOD_ID, id);
    }
}