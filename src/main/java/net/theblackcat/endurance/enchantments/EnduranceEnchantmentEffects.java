package net.theblackcat.endurance.enchantments;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.theblackcat.endurance.Endurance;
import net.theblackcat.endurance.enchantments.effects.BorrowedLifeEnchantmentEffect;
import net.theblackcat.endurance.enchantments.effects.UndyingEnchantmentEffect;

public class EnduranceEnchantmentEffects {
    public static final RegistryKey<Enchantment> UNDYING = ofKey("undying");
    public static MapCodec<UndyingEnchantmentEffect> UNDYING_EFFECT = register("effect.undying", UndyingEnchantmentEffect.CODEC);

    public static final RegistryKey<Enchantment> BORROWED_LIFE = ofKey("borrowed_life");
    public static MapCodec<BorrowedLifeEnchantmentEffect> BORROWED_LIFE_EFFECT = register("effect.borrowed_life", BorrowedLifeEnchantmentEffect.CODEC);

    private static RegistryKey<Enchantment> ofKey(String id) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Endurance.id(id));
    }

    private static <T extends EnchantmentEntityEffect> MapCodec<T> register(String id, MapCodec<T> codec) {
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Endurance.id(id), codec);
    }

    public static void initialise() {}
}