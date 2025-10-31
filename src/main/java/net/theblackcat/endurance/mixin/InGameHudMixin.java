package net.theblackcat.endurance.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.theblackcat.endurance.Endurance;
import net.theblackcat.endurance.interfaces.IPlayerEntity;
import net.theblackcat.endurance.status_effects.ModStatusEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique
    private static final String path = "textures/gui/sprites/hud/heart/";
    @Unique
    private static final Identifier DEEP_WOUND_FULL = Endurance.Id(path + "deep_wound_full.png");
    @Unique
    private static final Identifier DEEP_WOUND_HALF = Endurance.Id(path + "deep_wound_half.png");
    @Unique
    private static final Identifier HEALED_DEEP_WOUND_FULL = Endurance.Id(path + "healed_deep_wound_container_full.png");
    @Unique
    private static final Identifier HEALED_DEEP_WOUND_HALF = Endurance.Id(path + "healed_deep_wound_container_half.png");
    @Unique
    private static final Identifier HEART_CONTAINER = Endurance.Id(path + "normal_container.png");

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private Random random;

    @ModifyArgs(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V"))
    private void PassDeepWound(Args args) {
        if (this.client.player.hasStatusEffect(ModStatusEffects.DEEP_WOUND) && this.client.player instanceof IPlayerEntity player) {
            args.set(8, MathHelper.ceil(player.GetTemporaryHealth()));
            args.set(9, player.GetMendedProgress());
            args.set(10, player.LossPaused());
        }
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void RenderDeepWound(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo info) {
        //health = deep wound, absorption = mending, blinking = is loss paused
        if (this.client.player.hasStatusEffect(ModStatusEffects.DEEP_WOUND)) {
            for (int j = 9; j >= 0; j--) {
                int heartX = x + j * 8;
                int heartY = y + (!blinking ? this.random.nextInt(2) : 0);

                if (absorption > 0) {
                    Draw(context, heartX, heartY, j, absorption, HEALED_DEEP_WOUND_FULL, HEALED_DEEP_WOUND_HALF, HEART_CONTAINER);
                } else {
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, HEART_CONTAINER, heartX, heartY, 0, 0, 9, 9, 9, 9);
                }

                if (health > 0) {
                    Draw(context, heartX, heartY, j, health, DEEP_WOUND_FULL, DEEP_WOUND_HALF, null);
                }
            }
            info.cancel();
        }
    }

    @Unique
    private void Draw(DrawContext context, int x, int y, int current, float value, Identifier texture1, Identifier texture2, Identifier texture3) {
        if (value > current * 2 + 1) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture1, x, y, 0, 0, 9, 9, 9, 9);
        } else if (value == current * 2 + 1) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture2, x, y, 0, 0, 9, 9, 9, 9);
        } else if (texture3 != null) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture3, x, y, 0, 0, 9, 9, 9, 9);
        }
    }
}
