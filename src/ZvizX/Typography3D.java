package ZvizX;

import geomerative.RFont;
import geomerative.RMesh;
import geomerative.RPoint;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import wblut.geom.WB_Plane;
import wblut.geom.WB_Point;
import wblut.geom.WB_Triangle;
import wblut.geom.WB_Vector;
import wblut.hemesh.*;

import java.util.ArrayList;

class Typography3D {
    private PApplet pApplet;
    private RFont rFont;
    private ArrayList<HE_Mesh> he_Meshes;
    private int numIterations;
    private float initialOffset;

    public Typography3D(PApplet pApplet, RFont rFont, ArrayList<HE_Mesh> he_Meshes, int numIterations, float initialOffset) {
        this.pApplet = pApplet;
        this.rFont = rFont;
        this.he_Meshes = he_Meshes;
        this.numIterations = numIterations;
        this.initialOffset = initialOffset;
    }

    public HE_Mesh createHemeshFromString(String s) {
        RMesh rMesh = rFont.toGroup(s).toMesh();
        rMesh.translate(-rMesh.getWidth() / 2, rMesh.getHeight() / 2);
        ArrayList<WB_Triangle> wb_Triangles = new ArrayList<>();
        ArrayList<WB_Triangle> wb_TrianglesFlipped = new ArrayList<>();
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

    public void colorFaces(HE_Mesh he_Mesh) {
        pApplet.colorMode(PConstants.HSB, 1);
        for (HE_Face he_Face : he_Mesh.getFacesAsArray()) {
            WB_Point c = (WB_Point) he_Face.getFaceCenter();
            he_Face.setLabel(pApplet.color(PApplet.map(c.xf() + c.yf(), -500, 500, 0, 1), 1, 1));
        }
        pApplet.colorMode(PConstants.RGB, 255);
    }

    public void generateMeshes(HE_Mesh he_Mesh) {
        he_Meshes = new ArrayList<>();
        he_Meshes.add(he_Mesh);
        for (int i = 0; i < numIterations; i++) {
            he_Meshes = slice(he_Meshes, initialOffset);
        }
    }

    private ArrayList<HE_Mesh> slice(ArrayList<HE_Mesh> he_Meshes, float offset) {
        ArrayList<HE_Mesh> he_MeshesNew = new ArrayList<>();
        for (HE_Mesh he_Mesh : he_Meshes) {
            WB_Point center = (WB_Point) he_Mesh.getCenter();
            HEMC_SplitMesh hemc_SplitMesh = new HEMC_SplitMesh();
            hemc_SplitMesh.setMesh(he_Mesh);
            hemc_SplitMesh.setOffset(offset);
            hemc_SplitMesh.setPlane(new WB_Plane(center.xf(), center.xf(), center.xf(), pApplet.random(-1, 1), pApplet.random(-1, 1), pApplet.random(-1, 1)));
            HE_MeshCollection cells = hemc_SplitMesh.create();
            //java.util.Collections.addAll(he_MeshesNew, cells);
        }
        return he_MeshesNew;
    }

    public void move(ArrayList<HE_Mesh> he_Meshes, float offset) {
        for (HE_Mesh he_Mesh : he_Meshes) {
            WB_Point center = (WB_Point) he_Mesh.getCenter();
            center.normalizeSelf();
            center.mulSelf(offset);
            he_Mesh.move(center);
        }
    }

    public PShape createPShapeFromHemesh(HE_Mesh he_Mesh, boolean perVertexNormals) {
        he_Mesh.triangulate();
        int[][] facesHemesh = he_Mesh.getFacesAsInt();
        float[][] verticesHemesh = he_Mesh.getVerticesAsFloat();
        HE_Face[] faceArray = he_Mesh.getFacesAsArray();
        WB_Vector normal = null;
        WB_Vector[] vertexNormals = null;
        if (perVertexNormals) {
            vertexNormals = (WB_Vector[]) he_Mesh.getVertexNormals();
        }

        PShape pShape = pApplet.createShape();
        pShape.beginShape(PConstants.TRIANGLES);
        pShape.stroke(0, 125);
        pShape.strokeWeight(0.5f);
        for (int i = 0; i < facesHemesh.length; i++) {
            if (!perVertexNormals) {
                normal = (WB_Vector) faceArray[i].getFaceNormal();
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

