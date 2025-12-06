package net.theblackcat.endurance.data_components;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.theblackcat.duskywisplibrary.data_component_types.Cooldown;
import net.theblackcat.endurance.Endurance;

public class EnduranceDataComponents {
    public static final ComponentType<Cooldown> UNDYING_COOLDOWN = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Endurance.id("undying_cooldown"),
            ComponentType.<Cooldown>builder().codec(Cooldown.CODEC).build()
    );

    public static final ComponentType<Cooldown> BORROWED_LIFE_COOLDOWN = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Endurance.id("borrowed_life_consumed"),
            ComponentType.<Cooldown>builder().codec(Cooldown.CODEC).build()
    );

    public static void initialise() {}
}
