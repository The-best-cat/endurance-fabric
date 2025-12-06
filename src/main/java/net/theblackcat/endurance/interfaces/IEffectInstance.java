package net.theblackcat.endurance.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface IEffectInstance {
    void setEntity(LivingEntity entity);
    void setSourceStack(ItemStack stack, boolean assignId);
    ItemStack getSourceStack();
    void setFromEndurance();
    boolean isFromEndurance();
}
