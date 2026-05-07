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
        // 1. Grund-Checks (Mod an?)
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // 2. Waffen-Check (Exakt wie BHC - prüft die Item-Klasse)
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem) && 
            !(mc.player.getMainHandStack().getItem() instanceof AxeItem)) return;

        // 3. Status-Checks (Blocken, Essen, Inventar)
        if (mc.player.isBlocking() || mc.player.isUsingItem()) return;
        if (mc.currentScreen instanceof HandledScreen) return;
        if (mc.player.getHealth() <= 0.0f) return;

        // 4. Ziel-Check
        if (!(mc.targetedEntity instanceof LivingEntity target)) return;
        if (target.getHealth() <= 0.0f) return;

        // 5. Reichweiten-Check (3.0 Blöcke Limit für maximale Sicherheit)
        if (mc.player.distanceTo(target) > 3.0f) return;

        // 6. COOLDOWN-ABFRAGE (Hier ist die mc.player.getAttackCooldownProgress(0.5f) Methode!)
        float cooldownProgress = mc.player.getAttackCooldownProgress(0.5f);

        // 7. Angriffs-Logik (Getrennt nach Boden und Luft)
        if (mc.player.isOnGround()) {
            // Boden: Nur beim Sprinten
            if (!mc.player.isSprinting()) return;
            
            // 0.88 Basis + kleiner Zufall
            if ((double)cooldownProgress < 0.88 + Math.random() * 0.1) return;

            // Angriff über InteractionManager
            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            // Luft: Nur beim Fallen (Crit)
            if (mc.player.getVelocity().y > -0.1) return;
            
            // 0.88 Basis + kleiner Zufall
            if ((double)cooldownProgress < 0.88 + Math.random() * 0.05) return;

            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
