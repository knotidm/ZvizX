package ZvizX;

import geomerative.RCommand;
import geomerative.RFont;
import geomerative.RG;
import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PShape;
import toxi.geom.AABB;
import toxi.geom.Vec3D;
import toxi.physics.VerletPhysics;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

import java.util.ArrayList;

public class Main extends PApplet {
    private VerletPhysics verletPhysics;
    private ArrayList<Particle> particles;
    private Cluster cluster;
    private AnimatedText animatedText;
    private WB_Render wb_Render;
    private HE_Mesh he_Mesh;
    private ArrayList<HE_Mesh> he_Meshes;
    String input = "TYPE";
    private Typography3D typography3D;

    private boolean exploding;
    private int background;
    private float separate;
    private float cohesion;
    private float align;
    private int minDistance;
    private float speedX;
    private float speedY;
    private float speedZ;
    private float accelerationX;
    private float accelerationY;
    private float accelerationZ;
    private float gravityX;
    private float gravityY;
    private float gravityZ;
    private int textIndex = 5;

    static public void main(String args[]) {
        PApplet.main(new String[]{"ZvizX.Main"});
    }

    public void settings() {
        size(1280, 720, P3D);
        smooth();
    }

    public void setup() {
        new PeasyCam(this, 100);
        verletPhysics = new VerletPhysics();
        verletPhysics.setWorldBounds(new AABB(new Vec3D(), 180));
        particles = new ArrayList<>();
        he_Meshes = new ArrayList<>();

        int particlesAmount = 100;
        for (int i = 0; i < particlesAmount; i++) {
            particles.add(new Particle(this, new Vec3D(random(width), random(height), random(height)), new Vec3D(accelerationX, accelerationY, accelerationZ), new Vec3D(speedX, speedY, speedZ), new Vec3D(gravityX, gravityY, gravityZ)));
        }

        cluster = new Cluster(this, verletPhysics, 4, 200, new Vec3D());
        PFont pFont = createFont("Penguin Sans", 32, true);
        animatedText = new AnimatedText(this, new Vec3D(), new Vec3D(), new Vec3D(), new Vec3D(), pFont, textIndex);

        RG.init(this);
        RCommand.setSegmentator(RCommand.UNIFORMSTEP);
        RCommand.setSegmentStep(2);
        RFont rFont = new RFont("Fonts/PenguinSans.ttf", 350);
        wb_Render = new WB_Render(this);
        int numIterations = 9;
        float initialOffset = 0;
        typography3D = new Typography3D(this, rFont, he_Meshes, numIterations, initialOffset);

        DisplayArea area = new DisplayArea(this, 0, -300, -15, 600, 300, 15);
    }

    public void draw() {
        background(background);
        lights();
        he_Mesh = typography3D.createHemeshFromString(animatedText.textArray[textIndex]);
        typography3D.colorFaces(he_Mesh);
        typography3D.generateMeshes(he_Mesh);

        verletPhysics.update();
        cluster.showLines();
        cluster.showInsideShape();
        cluster.showText(animatedText);

        PShape pShape = typography3D.createPShapeFromHemesh(he_Mesh, false);
        shape(pShape);

        float moveOffset = 0.35f;
        if (exploding) typography3D.move(he_Meshes, moveOffset);

        noStroke();
        for (HE_Mesh he_Mesh : he_Meshes) {
            for (HE_Face he_Face : he_Mesh.getFacesAsArray()) {
                int color = he_Face.getLabel();
                fill(color == -1 ? 255 : color);
                wb_Render.drawFace(he_Face, false);
            }
        }

        translate(-width / 2, -height / 2, -height / 2);
        for (Particle particle : particles) {
            particle.display();
            particle.move();
            particle.bounce();
            particle.gravity();
            particle.separate(particles, separate, minDistance);
            particle.cohesion(particles, cohesion, minDistance);
            particle.align(particles, align, minDistance);
            particle.lineBetween(particles, minDistance);
            particle.shapeBetween(particles, minDistance);
        }
    }

    public void mousePressed() {
        if (mouseButton == LEFT) {
            exploding = !exploding;
        } else if (mouseButton == RIGHT) {
            println("Resetting sketch.");
            exploding = false;
            typography3D.generateMeshes(he_Mesh);
        }
    }

    public void keyPressed() {
        if (key == 'n') {
            verletPhysics.clear();
            cluster = new Cluster(this, verletPhysics, (int) (random(3, 7)), random(1000), new Vec3D());
        }
    }
}