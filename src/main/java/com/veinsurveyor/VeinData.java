package com.veinsurveyor;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class VeinData {
    private static final VeinData INSTANCE = new VeinData();

    public static VeinData getInstance() {
        return INSTANCE;
    }

    private final List<BlockPos> sampleBlocks = new ArrayList<>();
    private final List<Vec3d> samplePoints = new ArrayList<>();
    private VeinAnalysis currentAnalysis = new VeinAnalysis(samplePoints);

    private boolean hudEnabled = true;
    private boolean renderEnabled = true;
    private double projectionDistance = 50.0; // default 50 blocks reach

    private VeinData() {}

    public synchronized boolean addSample(BlockPos pos) {
        if (sampleBlocks.contains(pos)) {
            return false;
        }
        sampleBlocks.add(pos.toImmutable());
        samplePoints.add(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        currentAnalysis = new VeinAnalysis(samplePoints);
        return true;
    }

    public synchronized boolean undoLast() {
        if (!samplePoints.isEmpty()) {
            sampleBlocks.remove(sampleBlocks.size() - 1);
            samplePoints.remove(samplePoints.size() - 1);
            currentAnalysis = new VeinAnalysis(samplePoints);
            return true;
        }
        return false;
    }

    public synchronized void clear() {
        sampleBlocks.clear();
        samplePoints.clear();
        currentAnalysis = new VeinAnalysis(samplePoints);
    }

    public synchronized List<BlockPos> getSampleBlocks() {
        return new ArrayList<>(sampleBlocks);
    }

    public synchronized List<Vec3d> getSamplePoints() {
        return new ArrayList<>(samplePoints);
    }

    public synchronized VeinAnalysis getAnalysis() {
        return currentAnalysis;
    }

    public boolean isHudEnabled() {
        return hudEnabled;
    }

    public void toggleHud() {
        this.hudEnabled = !this.hudEnabled;
    }

    public boolean isRenderEnabled() {
        return renderEnabled;
    }

    public void toggleRender() {
        this.renderEnabled = !this.renderEnabled;
    }

    public double getProjectionDistance() {
        return projectionDistance;
    }

    public void cycleProjection() {
        if (projectionDistance == 25.0) projectionDistance = 50.0;
        else if (projectionDistance == 50.0) projectionDistance = 100.0;
        else if (projectionDistance == 100.0) projectionDistance = 200.0;
        else projectionDistance = 25.0;
    }
}
