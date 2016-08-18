package ZvizX;

import processing.core.PApplet;
import toxi.geom.Vec3D;
import toxi.physics.VerletPhysics;
import toxi.physics.VerletSpring;

import java.util.ArrayList;

public class Cluster {
    public  PApplet pApplet;
    public  ArrayList<Node> nodes = new ArrayList<>();

    public Cluster(PApplet pApplet, VerletPhysics verletPhysics, int amount, float diameter, Vec3D center) {
        this.pApplet = pApplet;
        for (int i = 0; i < amount; i++) {
            nodes.add(new Node(pApplet, center.add(Vec3D.randomVector())));
        }
        for (int i = 0; i < nodes.size(); i++) {
            Node ni = nodes.get(i);
            for (Node nj : nodes) {
                verletPhysics.addSpring(new VerletSpring(ni, nj, diameter, 0.01f));
            }
        }
    }

    public void showText(AnimatedText animatedText) {
        int index = pApplet.mouseX / 500;
        Node particle = nodes.get(index);
        animatedText.writingOut(particle);
    }

    public void showLines() {
        for (Node n : nodes) {
            for (int i = 0; i < nodes.size(); i++) {
                Node pi = nodes.get(i);
                for (Node pj : nodes) {
                    n.showLines(pi, pj);
                }
            }
        }
    }

    public void showInsideShape() {
        for (Node n : nodes) {
            for (int i = 0; i < nodes.size(); i++) {
                Node pi = nodes.get(i);
                for (Node pj : nodes) {
                    n.showInsideShape(pi, pj);
                }
            }
        }
    }

    public void showOutsideShape() {
        for (Node n : nodes) {
            for (int i = 0; i < nodes.size(); i++) {
                Node pi = nodes.get(i);
                for (int j = 0; j < nodes.size(); j++) {
                    Node pj = nodes.get(j);
                    for (Node pk : nodes) {
                        n.showOutsideShape(pi, pj, pk);
                    }
                }
            }
        }
    }
}

