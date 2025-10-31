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
import net.theblackcat.endurance.status_effects.ModStatusEffects;

public class ModPotions {
    public static final Potion ENDURANCE_POTION = Register("endurance", new StatusEffectInstance(ModStatusEffects.ENDURANCE, 3600, 0));
    public static final Potion LONG_ENDURANCE_POTION = Register("long_endurance", new StatusEffectInstance(ModStatusEffects.ENDURANCE, 9600, 0));
    public static final Potion BLEEDING_POTION = Register("bleeding", new StatusEffectInstance(ModStatusEffects.DEEP_WOUND, 400, 0));
    public static final Potion LONG_BLEEDING_POTION = Register("long_bleeding", new StatusEffectInstance(ModStatusEffects.DEEP_WOUND, 1000, 0));

    private static Potion Register(String id, StatusEffectInstance instance) {
        return Registry.register(Registries.POTION, Endurance.Id(id), new Potion(id, instance));
    }

    public static void BuildRecipe(FabricBrewingRecipeRegistryBuilder builder) {
        builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItem(Items.GOLD_INGOT), Registries.POTION.getEntry(ModPotions.ENDURANCE_POTION));
        builder.registerPotionRecipe(Registries.POTION.getEntry(ModPotions.ENDURANCE_POTION), Ingredient.ofItem(Items.GLOWSTONE_DUST), Registries.POTION.getEntry(ModPotions.LONG_ENDURANCE_POTION));

        builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItem(Items.REDSTONE), Registries.POTION.getEntry(ModPotions.BLEEDING_POTION));
        builder.registerPotionRecipe(Registries.POTION.getEntry(ModPotions.BLEEDING_POTION), Ingredient.ofItem(Items.GLOWSTONE_DUST), Registries.POTION.getEntry(ModPotions.LONG_BLEEDING_POTION));
    }

    public static void Initialise() {}
}
