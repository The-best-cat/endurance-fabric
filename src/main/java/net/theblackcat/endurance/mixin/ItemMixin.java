package net.theblackcat.endurance.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.theblackcat.endurance.status_effects.EnduranceStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onEaten(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        if (isFood(stack) && user instanceof ServerPlayerEntity) {
            int duration = getDuration(stack);
            var instance = user.getStatusEffect(EnduranceStatusEffects.ENDURANCE);
            if (instance == null || instance.getDuration() < duration) {
                user.removeStatusEffect(EnduranceStatusEffects.ENDURANCE);
                user.addStatusEffect(new StatusEffectInstance(EnduranceStatusEffects.ENDURANCE, duration, getAmplifier(stack), false, true, true));
            }
        }
    }

    @Unique
    private boolean isFood(ItemStack stack) {
        return stack.get(DataComponentTypes.CONSUMABLE) != null;
    }

    @Unique
    private int getAmplifier(ItemStack stack) {
        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            return 2;
        }
        if (stack.isOf(Items.GOLDEN_APPLE)) {
            return 1;
        }
        return 0;
    }

    @Unique
    private int getDuration(ItemStack stack) {
        if (stack.isOf(Items.GOLDEN_APPLE)) {
            return 3000;
        }

        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            return 6000;
        }

        var component = stack.get(DataComponentTypes.FOOD);
        if (component != null) {
            int hunger = component.nutrition();
            float saturation = component.saturation();
            float sec = Math.min(hunger * 0.5f + saturation * 1.185f, 20f);
            return (int) (MathHelper.lerp(sec / 20f, 1f, 2f) * sec * 20);
        }

        return 0;
    }
}
