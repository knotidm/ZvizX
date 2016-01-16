package ZvizX;

import geomerative.*;
import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PShape;
import processing.core.PVector;
import saito.objloader.Face;
import saito.objloader.OBJModel;
import saito.objloader.Segment;
import toxi.geom.AABB;
import toxi.geom.Vec3D;
import toxi.physics.VerletParticle;
import toxi.physics.VerletPhysics;
import toxi.physics.VerletSpring;
import wblut.geom.WB_Plane;
import wblut.geom.WB_Point;
import wblut.geom.WB_Triangle;
import wblut.geom.WB_Vector;
import wblut.hemesh.*;
import wblut.processing.WB_Render;

import java.util.ArrayList;

public class Main extends PApplet {
    PeasyCam peasyCam;
    VerletPhysics verletPhysics;
    ArrayList<Particles> particles;
    Cluster cluster;
    AnimatedText animatedText;
    PFont pFont;
    PShape pShape;
    RFont rFont;
    WB_Render wb_Render;
    HE_Mesh he_Mesh;
    ArrayList<HE_Mesh> he_Meshes;
    String input = "TYPE";
    Typography3D typography3D;
    DisplayArea area;

    int numIterations = 9;
    float initialOffset = 0;
    float moveOffset = 0.35f;
    boolean exploding;
    int background;
    float separate;
    float cohesion;
    float align;
    int minDistance;
    float speedX, speedY, speedZ, accelerationX, accelerationY, accelerationZ, gravityX, gravityY, gravityZ;
    int textIndex = 5;

    public void setup() {
        peasyCam = new PeasyCam(this, 100);
        verletPhysics = new VerletPhysics();
        verletPhysics.setWorldBounds(new AABB(new Vec3D(), 180));
        particles = new ArrayList<Particles>();

        int particlesAmount = 100;
        for (int i = 0; i < particlesAmount; i++) {
            particles.add(new Particles(new Vec3D(random(width), random(height), random(height))));
        }

        cluster = new Cluster(4, 200, new Vec3D());
        animatedText = new AnimatedText(new Vec3D());
        pFont = createFont("Penguin Sans", 32, true);

        RG.init(this);
        RCommand.setSegmentator(RCommand.UNIFORMSTEP);
        RCommand.setSegmentStep(2);
        rFont = new RFont("Fonts/PenguinSans.ttf", 350);
        wb_Render = new WB_Render(this);
        typography3D = new Typography3D();

        area = new DisplayArea(0, -300, -15, 600, 300, 15); // rozmiar obszaru wyświetlania x1 y1 z1 x2 y2 z2
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
        cluster.showText();

        pShape = typography3D.createPShapeFromHemesh(he_Mesh, false);
        shape(pShape);

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
        for (Particles particle : particles) {
            particle.display();
            particle.move();
            particle.bounce();
            particle.gravity();
            particle.separate(separate, minDistance);
            particle.cohesion(cohesion, minDistance);
            particle.align(align, minDistance);
            particle.lineBetween(minDistance);
            particle.shapeBetween(minDistance);
        }
    }

