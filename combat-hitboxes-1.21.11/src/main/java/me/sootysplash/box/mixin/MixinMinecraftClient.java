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
        // Toggle-Check aus deiner Main-Klasse
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        // 1. VALIDIERUNG: Prüfen, ob wir ein Ziel im Fadenkreuz haben
        HitResult crosshairTarget = mc.crosshairTarget;
        if (!(crosshairTarget instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }

        // Sicherheits-Checks (Tot, unsichtbar oder zu weit weg)
        if (target.getHealth() <= 0 || target.isDead() || target.isInvisible()) return;
        if (mc.player.distanceTo(target) > 2.98) return;

        // 2. COOLDOWN & TIMING
        float progress = mc.player.getAttackCooldownProgress(0.5f);
        
        if (shouldExecute(mc, progress)) {
            executeSilentAttack(mc, target);
        }
    }

    @Unique
    private boolean shouldExecute(MinecraftClient mc, float progress) {
        // Bereich zwischen ~89% und ~93%
        double threshold = 0.89 + ThreadLocalRandom.current().nextDouble(0.0, 0.04);
        
        // Kleiner Boost, wenn wir fallen (für Crits)
        if (mc.player.getVelocity().y < -0.05 && !mc.player.isOnGround()) {
            threshold -= 0.03;
        }
        
        return progress >= threshold;
    }

    @Unique
    private void executeSilentAttack(MinecraftClient mc, Entity target) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;

        // A: Das Angriffs-Paket direkt an den Server (Umgeht viele Client-Side Checks)
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        
        // B: Das Swing-Paket (Sorgt dafür, dass der Server die Animation registriert)
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        // C: COOLDOWN RESET (FIX für den Error in image_9d73c4.png)
        // Wir nutzen den direkten Feldzugriff statt der Methode
        mc.player.lastAttackedTicks = 0;

        // D: Visuelles Feedback im eigenen Client
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
