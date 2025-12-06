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

import java.util.concurrent.CompletableFuture;

public class EnchantmentGenerator extends FabricDynamicRegistryProvider {
    public EnchantmentGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup, Entries entries) {
        register(entries, EnduranceEnchantmentEffects.UNDYING, Enchantment.builder(
                Enchantment.definition(
                        wrapperLookup.getOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                        7,
                        3,
                        Enchantment.leveledCost(3, 4),
                        Enchantment.leveledCost(6, 6),
                        4,
                        AttributeModifierSlot.CHEST
                )
        ));

        register(entries, EnduranceEnchantmentEffects.BORROWED_LIFE, Enchantment.builder(
                Enchantment.definition(
                        wrapperLookup.getOrThrow(RegistryKeys.ITEM).getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                        3,
                        3,
                        Enchantment.leveledCost(5, 4),
                        Enchantment.leveledCost(10, 7),
                        6,
                        AttributeModifierSlot.MAINHAND
                )
        ));
    }

    @Override
    public String getName() {
        return "EnduranceEnchantmentGenerator";
    }

    private void register(Entries entries, RegistryKey<Enchantment> key, Enchantment.Builder builder, ResourceCondition... conditions) {
        entries.add(key, builder.build(key.getValue()), conditions);
    }
}
