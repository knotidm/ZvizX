package ZvizX;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import saito.objloader.Face;
import saito.objloader.OBJModel;
import saito.objloader.Segment;

public class AnimatedShape {
    public PApplet pApplet;
    public OBJModel model;
    public PVector position = new PVector(0, 0, 0);
    public PVector direction = new PVector(0, 0, 5); // kierunek i szybkość poruszania się obszaru wyświetlania x y z

    public AnimatedShape(PApplet pApplet, String s) {
        this.pApplet = pApplet;
        model = new OBJModel(pApplet, s, "relative", PConstants.POLYGON);
        model.scale(200);
        model.translateToCenter();
    }

    public void changeMode() {
        if (pApplet.key == '1') {
            pApplet.noFill();
            model.shapeMode(PConstants.POINTS);
        } else if (pApplet.key == '2') {
            pApplet.noFill();
            model.shapeMode(PConstants.LINES);
        } else if (pApplet.key == '3') {
            model.shapeMode(PConstants.POLYGON);
        }
    }

    public void display() {
        changeMode();
        model.draw();
    }

    public void moveNormals() {
        changeMode();
        pApplet.pushMatrix();
        for (int j = 0; j < model.getSegmentCount(); j++) {
            Segment segment = model.getSegment(j);
            Face[] faces = segment.getFaces();
            pApplet.beginShape(PConstants.QUADS);
            for (int i = 0; i < faces.length; i++) {
                PVector[] v = faces[i].getVertices();
                PVector n = faces[i].getNormal();
                float nor = PApplet.abs(PApplet.sin(PApplet.radians((pApplet.frameCount + i))) * 100);
                for (int k = 0; k < v.length; k++) {
                    pApplet.vertex(v[k].x + (n.x * nor), v[k].y + (n.y * nor), v[k].z + (n.z * nor));
                }
            }
            pApplet.endShape();
        }
        pApplet.popMatrix();
    }

    public void displayByArea(DisplayArea area) {
        changeMode();
        position.add(direction);
        if (position.z > 300 || position.z < -300) { // ograniczenie poruszania się obszaru wyświetlania
            direction.mult(-1);
        }
        area.moveCenterTo(position);
        pApplet.stroke(255, 0, 0);
        pApplet.pushMatrix();
        for (int j = 0; j < model.getSegmentCount(); j++) {
            Segment segment = model.getSegment(j);
            Face[] faces = segment.getFaces();
            pApplet.beginShape(PConstants.QUADS);
            for (int i = 0; i < faces.length; i++) {
                Face f = faces[i];
                if (area.isColliding(f.getCenter())) {
                    PVector[] v = f.getVertices();
                    PVector[] n = f.getNormals();
                    for (int k = 0; k < v.length; k++) {
                        pApplet.normal(n[k].x, n[k].y, n[k].z);
                        pApplet.vertex(v[k].x, v[k].y, v[k].z);
                    }
                }
            }
            pApplet.endShape();
        }
        pApplet.popMatrix();
    }
}