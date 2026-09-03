package com.veinsurveyor;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class AutoScanner {
    private static final AutoScanner INSTANCE = new AutoScanner();

    public static AutoScanner getInstance() {
        return INSTANCE;
    }

    private boolean enabled = false;
    private static final double SCAN_RADIUS = 50.0;
    private int tickCooldown = 0;

    private AutoScanner() {}

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle(MinecraftClient client) {
        this.enabled = !this.enabled;
        DebugLog.log("Auto-Scan toggled: " + this.enabled);
        if (this.enabled) {
            scanNow(client, true);
        }
    }

    public void tick(MinecraftClient client) {
        if (!enabled || client.player == null || client.world == null) return;

        tickCooldown++;
        if (tickCooldown >= 10) { // Scan every 10 ticks (0.5 seconds)
            tickCooldown = 0;
            scanNow(client, false);
        }
    }

    public int scanNow(MinecraftClient client, boolean notifyIfNone) {
        if (client.player == null || client.world == null) return 0;

        ClientWorld world = client.world;
        BlockPos playerPos = client.player.getBlockPos();
        VeinData data = VeinData.getInstance();
        VeinSession activeSession = data.getActiveSession();

        int minChunkX = (playerPos.getX() - 50) >> 4;
        int maxChunkX = (playerPos.getX() + 50) >> 4;
        int minChunkZ = (playerPos.getZ() - 50) >> 4;
        int maxChunkZ = (playerPos.getZ() + 50) >> 4;

        List<BlockPos> newlyFound = new ArrayList<>();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                WorldChunk chunk = world.getChunkManager().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue;

                ChunkSection[] sections = chunk.getSectionArray();
                for (int sIdx = 0; sIdx < sections.length; sIdx++) {
                    ChunkSection section = sections[sIdx];
                    if (section == null || section.isEmpty()) continue;

                    int sectionBaseY = world.sectionIndexToCoord(sIdx) << 4;
                    if (sectionBaseY + 16 < playerPos.getY() - 50 || sectionBaseY > playerPos.getY() + 50) {
                        continue;
                    }

                    // Ultra-fast palette check: skip section if no diamond ore exists in its palette container
                    boolean hasDiamond = section.getBlockStateContainer().hasAny(AutoScanner::isDiamondOre);
                    if (!hasDiamond) continue;

                    for (int lx = 0; lx < 16; lx++) {
                        for (int lz = 0; lz < 16; lz++) {
                            for (int ly = 0; ly < 16; ly++) {
                                BlockState state = section.getBlockState(lx, ly, lz);
                                if (isDiamondOre(state)) {
                                    int wx = (cx << 4) + lx;
                                    int wy = sectionBaseY + ly;
                                    int wz = (cz << 4) + lz;
                                    BlockPos orePos = new BlockPos(wx, wy, wz);

                                    if (orePos.isWithinDistance(playerPos, SCAN_RADIUS)) {
                                        boolean added = activeSession.addSample(orePos);
                                        if (added) {
                                            newlyFound.add(orePos);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!newlyFound.isEmpty()) {
            DebugLog.log(String.format("[%s] Auto-detected %d new diamond ore blocks within 50m! (Total: %d)",
                    activeSession.getName(), newlyFound.size(), activeSession.getSampleCount()));

            client.player.sendMessage(
                    Text.literal(String.format("§a[VeinSurveyor]§r [%s] Auto-detected §e%d§r diamond ore(s) within 50m! (Total: %d)",
                            activeSession.getName(), newlyFound.size(), activeSession.getSampleCount())),
                    true
            );
            client.player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
        } else if (notifyIfNone) {
            client.player.sendMessage(
                    Text.literal("§7[VeinSurveyor]§r Auto-Scan enabled (50m radius). No unlogged diamond ores in range."),
                    true
            );
        }

        return newlyFound.size();
    }

    private static boolean isDiamondOre(BlockState state) {
        return state.isOf(Blocks.DIAMOND_ORE) || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE);
    }
}
