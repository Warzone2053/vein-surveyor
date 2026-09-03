package com.veinsurveyor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;

public class VeinHudOverlay {

    public static void render(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden) return;

        VeinData data = VeinData.getInstance();
        if (!data.isHudEnabled()) return;

        VeinSession activeSession = data.getActiveSession();
        VeinAnalysis analysis = activeSession.getAnalysis();
        List<VeinSession> allSessions = data.getAllSessions();

        TextRenderer tr = client.textRenderer;
        if (tr == null) return;

        int x = 10;
        int y = 10;
        int width = 252;

        boolean hasMultipleSessions = allSessions.size() > 1;
        int extraSessionHeight = hasMultipleSessions ? 14 : 0;

        int height;
        if (analysis.sampleCount == 0) {
            height = 46 + extraSessionHeight;
        } else if (analysis.sampleCount == 1) {
            height = 46 + extraSessionHeight;
        } else {
            height = 108 + extraSessionHeight;
        }

        // Glass panel background
        drawContext.fill(x, y, x + width, y + height, 0xD010141C);

        // Border outline
        int borderColor = 0xFF00D4FF;
        drawContext.fill(x, y, x + width, y + 1, borderColor); // top
        drawContext.fill(x, y + height - 1, x + width, y + height, borderColor); // bottom
        drawContext.fill(x, y, x + 1, y + height, borderColor); // left
        drawContext.fill(x + width - 1, y, x + width, y + height, borderColor); // right

        // Header (32-bit ARGB colors with full 0xFF alpha)
        String headerTitle = String.format("◆ %s ◆", activeSession.getName().toUpperCase());
        drawContext.drawText(tr, headerTitle, x + 6, y + 6, 0xFF00FFFF, true);

        String autoText = AutoScanner.getInstance().isEnabled() ? "§aAuto§r" : "§8Auto§r";
        String rightHeader = String.format("%s §7| %.0fm§r", autoText, data.getProjectionDistance());
        int rightWidth = tr.getWidth(rightHeader);
        drawContext.drawText(tr, rightHeader, x + width - rightWidth - 6, y + 6, 0xFFAAAAAA, false);

        int lineY = y + 20;
        int spacing = 11;

        if (analysis.sampleCount == 0) {
            drawContext.drawText(tr, String.format("Session: §e%s§r (Empty)", activeSession.getName()), x + 6, lineY, 0xFFFFFFAA, false);
            lineY += spacing;
            drawContext.drawText(tr, "Aim at ore and press [Numpad 0] to survey", x + 6, lineY, 0xFF888888, false);
            lineY += spacing;
        } else if (analysis.sampleCount == 1) {
            drawContext.drawText(tr, "Samples: 1 block (Need >= 2 for line)", x + 6, lineY, 0xFFFFFFAA, false);
            lineY += spacing;
            drawContext.drawText(tr, "Aim at next ore and press [Numpad 0]", x + 6, lineY, 0xFF888888, false);
            lineY += spacing;
        } else {
            // Stats
            // Samples & Span
            drawContext.drawText(tr, String.format("Samples: §b%d ores§r | Span: §e%.1fm§r", analysis.sampleCount, analysis.spanLength), x + 6, lineY, 0xFFFFFFFF, false);
            lineY += spacing;

            // Heading & Pitch/Yaw
            drawContext.drawText(tr, String.format("Heading: §6%s§r", analysis.compassHeading), x + 6, lineY, 0xFFFFFFFF, false);
            lineY += spacing;

            // Linear Density & Expected Gap
            drawContext.drawText(tr, String.format("Density: §a%.2f ores/m§r (Gap: §e%.1fm§r)", analysis.linearDensity, analysis.expectedGap), x + 6, lineY, 0xFFFFFFFF, false);
            lineY += spacing;

            // Predicted Cutoff Distance
            drawContext.drawText(tr, String.format("Cutoff: §c+%.1fm§r (Stop if 0 ores in %.1fm)", analysis.cutoffDistance, analysis.cutoffDistance), x + 6, lineY, 0xFFFFFFFF, false);
            lineY += spacing;

            // Scatter & Tunnel cross section
            int tunnelSize = (int) Math.ceil(analysis.maxScatterRadius * 2.0);
            if (tunnelSize < 2) tunnelSize = 2;
            drawContext.drawText(tr, String.format("Scatter: §c±%.1fm§r (Dig §f%dx%d§r tunnel)", analysis.avgScatterRadius, tunnelSize, tunnelSize), x + 6, lineY, 0xFFFFFFFF, false);
            lineY += spacing;

            // Fit Confidence Bar
            int confPercent = (int) Math.round(analysis.fitConfidence * 100.0);
            int barColor = confPercent >= 85 ? 0xFF55FF55 : (confPercent >= 60 ? 0xFFFFFF55 : 0xFFFF5555);
            String bar = buildProgressBar(analysis.fitConfidence, 10);
            drawContext.drawText(tr, String.format("Fit: %d%% [%s]", confPercent, bar), x + 6, lineY, barColor, false);
            lineY += spacing;
        }

        // Multiple Sessions Summary Row at bottom
        if (hasMultipleSessions) {
            lineY = y + height - 12;
            StringBuilder sb = new StringBuilder("§7Veins:§r ");
            int activeIdx = data.getActiveSessionIndex();
            for (int i = 0; i < allSessions.size(); i++) {
                VeinSession s = allSessions.get(i);
                if (i == activeIdx) {
                    sb.append(String.format("§b▶[%s: %d]§r ", s.getName(), s.getSampleCount()));
                } else {
                    sb.append(String.format("§8[%s: %d]§r ", s.getName(), s.getSampleCount()));
                }
            }
            drawContext.drawText(tr, sb.toString(), x + 6, lineY, 0xFFFFFFFF, false);
        }
    }

    private static String buildProgressBar(double fraction, int segments) {
        int filled = (int) Math.round(fraction * segments);
        if (filled > segments) filled = segments;
        if (filled < 0) filled = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filled; i++) sb.append("■");
        for (int i = filled; i < segments; i++) sb.append("□");
        return sb.toString();
    }
}
