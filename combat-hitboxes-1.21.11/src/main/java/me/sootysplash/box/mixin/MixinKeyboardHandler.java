package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinKeyboardHandler {

    @Unique private boolean lastPressedTrigger = false;

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // Safety check: only work if player is in-game, not in a menu, and Master Switch is ON
        if (mc.player != null && mc.currentScreen == null && Main.c) {

            // Keycode 44 is the Comma / < key
            boolean isPressed = InputUtil.isKeyPressed(mc.getWindow(), 44);

            // Toggle logic: flips the boolean only once when you first press the key
            if (isPressed && !lastPressedTrigger) {
                Main.a = !Main.a; // Toggles Triggerbot ON/OFF
            }
            
            lastPressedTrigger = isPressed; // Update state
        }
    }
}
