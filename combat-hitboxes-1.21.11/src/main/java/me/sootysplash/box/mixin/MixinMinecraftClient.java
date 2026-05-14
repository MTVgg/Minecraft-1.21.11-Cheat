package me.sootysplash.box.mixin;

import me.sootysplash.box.Main;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_310.class})
public abstract class MixinMinecraftClient {
    @Inject(method={"method_1508()V"}, at={@At(value="RETURN")})
    private void onInput(CallbackInfo info) {
        if (!Main.c || !Main.a) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1692 == null) {
            return;
        }
        if (!mc.field_1724.method_6047().method_31573(class_3489.field_42611) && !mc.field_1724.method_6047().method_31573(class_3489.field_42612)) {
            return;
        }
        if (mc.field_1724.method_6039() || mc.field_1724.method_6115()) {
            return;
        }
        if (mc.field_1755 instanceof class_465) {
            return;
        }
        if (mc.field_1724.method_6032() <= 0.0f) {
            return;
        }
        class_1297 class_12972 = mc.field_1692;
        if (!(class_12972 instanceof class_1309)) {
            return;
        }
        class_1309 target = (class_1309)class_12972;
        if (target.method_6032() <= 0.0f) {
            return;
        }
        float progress = mc.field_1724.method_7261(0.5f);
        if (mc.field_1724.method_24828()) {
            if (!mc.field_1724.method_5624()) {
                return;
            }
            if ((double)progress < 0.85 + Math.random() * 0.1) {
                return;
            }
            if (mc.field_1761 != null) {
                mc.field_1761.method_2918((class_1657)mc.field_1724, (class_1297)target);
                mc.field_1724.method_6104(class_1268.field_5808);
            }
        } else {
            if ((double)progress < 0.85 + Math.random() * 0.05) {
                return;
            }
            // Fall-Velocity auf -0.08 gesetzt (entspricht dem Gravitations-Standard)
            if (mc.field_1724.method_18798().field_1351 > -0.08) {
                return;
            }
            if (mc.field_1761 != null) {
                mc.field_1761.method_2918((class_1657)mc.field_1724, (class_1297)target);
                mc.field_1724.method_6104(class_1268.field_5808);
            }
        }
    }
}
