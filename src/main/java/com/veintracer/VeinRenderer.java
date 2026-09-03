package com.veintracer;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class VeinRenderer {
    private static int renderLogCount = 0;

    public static void render(WorldRenderContext context) {
        VeinData data = VeinData.getInstance();
        if (!data.isRenderEnabled()) return;

        List<BlockPos> sampleBlocks = data.getSampleBlocks();
        if (sampleBlocks.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.gameRenderer == null) return;

        Camera camera = client.gameRenderer.getCamera();
        if (camera == null) return;
        Vec3d camPos = camera.getCameraPos();
        if (camPos == null) return;

        MatrixStack matrices = context.matrices();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        VeinAnalysis analysis = data.getAnalysis();
        RenderLayer linesLayer = RenderLayers.linesTranslucent();

        if (renderLogCount < 10) {
            DebugLog.log(String.format("Rendering in-world #%d: %d blocks, %d analysis pts, cutoff=%.1fm, cam=(%.1f, %.1f, %.1f)",
                    renderLogCount, sampleBlocks.size(), analysis.sampleCount, analysis.cutoffDistance, camPos.x, camPos.y, camPos.z));
            renderLogCount++;
        }

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        MatrixStack.Entry entry = matrices.peek();

        VertexConsumer lineConsumer = consumers.getBuffer(linesLayer);

        // 1. Draw Bounding Boxes around each sampled Ore Block
        for (BlockPos pos : sampleBlocks) {
            drawBlockBox(lineConsumer, entry, pos, 0.0f, 0.9f, 1.0f, 1.0f);
        }

        // 2. Draw Centerline and Multi-Zone Projections if we have at least 2 samples
        if (analysis.sampleCount >= 2) {
            double projDist = data.getProjectionDistance();
            Vec3d clusterStart = analysis.getClusterStart();
            Vec3d clusterEnd = analysis.getClusterEnd();

            Vec3d fwdWarn = analysis.getForwardWarningPoint();
            Vec3d fwdCutoff = analysis.getForwardCutoffPoint();
            Vec3d fwdEnd = analysis.getProjectedEnd(projDist);

            Vec3d bwdWarn = analysis.getBackwardWarningPoint();
            Vec3d bwdCutoff = analysis.getBackwardCutoffPoint();
            Vec3d bwdEnd = analysis.getProjectedStart(projDist);

            // --- Active Vein Core Line (Bright Gold, 5.0px) ---
            drawLine(lineConsumer, entry, clusterStart, clusterEnd, 1.0f, 0.9f, 0.0f, 1.0f, 5.0f);

            // --- Forward Projection Multi-Zone ---
            // Zone 1: High Confidence (Neon Green, 4.5px)
            drawLine(lineConsumer, entry, clusterEnd, fwdWarn, 0.0f, 1.0f, 0.4f, 1.0f, 4.5f);

            // Zone 2: Warning / Thinning (Orange, 4.0px)
            drawLine(lineConsumer, entry, fwdWarn, fwdCutoff, 1.0f, 0.65f, 0.0f, 1.0f, 4.0f);

            // Zone 3: Past Predicted Cutoff / Over-Tunneling (Red, 3.0px)
            if (projDist > analysis.cutoffDistance) {
                drawLine(lineConsumer, entry, fwdCutoff, fwdEnd, 1.0f, 0.15f, 0.15f, 0.8f, 3.0f);
            }

            // --- Backward Projection Multi-Zone ---
            // Zone 1: High Confidence (Cyan, 4.0px)
            drawLine(lineConsumer, entry, clusterStart, bwdWarn, 0.2f, 0.8f, 1.0f, 0.9f, 4.0f);

            // Zone 2: Warning (Orange, 3.5px)
            drawLine(lineConsumer, entry, bwdWarn, bwdCutoff, 1.0f, 0.65f, 0.0f, 0.9f, 3.5f);

            // Zone 3: Past Cutoff (Red, 3.0px)
            if (projDist > analysis.cutoffDistance) {
                drawLine(lineConsumer, entry, bwdCutoff, bwdEnd, 1.0f, 0.15f, 0.15f, 0.7f, 3.0f);
            }

            // --- Termination Stop Markers (Red Endcap Ring & Cross) ---
            double radius = Math.max(1.0, analysis.maxScatterRadius);
            drawTerminationMarker(lineConsumer, entry, analysis, fwdCutoff, radius);
            drawTerminationMarker(lineConsumer, entry, analysis, bwdCutoff, radius);

            // Centroid marker (White Diamond Cross)
            drawCross(lineConsumer, entry, analysis.centroid, 0.6, 1.0f, 1.0f, 1.0f, 1.0f, 3.0f);

            // Tunnel Cross-Section Envelope with dynamic zone colors
            drawTunnelEnvelope(lineConsumer, entry, analysis, radius, projDist);
        }

        matrices.pop();

        // Flush buffer immediately
        if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw(linesLayer);
            immediate.draw();
        }
    }

    private static void drawTerminationMarker(VertexConsumer consumer, MatrixStack.Entry entry, VeinAnalysis analysis, Vec3d center, double radius) {
        Vec3d dir = analysis.direction;
        Vec3d arbitrary = Math.abs(dir.y) < 0.9 ? new Vec3d(0, 1, 0) : new Vec3d(1, 0, 0);
        Vec3d u = dir.crossProduct(arbitrary).normalize().multiply(radius);
        Vec3d v = dir.crossProduct(u).normalize().multiply(radius);

        int ringSegments = 16;
        Vec3d prevPoint = null;
        Vec3d firstPoint = null;

        // Bright Red Cutoff Ring (4.0px)
        for (int i = 0; i <= ringSegments; i++) {
            double angle = (2.0 * Math.PI * i) / ringSegments;
            Vec3d ringPoint = center.add(u.multiply(Math.cos(angle))).add(v.multiply(Math.sin(angle)));

            if (prevPoint != null) {
                drawLine(consumer, entry, prevPoint, ringPoint, 1.0f, 0.1f, 0.1f, 1.0f, 4.0f);
            } else {
                firstPoint = ringPoint;
            }
            prevPoint = ringPoint;
        }

        // Red Stop Cross in center
        drawLine(consumer, entry, center.subtract(u), center.add(u), 1.0f, 0.1f, 0.1f, 1.0f, 4.0f);
        drawLine(consumer, entry, center.subtract(v), center.add(v), 1.0f, 0.1f, 0.1f, 1.0f, 4.0f);
    }

    private static void drawBlockBox(VertexConsumer consumer, MatrixStack.Entry entry, BlockPos pos, float r, float g, float b, float a) {
        double minX = pos.getX();
        double minY = pos.getY();
        double minZ = pos.getZ();
        double maxX = minX + 1.0;
        double maxY = minY + 1.0;
        double maxZ = minZ + 1.0;

        // 12 edges of a cube
        drawLine(consumer, entry, new Vec3d(minX, minY, minZ), new Vec3d(maxX, minY, minZ), r, g, b, a, 2.5f);
        drawLine(consumer, entry, new Vec3d(maxX, minY, minZ), new Vec3d(maxX, minY, maxZ), r, g, b, a, 2.5f);
        drawLine(consumer, entry, new Vec3d(maxX, minY, maxZ), new Vec3d(minX, minY, maxZ), r, g, b, a, 2.5f);
        drawLine(consumer, entry, new Vec3d(minX, minY, maxZ), new Vec3d(minX, minY, minZ), r, g, b, a, 2.5f);

        drawLine(consumer, entry, new Vec3d(minX, maxY, minZ), new Vec3d(maxX, maxY, minZ), r, g, b, a, 2.5f);
        drawLine(consumer, entry, new Vec3d(maxX, maxY, minZ), new Vec3d(maxX, maxY, maxZ), r, g, b, a, 2.5f);
        drawLine(consumer, entry, new Vec3d(maxX, maxY, maxZ), new Vec3d(minX, maxY, maxZ), r, g, b, a, 2.5f);
        drawLine(consumer, entry, new Vec3d(minX, maxY, maxZ), new Vec3d(minX, maxY, minZ), r, g, b, a, 2.5f);

        drawLine(consumer, entry, new Vec3d(minX, minY, minZ), new Vec3d(minX, maxY, minZ), r, g, b, a, 2.5f);
        drawLine(consumer, entry, new Vec3d(maxX, minY, minZ), new Vec3d(maxX, maxY, minZ), r, g, b, a, 2.5f);
        drawLine(consumer, entry, new Vec3d(maxX, minY, maxZ), new Vec3d(maxX, maxY, maxZ), r, g, b, a, 2.5f);
        drawLine(consumer, entry, new Vec3d(minX, minY, maxZ), new Vec3d(minX, maxY, maxZ), r, g, b, a, 2.5f);
    }

    private static void drawCross(VertexConsumer consumer, MatrixStack.Entry entry, Vec3d pos, double size, float r, float g, float b, float a, float width) {
        drawLine(consumer, entry, pos.add(-size, 0, 0), pos.add(size, 0, 0), r, g, b, a, width);
        drawLine(consumer, entry, pos.add(0, -size, 0), pos.add(0, size, 0), r, g, b, a, width);
        drawLine(consumer, entry, pos.add(0, 0, -size), pos.add(0, 0, size), r, g, b, a, width);
    }

    private static void drawTunnelEnvelope(VertexConsumer consumer, MatrixStack.Entry entry, VeinAnalysis analysis, double radius, double projDist) {
        Vec3d dir = analysis.direction;
        Vec3d arbitrary = Math.abs(dir.y) < 0.9 ? new Vec3d(0, 1, 0) : new Vec3d(1, 0, 0);
        Vec3d u = dir.crossProduct(arbitrary).normalize().multiply(radius);
        Vec3d v = dir.crossProduct(u).normalize().multiply(radius);

        int ringSegments = 16;
        double startT = analysis.minT - (projDist * 0.5);
        double endT = analysis.maxT + projDist;
        double step = 4.0;

        for (double t = startT; t <= endT; t += step) {
            Vec3d center = analysis.centroid.add(dir.multiply(t));
            Vec3d prevPoint = null;
            Vec3d firstPoint = null;

            // Determine ring color based on zone
            float r = 0.2f, g = 0.8f, b = 1.0f, a = 0.5f;
            if (t > analysis.maxT + analysis.cutoffDistance || t < analysis.minT - analysis.cutoffDistance) {
                // Past cutoff -> Red
                r = 1.0f; g = 0.15f; b = 0.15f; a = 0.6f;
            } else if (t > analysis.maxT + analysis.warningDistance || t < analysis.minT - analysis.warningDistance) {
                // Warning zone -> Orange
                r = 1.0f; g = 0.65f; b = 0.0f; a = 0.6f;
            } else if (t >= analysis.minT && t <= analysis.maxT) {
                // Inside active core -> Gold/Cyan
                r = 0.2f; g = 0.9f; b = 1.0f; a = 0.6f;
            } else {
                // High confidence forward reach -> Neon Green
                r = 0.0f; g = 1.0f; b = 0.4f; a = 0.6f;
            }

            for (int i = 0; i <= ringSegments; i++) {
                double angle = (2.0 * Math.PI * i) / ringSegments;
                Vec3d ringPoint = center.add(u.multiply(Math.cos(angle))).add(v.multiply(Math.sin(angle)));

                if (prevPoint != null) {
                    drawLine(consumer, entry, prevPoint, ringPoint, r, g, b, a, 1.8f);
                } else {
                    firstPoint = ringPoint;
                }
                prevPoint = ringPoint;
            }
        }
    }

    private static void drawLine(VertexConsumer consumer, MatrixStack.Entry entry, Vec3d p1, Vec3d p2, float r, float g, float b, float a, float width) {
        Vec3d normal = p2.subtract(p1);
        double len = normal.length();
        if (len > 1e-6) {
            normal = normal.multiply(1.0 / len);
        } else {
            normal = new Vec3d(0, 1, 0);
        }

        consumer.vertex(entry, (float) p1.x, (float) p1.y, (float) p1.z)
                .color(r, g, b, a)
                .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
                .lineWidth(width);

        consumer.vertex(entry, (float) p2.x, (float) p2.y, (float) p2.z)
                .color(r, g, b, a)
                .normal(entry, (float) normal.x, (float) normal.y, (float) normal.z)
                .lineWidth(width);
    }
}
