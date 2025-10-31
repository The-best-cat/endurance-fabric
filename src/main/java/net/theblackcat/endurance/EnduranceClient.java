package net.theblackcat.endurance;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.theblackcat.endurance.data_components.ModDataComponents;

public class EnduranceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, list) -> {
            if (stack.isIn(ItemTags.CHEST_ARMOR)) {
                var cd = stack.getOrDefault(ModDataComponents.UNDYING_COOLDOWN_TYPE, -1);
                if (cd > 0) {
                    float totalSec = cd / 20f;
                    int min = (int)(totalSec / 60f);
                    int sec = (int)(totalSec % 60f);
                    list.add(Text.empty());
                    list.add(Text.translatable("item." + Endurance.MOD_ID + ".undying_cooldown.tooltip", min, sec < 10 ? "0" + sec : String.valueOf(sec)));
                } else if (cd == 0) {
                    list.add(Text.empty());
                    list.add(Text.translatable("item." + Endurance.MOD_ID + ".undying_cooldown.activated.tooltip"));
                }
            }
        });
    }
}
