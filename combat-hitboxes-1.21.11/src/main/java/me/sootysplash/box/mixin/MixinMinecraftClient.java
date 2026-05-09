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
        // Toggle-Check aus deiner Main
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        // 1. ZIEL-VALIDIERUNG (Raytrace-Check für Legit-Look)
        HitResult crosshairTarget = mc.crosshairTarget;
        if (!(crosshairTarget instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }

        // Grundlegende Sicherheits-Checks
        if (target.getHealth() <= 0 || target.isDead() || target.isInvisible()) return;
        
        // Reichweite auf 2.97 Blöcke begrenzt (Sicherer als Vanilla 3.0)
        if (mc.player.distanceTo(target) > 2.97) return;

        // 2. COOLDOWN-LOGIK (88% - 95%)
        float progress = mc.player.getAttackCooldownProgress(0.5f);
        
        if (shouldExecute(mc, progress)) {
            executeSilentAttack(mc, target);
        }
    }

    @Unique
    private boolean shouldExecute(MinecraftClient mc, float progress) {
        // Bereich: 0.88 bis 0.95 (Deine Vorgabe)
        // 0.88 Basis + bis zu 0.07 Randomness = max 0.95
        double threshold = 0.88 + ThreadLocalRandom.current().nextDouble(0.0, 0.07);
        
        // Crit-Anpassung: Wenn wir fallen, schlagen wir minimal früher
        if (mc.player.getVelocity().y < -0.01 && !mc.player.isOnGround()) {
            threshold -= 0.02;
        }
        
        return progress >= threshold;
    }

    @Unique
    private void executeSilentAttack(MinecraftClient mc, Entity target) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;

        // --- SILENT EXECUTION (Doomsday Style) ---
        
        // Paket 1: Angriff an den Server senden
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        
        // Paket 2: Hand-Animation an den Server senden
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        // --- BYPASS CLEANUP ---
        // Wir haben lastAttackedTicks entfernt, um die Fehler aus image_9d6535.png zu fixen.
        // Wir rufen KEIN mc.player.attack() auf, um Doppel-Pakete (Insta-Ban) zu vermeiden.
        
        // Visueller Swing nur für deinen Client:
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
