package net.theblackcat.endurance.damage_types;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.theblackcat.endurance.Endurance;

public class EnduranceDamageTypes {
    public static final RegistryKey<DamageType> DEEP_WOUND_DAMAGE = Register("deep_wound");

    private static RegistryKey<DamageType> Register(String id) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Endurance.Id(id));
    }

    public static DamageSource GetSource(World world, RegistryKey<DamageType> type) {
        return new DamageSource(world.getRegistryManager().getEntryOrThrow(type), null, null);
    }

    public static void Initialise() {}
}
