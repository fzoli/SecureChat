package org.dyndns.fzoli.ui;

import java.awt.Point;
import java.awt.Polygon;

public class RotateablePolygon extends Polygon {
    
    public RotateablePolygon(Point[] points) {
        this(createPoints(points, true), createPoints(points, false), points.length);
    }
    
    public RotateablePolygon(int[] xpoints, int[] ypoints, int npoints) {
        super(xpoints, ypoints, npoints);
    }

    public RotateablePolygon rotate(double angle, Point centre) {
        if (angle != 0) {

            int[] x = new int[npoints];
            int[] y = new int[npoints];

            double rad = (Math.PI / 180) * angle;
            float xOld, yOld;
            
            for(int i = 0; i < npoints; i++) {

                xOld = xpoints[i] - centre.x;
                yOld = ypoints[i] - centre.y;

                x[i] = (int)(centre.x +
                             xOld * Math.cos(rad) -
                             yOld * Math.sin(rad));

                y[i] = (int)(centre.y +
                             xOld * Math.sin(rad) +
                             yOld * Math.cos(rad));

            }

            xpoints = x;
            ypoints = y;

        }
        return this;
    }
    
    private static int[] createPoints(Point[] points, boolean x) {
        int[] pts = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            pts[i] = x ? points[i].x : points[i].y;
        }
        return pts;
    }

}