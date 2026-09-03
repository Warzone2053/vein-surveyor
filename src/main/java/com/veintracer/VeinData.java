package com.veintracer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VeinData {
    private static final VeinData INSTANCE = new VeinData();
    public static VeinData getInstance() { return INSTANCE; }

    private final List<BlockPos> sampleBlocks = new ArrayList<>();
    private final List<Vec3d> samplePoints = new ArrayList<>();
    private VeinAnalysis currentAnalysis = new VeinAnalysis(Collections.emptyList());

    private boolean hudEnabled = true;
    private boolean renderEnabled = true;
    private double projectionDistance = 50.0; // default 50 blocks

    private static final double[] PROJECTION_PRESETS = { 25.0, 50.0, 100.0, 150.0, 200.0 };
    private int projectionIndex = 1; // 50.0

    private VeinData() {}

    public synchronized boolean addSample(BlockPos pos) {
        if (sampleBlocks.contains(pos)) {
            return false; // Already added
        }
        sampleBlocks.add(pos);
        samplePoints.add(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        recalculate();
        return true;
    }

    public synchronized boolean undoLast() {
        if (sampleBlocks.isEmpty()) return false;
        sampleBlocks.remove(sampleBlocks.size() - 1);
        samplePoints.remove(samplePoints.size() - 1);
        recalculate();
        return true;
    }

    public synchronized void clear() {
        sampleBlocks.clear();
        samplePoints.clear();
        recalculate();
    }

    private void recalculate() {
        this.currentAnalysis = new VeinAnalysis(this.samplePoints);
    }

    public synchronized List<BlockPos> getSampleBlocks() {
        return Collections.unmodifiableList(new ArrayList<>(sampleBlocks));
    }

    public synchronized List<Vec3d> getSamplePoints() {
        return Collections.unmodifiableList(new ArrayList<>(samplePoints));
    }

    public synchronized VeinAnalysis getAnalysis() {
        return currentAnalysis;
    }

    public boolean isHudEnabled() { return hudEnabled; }
    public void toggleHud() { this.hudEnabled = !this.hudEnabled; }

    public boolean isRenderEnabled() { return renderEnabled; }
    public void toggleRender() { this.renderEnabled = !this.renderEnabled; }

    public double getProjectionDistance() { return projectionDistance; }
    public void cycleProjection() {
        projectionIndex = (projectionIndex + 1) % PROJECTION_PRESETS.length;
        projectionDistance = PROJECTION_PRESETS[projectionIndex];
    }
}
