package com.veinsurveyor;

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
    private static KeyBinding newSessionKey;
    private static KeyBinding deleteSessionKey;
    private static KeyBinding cycleSessionKey;
    private static KeyBinding toggleHudKey;
    private static KeyBinding cycleProjKey;

    public static void register() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("veinsurveyor", "general"));

        // Numpad Defaults
        addSampleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veinsurveyor.add_point",
                GLFW.GLFW_KEY_KP_0,
                category
        ));

        undoSampleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veinsurveyor.undo_point",
                GLFW.GLFW_KEY_KP_1,
                category
        ));

        clearSamplesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veinsurveyor.clear_points",
                GLFW.GLFW_KEY_KP_2,
                category
        ));

        newSessionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veinsurveyor.new_session",
                GLFW.GLFW_KEY_KP_4,
                category
        ));

        deleteSessionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veinsurveyor.delete_session",
                GLFW.GLFW_KEY_KP_5,
                category
        ));

        cycleSessionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veinsurveyor.cycle_session",
                GLFW.GLFW_KEY_KP_6,
                category
        ));

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veinsurveyor.toggle_hud",
                GLFW.GLFW_KEY_KP_7,
                category
        ));

        cycleProjKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.veinsurveyor.toggle_projection",
                GLFW.GLFW_KEY_KP_8,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::onClientTick);
        DebugLog.log("All 8 Numpad keybindings registered successfully (VeinSurveyor)");
    }

    private static void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        VeinData data = VeinData.getInstance();

        // 1. Add Sample Key (Numpad 0)
        while (addSampleKey.wasPressed()) {
            HitResult hit = client.crosshairTarget;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                BlockState state = client.world.getBlockState(pos);
                VeinSession session = data.getActiveSession();
                boolean added = session.addSample(pos);

                DebugLog.log(String.format("[%s] Add sample pressed at (%d, %d, %d) [%s] - added=%s, total=%d",
                        session.getName(), pos.getX(), pos.getY(), pos.getZ(), state.getBlock().getName().getString(), added, session.getSampleCount()));

                if (added) {
                    int count = session.getSampleCount();
                    client.player.sendMessage(
                            Text.literal(String.format("§b[VeinSurveyor]§r [%s] Added sample #%d at §e(%d, %d, %d)§r [%s]",
                                    session.getName(), count, pos.getX(), pos.getY(), pos.getZ(), state.getBlock().getName().getString())),
                            true
                    );
                    client.player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.3f);
                } else {
                    client.player.sendMessage(Text.literal(String.format("§c[VeinSurveyor]§r [%s] Block already sampled!", session.getName())), true);
                }
            } else {
                client.player.sendMessage(Text.literal("§e[VeinSurveyor]§r Not looking at a block!"), true);
            }
        }

        // 2. Undo Sample Key (Numpad 1) - Operates on active session
        while (undoSampleKey.wasPressed()) {
            VeinSession session = data.getActiveSession();
            boolean undone = session.undoLast();
            DebugLog.log(String.format("[%s] Undo sample pressed - undone=%s", session.getName(), undone));
            if (undone) {
                int count = session.getSampleCount();
                client.player.sendMessage(Text.literal(String.format("§e[VeinSurveyor]§r [%s] Undone last sample (%d remaining)", session.getName(), count)), true);
            } else {
                client.player.sendMessage(Text.literal(String.format("§7[VeinSurveyor]§r [%s] No samples to undo.", session.getName())), true);
            }
        }

        // 3. Clear Key (Numpad 2) - Operates on active session
        while (clearSamplesKey.wasPressed()) {
            VeinSession session = data.getActiveSession();
            session.clear();
            DebugLog.log(String.format("[%s] Clear samples pressed", session.getName()));
            client.player.sendMessage(Text.literal(String.format("§6[VeinSurveyor]§r Cleared all points in §e%s§r.", session.getName())), true);
        }

        // 4. New Session Key (Numpad 4)
        while (newSessionKey.wasPressed()) {
            VeinSession session = data.newSession();
            DebugLog.log("Created new session: " + session.getName());
            client.player.sendMessage(Text.literal(String.format("§a[VeinSurveyor]§r Started new session: §e%s§r", session.getName())), true);
            client.player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, 0.9f, 1.5f);
        }

        // 5. Delete Session Key (Numpad 5)
        while (deleteSessionKey.wasPressed()) {
            String oldName = data.getActiveSession().getName();
            int prevCount = data.getSessionCount();
            VeinSession active = data.deleteActiveSession();
            if (prevCount > 1) {
                DebugLog.log("Deleted session: " + oldName + ", new active: " + active.getName());
                client.player.sendMessage(Text.literal(String.format("§c[VeinSurveyor]§r Deleted §e%s§r. Now active: §e%s§r (%d samples)", oldName, active.getName(), active.getSampleCount())), true);
                client.player.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.7f, 1.4f);
            } else {
                DebugLog.log("Reset only remaining session: " + oldName);
                client.player.sendMessage(Text.literal(String.format("§6[VeinSurveyor]§r Cleared sole session: §e%s§r", oldName)), true);
            }
        }

        // 6. Cycle Session Key (Numpad 6)
        while (cycleSessionKey.wasPressed()) {
            if (data.getSessionCount() > 1) {
                VeinSession session = data.nextSession();
                DebugLog.log("Switched to session: " + session.getName());
                client.player.sendMessage(Text.literal(String.format("§b[VeinSurveyor]§r Switched to active session: §e%s§r (%d samples)", session.getName(), session.getSampleCount())), true);
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.2f);
            } else {
                client.player.sendMessage(Text.literal("§7[VeinSurveyor]§r Only 1 session exists. Press [Numpad 4] to create a new vein session."), true);
            }
        }

        // 7. Toggle HUD Key (Numpad 7)
        while (toggleHudKey.wasPressed()) {
            data.toggleHud();
            boolean enabled = data.isHudEnabled();
            DebugLog.log("Toggle HUD pressed: " + enabled);
            client.player.sendMessage(Text.literal("§b[VeinSurveyor]§r HUD: " + (enabled ? "§aEnabled" : "§cDisabled")), true);
        }

        // 8. Cycle Projection Key (Numpad 8)
        while (cycleProjKey.wasPressed()) {
            data.cycleProjection();
            DebugLog.log("Cycle projection distance: " + data.getProjectionDistance());
            client.player.sendMessage(Text.literal(String.format("§b[VeinSurveyor]§r Projection Reach: §e%.0f blocks§r", data.getProjectionDistance())), true);
        }
    }
}