    public void settings() {
        size(1280, 720, P3D);
        smooth();
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[]{"--window-color=#666666", "--stop-color=#cccccc", "ZvizX.Main"};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
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
            cluster = new Cluster((int) (random(3, 7)), random(1000), new Vec3D());
        }
    }

    public class Node extends VerletParticle {
        float area = mouseX * 0.01f;

        Node(Vec3D location) {
            super(location);
        }

        void colors(Node pi) {
            if (area <= pi.x / 2.0) {
                fill(50, 89, 162);
            }
            if (area > pi.x / 2.0) {
                fill(150, 28, 32);
            }
        }

        void showLines(Node pi, Node pj) {
            colors(pi);
            stroke(255);
            pushMatrix();
            line(pi.x, pi.y, pi.z, pj.x, pj.y, pj.z);
            popMatrix();
        }

        void showInsideShape(Node pi, Node pj) {
            colors(pi);
            noStroke();
            pushMatrix();
            beginShape();
            vertex(pi.x, pi.y, pi.z);
            vertex(pj.x, pj.y, pj.z);
            vertex(0, 0, 0);
            endShape(CLOSE);
            popMatrix();
        }

        void showOutsideShape(Node pi, Node pj, Node pk) {
            colors(pi);
            noStroke();
            pushMatrix();
            beginShape();
            vertex(pi.x, pi.y, pi.z);
            vertex(pj.x, pj.y, pj.z);
            vertex(pk.x, pk.y, pk.z);
            endShape(CLOSE);
            popMatrix();
        }
    }

    public class Cluster {
        ArrayList<Node> nodes = new ArrayList<Node>();
        float diameter;

        Cluster(int amount, float diameter, Vec3D center) {
            this.diameter = diameter;
            for (int i = 0; i < amount; i++) {
                nodes.add(new Node(center.add(Vec3D.randomVector())));
            }
            for (int i = 0; i < nodes.size(); i++) {
                Node ni = nodes.get(i);
                for (Node nj : nodes) {
                    verletPhysics.addSpring(new VerletSpring(ni, nj, diameter, 0.01f));
                }
            }
        }

        void showText() {
            int index = mouseX / 500;
            Node particle = nodes.get(index);
            animatedText.writingOut(particle);
        }

        void showLines() {
            for (Node n : nodes) {
                for (int i = 0; i < nodes.size(); i++) {
                    Node pi = nodes.get(i);
                    for (Node pj : nodes) {
                        n.showLines(pi, pj);
                    }
                }
            }
        }

        void showInsideShape() {
            for (Node n : nodes) {
                for (int i = 0; i < nodes.size(); i++) {
                    Node pi = nodes.get(i);
                    for (Node pj : nodes) {
                        n.showInsideShape(pi, pj);
                    }
                }
            }
        }

        void showOutsideShape() {
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

    public class Particles {
        Vec3D location = new Vec3D();
        Vec3D acceleration = new Vec3D(accelerationX, accelerationY, accelerationZ);
        Vec3D speed = new Vec3D(speedX, speedY, speedZ);
        Vec3D gravity = new Vec3D(gravityX, gravityY, gravityZ);

        Particles(Vec3D location) {
            this.location = location;
        }

        void display() {
            pushMatrix();
            translate(location.x, location.y, location.z);
            noFill();
            stroke(255, 0, 0);
            sphereDetail(10);
            sphere(0.8f);
            popMatrix();
        }

        void move() {
            this.speed.addSelf(acceleration);
            this.speed.limit(8);
            location.addSelf(speed);
            this.acceleration.clear();
        }

        void bounce() {
            if (location.x > width) speed.x = speed.x * -1;
            if (location.x < 0) speed.x = speed.x * -1;
            if (location.y > height) speed.y = speed.y * -1;
            if (location.y < 0) speed.y = speed.y * -1;
            if (location.z > height) speed.z = speed.z * -1;
            if (location.z < 0) speed.z = speed.z * -1;
        }

        void gravity() {
            this.speed.addSelf(gravity);
        }

        void separate(float separate, int minDistance) {
            Vec3D steer = new Vec3D();
            int count = 0;
            for (Particles other : particles) {
                float distance = location.distanceTo(other.location);
                if (distance > 0 && distance < minDistance) {
                    Vec3D difference = location.sub(other.location);
                    difference.normalizeTo(1.0f / distance);
                    steer.addSelf(difference);
                    count++;
                }
            }
            if (count > 0) {
                steer.scaleSelf(1.0f / count);
            }
            steer.scaleSelf(separate);
            acceleration.addSelf(steer);
        }

        void cohesion(float cohesion, int minDistance) {
            Vec3D sum = new Vec3D();
            int count = 0;
            for (Particles other : particles) {
                float distance = location.distanceTo(other.location);
                if (distance > 0 && distance < minDistance) {
                    sum.addSelf(other.location);
                    count++;
                }
            }
            if (count > 0) {
                sum.scaleSelf(1.0f / count);
            }
            Vec3D steer = sum.sub(location);
            steer.scaleSelf(cohesion * 0.1f);
            acceleration.addSelf(steer);
        }

        void align(float align, int minDistance) {
            Vec3D steer = new Vec3D();
            int count = 0;
            for (Particles other : particles) {
                float distance = location.distanceTo(other.location);
                if (distance > 0 && distance < minDistance) {
                    steer.addSelf(other.speed);
                    count++;
                }
            }
            if (count > 0) {
                steer.scaleSelf(1.0f / count);
            }
            steer.scaleSelf(align);
            acceleration.addSelf(steer);
        }

        void lineBetween(int minDistance) {
            for (Particles other : particles) {
                float distance = location.distanceTo(other.location);
                if (distance > 0 && distance < minDistance) {
                    stroke(255);
                    strokeWeight(0.5f);
                    line(location.x, location.y, location.z, other.location.x, other.location.y, other.location.z);
                }
            }
        }

        void shapeBetween(int minDistance) {
            for (int i = 0; i < particles.size(); i++) {
                Particles other1 = particles.get(i);
                float distance1 = location.distanceTo(other1.location);
                for (int j = 0; j < particles.size() - 1; j++) {
                    Particles other2 = particles.get(j);
                    float distance2 = location.distanceTo(other2.location);
                    if (distance1 > 0 && distance1 < minDistance && distance2 > 0 && distance2 < minDistance) {
                        fill(150, 28, 32);
                        beginShape();
                        noStroke();
                        vertex(location.x, location.y, location.z);
                        vertex(other1.location.x, other1.location.y, other1.location.z);
                        vertex(other2.location.x, other2.location.y, other2.location.z);
                        endShape(CLOSE);
                    }
                }
            }
        }
    }

    public class DisplayArea {
        PVector min = new PVector();
        PVector max = new PVector();

        DisplayArea() {
            min.set(0, 0, 0);
            max.set(0, 0, 0);
        }

        DisplayArea(float x1, float y1, float z1, float x2, float y2, float z2) {
            reset();
            addPoint(x1, y1, z1);
            addPoint(x2, y2, z2);
        }

        DisplayArea(PVector p1, PVector p2) {
            reset();
            addPoint(p1);
            addPoint(p2);
        }

        void reset() {
            min.x = MAX_FLOAT;
            min.y = MAX_FLOAT;
            min.z = MAX_FLOAT;
            max.x = MIN_FLOAT;
            max.y = MIN_FLOAT;
            max.z = MIN_FLOAT;
        }

        void addPoint(PVector point) {
            addPoint(point.x, point.y, point.z);
        }

        void addPoint(float x, float y, float z) {
            if (x < min.x) min.x = x;
            if (x > max.x) max.x = x;
            if (y < min.y) min.y = y;
            if (y > max.y) max.y = y;
            if (z < min.z) min.z = z;
            if (z > max.z) max.z = z;
        }

        void moveCenterTo(PVector p) {
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

        void display() {
            pushStyle();
            noFill();
            stroke(255, 0, 255);
            pushMatrix();
            PVector center = new PVector(max.x, max.y, max.z);
            center.sub(min);
            center.div(2);
            center.add(min);
            PVector whd = new PVector(max.x, max.y, max.z);
            whd.sub(min);
            translate(center.x, center.y, center.z);
            box(whd.x, whd.y, whd.z);
            popMatrix();
            popStyle();
        }

        boolean isColliding(PVector p) {
            return isColliding(p.x, p.y, p.z);
        }

        boolean isColliding(float x, float y, float z) {
            if (min.x > x) return false;
            if (max.x < x) return false;
            if (min.y > y) return false;
            if (max.y < y) return false;
            if (min.z > z) return false;
            if (max.z < z) return false;
            return true;
        }
    }

    public class AnimatedShape {
        OBJModel model;
        PVector position = new PVector(0, 0, 0);
        PVector direction = new PVector(0, 0, 5); // kierunek i szybkość poruszania się obszaru wyświetlania x y z

        AnimatedShape(String s, PApplet parent) {
            model = new OBJModel(parent, s, "relative", POLYGON);
            model.scale(200);
            model.translateToCenter();
        }

        public void changeMode() {
            if (key == '1') {
                noFill();
                model.shapeMode(POINTS);
            } else if (key == '2') {
                noFill();
                model.shapeMode(LINES);
            } else if (key == '3') {
                model.shapeMode(POLYGON);
            }
        }

        public void display() {
            changeMode();
            model.draw();
        }

        public void moveNormals() {
            changeMode();
            pushMatrix();
            for (int j = 0; j < model.getSegmentCount(); j++) {
                Segment segment = model.getSegment(j);
                Face[] faces = segment.getFaces();
                beginShape(QUADS);
                for (int i = 0; i < faces.length; i++) {
                    PVector[] v = faces[i].getVertices();
                    PVector n = faces[i].getNormal();
                    float nor = abs(sin(radians((frameCount + i))) * 100);
                    for (int k = 0; k < v.length; k++) {
                        vertex(v[k].x + (n.x * nor), v[k].y + (n.y * nor), v[k].z + (n.z * nor));
                    }
                }
                endShape();
            }
            popMatrix();
        }

        public void displayByArea() {
            changeMode();
            position.add(direction);
            if (position.z > 300 || position.z < -300) { // ograniczenie poruszania się obszaru wyświetlania
                direction.mult(-1);
            }
            area.moveCenterTo(position);
            stroke(255, 0, 0);
            pushMatrix();
            for (int j = 0; j < model.getSegmentCount(); j++) {
                Segment segment = model.getSegment(j);
                Face[] faces = segment.getFaces();
                beginShape(QUADS);
                for (int i = 0; i < faces.length; i++) {
                    Face f = faces[i];
                    if (area.isColliding(f.getCenter())) {
                        PVector[] v = f.getVertices();
                        PVector[] n = f.getNormals();
                        for (int k = 0; k < v.length; k++) {
                            normal(n[k].x, n[k].y, n[k].z);
                            vertex(v[k].x, v[k].y, v[k].z);
                        }
                    }
                }
                endShape();
            }
            popMatrix();
        }
    }

    public class AnimatedText extends Particles {
        String[] textArray = {
                "CRL STUDIOS",
                "THE FOURTH WAVELENGTH (FOUND)",
                "LUCIDSTATIC",
                "KOLANEK",
                "PANDORA'S BLACK BOOK",
                "GHEISTS",
                "BLAST RADIUS",
                "PAUL VENKAM",
                "CROCODILE TEARS",
                "[MAKINA]",
                "STATIS DEVICE",
                "TAPAGE",
                "CHAOSWYRM",
                "ENDIF",
                "RECFRAG",
                "MILIPEDE",
                "MASSAITH",
                "K-NOT",
                "IMPURFEKT",
                "L o W",
                "CLOUD ROOTS",
                "AERODRAMA",
                "HUMAN ERROR",
                "ARBITARIUM",
                "ECHO GRID",
                "MIND.DIVIDED",
                "GHOST LOOP",
                "MR PRECISE",
        };
        ArrayList<String> subText = new ArrayList<String>();
        int writerIndex;

        AnimatedText(Vec3D location) {
            super(location);
        }

        public void display(VerletParticle particle) {
            location = particle;
            hint(DISABLE_DEPTH_TEST);
            textFont(pFont, 32);
            textAlign(CENTER);
            fill(50, 89, 162);
            pushMatrix();
            translate(location.x, location.y, location.z);
            text(textArray[textIndex], 0, 0, 0);
            popMatrix();
        }

        public void writingOut(VerletParticle particle) {
            location = particle;
            for (int i = 0; i < textArray[textIndex].length() + 1; i++) {
                subText.add(textArray[textIndex].substring(0, i));
            }
            if (millis() % 20 == 0) {
                writerIndex++;
                if (writerIndex == subText.size()) writerIndex = 0;
            }
            hint(DISABLE_DEPTH_TEST);
            textFont(pFont, 32);
            textAlign(CENTER);
            fill(50, 89, 162);
            pushMatrix();
            translate(location.x, location.y, location.z);
            text(subText.get(writerIndex), 0, 0, 0);
            popMatrix();
            subText.clear();
        }
    }

    public class Typography3D {

        Typography3D() {
        }

        HE_Mesh createHemeshFromString(String s) {
            RMesh rMesh = rFont.toGroup(s).toMesh();
            rMesh.translate(-rMesh.getWidth() / 2, rMesh.getHeight() / 2);
            ArrayList<WB_Triangle> wb_Triangles = new ArrayList<WB_Triangle>();
            ArrayList<WB_Triangle> wb_TrianglesFlipped = new ArrayList<WB_Triangle>();
            RPoint[] rPoints;
            WB_Triangle wb_Triangle, wb_TriangleFlipped;
            WB_Point a, b, c;

            for (int i = 0; i < rMesh.strips.length; i++) {
                rPoints = rMesh.strips[i].getPoints();
                for (int j = 2; j < rPoints.length; j++) {
                    a = new WB_Point(rPoints[j - 2].x, rPoints[j - 2].y, 0);
                    b = new WB_Point(rPoints[j - 1].x, rPoints[j - 1].y, 0);
                    c = new WB_Point(rPoints[j].x, rPoints[j].y, 0);
                    if (j % 2 == 0) {
                        wb_Triangle = new WB_Triangle(a, b, c);
                        wb_TriangleFlipped = new WB_Triangle(c, b, a);
                    } else {
                        wb_Triangle = new WB_Triangle(c, b, a);
                        wb_TriangleFlipped = new WB_Triangle(a, b, c);
                    }
                    wb_Triangles.add(wb_Triangle);
                    wb_TrianglesFlipped.add(wb_TriangleFlipped);
                }
            }
            HE_Mesh he_Mesh = new HE_Mesh(new HEC_FromTriangles().setTriangles(wb_Triangles));
            he_Mesh.modify(new HEM_Extrude().setDistance(100));
            he_Mesh.add(new HE_Mesh(new HEC_FromTriangles().setTriangles(wb_TrianglesFlipped)));
            he_Mesh.clean();
            return he_Mesh;
        }

        void colorFaces(HE_Mesh he_Mesh) {
            colorMode(HSB, 1);
            for (HE_Face he_Face : he_Mesh.getFacesAsArray()) {
                WB_Point c = he_Face.getFaceCenter();
                he_Face.setLabel(color(map(c.xf() + c.yf(), -500, 500, 0, 1), 1, 1));
            }
            colorMode(RGB, 255);
        }

        void generateMeshes(HE_Mesh he_Mesh) {
            he_Meshes = new ArrayList<HE_Mesh>();
            he_Meshes.add(he_Mesh);
            for (int i = 0; i < numIterations; i++) {
                he_Meshes = slice(he_Meshes, initialOffset);
            }
        }

        ArrayList<HE_Mesh> slice(ArrayList<HE_Mesh> he_Meshes, float offset) {
            ArrayList<HE_Mesh> he_MeshesNew = new ArrayList<HE_Mesh>();
            for (HE_Mesh he_Mesh : he_Meshes) {
                WB_Point center = he_Mesh.getCenter();
                HEMC_SplitMesh hemc_SplitMesh = new HEMC_SplitMesh();
                hemc_SplitMesh.setMesh(he_Mesh);
                hemc_SplitMesh.setOffset(offset);
                hemc_SplitMesh.setPlane(new WB_Plane(center.xf(), center.xf(), center.xf(), random(-1, 1), random(-1, 1), random(-1, 1)));
                HE_Mesh[] cells = hemc_SplitMesh.create();
                java.util.Collections.addAll(he_MeshesNew, cells);
            }
            return he_MeshesNew;
        }

        void move(ArrayList<HE_Mesh> he_Meshes, float offset) {
            for (HE_Mesh he_Mesh : he_Meshes) {
                WB_Point center = he_Mesh.getCenter();
                center.normalizeSelf();
                center.mulSelf(offset);
                he_Mesh.move(center);
            }
        }

        PShape createPShapeFromHemesh(HE_Mesh he_Mesh, boolean perVertexNormals) {
            he_Mesh.triangulate();
            int[][] facesHemesh = he_Mesh.getFacesAsInt();
            float[][] verticesHemesh = he_Mesh.getVerticesAsFloat();
            HE_Face[] faceArray = he_Mesh.getFacesAsArray();
            WB_Vector normal = null;
            WB_Vector[] vertexNormals = null;
            if (perVertexNormals) {
                vertexNormals = he_Mesh.getVertexNormals();
            }

            PShape pShape = createShape();
            pShape.beginShape(TRIANGLES);
            pShape.stroke(0, 125);
            pShape.strokeWeight(0.5f);
            for (int i = 0; i < facesHemesh.length; i++) {
                if (!perVertexNormals) {
                    normal = faceArray[i].getFaceNormal();
                }
                pShape.fill(faceArray[i].getLabel());
                for (int j = 0; j < 3; j++) {
                    int index = facesHemesh[i][j];
                    float[] vertexHemesh = verticesHemesh[index];
                    if (perVertexNormals) {
                        normal = vertexNormals[index];
                    }
                    pShape.normal(normal.xf(), normal.yf(), normal.zf());
                    pShape.vertex(vertexHemesh[0], vertexHemesh[1], vertexHemesh[2]);
                }
            }
            pShape.endShape();
            return pShape;
        }
    }
}