package ZvizX;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class DisplayArea {
    public  PApplet pApplet;
    public  PVector min = new PVector();
    public  PVector max = new PVector();

    public DisplayArea() {
        min.set(0, 0, 0);
        max.set(0, 0, 0);
    }

    public DisplayArea(PApplet pApplet, float x1, float y1, float z1, float x2, float y2, float z2) {
        this.pApplet = pApplet;
        reset();
        addPoint(x1, y1, z1);
        addPoint(x2, y2, z2);
    }

    public DisplayArea(PVector p1, PVector p2) {
        reset();
        addPoint(p1);
        addPoint(p2);
    }

    public void reset() {
        min.x = PConstants.MAX_FLOAT;
        min.y = PConstants.MAX_FLOAT;
        min.z = PConstants.MAX_FLOAT;
        max.x = PConstants.MIN_FLOAT;
        max.y = PConstants.MIN_FLOAT;
        max.z = PConstants.MIN_FLOAT;
    }

    public void addPoint(PVector point) {
        addPoint(point.x, point.y, point.z);
    }

    public void addPoint(float x, float y, float z) {
        if (x < min.x) min.x = x;
        if (x > max.x) max.x = x;
        if (y < min.y) min.y = y;
        if (y > max.y) max.y = y;
        if (z < min.z) min.z = z;
        if (z > max.z) max.z = z;
    }

    public void moveCenterTo(PVector p) {
        PVector a = new PVector(min.x, min.y, min.z);
        PVector b = new PVector(max.x, max.y, max.z);
        b.sub(a);
        b.div(2);
        b.add(min);
        PVector c = new PVector(p.x, p.y, p.z);
        c.sub(b);
        min.add(c);
        max.add(c);
    }

    public void display() {
        pApplet.pushStyle();
        pApplet.noFill();
        pApplet.stroke(255, 0, 255);
        pApplet.pushMatrix();
        PVector center = new PVector(max.x, max.y, max.z);
        center.sub(min);
        center.div(2);
        center.add(min);
        PVector whd = new PVector(max.x, max.y, max.z);
        whd.sub(min);
        pApplet.translate(center.x, center.y, center.z);
        pApplet.box(whd.x, whd.y, whd.z);
        pApplet.popMatrix();
        pApplet.popStyle();
    }

    public boolean isColliding(PVector p) {
        return isColliding(p.x, p.y, p.z);
    }

    public  boolean isColliding(float x, float y, float z) {
        return min.x <= x && max.x >= x && min.y <= y && max.y >= y && min.z <= z && max.z >= z;
    }
}