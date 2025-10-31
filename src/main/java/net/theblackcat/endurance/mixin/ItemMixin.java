package net.theblackcat.endurance.mixin;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.theblackcat.endurance.status_effects.ModStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void OnEaten(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        if (IsFood(stack) && user instanceof ServerPlayerEntity) {
            int duration = GetDuration(stack);
            var instance = user.getStatusEffect(ModStatusEffects.ENDURANCE);
            if (instance == null || instance.getDuration() < duration) {
                user.removeStatusEffect(ModStatusEffects.ENDURANCE);
                user.addStatusEffect(new StatusEffectInstance(ModStatusEffects.ENDURANCE, duration, 0, false, true, true));
            }
        }
    }

    @Unique
    private boolean IsFood(ItemStack stack) {
        return stack.get(DataComponentTypes.CONSUMABLE) != null;
    }

    @Unique
    private int GetDuration(ItemStack stack) {
        if (stack.isOf(Items.GOLDEN_APPLE)) {
            return 1200;
        }

        if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            return 2400;
        }

        var component = stack.get(DataComponentTypes.FOOD);
        if (component != null) {
            int hunger = component.nutrition();
            float saturation = component.saturation();
            float sec = Math.min((hunger + saturation * 1.5f) * 1.087f, 30);
            return (int) (sec * 20);
        }

        return 0;
    }
}
