package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow public ClientPlayerEntity player;
    @Shadow public Entity targetedEntity;

    @Unique private static UUID lastTargetUUID = null;

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void onManualAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.targetedEntity instanceof LivingEntity) {
            lastTargetUUID = this.targetedEntity.getUuid();
        }
    }

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void onInput(CallbackInfo info) {
        // Check if Global Master and Triggerbot are ON
        if (Main.c && Main.a) {
            if (player == null || targetedEntity == null) return;

            // Ensure we are looking at the right target
            if (lastTargetUUID != null && !targetedEntity.getUuid().equals(lastTargetUUID)) return;

            // Only trigger if holding a Sword or Axe
            if (player.getMainHandStack().isIn(ItemTags.SWORDS) || player.getMainHandStack().isIn(ItemTags.AXES)) {
                
                // Don't attack if in a menu or using an item (Shielding/Eating)
                if (!player.isBlocking() && !player.isUsingItem() && !(MinecraftClient.getInstance().currentScreen instanceof HandledScreen)) {
                    
                    if (targetedEntity instanceof LivingEntity livingTarget && livingTarget.getHealth() > 0.0F) {
                        double cooldown = player.getAttackCooldownProgress(0.5F);

                        // Movement Logic for Crits
                        if (player.isOnGround()) {
                            if (!player.isSprinting()) return;
                            if (cooldown < 0.85D + Math.random() * 0.1D) return;
                            performBotAttack(livingTarget);
                        } else {
                            // Perfect Critical Hit timing (falling)
                            if (player.getVelocity().y > -0.08) return;
                            if (player.isClimbing() || player.isTouchingWater()) return;
                            if (cooldown < 0.85D + Math.random() * 0.05D) return;
                            performBotAttack(livingTarget);
                        }
                    }
                }
            }
        }
    }

    @Unique
    private void performBotAttack(LivingEntity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.interactionManager != null) {
            mc.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
            lastTargetUUID = target.getUuid();
        }
    }
}
