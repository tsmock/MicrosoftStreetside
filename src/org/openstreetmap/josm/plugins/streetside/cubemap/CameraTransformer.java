// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cubemap;

import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * A transformer for the camera rotations
 */
public class CameraTransformer extends Group {

    public final Translate t = new Translate();
    private final Translate p = new Translate();
    private final Translate ip = new Translate();
    public final Rotate rx = new Rotate();
    public final Rotate ry = new Rotate();
    private final Rotate rz = new Rotate();
    private final Scale s = new Scale();

    /**
     * Create a new transformer
     */
    public CameraTransformer() {
        super();
        rx.setAxis(Rotate.X_AXIS);
        ry.setAxis(Rotate.Y_AXIS);
        rz.setAxis(Rotate.Z_AXIS);
        getTransforms().addAll(t, rz, ry, rx, s);
    }

    /**
     * Create a new transformer with a specific rotation
     * @param rotateOrder The order in which rotations will occur
     */
    public CameraTransformer(CameraTransformer.RotateOrder rotateOrder) {
        super();
        switch (rotateOrder) {
        case XYZ:
            getTransforms().addAll(t, p, rz, ry, rx, s, ip);
            break;
        case XZY:
            getTransforms().addAll(t, p, ry, rz, rx, s, ip);
            break;
        case YXZ:
            getTransforms().addAll(t, p, rz, rx, ry, s, ip);
            break;
        case YZX:
            getTransforms().addAll(t, p, rx, rz, ry, s, ip); // For Camera
            break;
        case ZXY:
            getTransforms().addAll(t, p, ry, rx, rz, s, ip);
            break;
        case ZYX:
            getTransforms().addAll(t, p, rx, ry, rz, s, ip);
            break;
        }
    }

    public void setTranslate(double x, double y, double z) {
        t.setX(x);
        t.setY(y);
        t.setZ(z);
    }

    public void setTranslate(double x, double y) {
        t.setX(x);
        t.setY(y);
    }

    public void setTx(double x) {
        t.setX(x);
    }

    public void setTy(double y) {
        t.setY(y);
    }

    public void setTz(double z) {
        t.setZ(z);
    }

    public void setRotate(double x, double y, double z) {
        rx.setAngle(x);
        ry.setAngle(y);
        rz.setAngle(z);
    }

    public void setRotateX(double x) {
        rx.setAngle(x);
    }

    public void setRotateY(double y) {
        ry.setAngle(y);
    }

    public void setRotateZ(double z) {
        rz.setAngle(z);
    }

    public void setRx(double x) {
        rx.setAngle(x);
    }

    public void setRy(double y) {
        ry.setAngle(y);
    }

    public void setRz(double z) {
        rz.setAngle(z);
    }

    public void setScale(double scaleFactor) {
        s.setX(scaleFactor);
        s.setY(scaleFactor);
        s.setZ(scaleFactor);
    }

    public void setScale(double x, double y, double z) {
        s.setX(x);
        s.setY(y);
        s.setZ(z);
    }

    public void setSx(double x) {
        s.setX(x);
    }

    public void setSy(double y) {
        s.setY(y);
    }

    public void setSz(double z) {
        s.setZ(z);
    }

    public void setPivot(double x, double y, double z) {
        p.setX(x);
        p.setY(y);
        p.setZ(z);
        ip.setX(-x);
        ip.setY(-y);
        ip.setZ(-z);
    }

    public void reset() {
        rx.setAngle(0.0);
        ry.setAngle(0.0);
        rz.setAngle(0.0);
        resetTSP();
    }

    public void resetTSP() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
        ip.setX(0.0);
        ip.setY(0.0);
        ip.setZ(0.0);
    }

    public enum RotateOrder {
        XYZ, XZY, YXZ, YZX, ZXY, ZYX
    }
}
