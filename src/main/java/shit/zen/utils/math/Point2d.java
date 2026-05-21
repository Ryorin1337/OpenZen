package shit.zen.utils.math;

public class Point2d {
    public double x;
    public double y;

    public Point2d() {
        this.x = 0.0;
        this.y = 0.0;
    }

    public Point2d(double d, double d2) {
        this.x = d;
        this.y = d2;
    }

    public Point2d(Point2d point2d) {
        this.x = point2d.x;
        this.y = point2d.y;
    }

    public Point2d(String string) {
        string = string.replace(" ", "");
        if (!string.contains(",")) {
            return;
        }
        String[] stringArray = string.split(",");
        if (stringArray.length <= 1) {
            return;
        }
        String string2 = stringArray[0];
        String string3 = stringArray[1];
        this.x = Double.parseDouble(string2.trim().replace("﻿", ""));
        this.y = Double.parseDouble(string3.trim().replace("﻿", ""));
    }

    public Point2d copy() {
        return new Point2d(this);
    }

    public Point2d scale(double d, double d2) {
        this.x *= d;
        this.y *= d2;
        return this;
    }

    public Point2d scale(double d) {
        return this.scale(d, d);
    }

    public Point2d scale(Point2d point2d) {
        return this.scale(point2d.x, point2d.y);
    }

    public Point2d add(double d, double d2) {
        this.x += d;
        this.y += d2;
        return this;
    }

    public Point2d add(Point2d point2d) {
        return this.add(point2d.x, point2d.y);
    }

    public Point2d add(double d) {
        return this.add(d, d);
    }

    public Point2d sub(double d, double d2) {
        this.x -= d;
        this.y -= d2;
        return this;
    }

    public Point2d sub(Point2d point2d) {
        return this.add(point2d.x, point2d.y);
    }

    public Point2d sub(double d) {
        return this.add(d, d);
    }

    public Point2d div(double d, double d2) {
        this.x /= d;
        this.y /= d2;
        return this;
    }

    public Point2d div(Point2d point2d) {
        return this.div(point2d.x, point2d.y);
    }

    public Point2d div(double d) {
        return this.div(d, d);
    }

    public Point2d set(double d, double d2) {
        this.x = d;
        this.y = d2;
        return this;
    }

    public Point2d set(Point2d point2d) {
        return this.set(point2d.x, point2d.y);
    }
}