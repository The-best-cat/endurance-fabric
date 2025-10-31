package net.theblackcat.endurance.enchantments.effects;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.theblackcat.endurance.Endurance;

public class ModEnchantmentEffects {
    public static final RegistryKey<Enchantment> UNDYING = OfKey("undying");
    public static MapCodec<UndyingEnchantmentEffect> UNDYING_EFFECT = Register("effect.undying", UndyingEnchantmentEffect.CODEC);

    private static RegistryKey<Enchantment> OfKey(String id) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Endurance.Id(id));
    }

    private static <T extends EnchantmentEntityEffect> MapCodec<T> Register(String id, MapCodec<T> codec) {
        return Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Endurance.Id(id), codec);
    }

    public static RegistryEntry<Enchantment> GetEntry(World world, RegistryKey<Enchantment> key) {
        return world.getRegistryManager().getEntryOrThrow(key);
    }

    public static int GetLevel(World world, RegistryKey<Enchantment> key, ItemStack stack) {
        return EnchantmentHelper.getLevel(GetEntry(world, key), stack);
    }

    public static void Initialise() {}
}
