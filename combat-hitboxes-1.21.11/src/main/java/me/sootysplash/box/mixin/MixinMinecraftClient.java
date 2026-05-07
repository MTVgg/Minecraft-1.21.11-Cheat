package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    private void onInput(CallbackInfo info) {
        // Exakt wie BetterHurtCam.c & .a
        if (!Main.c || !Main.a) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.targetedEntity == null) return;

        // Waffen-Check (BHC Style mit Tags für Kompatibilität)
        if (!(mc.player.getMainHandStack().isIn(ItemTags.SWORDS) || mc.player.getMainHandStack().isIn(ItemTags.AXES))) return;

        // Status-Checks (BHC nutzt method_6039, method_6115, field_1755)
        if (mc.player.isBlocking() || mc.player.isUsingItem()) return;
        if (mc.currentScreen instanceof HandledScreen) return;
        if (mc.player.getHealth() <= 0.0f) return;

        // Target-Check (BHC nutzt mc.field_1692 instanceof class_1309)
        if (!(mc.targetedEntity instanceof LivingEntity target)) return;
        if (target.getHealth() <= 0.0f) return;

        // Cooldown Progress (BHC nutzt 0.5f)
        float progress = mc.player.getAttackCooldownProgress(0.5f);

        if (mc.player.isOnGround()) {
            // Sprint Check (BHC: method_5624)
            if (!mc.player.isSprinting()) return;
            
            // BHC Cooldown Logic am Boden: 0.85 + 0.1 Random
            if ((double)progress < 0.85 + Math.random() * 0.1) return;

            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            // BHC Cooldown Logic in der Luft: 0.85 + 0.05 Random
            if ((double)progress < 0.85 + Math.random() * 0.05) return;
            
            // BHC Velocity Check: > -0.1
            if (mc.player.getVelocity().y > -0.1) return;

            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
