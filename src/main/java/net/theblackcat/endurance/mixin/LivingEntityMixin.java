package net.theblackcat.endurance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import net.theblackcat.duskywisplibrary.attributes.DuskyWispAttributes;
import net.theblackcat.duskywisplibrary.data_component_types.Cooldown;
import net.theblackcat.endurance.Endurance;
import net.theblackcat.endurance.data_components.EnduranceDataComponents;
import net.theblackcat.endurance.helpers.EnduranceHelper;
import net.theblackcat.endurance.interfaces.IEffectInstance;
import net.theblackcat.endurance.interfaces.IPlayerEntity;
import net.theblackcat.endurance.status_effects.EnduranceStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Unique
    private LivingEntity self;

    @Unique
    private static final Codec<HashMap<RegistryEntry<StatusEffect>, ItemStack>> ITEM_MAP_CODEC = Codec.unboundedMap(
            Registries.STATUS_EFFECT.getEntryCodec(),
            ItemStack.CODEC
    ).xmap(HashMap::new, Map::copyOf);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void construct(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        self = (LivingEntity) (Object)this;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo info) {
        if (self.getEntityWorld() instanceof ServerWorld world) {
            var chest = self.getEquippedStack(EquipmentSlot.CHEST);
            int lvl = EnduranceHelper.getUndyingLevel(world, chest);
            boolean hasUndying = !chest.isEmpty()
                    && chest.isIn(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                    && lvl > 0;
            boolean offCooldown = chest.getOrDefault(EnduranceDataComponents.UNDYING_COOLDOWN, Cooldown.EMPTY).isExpired(world);

            if (hasUndying && offCooldown) {
                if (!self.hasStatusEffect(EnduranceStatusEffects.ENDURANCE) && !self.hasStatusEffect(EnduranceStatusEffects.DEEP_WOUND)) {
                    var endurance = new StatusEffectInstance(EnduranceStatusEffects.ENDURANCE, -1, lvl - 1, false, true, true);
                    ((IEffectInstance)endurance).setSourceStack(chest, true);
                    self.addStatusEffect(endurance);
                }
            } else if (self.hasStatusEffect(EnduranceStatusEffects.ENDURANCE)) {
                var endurance = self.getStatusEffect(EnduranceStatusEffects.ENDURANCE);
                if (endurance instanceof IEffectInstance instance && EnduranceHelper.getUndyingLevel(world, instance.getSourceStack()) > 0) {
                    self.removeStatusEffect(EnduranceStatusEffects.ENDURANCE);
                }
            }
        }
    }
    
    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("RETURN"))
    private void applyVulnerability(StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> info) {
        if (EnduranceHelper.isDeepWound(effect) && !(self instanceof PlayerEntity)) {
            var instance = self.getAttributeInstance(DuskyWispAttributes.VULNERABILITY);
            if (instance != null) {
                var amp = 0.15f * (effect.getAmplifier() + 1);
                instance.overwritePersistentModifier(new EntityAttributeModifier(
                        Endurance.id("effect.deep_wound.non_player"),
                        amp,
                        EntityAttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        if (info.getReturnValue() && effect instanceof IEffectInstance instance1) {
            instance1.setEntity(self);
        }
    }

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void recoverDeepWound(float amount, CallbackInfo info) {
        if (self instanceof IPlayerEntity player && player.hasDeepWound()) {
            player.addMP(amount * player.getHealRate());
            info.cancel();
        }
    }

    @Inject(method = "onStatusEffectsRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/attribute/AttributeContainer;)V", shift = At.Shift.AFTER))
    private void removeDeepWound(Collection<StatusEffectInstance> effects, CallbackInfo info, @Local StatusEffectInstance instance) {
        if (!EnduranceHelper.isDeepWound(instance)) {
            return;
        }

        if (self instanceof IPlayerEntity player) {
            player.onRemovedDeepWound();
        } else {
            var vulnerability = self.getAttributeInstance(DuskyWispAttributes.VULNERABILITY);
            if (vulnerability != null) {
                vulnerability.removeModifier(Endurance.id("effect.deep_wound.non_player"));
            }
        }
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void save(WriteView view, CallbackInfo info) {
        HashMap<RegistryEntry<StatusEffect>, ItemStack> map = HashMap.newHashMap(0);
        List<RegistryEntry<StatusEffect>> list = new ArrayList<>();

        for (var effect : self.getStatusEffects()) {
            if (effect instanceof IEffectInstance instance) {
                var stack = instance.getSourceStack();
                if (!stack.isEmpty()) {
                    map.put(effect.getEffectType(), stack);
                }

                if (instance.isFromEndurance()) {
                    list.add(effect.getEffectType());
                }
            }
        }

        view.put("endurance:effect_with_source_stack", ITEM_MAP_CODEC, map);
        view.put("endurance:effect_from_endurance", Registries.STATUS_EFFECT.getEntryCodec().listOf(), list);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void read(ReadView view, CallbackInfo info) {
        var map = view.read("endurance:effect_with_source_stack", ITEM_MAP_CODEC).orElse(HashMap.newHashMap(0));
        for (var entry : map.entrySet()) {
            if (self.getStatusEffect(entry.getKey()) instanceof IEffectInstance instance) {
                instance.setEntity(self);
                instance.setSourceStack(entry.getValue(), false);
            }
        }

        var list = view.read("endurance:effect_from_endurance", Registries.STATUS_EFFECT.getEntryCodec().listOf()).orElse(new ArrayList<>());
        for (var effect : list) {
            if (self.getStatusEffect(effect) instanceof IEffectInstance instance) {
                instance.setFromEndurance();
            }
        }
    }
}
