package me.sootysplash.box.mixin;

import me.sootysplash.box.Config;
import me.sootysplash.box.Main;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;


@Mixin(EntityHitboxDebugRenderer.class)
public abstract class HitBoxRenderMixin {

    @Inject(method = "drawHitbox", at = @At("HEAD"), cancellable = true)
    private void onDrawHitbox(Entity entity, float tickProgress, boolean inLocalServer, CallbackInfo ci) {
        if (inLocalServer) {// they want to debug, let them
            return;
        }
        Config config = Config.getInstance();
        if (!config.enabled) {
            return;
        }
        ci.cancel();
        if (config.hideFireworks && entity instanceof FireworkRocketEntity) {
            return;
        }

        float lineWidth = Main.mc.player != null && Main.mc.player.distanceTo(entity) > config.distFor2 ? config.line2 : config.line1;

        render_1_21_1_boxes(lineWidth, entity, tickProgress,
                new Color(config.eyeColor, true),
                new Color(config.lookColor, true),
                new Color(config.hitBoxColor, true),
                new Color(config.targetBoxColor, true),
                new Color(config.hurtBoxColor, true),
                new Color(config.outlineColor, true),
                config.changeTargetColor,
                config.hitBoxHurt,
                config.renderEyeHeight,
                config.renderLookDir,
                config.lineLookDir,
                config.outlineEnabled,
                config.outlineMultiplier);
    }

    @Unique
    private static void render_1_21_1_boxes(float lineWidth, Entity entity, float tickProgress,
                                            Color eyeHeight,
                                            Color lookDir,
                                            Color main,
                                            Color ifTarget,
                                            Color ifHurt,
                                            Color outlineColor,
                                            boolean targetCol,
                                            boolean hurtCol,
                                            boolean renderEyeHeight,
                                            boolean renderLookDir,
                                            boolean lineLookDir,
                                            boolean outlineEnabled,
                                            float outlineMultiplier) {
        Vec3d vec3d = entity.getEntityPos();
        Vec3d vec3d2 = entity.getLerpedPos(tickProgress);
        Vec3d vec3d3 = vec3d2.subtract(vec3d);
        Color outer = entity instanceof LivingEntity le && le.hurtTime != 0 && hurtCol ? ifHurt : (targetCol && Main.mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() == entity ? ifTarget : main);
        int i = outer.getRGB();
        if (outlineEnabled) {
            GizmoDrawing.box(entity.getBoundingBox().offset(vec3d3), DrawStyle.stroked(outlineColor.getRGB(), lineWidth * outlineMultiplier));
        }
        GizmoDrawing.box(entity.getBoundingBox().offset(vec3d3), DrawStyle.stroked(i, lineWidth));
        GizmoDrawing.point(vec3d2, i, 2.0F);
        Entity entity2 = entity.getVehicle();
        if (entity2 != null) {
            float f = Math.min(entity2.getWidth(), entity.getWidth()) / 2.0F;
            float g = 0.0625F;
            Vec3d vec3d4 = entity2.getPassengerRidingPos(entity).add(vec3d3);
            GizmoDrawing.box(new Box(vec3d4.x - f, vec3d4.y, vec3d4.z - f, vec3d4.x + f, vec3d4.y + 0.0625, vec3d4.z + f), DrawStyle.stroked(-256, lineWidth));
        }

        if (entity instanceof LivingEntity && renderEyeHeight) {
            Box box = entity.getBoundingBox().offset(vec3d3);
            float g = 0.01F;
            GizmoDrawing.box(
                    new Box(box.minX, box.minY + entity.getStandingEyeHeight() - 0.01F, box.minZ, box.maxX, box.minY + entity.getStandingEyeHeight() + 0.01F, box.maxZ),
                    DrawStyle.stroked(eyeHeight.getRGB(), lineWidth)
            );
        }

        if (entity instanceof EnderDragonEntity enderDragonEntity) {
            for (EnderDragonPart enderDragonPart : enderDragonEntity.getBodyParts()) {
                Vec3d vec3d5 = enderDragonPart.getEntityPos();
                Vec3d vec3d6 = enderDragonPart.getLerpedPos(tickProgress);
                Vec3d vec3d7 = vec3d6.subtract(vec3d5);
                GizmoDrawing.box(enderDragonPart.getBoundingBox().offset(vec3d7), DrawStyle.stroked(ColorHelper.fromFloats(1.0F, 0.25F, 1.0F, 0.0F)));
            }
        }

        Vec3d vec3d8 = vec3d2.add(0.0, entity.getStandingEyeHeight(), 0.0);
        Vec3d vec3d9 = entity.getRotationVec(tickProgress);
        if (renderLookDir) {
            if (lineLookDir) {
                GizmoDrawing.line(vec3d8, vec3d8.add(vec3d9.multiply(2.0)), lookDir.getRGB(), lineWidth);
            } else {
                GizmoDrawing.arrow(vec3d8, vec3d8.add(vec3d9.multiply(2.0)), lookDir.getRGB(), lineWidth);
            }
        }
    }
}
