package com.veintracer;

import net.minecraft.util.math.Vec3d;
import java.util.List;

public class VeinAnalysis {
    public final int sampleCount;
    public final Vec3d centroid;
    public final Vec3d direction; // Normalized dominant eigenvector
    public final double spanLength;
    public final double minT;
    public final double maxT;
    public final double linearDensity; // ores per meter
    public final double expectedGap; // average distance between ores (m)
    public final double warningDistance; // 2.0 * expectedGap
    public final double cutoffDistance; // 3.5 * expectedGap
    public final double avgScatterRadius;
    public final double maxScatterRadius;
    public final double fitConfidence; // R^2 approximation
    public final double yaw; // degrees
    public final double pitch; // degrees
    public final String compassHeading;

    public VeinAnalysis(List<Vec3d> points) {
        this.sampleCount = points.size();

        if (sampleCount < 2) {
            this.centroid = sampleCount == 1 ? points.get(0) : Vec3d.ZERO;
            this.direction = new Vec3d(0, 0, 1);
            this.spanLength = 0;
            this.minT = 0;
            this.maxT = 0;
            this.linearDensity = 0;
            this.expectedGap = 0;
            this.warningDistance = 0;
            this.cutoffDistance = 0;
            this.avgScatterRadius = 0;
            this.maxScatterRadius = 0;
            this.fitConfidence = 0;
            this.yaw = 0;
            this.pitch = 0;
            this.compassHeading = "N/A";
            return;
        }

        // 1. Calculate Centroid
        double sumX = 0, sumY = 0, sumZ = 0;
        for (Vec3d p : points) {
            sumX += p.x;
            sumY += p.y;
            sumZ += p.z;
        }
        this.centroid = new Vec3d(sumX / sampleCount, sumY / sampleCount, sumZ / sampleCount);

        // 2. Covariance Matrix
        double cXX = 0, cYY = 0, cZZ = 0;
        double cXY = 0, cXZ = 0, cYZ = 0;

        for (Vec3d p : points) {
            double dx = p.x - centroid.x;
            double dy = p.y - centroid.y;
            double dz = p.z - centroid.z;

            cXX += dx * dx;
            cYY += dy * dy;
            cZZ += dz * dz;
            cXY += dx * dy;
            cXZ += dx * dz;
            cYZ += dy * dz;
        }

        cXX /= sampleCount;
        cYY /= sampleCount;
        cZZ /= sampleCount;
        cXY /= sampleCount;
        cXZ /= sampleCount;
        cYZ /= sampleCount;

        // 3. Dominant Eigenvector via Power Iteration
        Vec3d v = new Vec3d(1, 1, 1).normalize();
        for (int i = 0; i < 20; i++) {
            double nx = cXX * v.x + cXY * v.y + cXZ * v.z;
            double ny = cXY * v.x + cYY * v.y + cYZ * v.z;
            double nz = cXZ * v.x + cYZ * v.y + cZZ * v.z;
            double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len < 1e-9) break;
            v = new Vec3d(nx / len, ny / len, nz / len);
        }

        // Orient direction forward along majority of points
        Vec3d first = points.get(0);
        Vec3d last = points.get(sampleCount - 1);
        if (last.subtract(first).dotProduct(v) < 0) {
            v = v.multiply(-1.0);
        }
        this.direction = v;

        // 4. 1D Projections and Span
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double totalVariance = 0;
        double lineVariance = 0;
        double totalScatter = 0;
        double maxScatter = 0;

        for (Vec3d p : points) {
            Vec3d diff = p.subtract(centroid);
            double t = diff.dotProduct(direction);
            min = Math.min(min, t);
            max = Math.max(max, t);

            double distSq = diff.lengthSquared();
            totalVariance += distSq;
            lineVariance += t * t;

            double perpDist = Math.sqrt(Math.max(0, distSq - (t * t)));
            totalScatter += perpDist;
            maxScatter = Math.max(maxScatter, perpDist);
        }

        this.minT = min;
        this.maxT = max;
        this.spanLength = Math.max(0.1, max - min);
        this.linearDensity = sampleCount / this.spanLength;
        this.expectedGap = this.spanLength / (sampleCount - 1);
        this.warningDistance = Math.max(3.0, expectedGap * 2.0);
        this.cutoffDistance = Math.max(5.0, expectedGap * 3.5);
        this.avgScatterRadius = totalScatter / sampleCount;
        this.maxScatterRadius = maxScatter;

        // R^2 fit confidence
        this.fitConfidence = totalVariance > 1e-6 ? Math.min(1.0, lineVariance / totalVariance) : 1.0;

        // 5. Yaw, Pitch, Compass Heading
        double degYaw = Math.toDegrees(Math.atan2(-direction.x, direction.z));
        this.yaw = (degYaw % 360 + 360) % 360;
        this.pitch = Math.toDegrees(Math.asin(-direction.y));
        this.compassHeading = formatCompass(this.yaw, this.pitch);
    }

    public Vec3d getClusterStart() {
        return centroid.add(direction.multiply(minT));
    }

    public Vec3d getClusterEnd() {
        return centroid.add(direction.multiply(maxT));
    }

    public Vec3d getProjectedStart(double distance) {
        return centroid.add(direction.multiply(minT - distance));
    }

    public Vec3d getProjectedEnd(double distance) {
        return centroid.add(direction.multiply(maxT + distance));
    }

    public Vec3d getForwardWarningPoint() {
        return centroid.add(direction.multiply(maxT + warningDistance));
    }

    public Vec3d getForwardCutoffPoint() {
        return centroid.add(direction.multiply(maxT + cutoffDistance));
    }

    public Vec3d getBackwardWarningPoint() {
        return centroid.add(direction.multiply(minT - warningDistance));
    }

    public Vec3d getBackwardCutoffPoint() {
        return centroid.add(direction.multiply(minT - cutoffDistance));
    }

    private static String formatCompass(double yaw, double pitch) {
        String[] directions = {"S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE"};
        int idx = (int) Math.round(yaw / 22.5) % 16;
        return String.format("%s (%.1f°, Pitch %+.1f°)", directions[idx], yaw, pitch);
    }
}
