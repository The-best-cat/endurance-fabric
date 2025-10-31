package net.theblackcat.endurance.data_components;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.theblackcat.endurance.Endurance;

public class ModDataComponents {
    public static final ComponentType<Integer> UNDYING_COOLDOWN_TYPE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Endurance.Id("undying_cooldown"),
            ComponentType.<Integer>builder().codec(Codec.INT).build()
    );

    public static void Initialise() {}
}
