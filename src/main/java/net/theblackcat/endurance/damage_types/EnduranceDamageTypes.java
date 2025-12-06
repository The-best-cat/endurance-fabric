package net.theblackcat.endurance.damage_types;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.theblackcat.endurance.Endurance;
import org.jetbrains.annotations.Nullable;

public class EnduranceDamageTypes {
    public static final RegistryKey<DamageType> DEEP_WOUND = register("deep_wound");
    public static final RegistryKey<DamageType> BORROWED_LIFE = register("borrowed_life");

    private static RegistryKey<DamageType> register(String id) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Endurance.id(id));
    }

    private static RegistryEntry<DamageType> getEntry(World world, RegistryKey<DamageType> type) {
        return world.getRegistryManager().getEntryOrThrow(type);
    }

    public static DamageSource deepWound(World world, @Nullable Entity source, @Nullable Entity attacker) {
        return new DamageSource(getEntry(world, DEEP_WOUND), source, attacker);
    }

    public static DamageSource borrowedLife(World world, @Nullable Entity source, @Nullable Entity attacker) {
        return new DamageSource(getEntry(world, BORROWED_LIFE), source, attacker);
    }

    public static void initialise() {}
}
