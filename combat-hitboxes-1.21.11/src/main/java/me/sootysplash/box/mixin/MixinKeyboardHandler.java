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

        // Ensure player is in game and no menu is open
        if (mc.player != null && mc.currentScreen == null && Main.c) {

            // Keycode 44 is the Comma / < key
            boolean isPressed = InputUtil.isKeyPressed(mc.getWindow(), 44);

            // Toggle logic: only triggers once per press
            if (isPressed && !lastPressedTrigger) {
                Main.a = !Main.a; // Toggle Triggerbot logic
            }
            lastPressedTrigger = isPressed;
        }
    }
}
}
