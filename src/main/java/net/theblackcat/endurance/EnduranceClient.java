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
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.theblackcat.duskywisplibrary.data_component_types.Cooldown;
import net.theblackcat.endurance.config.EnduranceConfig;
import net.theblackcat.endurance.data_components.EnduranceDataComponents;
import net.theblackcat.endurance.helpers.EnduranceHelper;
import net.theblackcat.endurance.interfaces.IPlayerEntity;

import java.io.IOException;

public class EnduranceClient implements ClientModInitializer {
    private static final Identifier BLOOD_VIGNETTE = Identifier.ofVanilla("textures/misc/vignette.png");

    private MinecraftClient client;
    private float currentAlpha = 0f;
    private float targetAlpha = 0f;
    private float finalAlpha = 0f;

    @Override
    public void onInitializeClient() {
        client = MinecraftClient.getInstance();

        ItemTooltipCallback.EVENT.register((stack, context, type, list) -> {
            boolean spaced = false;

            if (stack.isIn(ItemTags.CHEST_ARMOR)) {
                boolean breaks = breaksOnNextUndying(stack);
                int cd = stack.getOrDefault(EnduranceDataComponents.UNDYING_COOLDOWN, Cooldown.EMPTY).getCooldown(client.world);
                if (cd > 0 || breaks) {
                    spaced = true;
                    list.add(Text.empty());

                    if (cd > 0) {
                        list.add(getCooldown(cd, "undying_cooldown"));
                    } else {
                        list.add(Text.translatable("item." + Endurance.MOD_ID + ".undying.will_break.tooltip").formatted(Formatting.RED));
                    }
                }
            }

            int cd = stack.getOrDefault(EnduranceDataComponents.BORROWED_LIFE_COOLDOWN, Cooldown.EMPTY).getCooldown(client.world);
            if (cd > 0) {
                if (!spaced) list.add(Text.empty());
                list.add(getCooldown(cd, "borrowed_life_cooldown"));
            }
        });


        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(ClientCommandManager.literal("endurance")
                    .then(ClientCommandManager.literal("showVignette")
                            .then(ClientCommandManager.argument("enable", BoolArgumentType.bool())
                                    .executes(context -> {
                                        boolean enabled = BoolArgumentType.getBool(context, "enable");
                                        try {
                                            EnduranceConfig.Instance().SetShowVignette(enabled);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }

                                        context.getSource().sendFeedback(Text.literal("Showing vignette is " + (enabled ? "enabled" : "disabled")));
                                        return 1;
                                    })))
            );
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.HOTBAR, Endurance.id("blood_vignette"), (context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            try {
                if (client.player instanceof IPlayerEntity player && EnduranceConfig.Instance().ShouldShowVignette() && (player.getInjuredTime() > 0 || (player.getRemovedInjuriesTime() > 0 && player.getRemovedInjuriesTime() < 30))) {
                    float rawAlpha;
                    float smoothing;

                    if (player.hasDeepWound()) {
                        rawAlpha = MathHelper.lerp(Math.clamp((float) player.getInjuredTime() / 10f, 0f, 1f), 0f, 0.25f);
                        rawAlpha += MathHelper.lerp(player.getBP() / 20f, 0.45f, 0f);
                        smoothing = 0.05f;
                    } else {
                        rawAlpha = MathHelper.lerp(Math.clamp((float) player.getRemovedInjuriesTime() / 30f, 0f, 1f), finalAlpha, 0);
                        smoothing = 0.15f;
                    }

                    targetAlpha = rawAlpha;
                    currentAlpha += (targetAlpha - currentAlpha) * smoothing;

                    int i = ColorHelper.getArgb((int) (currentAlpha * 255f), 136, 8, 8);
                    context.drawTexture(RenderPipelines.GUI_TEXTURED,
                            BLOOD_VIGNETTE,
                            0, 0, 0f, 0f,
                            context.getScaledWindowWidth(),
                            context.getScaledWindowHeight(),
                            context.getScaledWindowWidth(),
                            context.getScaledWindowHeight(),
                            i
                    );

                    finalAlpha = currentAlpha;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static MutableText getCooldown(int cooldown, String key) {
        float totalSec = cooldown / 20f;
        int min = (int)(totalSec / 60f);
        int sec = (int)(totalSec % 60f);
        String secStr = sec < 10 ? "0" + sec : String.valueOf(sec);
        return Text.translatable("item." + Endurance.MOD_ID + "." + key + ".tooltip", min, secStr).formatted(Formatting.DARK_GRAY);
    }

    private static boolean breaksOnNextUndying(ItemStack stack) {
        return stack.getDamage() + EnduranceHelper.getUndyingDamage(MinecraftClient.getInstance().world, stack) >= stack.getMaxDamage();
    }
}
