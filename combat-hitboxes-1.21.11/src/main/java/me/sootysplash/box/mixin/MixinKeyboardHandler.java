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

        // Check: Spieler im Spiel, kein Menü offen, Mod ist generell an
        if (mc.player != null && mc.currentScreen == null && Main.c) {

            // Keycode 341 ist die LINKE STRG-Taste (Left Control)
            boolean isPressed = InputUtil.isKeyPressed(mc.getWindow(), 341);

            if (isPressed && !lastPressedTrigger) {
                Main.a = !Main.a; // Schaltet den Triggerbot an/aus
            }
            
            lastPressedTrigger = isPressed; 
        }
    }
}
