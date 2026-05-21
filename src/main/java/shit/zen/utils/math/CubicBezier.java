package shit.zen.utils.math;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import shit.zen.utils.math.AbstractEasing;
import shit.zen.utils.math.Point2d;

public class CubicBezier
extends AbstractEasing {
    private final Point2d localP1;
    private final Point2d localP2;
    private final List<Point2d> lookupTable = new ArrayList<>();
    private int sampleCount = 0;

    public CubicBezier() {
        this.localP1 = new Point2d(0.0, 0.0);
        this.localP2 = new Point2d(1.0, 1.0);
        this.setSampleCount(30);
    }

    public CubicBezier(int n) {
        this();
        this.setSampleCount(n);
    }

    public CubicBezier(Point2d point2d, Point2d point2d2) {
        this.localP1 = point2d;
        this.localP2 = point2d2;
        this.setSampleCount(30);
    }

    public CubicBezier(Point2d point2d, Point2d point2d2, int n) {
        this(point2d, point2d2);
        this.setSampleCount(n);
    }

    public CubicBezier(CubicBezier cubicBezier) {
        this.localP1 = cubicBezier.getP1();
        this.localP2 = cubicBezier.getP2();
        this.setSampleCount(30);
    }

    public CubicBezier(CubicBezier cubicBezier, int n) {
        this(cubicBezier);
        this.setSampleCount(n);
    }

    public CubicBezier(String string) {
        String[] stringArray = string.replace(" ", "").split(",");
        if (stringArray.length != 4) {
            throw new IllegalArgumentException("Couldn't parse " + string + ", please follow this format: x1,y1,x2,y2");
        }
        this.localP1 = new Point2d(stringArray[0] + "," + stringArray[1]);
        this.localP2 = new Point2d(stringArray[2] + "," + stringArray[3]);
        this.setSampleCount(30);
    }

    public CubicBezier(String string, int n) {
        this(string);
        this.setSampleCount(n);
    }

    private void buildLookupTable() {
        if (this.localP1 == null || this.localP2 == null) {
            return;
        }
        this.lookupTable.clear();
        double d = 0.03333333333333333;
        for (double d2 = 0.0; d2 <= 1.0; d2 += d) {
            Point2d point2d = this.computeBezier(d2);
            this.lookupTable.add(new Point2d(point2d.x, 1.0).sub(0.0, point2d.y));
        }
        this.lookupTable.add(new Point2d(1.0, 0.0));
    }

    private Point2d computeBezier(double d) {
        if (this.localP1 == null || this.localP2 == null) {
            throw new NullPointerException("firstPoint or secondPoint is null");
        }
        Point2d point2d = this.localP1.copy();
        Point2d point2d2 = this.localP2.copy();
        double d2 = 1.0 - d;
        return new Point2d(this.p1.x * Math.pow(d2, 3.0) + 3.0 * point2d.x * d * Math.pow(d2, 2.0) + 3.0 * point2d2.x * Math.pow(d, 2.0) * d2 + this.p2.x * Math.pow(d, 3.0), this.p1.y * Math.pow(d2, 3.0) + 3.0 * point2d.y * d * Math.pow(d2, 2.0) + 3.0 * point2d2.y * Math.pow(d, 2.0) * d2 + this.p2.y * Math.pow(d, 3.0));
    }

    private Map.Entry<Point2d, Point2d> findClosestEntry(double d) {
        if (this.lookupTable.isEmpty()) {
            return new AbstractMap.SimpleEntry(new Point2d(0.0, 0.0), new Point2d(0.0, 0.0));
        }
        Point2d point2d = this.lookupTable.get(0);
        Point2d point2d2 = this.lookupTable.get(this.lookupTable.size() - 1);
        for (Point2d point2d3 : this.lookupTable) {
            if (point2d3.x < d) {
                point2d = point2d3;
                continue;
            }
            if (!(point2d3.x > d) || !(point2d2.x >= point2d3.x)) continue;
            point2d2 = point2d3;
            break;
        }
        if (point2d2.x < d) {
            point2d2 = point2d;
        }
        if (point2d.x > d) {
            point2d = point2d2;
        }
        return new AbstractMap.SimpleEntry(point2d, point2d2);
    }

    @Override
    public double ease(double d) {
        Point2d point2d;
        if (this.localP1 == null || this.localP2 == null) {
            return 0.0;
        }
        Map.Entry<Point2d, Point2d> entry = this.findClosestEntry(d);
        Point2d point2d2 = entry.getKey();
        if (point2d2.equals(point2d = entry.getValue())) {
            return 1.0 - point2d2.y;
        }
        double d2 = (point2d.y - point2d2.y) / (point2d.x - point2d2.x) * (d - point2d2.x) + point2d2.y;
        return 1.0 - d2;
    }

    public Point2d getP1() {
        return this.localP1.copy();
    }

    public Point2d getP2() {
        return this.localP2.copy();
    }

    public List<Point2d> getLookupTable() {
        return Collections.unmodifiableList(this.lookupTable);
    }

    public int getSampleCount() {
        return this.sampleCount;
    }

    public void setSampleCount(int n) {
        if (this.sampleCount == n) {
            return;
        }
        this.sampleCount = n;
        this.buildLookupTable();
    }

    public CubicBezier copy() {
        return new CubicBezier(this);
    }
}