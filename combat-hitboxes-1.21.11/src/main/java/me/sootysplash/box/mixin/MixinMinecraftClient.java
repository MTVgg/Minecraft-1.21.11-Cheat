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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow public ClientPlayerEntity player;
    @Shadow public Entity targetedEntity;

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void onInput(CallbackInfo info) {
        // 1. Mod-Check
        if (!Main.c || !Main.a || player == null) return;

        // 2. Target-Check (Nutzt Minecrafts internes Raytracing)
        if (!(targetedEntity instanceof LivingEntity target)) return;

        // 3. Waffen-Check
        if (!(player.getMainHandStack().isIn(ItemTags.SWORDS) || player.getMainHandStack().isIn(ItemTags.AXES))) return;

        // 4. Status-Check (Kein Essen, kein Blocken, kein Inventar, beide leben noch)
        if (player.isBlocking() || player.isUsingItem() || 
            MinecraftClient.getInstance().currentScreen instanceof HandledScreen || 
            player.getHealth() <= 0.0f || target.getHealth() <= 0.0f) {
            return;
        }

        double cooldown = player.getAttackCooldownProgress(0.5f);
        MinecraftClient mc = MinecraftClient.getInstance();

        // 5. Die Logik vom "sicheren" Client
        if (player.isOnGround()) {
            // SPRINT-LOGIK
            if (!player.isSprinting()) return;
            if (cooldown < 0.85 + Math.random() * 0.1) return;

            // INTERACTION MANAGER ATTACK
            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(player, target);
                player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            // CRIT-LOGIK (Luft)
            // Wir nutzen -0.1 für perfektes Falling-Timing
            if (player.getVelocity().y > -0.1) return;
            if (cooldown < 0.85 + Math.random() * 0.05) return;

            // INTERACTION MANAGER ATTACK
            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(player, target);
                player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
