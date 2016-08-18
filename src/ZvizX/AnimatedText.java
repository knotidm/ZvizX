package ZvizX;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import toxi.geom.Vec3D;
import toxi.physics.VerletParticle;

import java.util.ArrayList;

public class AnimatedText extends Particle {
    public PApplet pApplet;
    public PFont pFont;
    public int textIndex;
    public String[] textArray = {
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
    public ArrayList<String> subText = new ArrayList<>();
    public int writerIndex;

    public AnimatedText(PApplet pApplet, Vec3D location, Vec3D acceleration, Vec3D speed, Vec3D gravity, PFont pFont, int textIndex) {
        super(pApplet, location, acceleration, speed, gravity);
        this.pApplet = pApplet;
        this.pFont = pFont;
        this.textIndex = textIndex;
    }

    public void display(VerletParticle particle) {
        location = particle;
        pApplet.hint(PConstants.DISABLE_DEPTH_TEST);
        pApplet.textFont(pFont, 32);
        pApplet.textAlign(PConstants.CENTER);
        pApplet.fill(50, 89, 162);
        pApplet.pushMatrix();
        pApplet.translate(location.x, location.y, location.z);
        pApplet.text(textArray[textIndex], 0, 0, 0);
        pApplet.popMatrix();
    }

    public void writingOut(VerletParticle particle) {
        location = particle;
        for (int i = 0; i < textArray[textIndex].length() + 1; i++) {
            subText.add(textArray[textIndex].substring(0, i));
        }
        if (pApplet.millis() % 20 == 0) {
            writerIndex++;
            if (writerIndex == subText.size()) writerIndex = 0;
        }
        pApplet.hint(PConstants.DISABLE_DEPTH_TEST);
        pApplet.textFont(pFont, 32);
        pApplet.textAlign(PConstants.CENTER);
        pApplet.fill(50, 89, 162);
        pApplet.pushMatrix();
        pApplet.translate(location.x, location.y, location.z);
        pApplet.text(subText.get(writerIndex), 0, 0, 0);
        pApplet.popMatrix();
        subText.clear();
    }
}

