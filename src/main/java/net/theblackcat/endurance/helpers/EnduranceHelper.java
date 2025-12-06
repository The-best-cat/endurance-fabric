package net.theblackcat.endurance.helpers;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.theblackcat.duskywisplibrary.helpers.DuskyWispEnchantmentHelper;
import net.theblackcat.endurance.enchantments.EnduranceEnchantmentEffects;
import net.theblackcat.endurance.status_effects.EnduranceStatusEffects;

public class EnduranceHelper {
    public static boolean isDeepWound(StatusEffectInstance instance) {
        var key = EnduranceStatusEffects.DEEP_WOUND.getKey();
        return key.isPresent() && instance.getEffectType().matchesKey(key.get());
    }

    public static int getBorrowedLifeLevel(World world, ItemStack stack) {
        return DuskyWispEnchantmentHelper.getLevel(world, stack, EnduranceEnchantmentEffects.BORROWED_LIFE);
    }

    public static int getUndyingLevel(World world, ItemStack stack) {
        return DuskyWispEnchantmentHelper.getLevel(world, stack, EnduranceEnchantmentEffects.UNDYING);
    }

    public static int getUndyingDamage(World world, ItemStack stack) {
        int level = getUndyingLevel(world, stack);
        if (level > 0) {
            return (int) (stack.getMaxDamage() * (0.1f - 0.01f * (level - 1)));
        }
        return 0;
    }
}
