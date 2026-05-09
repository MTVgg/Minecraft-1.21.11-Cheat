package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onInput(CallbackInfo info) {
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.targetedEntity == null || mc.getNetworkHandler() == null) return;

        // 1. Ziel-Check (Lebt es? Ist es nah genug?)
        if (!(mc.targetedEntity instanceof LivingEntity target) || target.getHealth() <= 0) return;

        // 2. Der aggressive Cooldown-Check (88% bis 92% + minimales Rauschen)
        // Wir nehmen 0.90 als Basis, damit du fast immer den First-Hit hast.
        float cooldown = mc.player.getAttackCooldownProgress(0.5f);
        double threshold = 0.89 + (Math.random() * 0.03); // Variiert zwischen 0.89 und 0.92

        if (cooldown >= threshold) {
            // 3. EXECUTION: Der "Silent" Angriff
            // Wir schicken die Pakete direkt. Kein Warten auf den nächsten Tick.
            
            // Paket A: Der Angriff (C2S)
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
            
            // Paket B: Die Animation (Swing)
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            // 4. Client-State Sync
            // Wir müssen dem Client sagen, dass wir geschlagen haben, sonst schlägt der Triggerbot 
            // im nächsten Frame sofort wieder (weil der Cooldown noch nicht visuell resettet ist).
            mc.player.resetLastAttackedTicks();
        }
    }
}
