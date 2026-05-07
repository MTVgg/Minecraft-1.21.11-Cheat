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
        // 1. Grund-Checks (Mod an?)
        if (!Main.c || !Main.a || player == null || targetedEntity == null) return;

        // 2. Ziel-Validierung (Nur lebende Ziele, die noch HP haben)
        if (!(targetedEntity instanceof LivingEntity target) || target.getHealth() <= 0.0f) return;

        // 3. DISTANZ-CHECK (Maximal 3.0 Blöcke - Extrem wichtig gegen Flags!)
        if (player.distanceTo(target) > 3.0f) return;

        // 4. Waffen- & Status-Check
        if (!(player.getMainHandStack().isIn(ItemTags.SWORDS) || player.getMainHandStack().isIn(ItemTags.AXES))) return;
        if (player.isBlocking() || player.isUsingItem() || MinecraftClient.getInstance().currentScreen instanceof HandledScreen) return;

        double cooldown = player.getAttackCooldownProgress(0.5f);
        
        // 5. Cooldown Logik mit 0.88 Basis
        if (player.isOnGround()) {
            // Am Boden: Nur beim Sprinten
            if (!player.isSprinting()) return;
            // Würfelt zwischen 0.88 und 0.98
            if (cooldown < 0.88D + Math.random() * 0.1D) return;
        } else {
            // In der Luft: Nur beim Fallen (Crit)
            if (player.getVelocity().y > -0.1) return;
            // Würfelt zwischen 0.88 und 0.93
            if (cooldown < 0.88D + Math.random() * 0.05D) return;
        }

        // 6. DER SCHLAG (InteractionManager + Swing)
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.interactionManager != null) {
            mc.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
        }
    }
}
