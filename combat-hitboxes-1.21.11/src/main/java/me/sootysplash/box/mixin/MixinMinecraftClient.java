package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags; // Wichtig für Schwerter/Äxte
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
        // Grund-Checks: Mod an? Spieler da? Target ein lebendes Wesen?
        if (!Main.c || !Main.a || player == null || !(targetedEntity instanceof LivingEntity target)) {
            return;
        }

        // Check: Hält der Spieler Schwert oder Axt? (Über Tags gelöst)
        boolean hasWeapon = player.getMainHandStack().isIn(ItemTags.SWORDS) || 
                            player.getMainHandStack().isIn(ItemTags.AXES);
        
        if (!hasWeapon) {
            return;
        }

        // Check: Ist der Spieler handlungsfähig?
        if (player.isBlocking() || player.isUsingItem() || 
            MinecraftClient.getInstance().currentScreen instanceof HandledScreen || 
            player.getHealth() <= 0.0f || target.getHealth() <= 0.0f) {
            return;
        }

        double cooldown = player.getAttackCooldownProgress(0.5f);

        if (player.isOnGround()) {
            // SPRINT-LOGIK (Am Boden)
            if (!player.isSprinting()) return;

            // Cooldown Check (0.85 + Random)
            if (cooldown < 0.85 + Math.random() * 0.1) return;

            performBotAttack(target);
        } else {
            // CRIT-LOGIK (In der Luft)
            // Nur schlagen, wenn wir fallen (Velocity Y < -0.1)
            if (player.getVelocity().y > -0.1) return;

            // Cooldown Check (Etwas schneller in der Luft)
            if (cooldown < 0.85 + Math.random() * 0.05) return;

            performBotAttack(target);
        }
    }

    private void performBotAttack(LivingEntity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.interactionManager != null) {
            mc.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
        }
    }
}
