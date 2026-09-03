package com.veinsurveyor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.util.Identifier;

public class VeinSurveyorClient implements ClientModInitializer {
    public static final String MOD_ID = "veinsurveyor";

    @Override
    public void onInitializeClient() {
        DebugLog.log("Initializing Vein Surveyor Client (1.21.11 native)");

        KeyBindings.register();

        // 1. Register HUD Element via 1.21.11 Fabric API
        HudElementRegistry.addLast(Identifier.of(MOD_ID, "hud"), VeinHudOverlay::render);
        DebugLog.log("HUD Element registered successfully");

        // 2. Register World 3D Renderer via 1.21.11 Fabric API (AFTER_ENTITIES)
        WorldRenderEvents.AFTER_ENTITIES.register(VeinRenderer::render);
        DebugLog.log("World renderer registered successfully (AFTER_ENTITIES)");
    }
}
