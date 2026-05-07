package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void onInput(CallbackInfo info) {
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // 1. Waffen-Check
        boolean hasWeapon = mc.player.getMainHandStack().isIn(ItemTags.SWORDS) || 
                            mc.player.getMainHandStack().isIn(ItemTags.AXES);
        if (!hasWeapon) return;

        // 2. Status-Checks
        if (mc.player.isBlocking() || mc.player.isUsingItem()) return;
        if (mc.currentScreen instanceof HandledScreen) return;
        if (mc.player.getHealth() <= 0.0f) return;

        // 3. Target-Check
        if (!(mc.targetedEntity instanceof LivingEntity target)) return;
        if (target.getHealth() <= 0.0f) return;

        // 4. Reichweiten-Check (3.0 Blöcke)
        if (mc.player.distanceTo(target) > 3.0f) return;

        // 5. Cooldown (0.5f wie im Original)
        float cooldownProgress = mc.player.getAttackCooldownProgress(0.5f);

        if (mc.player.isOnGround()) {
            if (!mc.player.isSprinting()) return;
            if ((double)cooldownProgress < 0.88 + Math.random() * 0.1) return;

            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                // RICHTIGER NAME FÜR 1.21.1:
                mc.player.resetAttackCooldown(); 
            }
        } else {
            // CRIT-Logik
            if (mc.player.getVelocity().y > -0.1) return;
            if ((double)cooldownProgress < 0.88 + Math.random() * 0.05) return;

            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                // RICHTIGER NAME FÜR 1.21.1:
                mc.player.resetAttackCooldown();
            }
        }
    }
}
