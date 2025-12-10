package net.theblackcat.endurance.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.theblackcat.duskywisplibrary.helpers.DuskyWispItemHelper;
import net.theblackcat.endurance.helpers.EnduranceHelper;
import net.theblackcat.endurance.interfaces.IEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements IEffectInstance {
    @Unique
    private LivingEntity entity;
    @Unique
    private ItemStack fromStack;
    @Unique
    private boolean fromEndurance;

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copy(StatusEffectInstance that, CallbackInfo info) {
        if (that instanceof IEffectInstance instance) {
            this.fromStack = instance.getSourceStack();
            this.fromEndurance = instance.isFromEndurance();
        }
    }

    @Override
    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void setSourceStack(ItemStack stack, boolean assignId) {
        if (stack.isEmpty()) return;

        if (assignId) {
            DuskyWispItemHelper.assignId(stack, "endurance_apply_stack");
        }
        fromStack = stack.copy();
    }

    @Override
    public ItemStack getSourceStack() {
        if (entity instanceof PlayerEntity player && fromStack != null) {
            return DuskyWispItemHelper.findItemById(player, fromStack, "endurance_apply_stack");
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isFromUndying() {
        return fromStack != null;
    }

    @Override
    public void setFromEndurance() {
        fromEndurance = true;
    }

    @Override
    public boolean isFromEndurance() {
        return EnduranceHelper.isDeepWound((StatusEffectInstance)(Object)this) && fromEndurance;
    }
}
