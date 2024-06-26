// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.utils;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MapView;

/**
 * Utility class to convert entities like {@link Bounds}  into {@link Shape}s that
 * can then easily be drawn on a {@link MapView}s {@link Graphics2D}-context.
 */
public final class MapViewGeometryUtil {
    private MapViewGeometryUtil() {
        // Private constructor to avoid instantiation
    }

    /**
     * Subtracts the download bounds from the rectangular bounds of the map view.
     *
     * @param mv       the MapView that is used for the LatLon-to-Point-conversion and that determines
     *             the Bounds from which the downloaded Bounds are subtracted
     * @param downloadBounds multiple {@link Bounds} objects that represent the downloaded area
     * @return the difference between the {@link MapView}s bounds and the downloaded area
     */
    public static Area getNonDownloadedArea(MapView mv, Iterable<Bounds> downloadBounds) {
        Rectangle b = mv.getBounds();
        // on some platforms viewport bounds seem to be offset from the left,
        // over-grow it just to be sure
        b.grow(100, 100);
        Area a = new Area(b);
        // now successively subtract downloaded areas
        for (Bounds bounds : downloadBounds) {
            Point p1 = mv.getPoint(bounds.getMin());
            Point p2 = mv.getPoint(bounds.getMax());
            Rectangle r = new Rectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.abs(p2.x - p1.x),
                    Math.abs(p2.y - p1.y));
            a.subtract(new Area(r));
        }
        return a;
    }

}
