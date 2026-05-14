package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.item.Items;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void onInput(CallbackInfo info) {
        if (!Main.c || !Main.a) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.crosshairTarget == null) {
            return;
        }

        // Check ob Schwert/Axt in der Hand (Entspricht field_42611/42612)
        if (!mc.player.getMainHandStack().isOf(Items.NETHERITE_SWORD) && 
            !mc.player.getMainHandStack().isOf(Items.DIAMOND_SWORD) &&
            !mc.player.getMainHandStack().isOf(Items.NETHERITE_AXE) &&
            !mc.player.getMainHandStack().isOf(Items.DIAMOND_AXE)) {
            // Du kannst hier weitere Items hinzufügen, falls nötig
        }

        // GUI Check & Status Check
        if (mc.player.isUsingItem() || mc.player.isDead() || mc.currentScreen instanceof HandledScreen) {
            return;
        }

        if (mc.player.getHealth() <= 0.0f) {
            return;
        }

        // Target Validation
        Entity rawTarget = mc.targetedEntity;
        if (!(rawTarget instanceof LivingEntity target)) {
            return;
        }

        if (target.getHealth() <= 0.0f) {
            return;
        }

        float progress = mc.player.getAttackCooldownProgress(0.5f);

        // --- ATTACK LOGIC ---
        if (mc.player.isOnGround()) { // field_24828 -> isOnGround
            if (!mc.player.isSprinting()) return; 
            
            if ((double) progress < 0.85 + Math.random() * 0.1) return;
            
            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else { // In Air
            if ((double) progress < 0.85 + Math.random() * 0.05) return;
            
            // DEINE FALL-VELOCITY ANPASSUNG (-0.08)
            // field_1351 ist die Y-Velocity
            if (mc.player.getVelocity().y > -0.08) return;
            
            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
