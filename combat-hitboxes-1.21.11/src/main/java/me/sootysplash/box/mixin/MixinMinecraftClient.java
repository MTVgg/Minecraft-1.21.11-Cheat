package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_310")
public abstract class MixinMinecraftClient {
    
    @Inject(method = "method_1508()V", at = @At("RETURN"))
    private void onInput(CallbackInfo info) {
        if (!Main.c || !Main.a) {
            return;
        }

        // Wir nutzen den direkten Pfad zu den Klassen, um "Symbol not found" zu verhindern
        net.minecraft.class_310 mc = net.minecraft.class_310.method_1551();
        
        if (mc.field_1724 == null || mc.field_1692 == null) {
            return;
        }

        // Item-Check (Schwerter/Tools)
        if (!mc.field_1724.method_6047().method_31573(net.minecraft.class_3489.field_42611) && 
            !mc.field_1724.method_6047().method_31573(net.minecraft.class_3489.field_42612)) {
            return;
        }

        // GUI oder Benutzung-Check
        if (mc.field_1724.method_6039() || mc.field_1724.method_6115() || mc.field_1755 instanceof net.minecraft.class_465) {
            return;
        }

        if (mc.field_1724.method_6032() <= 0.0f) {
            return;
        }

        net.minecraft.class_1297 rawTarget = mc.field_1692;
        if (!(rawTarget instanceof net.minecraft.class_1309)) {
            return;
        }

        net.minecraft.class_1309 target = (net.minecraft.class_1309) rawTarget;
        if (target.method_6032() <= 0.0f) {
            return;
        }

        float progress = mc.field_1724.method_7261(0.5f);

        // --- ATTACK LOGIC ---
        if (mc.field_1724.method_24828()) { // On Ground
            if (!mc.field_1724.method_5624()) return; // Sprinting check
            
            if ((double) progress < 0.85 + Math.random() * 0.1) return;
            
            if (mc.field_1761 != null) {
                mc.field_1761.method_2918((net.minecraft.class_1657) mc.field_1724, (net.minecraft.class_1297) target);
                mc.field_1724.method_6104(net.minecraft.class_1268.field_5808);
            }
        } else { // In Air
            if ((double) progress < 0.85 + Math.random() * 0.05) return;
            
            // DEINE FALL-VELOCITY ANPASSUNG (-0.08)
            if (mc.field_1724.method_18798().field_1351 > -0.08) return;
            
            if (mc.field_1761 != null) {
                mc.field_1761.method_2918((net.minecraft.class_1657) mc.field_1724, (net.minecraft.class_1297) target);
                mc.field_1724.method_6104(net.minecraft.class_1268.field_5808);
            }
        }
    }
}
