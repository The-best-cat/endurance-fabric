package net.theblackcat.endurance.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.theblackcat.endurance.Endurance;
import net.theblackcat.endurance.damage_types.ModDamageTypes;
import net.theblackcat.endurance.data_components.ModDataComponents;
import net.theblackcat.endurance.enchantments.effects.ModEnchantmentEffects;
import net.theblackcat.endurance.interfaces.IPlayerEntity;
import net.theblackcat.endurance.status_effects.ModStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements IPlayerEntity {
    @Unique
    private static final TrackedData<Float> TEMPORARY_HEALTH = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    @Unique
    private static final TrackedData<Float> MENDING_PROGRESS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    @Unique
    private static final TrackedData<Integer> PAUSE_DEDUCTION_TICK = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Unique
    private float tickToDuction = 0f;
    @Unique
    private int tickToMend = 0;
    @Unique
    private int tickToLoseMend = 0;
    @Unique
    private int damagedWhileDeepWoundTime = 0;
    @Unique
    private int killEnduranceCd = 0;
    @Unique
    private Vec3d prevPos = Vec3d.ZERO;

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void InitCustomData(DataTracker.Builder builder, CallbackInfo info) {
        builder.add(TEMPORARY_HEALTH, 0f);
        builder.add(MENDING_PROGRESS, 0f);
        builder.add(PAUSE_DEDUCTION_TICK, 0);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void Tick(CallbackInfo info) {
        var self = GetSelf();
        if (self.getWorld() instanceof ServerWorld world) {
            var chest = self.getEquippedStack(EquipmentSlot.CHEST);
            boolean hasUndying = !chest.isEmpty()
                    && chest.isIn(ItemTags.CHEST_ARMOR)
                    && ModEnchantmentEffects.GetLevel(world, ModEnchantmentEffects.UNDYING, chest) > 0;
            boolean offCooldown = chest.getOrDefault(ModDataComponents.UNDYING_COOLDOWN_TYPE, -1) == 0;

            if (hasUndying && offCooldown) {
                if (!self.hasStatusEffect(ModStatusEffects.ENDURANCE) && !self.hasStatusEffect(ModStatusEffects.DEEP_WOUND)) {
                    self.addStatusEffect(new StatusEffectInstance(ModStatusEffects.ENDURANCE, -1, 0, false, true, true));
                }
            } else if (self.hasStatusEffect(ModStatusEffects.ENDURANCE) && self.getStatusEffect(ModStatusEffects.ENDURANCE).isInfinite()) {
                self.removeStatusEffect(ModStatusEffects.ENDURANCE);
            }

            if (HasDeepWound()) {
                if (GetTrackedData(PAUSE_DEDUCTION_TICK) <= 0) {
                    boolean bl = self.isSprinting() || self.getActiveItem().getUseAction() == UseAction.EAT || self.getActiveItem().getUseAction() == UseAction.DRINK;

                    float deduct = bl ? 0.4f : 1f;
                    deduct = damagedWhileDeepWoundTime > 0 ? deduct * 1.5f : deduct;
                    tickToDuction -= deduct;

                    if (tickToDuction <= 0) {
                        self.getStatusEffect(ModStatusEffects.DEEP_WOUND).getEffectType().value().applyUpdateEffect(world, self, 0);
                        tickToDuction = 20f;
                    }
                } else {
                    IncrementTrackedInt(PAUSE_DEDUCTION_TICK, -1);
                }

                if (GetTemporaryHealth() < 1) {
                    SetTemporaryHealth(0);
                    self.damage(world, ModDamageTypes.GetSource(world, ModDamageTypes.DEEP_WOUND_DAMAGE), Float.MAX_VALUE);
                }

                if (CanRecoverDeepWound()) {
                    SetTrackedData(PAUSE_DEDUCTION_TICK, 1);
                    tickToMend += 1;

                    if (tickToMend >= 5) {
                        AddMendingProgress(1);
                        tickToMend = 0;
                    }
                } else {
                    tickToMend = 0;
                }

                if (tickToLoseMend > 0) {
                    tickToLoseMend--;
                    if (tickToLoseMend <= 0) {
                        AddMendingProgress(-1);
                        tickToLoseMend =  10;
                    }
                }

                prevPos = self.getPos();
            }

            if (killEnduranceCd > 0) {
                killEnduranceCd--;
            }
        }
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void PauseOnAttacked(Entity target, CallbackInfo info) {
        if (!(target instanceof PassiveEntity)) {
            PauseDeduction();
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void PauseOnDamaged(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (source.getSource() != null && info.getReturnValue()) {
            PauseDeduction();
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isInvulnerableTo(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)Z", shift = At.Shift.AFTER), cancellable = true)
    private void DamageDeepWound(ServerWorld world, DamageSource source, float amount, CallbackInfo info) {
        if (GetSelf().hasStatusEffect(ModStatusEffects.DEEP_WOUND) && !source.isOf(ModDamageTypes.DEEP_WOUND_DAMAGE)) {
            int time = damagedWhileDeepWoundTime;
            if (time == 0) {
                IncrementTrackedFloat(TEMPORARY_HEALTH, -amount);
            } else if (time == 1) {
                SetTemporaryHealth(GetTemporaryHealth() * 0.5f);
            } else {
                return;
            }

            damagedWhileDeepWoundTime++;
            info.cancel();
        }
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void RemoveShield(ServerWorld world, DamageSource source, float amount, CallbackInfo info) {
        if (GetSelf().getAbsorptionAmount() <= 0) {
            GetSelf().getAttributeInstance(EntityAttributes.MAX_ABSORPTION).removeModifier(Endurance.Id("effect.deep_wound.removed"));
        }
    }

    @Inject(method = "onKilledOther", at = @At("RETURN"))
    private void ApplyEnduranceOnKilledOther(ServerWorld world, LivingEntity other, CallbackInfoReturnable<Boolean> info) {
        var self = GetSelf();
        if (info.getReturnValue() && killEnduranceCd <= 0 && !self.hasStatusEffect(ModStatusEffects.ENDURANCE)) {
            float chance = 0.2f;
            chance += MathHelper.lerp(self.getHealth() / self.getMaxHealth(), 0.6f, 0f);
            if (world.random.nextFloat() < chance) {
                self.addStatusEffect(new StatusEffectInstance(ModStatusEffects.ENDURANCE, 100, 0, false, true, true));
            }
        }
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void SaveData(WriteView view, CallbackInfo info) {
        view.putFloat("temp_hp", GetTemporaryHealth());
        view.putFloat("healed_temp_hp", GetMendedProgress());
        view.putInt("pause_deduction", GetTrackedData(PAUSE_DEDUCTION_TICK));
        view.putFloat("tick_to_deduction", tickToDuction);
        view.putInt("tick_to_recover", tickToMend);
        view.putInt("tick_to_lose_mend", tickToLoseMend);
        view.putInt("damaged_while_deep_wound", damagedWhileDeepWoundTime);
        view.put("prev_pos", Vec3d.CODEC, prevPos);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void ReadData(ReadView view, CallbackInfo info) {
        SetTemporaryHealth(view.getFloat("temp_hp", 0f));
        SetTrackedData(MENDING_PROGRESS, view.getFloat("healed_temp_hp", 0f));
        SetTrackedData(PAUSE_DEDUCTION_TICK, view.getInt("pause_deduction", 0));
        tickToDuction = view.getFloat("tick_to_deduction", 0f);
        tickToMend = view.getInt("tick_to_recover", 0);
        tickToLoseMend =  view.getInt("tick_to_lose_mend", 0);
        damagedWhileDeepWoundTime = view.getInt("damaged_while_deep_wound", 0);
        prevPos = view.read("prev_pos", Vec3d.CODEC).orElse(Vec3d.ZERO);
    }

    @Override
    public void OnAppliedDeepWound() {
        var self = GetSelf();

        List<StatusEffectInstance> effects = List.copyOf(self.getStatusEffects());
        for (var effect : effects) {
            if (!effect.getEffectType().matchesKey(ModStatusEffects.DEEP_WOUND.getKey().get())) {
                self.removeStatusEffect(effect.getEffectType());
            }
        }
        self.getAttributeInstance(EntityAttributes.MAX_ABSORPTION).clearModifiers();
        self.setAbsorptionAmount(0);

        SetTemporaryHealth(20);
        SetTrackedData(MENDING_PROGRESS, 0f);
        tickToDuction = 20f;
        SetTrackedData(PAUSE_DEDUCTION_TICK, 0);
        tickToMend = 0;
        tickToLoseMend = 0;
        damagedWhileDeepWoundTime = 0;
    }

    @Override
    public void OnRemovedDeepWound() {
        if (GetTemporaryHealth() > 0 && GetSelf().getHealth() > GetTemporaryHealth()) {
            GetSelf().setHealth(GetTemporaryHealth());
        }
    }

    @Override
    public void OnEndured() {
        killEnduranceCd = 6000;
    }

    @Override
    public float GetTemporaryHealth() {
        return GetTrackedData(TEMPORARY_HEALTH);
    }

    @Override
    public void SetTemporaryHealth(float thp) {
        SetTrackedData(TEMPORARY_HEALTH, Math.clamp(thp, 0f, 20f));
    }

    @Override
    public void AddMendingProgress(float thp) {
        SetTrackedData(MENDING_PROGRESS, Math.clamp(GetTrackedData(MENDING_PROGRESS) + thp, 0, 20));
        if (GetMendedProgress() >= 20) {
            RemoveDeepWound();
        } else if (thp > 0) {
            tickToLoseMend =  80;
            PauseDeduction();
        } else if (GetMendedProgress() <= 0) {
            tickToLoseMend =  0;
        }
    }

    @Override
    public boolean LossPaused() {
        return HasDeepWound() && GetTrackedData(PAUSE_DEDUCTION_TICK) > 0;
    }

    @Override
    public boolean HasDeepWound() {
        return GetSelf().hasStatusEffect(ModStatusEffects.DEEP_WOUND);
    }

    @Override
    public float GetHealRate() {
        return GetSelf().isSneaking() ? 1.2f : 0.75f;
    }

    @Override
    public int GetMendedProgress() {
        return Math.round(GetTrackedData(MENDING_PROGRESS));
    }

    @Unique
    private void PauseDeduction() {
        if (GetSelf().hasStatusEffect(ModStatusEffects.DEEP_WOUND)) {
            SetTrackedData(PAUSE_DEDUCTION_TICK, 40);
        }
    }

    @Unique
    private void RemoveDeepWound() {
        var self = GetSelf();
        self.removeStatusEffect(ModStatusEffects.DEEP_WOUND);

        float shield = Math.max(3, GetTemporaryHealth() * 0.3f);
        var id = Endurance.Id("effect.deep_wound.removed");
        var instance = self.getAttributeInstance(EntityAttributes.MAX_ABSORPTION);
        if (!instance.hasModifier(id) || instance.getModifier(id).value() < shield) {
            instance.overwritePersistentModifier(
                    new EntityAttributeModifier(
                            id,
                            shield,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );
            self.setAbsorptionAmount(self.getAbsorptionAmount() + shield);
        }
        SetTemporaryHealth(0);
        SetTrackedData(MENDING_PROGRESS, 0f);

        self.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 3, false, true, true));
        self.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 0, false, true, true));
    }

    @Unique
    private <T> T GetTrackedData(TrackedData<T> data) {
        return GetSelf().getDataTracker().get(data);
    }

    @Unique
    private <T> void SetTrackedData(TrackedData<T> data, T value) {
        GetSelf().getDataTracker().set(data, value);
    }

    @Unique
    private void IncrementTrackedFloat(TrackedData<Float> data, float amount) {
        SetTrackedData(data, GetTrackedData(data) + amount);
    }

    @Unique
    private void IncrementTrackedInt(TrackedData<Integer> data, int amount) {
        SetTrackedData(data, GetTrackedData(data) + amount);
    }

    @Unique
    private boolean CanRecoverDeepWound() {
        var self = GetSelf();
        return self.isSneaking() && !IsMoving();
    }

    @Unique
    private boolean IsMoving() {
        return GetSelf().getPos().distanceTo(prevPos) > 0;
    }

    @Unique
    private PlayerEntity GetSelf() {
        return (PlayerEntity) (Object) this;
    }
}