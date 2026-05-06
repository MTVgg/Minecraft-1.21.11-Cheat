package me.sootysplash.box.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import me.sootysplash.box.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinGameRenderer {
    @Unique
    private static int previousSlot = -1;
    @Unique
    private static boolean isReturning = false;
    @Unique
    private static int returnDelay = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Reset tracker if disabled
        if (!Main.b) {
            previousSlot = mc.player.getInventory().getSelectedSlot();
            isReturning = false;
            return;
        }

        // --- LOGIC: Handle the automatic return to Sword ---
        if (isReturning) {
            if (returnDelay > 0) {
                returnDelay--;
            } else {
                // Swap back to the slot we came from
                if (previousSlot != -1) {
                    mc.player.getInventory().setSelectedSlot(previousSlot);
                }
                isReturning = false;
            }
            return; // Skip the detection logic while we are busy returning
        }

        // --- LOGIC: Detect Slot Switch ---
        int currentSlot = mc.player.getInventory().getSelectedSlot();

        // Only trigger if slot CHANGED and we have a valid previous slot history
        if (previousSlot != -1 && currentSlot != previousSlot) {
            ItemStack stack = mc.player.getMainHandStack();

            // 1. Are we holding an AXE?
            if (stack.isIn(ItemTags.AXES)) {
                // 2. Are we looking at a SHIELDING player?
                Entity target = mc.crosshairTarget instanceof EntityHitResult res ? res.getEntity() : null;

                if (target instanceof PlayerEntity p && p.isUsingItem() && p.getActiveItem().isOf(Items.SHIELD)) {
                    // 3. EXECUTE: Attack immediately
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);

                    // 4. QUEUE: Go back to sword next tick
                    isReturning = true;
                    returnDelay = 1; // 1 tick delay prevents "Invalid Inventory" flags

                    // Do NOT update previousSlot here; we want to remember the sword slot
                    return;
                }
            }
        }

        // Update history (Track where we are so we know where to go back to)
        previousSlot = currentSlot;
    }
}
