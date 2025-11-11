package net.theblackcat.endurance;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.theblackcat.endurance.config.ModConfig;
import net.theblackcat.endurance.data_components.EnduranceDataComponents;

import java.io.IOException;

public class EnduranceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, list) -> {
            if (stack.isIn(ItemTags.CHEST_ARMOR)) {
                var cd = stack.getOrDefault(EnduranceDataComponents.UNDYING_COOLDOWN_TYPE, -1);
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

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(ClientCommandManager.literal("endurance")
                    .then(ClientCommandManager.literal("showVignette")
                            .then(ClientCommandManager.argument("enable", BoolArgumentType.bool())
                                    .executes(context -> {
                                        boolean enabled = BoolArgumentType.getBool(context, "enable");
                                        try {
                                            ModConfig.Instance().SetShowVignette(enabled);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }

                                        context.getSource().sendFeedback(Text.literal("Showing vignette is " + (enabled ? "enabled" : "disabled")));
                                        return 1;
                                    })))
            );
        });
    }
}
