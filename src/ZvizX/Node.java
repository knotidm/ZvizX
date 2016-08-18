package ZvizX;

import processing.core.PApplet;
import processing.core.PConstants;
import toxi.geom.Vec3D;
import toxi.physics.VerletParticle;

class Node extends VerletParticle {
    private PApplet pApplet;
    //public float area = pApplet.mouseX * 0.01f;

    public Node(PApplet pApplet, Vec3D location) {
        super(location);
        this.pApplet = pApplet;
    }

//    public void colors(Node pi) {
//        if (area <= pi.x / 2.0) {
//            pApplet.fill(50, 89, 162);
//        }
//        if (area > pi.x / 2.0) {
//            pApplet.fill(150, 28, 32);
//        }
//    }

    public void showLines(Node pi, Node pj) {
        //(pi);
        pApplet.stroke(255);
        pApplet.pushMatrix();
        pApplet.line(pi.x, pi.y, pi.z, pj.x, pj.y, pj.z);
        pApplet.popMatrix();
    }

    public void showInsideShape(Node pi, Node pj) {
        //colors(pi);
        pApplet.noStroke();
        pApplet.pushMatrix();
        pApplet.beginShape();
        pApplet.vertex(pi.x, pi.y, pi.z);
        pApplet.vertex(pj.x, pj.y, pj.z);
        pApplet.vertex(0, 0, 0);
        pApplet.endShape(PConstants.CLOSE);
        pApplet.popMatrix();
    }

    public void showOutsideShape(Node pi, Node pj, Node pk) {
        //colors(pi);
        pApplet.noStroke();
        pApplet.pushMatrix();
        pApplet.beginShape();
        pApplet.vertex(pi.x, pi.y, pi.z);
        pApplet.vertex(pj.x, pj.y, pj.z);
        pApplet.vertex(pk.x, pk.y, pk.z);
        pApplet.endShape(PConstants.CLOSE);
        pApplet.popMatrix();
    }
}

