package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
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

        // Check: Hält der Spieler Schwert oder Axt?
        if (!(player.getMainHandStack().getItem() instanceof SwordItem) && 
            !(player.getMainHandStack().getItem() instanceof AxeItem)) {
            return;
        }

        // Check: Ist der Spieler handlungsfähig? (Kein Item-Gebrauch, kein Inventar offen, nicht tot)
        if (player.isBlocking() || player.isUsingItem() || 
            MinecraftClient.getInstance().currentScreen instanceof HandledScreen || 
            player.getHealth() <= 0.0f || target.getHealth() <= 0.0f) {
            return;
        }

        double cooldown = player.getAttackCooldownProgress(0.5f);

        // Die Logik aus dem "Clean"-Client:
        if (player.isOnGround()) {
            // SPRINT-LOGIK
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
