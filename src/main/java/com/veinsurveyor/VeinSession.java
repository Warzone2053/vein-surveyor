package com.veinsurveyor;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class VeinSession {
    private final int id;
    private String name;
    private final List<BlockPos> sampleBlocks = new ArrayList<>();
    private final List<Vec3d> samplePoints = new ArrayList<>();
    private VeinAnalysis cachedAnalysis;

    public VeinSession(int id, String name) {
        this.id = id;
        this.name = name;
        this.cachedAnalysis = new VeinAnalysis(this.samplePoints);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public synchronized boolean addSample(BlockPos pos) {
        if (sampleBlocks.contains(pos)) {
            return false;
        }
        sampleBlocks.add(pos.toImmutable());
        samplePoints.add(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        this.cachedAnalysis = new VeinAnalysis(samplePoints);
        return true;
    }

    public synchronized boolean undoLast() {
        if (!samplePoints.isEmpty()) {
            sampleBlocks.remove(sampleBlocks.size() - 1);
            samplePoints.remove(samplePoints.size() - 1);
            this.cachedAnalysis = new VeinAnalysis(samplePoints);
            return true;
        }
        return false;
    }

    public synchronized void clear() {
        sampleBlocks.clear();
        samplePoints.clear();
        this.cachedAnalysis = new VeinAnalysis(samplePoints);
    }

    public synchronized List<BlockPos> getSampleBlocks() {
        return new ArrayList<>(sampleBlocks);
    }

    public synchronized List<Vec3d> getSamplePoints() {
        return new ArrayList<>(samplePoints);
    }

    public synchronized VeinAnalysis getAnalysis() {
        return cachedAnalysis;
    }

    public synchronized int getSampleCount() {
        return sampleBlocks.size();
    }
}
