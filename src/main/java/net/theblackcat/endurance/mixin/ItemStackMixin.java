package net.theblackcat.endurance.mixin;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.theblackcat.endurance.data_components.ModDataComponents;
import net.theblackcat.endurance.enchantments.effects.ModEnchantmentEffects;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void undyingCooldown(World world, Entity entity, EquipmentSlot slot, CallbackInfo info) {
        ItemStack self = getSelf();

        var cooldown = self.get(ModDataComponents.UNDYING_COOLDOWN_TYPE);
        if (cooldown == null) return;

        int level = EnchantmentHelper.getLevel(ModEnchantmentEffects.GetEntry(world, ModEnchantmentEffects.UNDYING), self);
        if (level <= 0) {
            self.set(ModDataComponents.UNDYING_COOLDOWN_TYPE, -1);
            return;
        }

        if (cooldown > 0) {
            self.set(ModDataComponents.UNDYING_COOLDOWN_TYPE, cooldown - 1);
        } else if (cooldown == -1) {
            self.set(ModDataComponents.UNDYING_COOLDOWN_TYPE, 0);
        }
    }

    @Unique
    private ItemStack getSelf() {
        return (ItemStack) (Object) this;
    }
}
