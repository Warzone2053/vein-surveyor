package com.veintracer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static KeyBinding addSampleKey;
    private static KeyBinding undoSampleKey;
    private static KeyBinding clearSamplesKey;
    private static KeyBinding toggleHudKey;
    private static KeyBinding cycleProjKey;

    public static void register() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("veintracer", "general"));

        addSampleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veintracer.add_point",
                GLFW.GLFW_KEY_V,
                category
        ));

        undoSampleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veintracer.undo_point",
                GLFW.GLFW_KEY_Z,
                category
        ));

        clearSamplesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veintracer.clear_points",
                GLFW.GLFW_KEY_C,
                category
        ));

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veintracer.toggle_hud",
                GLFW.GLFW_KEY_H,
                category
        ));

        cycleProjKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veintracer.toggle_projection",
                GLFW.GLFW_KEY_J,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::onClientTick);
        DebugLog.log("Keybindings registered successfully (1.21.11 API)");
    }

    private static void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        // Add Sample Key
        while (addSampleKey.wasPressed()) {
            HitResult hit = client.crosshairTarget;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                BlockState state = client.world.getBlockState(pos);
                boolean added = VeinData.getInstance().addSample(pos);

                DebugLog.log(String.format("Add sample pressed at (%d, %d, %d) [%s] - added=%s, total=%d",
                        pos.getX(), pos.getY(), pos.getZ(), state.getBlock().getName().getString(), added, VeinData.getInstance().getSamplePoints().size()));

                if (added) {
                    int count = VeinData.getInstance().getSamplePoints().size();
                    client.player.sendMessage(
                            Text.literal(String.format("§b[VeinTracer]§r Added sample #%d at §e(%d, %d, %d)§r [%s]",
                                    count, pos.getX(), pos.getY(), pos.getZ(), state.getBlock().getName().getString())),
                            true
                    );
                    client.player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.3f);
                } else {
                    client.player.sendMessage(Text.literal("§c[VeinTracer]§r Block already sampled!"), true);
                }
            } else {
                client.player.sendMessage(Text.literal("§e[VeinTracer]§r Not looking at a block!"), true);
            }
        }

        // Undo Sample Key
        while (undoSampleKey.wasPressed()) {
            boolean undone = VeinData.getInstance().undoLast();
            DebugLog.log("Undo sample pressed - undone=" + undone);
            if (undone) {
                int count = VeinData.getInstance().getSamplePoints().size();
                client.player.sendMessage(Text.literal(String.format("§e[VeinTracer]§r Undone last sample (%d remaining)", count)), true);
            } else {
                client.player.sendMessage(Text.literal("§7[VeinTracer]§r No samples to undo."), true);
            }
        }

        // Clear Key
        while (clearSamplesKey.wasPressed()) {
            VeinData.getInstance().clear();
            DebugLog.log("Clear samples pressed");
            client.player.sendMessage(Text.literal("§6[VeinTracer]§r Cleared all vein samples."), true);
        }

        // Toggle HUD Key
        while (toggleHudKey.wasPressed()) {
            VeinData.getInstance().toggleHud();
            boolean enabled = VeinData.getInstance().isHudEnabled();
            DebugLog.log("Toggle HUD pressed: " + enabled);
            client.player.sendMessage(Text.literal("§b[VeinTracer]§r HUD: " + (enabled ? "§aEnabled" : "§cDisabled")), true);
        }

        // Cycle Projection Key
        while (cycleProjKey.wasPressed()) {
            VeinData.getInstance().cycleProjection();
            DebugLog.log("Cycle projection distance: " + VeinData.getInstance().getProjectionDistance());
            client.player.sendMessage(Text.literal(String.format("§b[VeinTracer]§r Projection Distance: §e%.0f blocks§r", VeinData.getInstance().getProjectionDistance())), true);
        }
    }
}
