package bogomolov.aa.fitrack.model;

public class Point {
    private long time;
    private double x;
    private double y;

    public Point(long time, double x, double y) {
        this.time = time;
        this.x = x;
        this.y = y;
    }

    public long getTime() {
        return time;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
