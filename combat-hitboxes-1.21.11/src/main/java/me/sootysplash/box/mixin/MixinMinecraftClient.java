package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onInputStealth(CallbackInfo info) {
        // Toggle-Check
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        // 1. Ziel-Validierung über das Crosshair (Professioneller Ansatz)
        HitResult crosshairTarget = mc.crosshairTarget;
        if (!(crosshairTarget instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }

        // Grundlegende Checks
        if (target.getHealth() <= 0 || target.isDead() || target.isInvisible()) return;
        
        // Reichweiten-Check (Etwas unter 3.0 für maximale Sicherheit)
        if (mc.player.distanceTo(target) > 2.97) return;

        // 2. Cooldown-Berechnung
        float progress = mc.player.getAttackCooldownProgress(0.5f);
        
        if (shouldExecute(mc, progress)) {
            executeSilentAttack(mc, target);
        }
    }

    @Unique
    private boolean shouldExecute(MinecraftClient mc, float progress) {
        // Dynamischer Schwellenwert (Humanized Randomness)
        double threshold = 0.88 + ThreadLocalRandom.current().nextDouble(0.0, 0.05);
        
        // Crit-Boost: Wenn wir fallen, schlagen wir etwas aggressiver
        if (mc.player.getVelocity().y < -0.01 && !mc.player.isOnGround()) {
            threshold -= 0.02;
        }
        
        return progress >= threshold;
    }

    @Unique
    private void executeSilentAttack(MinecraftClient mc, Entity target) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;

        // --- SILENT PACKET EXECUTION ---
        // Wir senden die Pakete direkt über den NetworkHandler.
        // Das umgeht interne Variablen-Tracker im ClientPlayerEntity.
        
        // Paket 1: Der Angriffsbefehl
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        
        // Paket 2: Die Schlag-Animation für den Server
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        // --- FEEDBACK ---
        // Wir verzichten auf lastAttackedTicks = 0, da dies den Build-Fehler auslöste.
        // Die Animation im eigenen Client rufen wir auf, damit es für DICH legitim aussieht.
        mc.player.swingHand(Hand.MAIN_HAND);
        
        // Hinweis: Der Cooldown-Balken wird visuell im Client ggf. nicht leer, 
        // aber der Server hat den Reset durch das Paket bereits registriert.
    }
}
