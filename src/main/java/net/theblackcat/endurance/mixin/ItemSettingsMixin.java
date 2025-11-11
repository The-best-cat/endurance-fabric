package net.theblackcat.endurance.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.theblackcat.endurance.data_components.EnduranceDataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.Settings.class)
public class ItemSettingsMixin {
    @Inject(method = "armor", at = @At("RETURN"))
    private void AddCooldownComponent(ArmorMaterial material, EquipmentType type, CallbackInfoReturnable<Item.Settings> info) {
        if (type.equals(EquipmentType.CHESTPLATE)) {
            info.getReturnValue().component(EnduranceDataComponents.UNDYING_COOLDOWN_TYPE, -1);
        }
    }
}
