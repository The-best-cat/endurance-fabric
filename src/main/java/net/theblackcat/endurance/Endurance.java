package net.theblackcat.endurance;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.theblackcat.duskywisplibrary.data_component_types.Cooldown;
import net.theblackcat.duskywisplibrary.helpers.DuskyWispItemHelper;
import net.theblackcat.endurance.damage_types.EnduranceDamageTypes;
import net.theblackcat.endurance.data_components.EnduranceDataComponents;
import net.theblackcat.endurance.enchantments.EnduranceEnchantmentEffects;
import net.theblackcat.endurance.helpers.EnduranceHelper;
import net.theblackcat.endurance.interfaces.IEffectInstance;
import net.theblackcat.endurance.interfaces.IPlayerEntity;
import net.theblackcat.endurance.potions.EndurancePotions;
import net.theblackcat.endurance.status_effects.EnduranceStatusEffects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endurance implements ModInitializer {
	public static final String MOD_ID = "endurance";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        EnduranceStatusEffects.initialise();
        EnduranceDamageTypes.initialise();
        EnduranceEnchantmentEffects.initialise();
        EnduranceDataComponents.initialise();
        EndurancePotions.initialise();

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            var endurance = entity.getStatusEffect(EnduranceStatusEffects.ENDURANCE);
            if (endurance != null && !entity.hasStatusEffect(EnduranceStatusEffects.DEEP_WOUND)) {
                if (endurance instanceof IEffectInstance instance && instance.getSourceStack().isIn(ItemTags.CHEST_ARMOR_ENCHANTABLE)) {
                    var chest = entity.getEquippedStack(EquipmentSlot.CHEST);
                    if (!chest.isEmpty()) {
                        int level = EnduranceHelper.getUndyingLevel(entity.getEntityWorld(), chest);
                        if (level > 0) {
                            int cooldown = 3000 - (level - 1) * 600;
                            chest.set(EnduranceDataComponents.UNDYING_COOLDOWN, Cooldown.of(entity.getEntityWorld(), cooldown));
                            chest.damage(EnduranceHelper.getUndyingDamage(entity.getEntityWorld(), chest), (ServerWorld) entity.getEntityWorld(), null, item -> {});
                        }
                    }
                }

                entity.setHealth(1f);
                entity.removeStatusEffect(EnduranceStatusEffects.ENDURANCE);

                var deepWound = new StatusEffectInstance(EnduranceStatusEffects.DEEP_WOUND, -1, endurance.getAmplifier(), false, true, true);
                entity.addStatusEffect(deepWound);
                ((IEffectInstance)deepWound).setFromEndurance();
                return false;
            }

            if (entity instanceof PlayerEntity player && player.getEntityWorld() instanceof ServerWorld world) {
                int token = 0;
                int maxLvl = 0;
                float damage = 0;

                for (var stack : DuskyWispItemHelper.forEachItem(player)) {
                    if (stack.isEmpty()) continue;

                    int lvl = EnduranceHelper.getBorrowedLifeLevel(world, stack);
                    if (lvl > 0 && stack.getOrDefault(EnduranceDataComponents.BORROWED_LIFE_COOLDOWN, Cooldown.EMPTY).isExpired(world)) {
                        damage += 4f + (lvl - 1);
                        token += 3 + (lvl - 1);
                        stack.set(EnduranceDataComponents.BORROWED_LIFE_COOLDOWN, Cooldown.of(world, 20 * 60 * (5 - (lvl - 1))));

                        if (maxLvl < lvl) {
                            maxLvl = lvl;
                        }
                    }
                }

                if (token > 0) {
                    for (LivingEntity nearby : world.getEntitiesByClass(
                            LivingEntity.class,
                            player.getBoundingBox().expand(12f),
                            e -> e.isAlive()
                                    && !e.equals(player)
                                    && (!(e instanceof TameableEntity tameable) || !player.equals(tameable.getOwner()))
                                    && !e.isTeammate(player)
                                    && e.distanceTo(player) <= 12f
                                    && e instanceof MobEntity mob && player.equals(mob.getTarget())
                    )) {
                        nearby.damage(world, EnduranceDamageTypes.borrowedLife(world, player, player), damage);
                    }

                    player.setHealth(1f);
                    player.removeStatusEffect(EnduranceStatusEffects.DEEP_WOUND);
                    player.addStatusEffect(new StatusEffectInstance(EnduranceStatusEffects.DEEP_WOUND, -1, maxLvl - 1, false, true, true));
                    ((IPlayerEntity)player).addMP(Math.min(15f, token));
                    return false;
                }
            }

            return true;
        });

        FabricBrewingRecipeRegistryBuilder.BUILD.register(EndurancePotions::buildRecipe);
	}

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }
}