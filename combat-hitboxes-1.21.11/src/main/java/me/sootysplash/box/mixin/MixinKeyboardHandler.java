package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
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

        if (mc.player != null && mc.currentScreen == null && Main.c) {

            // Keycode 44 is the Comma / < key
            boolean isPressed = InputUtil.isKeyPressed(mc.getWindow(), 44);

            if (isPressed && !lastPressedTrigger) {
                Main.a = !Main.a; // Toggle Triggerbot
                
                // Play a sound so you know it toggled
                mc.player.playSound(
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        1.0F, 
                        Main.a ? 1.0F : 0.5F
                );
            }
            lastPressedTrigger = isPressed;
        }
    }
}
