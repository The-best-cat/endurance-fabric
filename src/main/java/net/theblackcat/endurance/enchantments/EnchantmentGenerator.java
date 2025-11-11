package net.theblackcat.endurance.enchantments;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.theblackcat.endurance.enchantments.effects.EnduranceEnchantmentEffects;

import java.util.concurrent.CompletableFuture;

public class EnchantmentGenerator extends FabricDynamicRegistryProvider {
    public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup, Entries entries) {
        Register(entries, EnduranceEnchantmentEffects.UNDYING, Enchantment.builder(
                Enchantment.definition(
                        wrapperLookup.getOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                        10,
                        3,
                        Enchantment.leveledCost(3, 4),
                        Enchantment.leveledCost(6, 8),
                        4,
                        AttributeModifierSlot.CHEST
                )
        ));
    }

    @Override
    public String getName() {
        return "EnduranceEnchantmentGenerator";
    }

    private void Register(Entries entries, RegistryKey<Enchantment> key, Enchantment.Builder builder, ResourceCondition... conditions) {
        entries.add(key, builder.build(key.getValue()), conditions);
    }
}
