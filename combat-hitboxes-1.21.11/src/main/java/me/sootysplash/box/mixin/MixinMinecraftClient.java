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
    private long lastAttackTime = 0L; // Speicher für den letzten Schlag

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onInputStealth(CallbackInfo info) {
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        // 1. SPAM-SCHUTZ: Prüfen, ob seit dem letzten Schlag genug Zeit vergangen ist (ca. 500ms für 1.9+ Combat)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < 500) { 
            return; 
        }

        HitResult crosshairTarget = mc.crosshairTarget;
        if (!(crosshairTarget instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (target.getHealth() <= 0 || target.isDead() || target.isInvisible()) return;
        if (mc.player.distanceTo(target) > 2.97) return;

        float progress = mc.player.getAttackCooldownProgress(0.5f);
        
        if (shouldExecute(mc, progress)) {
            executeSilentAttack(mc, target);
            lastAttackTime = currentTime; // Zeitstempel setzen, damit er nicht spammt
        }
    }

    @Unique
    private boolean shouldExecute(MinecraftClient mc, float progress) {
        double threshold = 0.88 + ThreadLocalRandom.current().nextDouble(0.0, 0.07);
        
        if (mc.player.getVelocity().y < -0.01 && !mc.player.isOnGround()) {
            threshold -= 0.02;
        }
        
        return progress >= threshold;
    }

    @Unique
    private void executeSilentAttack(MinecraftClient mc, Entity target) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;

        // Silent Packet Attack
        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        // Visueller Swing
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
