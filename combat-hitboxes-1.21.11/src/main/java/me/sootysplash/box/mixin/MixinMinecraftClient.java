package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void onInput(CallbackInfo info) {
        // BHC Checks (BetterHurtCam.c & .a)
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // BHC Waffen-Check
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem) && 
            !(mc.player.getMainHandStack().getItem() instanceof AxeItem)) return;

        // BHC Status-Checks
        if (mc.player.isBlocking() || mc.player.isUsingItem()) return;
        if (mc.currentScreen instanceof HandledScreen) return;
        if (mc.player.getHealth() <= 0.0f) return;

        // BHC Target-Check (targetedEntity ist mc.field_1692)
        if (!(mc.targetedEntity instanceof LivingEntity target)) return;
        if (target.getHealth() <= 0.0f) return;

        // BHC Angriffs-Logik (Getrennt nach Ground/Air)
        if (mc.player.isOnGround()) {
            if (!mc.player.isSprinting()) return;
            // Cooldown Check (0.5f) - Wir nutzen 0.88 für die Sicherheit
            if ((double)mc.player.getAttackCooldownProgress(0.5f) < 0.88 + Math.random() * 0.1) return;

            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            // BHC Crit-Logic (Velocity & Cooldown)
            if ((double)mc.player.getAttackCooldownProgress(0.5f) < 0.88 + Math.random() * 0.05) return;
            if (mc.player.getVelocity().y > -0.1) return;

            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
