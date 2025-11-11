package net.theblackcat.endurance;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.theblackcat.endurance.config.ModConfig;
import net.theblackcat.endurance.data_components.EnduranceDataComponents;
import net.theblackcat.endurance.interfaces.IPlayerEntity;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;

public class EnduranceClient implements ClientModInitializer {
    @Unique
    private static final Identifier BLOOD_VIGNETTE = Identifier.ofVanilla("textures/misc/vignette.png");

    //private float prevAlpha = 0;
    private float finalAlpha = 0;

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

        HudElementRegistry.attachElementBefore(VanillaHudElements.HOTBAR, Endurance.Id("blood_vignette"), (context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            try {
                if (client.player instanceof IPlayerEntity player && ModConfig.Instance().ShouldShowVignette() && (player.GetInjuredTime() > 0 || (player.GetRemovedInjuriesTime() > 0 && player.GetRemovedInjuriesTime() < 30))) {
                    float alpha;
                    if (player.HasDeepWound()) {
                        alpha = MathHelper.lerp(Math.clamp((float) player.GetInjuredTime() / 10f, 0f, 1f), 0f, 0.3f);
                        alpha += MathHelper.lerp(player.GetTemporaryHealth() / 20f, 0.5f, 0f);

//                        alpha = MathHelper.lerp(tickCounter.getTickProgress(false), prevAlpha, alpha);
//                        prevAlpha = alpha;
                    } else {
                        alpha = MathHelper.lerp(Math.clamp((float) player.GetRemovedInjuriesTime() / 30f, 0f, 1f), finalAlpha, 0);
                    }

                    int i = ColorHelper.getArgb((int) (alpha * 255f), 136, 8, 8);
                    context.drawTexture(RenderPipelines.GUI_TEXTURED,
                            BLOOD_VIGNETTE,
                            0, 0, 0f, 0f,
                            context.getScaledWindowWidth(),
                            context.getScaledWindowHeight(),
                            context.getScaledWindowWidth(),
                            context.getScaledWindowHeight(),
                            i
                    );

                    finalAlpha = alpha;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
