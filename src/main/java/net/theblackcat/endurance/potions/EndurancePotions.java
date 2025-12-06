package net.theblackcat.endurance.potions;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.theblackcat.endurance.Endurance;
import net.theblackcat.endurance.status_effects.EnduranceStatusEffects;

public class EndurancePotions {
    public static final Potion ENDURANCE_POTION = register("endurance", new StatusEffectInstance(EnduranceStatusEffects.ENDURANCE, 3600, 0));
    public static final Potion ENHANCED_ENDURANCE_POTION = register("enhanced_endurance", new StatusEffectInstance(EnduranceStatusEffects.ENDURANCE, 1800, 1));
    public static final Potion LONG_ENDURANCE_POTION = register("long_endurance", new StatusEffectInstance(EnduranceStatusEffects.ENDURANCE, 9600, 0));

    public static final Potion BLEEDING_POTION = register("bleeding", new StatusEffectInstance(EnduranceStatusEffects.DEEP_WOUND, 400, 0));
    public static final Potion ENHANCED_BLEEDING_POTION = register("enhanced_bleeding", new StatusEffectInstance(EnduranceStatusEffects.DEEP_WOUND, 200, 1));
    public static final Potion LONG_BLEEDING_POTION = register("long_bleeding", new StatusEffectInstance(EnduranceStatusEffects.DEEP_WOUND, 800, 0));

    private static Potion register(String id, StatusEffectInstance instance) {
        return Registry.register(Registries.POTION, Endurance.id(id), new Potion(id, instance));
    }

    public static void buildRecipe(FabricBrewingRecipeRegistryBuilder builder) {
        builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItem(Items.GOLD_INGOT), Registries.POTION.getEntry(EndurancePotions.ENDURANCE_POTION));
        builder.registerPotionRecipe(Registries.POTION.getEntry(EndurancePotions.ENDURANCE_POTION), Ingredient.ofItem(Items.REDSTONE), Registries.POTION.getEntry(EndurancePotions.LONG_ENDURANCE_POTION));
        builder.registerPotionRecipe(Registries.POTION.getEntry(EndurancePotions.ENDURANCE_POTION), Ingredient.ofItem(Items.GLOWSTONE_DUST), Registries.POTION.getEntry(EndurancePotions.ENHANCED_ENDURANCE_POTION));

        builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItem(Items.REDSTONE), Registries.POTION.getEntry(EndurancePotions.BLEEDING_POTION));
        builder.registerPotionRecipe(Registries.POTION.getEntry(EndurancePotions.BLEEDING_POTION), Ingredient.ofItem(Items.REDSTONE), Registries.POTION.getEntry(EndurancePotions.LONG_BLEEDING_POTION));
        builder.registerPotionRecipe(Registries.POTION.getEntry(EndurancePotions.BLEEDING_POTION), Ingredient.ofItem(Items.GLOWSTONE_DUST), Registries.POTION.getEntry(EndurancePotions.ENHANCED_BLEEDING_POTION));
    }

    public static void initialise() {}
}
