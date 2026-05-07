package me.sootysplash.box;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public static final Logger LOGGER = LoggerFactory.getLogger("CombatHitboxes");
    
    public static boolean a = false; // Triggerbot (Starts OFF so you can toggle it with '<')
    public static boolean c = true;  // Master Switch (Keep this TRUE)

    @Override
    public void onInitialize() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        LOGGER.info("CombatHitboxes | Loaded successfully!");
    }
}
