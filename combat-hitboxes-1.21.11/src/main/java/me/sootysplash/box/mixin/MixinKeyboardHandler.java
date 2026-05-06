package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinKeyboardHandler {

    // State tracking to prevent spamming while holding key
    private boolean lastPressed1 = false;
    private boolean lastPressedH = false;

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // Safety check
        if (mc.player != null && mc.currentScreen == null && Main.c) {

            // FIX: Pass 'mc.getWindow()' directly.
            // The error showed that isKeyPressed takes (Window, int), not (long, int).
            boolean isPressed1 = InputUtil.isKeyPressed(mc.getWindow(), 49); // Key 1

            if (isPressed1 && !lastPressed1) {
                Main.a = !Main.a;
                mc.player.playSound(
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        50.0F,
                        Main.a ? 1.0F : 0.5F
                );
            }
            lastPressed1 = isPressed1; // Update state

            // Handle Key H (72)
            boolean isPressedH = InputUtil.isKeyPressed(mc.getWindow(), 72);

            if (isPressedH && !lastPressedH) {
                Main.b = !Main.b;
                mc.player.playSound(
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        50.0F,
                        Main.b ? 1.0F : 0.5F
                );
            }
            lastPressedH = isPressedH;
        }
    }
}
