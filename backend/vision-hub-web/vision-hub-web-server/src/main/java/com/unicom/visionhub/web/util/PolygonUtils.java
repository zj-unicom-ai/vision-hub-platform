package com.unicom.visionhub.web.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.List;


public class PolygonUtils {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private PolygonUtils() {
    }


    public static double computeOverlapRate(List<Double> box, List<List<Double>> polygon) {
        if (box == null || box.size() < 4 || polygon == null || polygon.isEmpty()) {
            return 0.0;
        }

        double x1 = box.get(0);
        double y1 = box.get(1);
        double x2 = box.get(2);
        double y2 = box.get(3);

        double boxArea = Math.abs((x2 - x1) * (y2 - y1));
        if (boxArea == 0.0) {
            return 0.0;
        }

        Geometry boxGeom = createBoxGeometry(x1, y1, x2, y2);
        Geometry polyGeom = createPolygonGeometry(polygon);

        if (polyGeom == null || !polyGeom.isValid()) {
            return 0.0;
        }

        Geometry intersection = boxGeom.intersection(polyGeom);
        double intersectArea = intersection.getArea();

        return Math.min(intersectArea / boxArea, 1.0);
    }


    public static boolean isFullScreenPolygon(List<List<Double>> polygon) {
        if (polygon == null || polygon.isEmpty()) {
            return true;
        }
        Geometry geom = createPolygonGeometry(polygon);
        if (geom == null || !geom.isValid()) {
            return false;
        }
        return geom.getArea() >= 0.99;
    }

    private static Geometry createBoxGeometry(double x1, double y1, double x2, double y2) {
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(x1, y1),
                new Coordinate(x2, y1),
                new Coordinate(x2, y2),
                new Coordinate(x1, y2),
                new Coordinate(x1, y1)
        };
        return GEOMETRY_FACTORY.createPolygon(coords);
    }

    private static Geometry createPolygonGeometry(List<List<Double>> polygon) {
        int n = polygon.size();
        if (n < 3) {
            return null;
        }
        Coordinate[] coords = new Coordinate[n + 1];
        for (int i = 0; i < n; i++) {
            List<Double> vertex = polygon.get(i);
            if (vertex == null || vertex.size() < 2) {
                return null;
            }
            coords[i] = new Coordinate(vertex.get(0), vertex.get(1));
        }
        coords[n] = coords[0];
        Polygon poly = GEOMETRY_FACTORY.createPolygon(coords);
        return poly.isValid() ? poly : poly.union();
    }
}
