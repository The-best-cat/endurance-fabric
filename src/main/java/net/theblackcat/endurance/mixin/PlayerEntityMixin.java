package net.theblackcat.endurance.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.theblackcat.endurance.Endurance;
import net.theblackcat.endurance.damage_types.EnduranceDamageTypes;
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
    private float tickToBleed = 0f;
    @Unique
    private int tickToMend = 0;
    @Unique
    private int tickToLoseMend = 0;
    @Unique
    private int damagedWhileDeepWoundTime = 0;
    @Unique
    private int appliedDeepWoundTime = 0;
    @Unique
    private int removedDeepWoundTime = 0;
    @Unique
    private Vec3d prevPos = Vec3d.ZERO;
    @Unique
    private PlayerEntity self;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void construct(World world, GameProfile profile, CallbackInfo info) {
        self = (PlayerEntity) (Object)this;
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initCustomData(DataTracker.Builder builder, CallbackInfo info) {
        builder.add(TEMPORARY_HEALTH, 0f);
        builder.add(MENDING_PROGRESS, 0f);
        builder.add(PAUSE_DEDUCTION_TICK, 0);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo info) {
        if (self.getEntityWorld() instanceof ServerWorld world) {
            if (hasDeepWound()) {
                if (getData(PAUSE_DEDUCTION_TICK) <= 0) {
                    boolean bl = self.isSprinting() || self.getActiveItem().getUseAction() == UseAction.EAT || self.getActiveItem().getUseAction() == UseAction.DRINK;

                    float deduct = bl ? 0.4f : 1f;
                    deduct = damagedWhileDeepWoundTime > 0 ? deduct * 1.5f : deduct;
                    tickToBleed -= deduct;

                    if (tickToBleed <= 0) {
                        setBP(getBP() - 1);
                        tickToBleed = getTimeRequiredToBleed();
                    }
                } else {
                    incrementInt(PAUSE_DEDUCTION_TICK, -1);
                }

                if (getBP() < 1) {
                    setBP(0);
                    self.damage(world, EnduranceDamageTypes.deepWound(world, self, self), Float.MAX_VALUE);
                }

                if (canRecoverDeepWound()) {
                    tickToMend += 1;

                    if (tickToMend >= getTimeRequiredToMend()) {
                        addMP(1);
                        tickToMend = 0;
                    }
                } else {
                    tickToMend = 0;
                }

                if (tickToLoseMend > 0) {
                    tickToLoseMend--;
                    if (tickToLoseMend <= 0) {
                        addMP(-1);
                        tickToLoseMend =  10;
                    }
                }

                prevPos = self.getEntityPos();
            }
        } else {
            if (hasDeepWound()) {
                removedDeepWoundTime = 0;
                appliedDeepWoundTime++;
            } else if (removedDeepWoundTime < 30) {
                removedDeepWoundTime++;
            }
        }
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void pauseOnAttacked(Entity target, CallbackInfo info) {
        if (!(target instanceof PassiveEntity)) {
            pauseBleeding();
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void pauseOnDamaged(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (source.getAttacker() != null && info.getReturnValue()) {
            pauseBleeding();
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F", shift = At.Shift.AFTER), cancellable = true)
    private void damageDeepWound(ServerWorld world, DamageSource source, float amount, CallbackInfo info) {
        var deepWound = self.getStatusEffect(EnduranceStatusEffects.DEEP_WOUND);
        if (deepWound != null && !source.isOf(EnduranceDamageTypes.DEEP_WOUND)) {
            incrementFloat(TEMPORARY_HEALTH, -amount);

            if (isDeepWoundFromEndurance(deepWound)) {
                if (damagedWhileDeepWoundTime == 1) {
                    incrementFloat(TEMPORARY_HEALTH, -(getBP() * 0.25f));
                } else if (damagedWhileDeepWoundTime == 2) {
                    setBP(0);
                }
                damagedWhileDeepWoundTime++;
            }

            info.cancel();
        }
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void removeShield(ServerWorld world, DamageSource source, float amount, CallbackInfo info) {
        if (self.getAbsorptionAmount() <= 0) {
            self.getAttributeInstance(EntityAttributes.MAX_ABSORPTION).removeModifier(Endurance.id("effect.deep_wound.removed"));
        }
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void save(WriteView view, CallbackInfo info) {
        view.putFloat("temp_hp", getBP());
        view.putFloat("healed_temp_hp", getMP());
        view.putInt("pause_deduction", getData(PAUSE_DEDUCTION_TICK));
        view.putFloat("tick_to_deduction", tickToBleed);
        view.putInt("tick_to_recover", tickToMend);
        view.putInt("tick_to_lose_mend", tickToLoseMend);
        view.putInt("damaged_while_deep_wound", damagedWhileDeepWoundTime);
        view.put("prev_pos", Vec3d.CODEC, prevPos);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void read(ReadView view, CallbackInfo info) {
        setBP(view.getFloat("temp_hp", 0f));
        setData(MENDING_PROGRESS, view.getFloat("healed_temp_hp", 0f));
        setData(PAUSE_DEDUCTION_TICK, view.getInt("pause_deduction", 0));
        tickToBleed = view.getFloat("tick_to_deduction", 0f);
        tickToMend = view.getInt("tick_to_recover", 0);
        tickToLoseMend =  view.getInt("tick_to_lose_mend", 0);
        damagedWhileDeepWoundTime = view.getInt("damaged_while_deep_wound", 0);
        prevPos = view.read("prev_pos", Vec3d.CODEC).orElse(Vec3d.ZERO);
    }

    @Override
    public void onAppliedDeepWound() {
        List<StatusEffectInstance> effects = List.copyOf(self.getStatusEffects());
        for (var effect : effects) {
            if (!EnduranceHelper.isDeepWound(effect)) {
                self.removeStatusEffect(effect.getEffectType());
            }
        }
        self.getAttributeInstance(EntityAttributes.MAX_ABSORPTION).clearModifiers();
        self.setAbsorptionAmount(0);

        setBP(20);
        setData(MENDING_PROGRESS, 0f);
        tickToBleed = getTimeRequiredToBleed();
        setData(PAUSE_DEDUCTION_TICK, 0);
        tickToMend = 0;
        tickToLoseMend = 0;
        damagedWhileDeepWoundTime = 0;
    }

    @Override
    public void onRemovedDeepWound() {
        if (getBP() > 0 && self.getHealth() > getBP()) {
            self.setHealth(getBP());
        }
    }

    @Override
    public float getBP() {
        return getData(TEMPORARY_HEALTH);
    }

    @Override
    public void setBP(float thp) {
        setData(TEMPORARY_HEALTH, Math.clamp(thp, 0f, 20f));
    }

    @Override
    public void addMP(float thp) {
        setData(MENDING_PROGRESS, Math.clamp(getData(MENDING_PROGRESS) + thp, 0, 20));
        if (getMP() >= 20) {
            removeDeepWound();
        } else if (thp > 0) {
            tickToLoseMend = 100;
            pauseBleeding();
        } else if (getMP() <= 0) {
            tickToLoseMend = 0;
        }
    }

    @Override
    public boolean lossPaused() {
        return hasDeepWound() && getData(PAUSE_DEDUCTION_TICK) > 0;
    }

    @Override
    public boolean hasDeepWound() {
        return self.getGameMode() != null && self.getGameMode().isSurvivalLike() && self.hasStatusEffect(EnduranceStatusEffects.DEEP_WOUND);
    }

    @Override
    public float getHealRate() {
        return self.isSneaking() ? 1.2f : 0.75f;
    }

    @Override
    public int getMP() {
        return Math.round(getData(MENDING_PROGRESS));
    }

    @Override
    public int getInjuredTime() {
        return appliedDeepWoundTime;
    }

    @Override
    public int getRemovedInjuriesTime() {
        return removedDeepWoundTime;
    }

    @Unique
    private boolean isDeepWoundFromEndurance(StatusEffectInstance instance) {
        return EnduranceHelper.isDeepWound(instance)
                && instance instanceof IEffectInstance instance1
                && instance1.isFromEndurance();
    }

    @Unique
    private int getTimeRequiredToBleed() {
        if (!hasDeepWound()) return -1;

        int lvl = self.getStatusEffect(EnduranceStatusEffects.DEEP_WOUND).getAmplifier();
        return 20 - 2 * lvl;
    }

    @Unique
    private int getTimeRequiredToMend() {
        if (!hasDeepWound()) return -1;

        int lvl = self.getStatusEffect(EnduranceStatusEffects.DEEP_WOUND).getAmplifier();
        return 5 - lvl;
    }

    @Unique
    private void pauseBleeding() {
        if (self.hasStatusEffect(EnduranceStatusEffects.DEEP_WOUND)) {
            setData(PAUSE_DEDUCTION_TICK, 40);
        }
    }

    @Unique
    private void removeDeepWound() {
        if (self.isDead()) return;

        self.removeStatusEffect(EnduranceStatusEffects.DEEP_WOUND);

        float shield = Math.max(3, getBP() * 0.3f);
        var id = Endurance.id("effect.deep_wound.removed");
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

        setBP(0);
        setData(MENDING_PROGRESS, 0f);

        self.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 3, false, true, true));
        self.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, false, true, true));
    }

    @Unique
    private <T> T getData(TrackedData<T> data) {
        return self.getDataTracker().get(data);
    }

    @Unique
    private <T> void setData(TrackedData<T> data, T value) {
        self.getDataTracker().set(data, value);
    }

    @Unique
    private void incrementFloat(TrackedData<Float> data, float amount) {
        setData(data, getData(data) + amount);
    }

    @Unique
    private void incrementInt(TrackedData<Integer> data, int amount) {
        setData(data, getData(data) + amount);
    }

    @Unique
    private boolean canRecoverDeepWound() {
        return self.isSneaking() && !isMoving();
    }

    @Unique
    private boolean isMoving() {
        return self.getEntityPos().distanceTo(prevPos) > 0;
    }
}