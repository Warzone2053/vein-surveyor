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

    private final List<VeinSession> sessions = new ArrayList<>();
    private int activeSessionIndex = 0;
    private int nextSessionId = 1;

    private boolean hudEnabled = true;
    private boolean renderEnabled = true;
    private double projectionDistance = 50.0; // default 50 blocks reach

    private VeinData() {
        // Initialize with default first session
        sessions.add(new VeinSession(nextSessionId++, "Vein 1"));
    }

    public synchronized VeinSession getActiveSession() {
        if (sessions.isEmpty()) {
            sessions.add(new VeinSession(nextSessionId++, "Vein 1"));
            activeSessionIndex = 0;
        }
        if (activeSessionIndex < 0 || activeSessionIndex >= sessions.size()) {
            activeSessionIndex = 0;
        }
        return sessions.get(activeSessionIndex);
    }

    public synchronized List<VeinSession> getAllSessions() {
        return new ArrayList<>(sessions);
    }

    public synchronized int getActiveSessionIndex() {
        return activeSessionIndex;
    }

    public synchronized int getSessionCount() {
        return sessions.size();
    }

    public synchronized VeinSession newSession() {
        VeinSession newSession = new VeinSession(nextSessionId, "Vein " + nextSessionId);
        nextSessionId++;
        sessions.add(newSession);
        activeSessionIndex = sessions.size() - 1;
        return newSession;
    }

    public synchronized VeinSession nextSession() {
        if (sessions.size() <= 1) return getActiveSession();
        activeSessionIndex = (activeSessionIndex + 1) % sessions.size();
        return getActiveSession();
    }

    public synchronized VeinSession previousSession() {
        if (sessions.size() <= 1) return getActiveSession();
        activeSessionIndex = (activeSessionIndex - 1 + sessions.size()) % sessions.size();
        return getActiveSession();
    }

    public synchronized boolean addSample(BlockPos pos) {
        return getActiveSession().addSample(pos);
    }

    public synchronized boolean undoLast() {
        return getActiveSession().undoLast();
    }

    public synchronized void clear() {
        getActiveSession().clear();
    }

    public synchronized List<BlockPos> getSampleBlocks() {
        return getActiveSession().getSampleBlocks();
    }

    public synchronized List<Vec3d> getSamplePoints() {
        return getActiveSession().getSamplePoints();
    }

    public synchronized VeinAnalysis getAnalysis() {
        return getActiveSession().getAnalysis();
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
