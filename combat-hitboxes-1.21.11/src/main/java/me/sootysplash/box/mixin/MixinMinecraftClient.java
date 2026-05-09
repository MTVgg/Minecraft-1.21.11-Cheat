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

    @Unique
    private long lastAttackTimestamp = 0L; // Verhindert das Spamming

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onInputStealth(CallbackInfo info) {
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        // 1. ANTI-SPAM FILTER (Der wichtigste Part!)
        // Da der visuelle Cooldown im Client oft oben bleibt, 
        // erzwingen wir hier eine Pause von 600ms zwischen den Hits.
        long now = System.currentTimeMillis();
        if (now - lastAttackTimestamp < 600) {
            return;
        }

        // 2. TARGET VALIDATION
        HitResult targetResult = mc.crosshairTarget;
        if (!(targetResult instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (target.getHealth() <= 0 || target.isDead()) return;
        
        // Reichweite (Reach-Bypass)
        if (mc.player.distanceTo(target) > 2.97) return;

        // 3. COOLDOWN CALCULATION (88% - 95%)
        float progress = mc.player.getAttackCooldownProgress(0.5f);
        
        if (shouldExecute(mc, progress)) {
            executeSilentAttack(mc, target);
            lastAttackTimestamp = now; // Timer resetten
        }
    }

    @Unique
    private boolean shouldExecute(MinecraftClient mc, float progress) {
        // Bereich 0.88 bis 0.95 für maximale Unauffälligkeit
        double threshold = 0.88 + ThreadLocalRandom.current().nextDouble(0.0, 0.07);
        
        // Crit-Logik: Im Fall etwas aggressiver
        if (mc.player.getVelocity().y < -0.01 && !mc.player.isOnGround()) {
            threshold -= 0.02;
        }
        
        return progress >= threshold;
    }

    @Unique
    private void executeSilentAttack(MinecraftClient mc, Entity target) {
        // --- SERVER SIDE ---
        // Wir senden die Pakete direkt. Das AC sieht nur diese 2 Pakete.
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        // --- CLIENT SIDE (VISUALS) ---
        // Die Animation triggern
        mc.player.swingHand(Hand.MAIN_HAND);
        
        // Den Cooldown-Balken für dich zurücksetzen (Falls dein Mapping es erlaubt)
        // Sollte Gradle hier wieder meckern, lösche NUR die nächste Zeile.
        mc.player.resetLastAttackedTicks();
    }
}
