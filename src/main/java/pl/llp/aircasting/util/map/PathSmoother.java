package pl.llp.aircasting.util.map;

import com.google.android.maps.GeoPoint;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 2/27/12
 * Time: 4:41 PM
 */
public class PathSmoother {
    private static final double DIST_CUTOFF = 50;

    private List<GeoPoint> points;
    private boolean[] keep;

    public List<GeoPoint> getSmoothed(List<GeoPoint> geoPoints) {
        points = newArrayList(geoPoints);
        keep = new boolean[this.points.size()];
        ramerDouglasPeucke(0, keep.length - 1);

        List<GeoPoint> result = newArrayList();
        for (int i = 0; i < keep.length; i++) {
            if (keep[i]) {
                result.add(this.points.get(i));
            }
        }

        return result;
    }

    private void ramerDouglasPeucke(int left, int right) {
        if (right - left < 2) return;
        keep[left] = keep[right] = true;

        double best = perpendicularDist(points.get(left + 1), points.get(left), points.get(right));
        int bestIndex = left + 1;
        for (int i = left + 2; i < right; i++) {
            double dist = perpendicularDist(points.get(i), points.get(left), points.get(right));
            if (dist > best) {
                best = dist;
                bestIndex = i;
            }
        }

        if (best > DIST_CUTOFF) {
            ramerDouglasPeucke(left, bestIndex);
            ramerDouglasPeucke(bestIndex, right);
        }
    }

    private double perpendicularDist(GeoPoint from, GeoPoint to1, GeoPoint to2) {
        double x1 = from.getLongitudeE6();
        double y1 = from.getLatitudeE6();
        double x2 = to1.getLongitudeE6();
        double y2 = to1.getLatitudeE6();
        double x3 = to2.getLongitudeE6();
        double y3 = to2.getLatitudeE6();

        // Line equation of a straight line going through (x2, y2) and (x3, y3):
        // x(y3 - y2) + y(x2 - x3) - x2y3 + y2x3
        double a = y3 - y2;
        double b = x2 - x3;
        double c = -x2 * y3 + y2 * x3;

        // Perpendicular distance of (x1,y1) to L: ax + by + c = 0
        return Math.abs(a * x1 + b * y1 + c) / Math.sqrt(a * a + b * b);
    }
}
