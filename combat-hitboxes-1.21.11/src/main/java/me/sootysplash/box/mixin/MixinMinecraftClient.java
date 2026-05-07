package me.sootysplash.box.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import me.sootysplash.box.Main;
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
    @Unique private static int tickDelay = 0; 

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void onManualAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.targetedEntity instanceof LivingEntity) {
            lastTargetUUID = this.targetedEntity.getUuid();
        }
    }

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void onInput(CallbackInfo info) {
        if (Main.c && Main.a) {
            if (player == null || targetedEntity == null) return;

            if (lastTargetUUID != null) {
                boolean targetFoundAndInRange = false;
                for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
                    if (entity.getUuid().equals(lastTargetUUID)) {
                        if (player.distanceTo(entity) <= 50.0) {
                            targetFoundAndInRange = true;
                        }
                        break;
                    }
                }
                if (!targetFoundAndInRange) {
                    lastTargetUUID = null;
                }
            }

            if (lastTargetUUID != null && !targetedEntity.getUuid().equals(lastTargetUUID)) return;

            if (player.getMainHandStack().isIn(ItemTags.SWORDS) || player.getMainHandStack().isIn(ItemTags.AXES)) {
                if (!player.isBlocking() && !player.isUsingItem() && !(MinecraftClient.getInstance().currentScreen instanceof HandledScreen)) {
                    if (targetedEntity instanceof LivingEntity livingTarget && livingTarget.getHealth() > 0.0F) {
                        double cooldownProgress = player.getAttackCooldownProgress(0.5F);

                        if (player.isOnGround()) {
                            if (!player.isSprinting()) return;
                            
                            // Cooldown MINIMAL erhöht von 0.85 auf 0.88
                            if (cooldownProgress < 0.88D + Math.random() * 0.1D) return;
                            
                            if (tickDelay <= 0) {
                                tickDelay = (Math.random() < 0.3) ? 1 : 0;
                                if (tickDelay == 0) performBotAttack(livingTarget);
                            } else {
                                tickDelay--;
                                if (tickDelay == 0) performBotAttack(livingTarget);
                            }
                        } else {
                            if (player.getVelocity().y > -0.08) return;
                            if (player.isClimbing() || player.isTouchingWater() || player.hasVehicle()) return;
                            
                            // Cooldown MINIMAL erhöht von 0.85 auf 0.88
                            if (cooldownProgress < 0.88D + Math.random() * 0.05D) return;
                            
                            if (tickDelay <= 0) {
                                tickDelay = (Math.random() < 0.3) ? 1 : 0;
                                if (tickDelay == 0) performBotAttack(livingTarget);
                            } else {
                                tickDelay--;
                                if (tickDelay == 0) performBotAttack(livingTarget);
                            }
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
