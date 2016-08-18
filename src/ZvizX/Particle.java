package ZvizX;

import processing.core.PApplet;
import processing.core.PConstants;
import toxi.geom.Vec3D;

import java.util.ArrayList;

class Particle {
    private PApplet pApplet;
    Vec3D location;
    private Vec3D acceleration;
    private Vec3D speed;
    private Vec3D gravity;

    public Particle(PApplet pApplet, Vec3D location, Vec3D acceleration, Vec3D speed, Vec3D gravity) {
        this.pApplet = pApplet;
        this.location = location;
        this.acceleration = acceleration;
        this.speed = speed;
        this.gravity = gravity;
    }

    public void display() {
        pApplet.pushMatrix();
        pApplet.translate(location.x, location.y, location.z);
        pApplet.noFill();
        pApplet.stroke(255, 0, 0);
        pApplet.sphereDetail(10);
        pApplet.sphere(0.8f);
        pApplet.popMatrix();
    }

    public void move() {
        this.speed.addSelf(acceleration);
        this.speed.limit(8);
        location.addSelf(speed);
        this.acceleration.clear();
    }

    public void bounce() {
        if (location.x > pApplet.width) speed.x = speed.x * -1;
        if (location.x < 0) speed.x = speed.x * -1;
        if (location.y > pApplet.height) speed.y = speed.y * -1;
        if (location.y < 0) speed.y = speed.y * -1;
        if (location.z > pApplet.height) speed.z = speed.z * -1;
        if (location.z < 0) speed.z = speed.z * -1;
    }

    public void gravity() {
        this.speed.addSelf(gravity);
    }

    public void separate(ArrayList<Particle> particles, float separate, int minDistance) {
        Vec3D steer = new Vec3D();
        int count = 0;
        for (Particle particle : particles) {
            float distance = location.distanceTo(particle.location);
            if (distance > 0 && distance < minDistance) {
                Vec3D difference = location.sub(particle.location);
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

    public void cohesion(ArrayList<Particle> particles, float cohesion, int minDistance) {
        Vec3D sum = new Vec3D();
        int count = 0;
        for (Particle particle : particles) {
            float distance = location.distanceTo(particle.location);
            if (distance > 0 && distance < minDistance) {
                sum.addSelf(particle.location);
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

    public void align(ArrayList<Particle> particles, float align, int minDistance) {
        Vec3D steer = new Vec3D();
        int count = 0;
        for (Particle particle : particles) {
            float distance = location.distanceTo(particle.location);
            if (distance > 0 && distance < minDistance) {
                steer.addSelf(particle.speed);
                count++;
            }
        }
        if (count > 0) {
            steer.scaleSelf(1.0f / count);
        }
        steer.scaleSelf(align);
        acceleration.addSelf(steer);
    }

    public void lineBetween(ArrayList<Particle> particles, int minDistance) {
        for (Particle particle : particles) {
            float distance = location.distanceTo(particle.location);
            if (distance > 0 && distance < minDistance) {
                pApplet.stroke(255);
                pApplet.strokeWeight(0.5f);
                pApplet.line(location.x, location.y, location.z, particle.location.x, particle.location.y, particle.location.z);
            }
        }
    }

    public void shapeBetween(ArrayList<Particle> particles, int minDistance) {
        for (int i = 0; i < particles.size(); i++) {
            Particle particle1 = particles.get(i);
            float distance1 = location.distanceTo(particle1.location);
            for (int j = 0; j < particles.size() - 1; j++) {
                Particle particle2 = particles.get(j);
                float distance2 = location.distanceTo(particle2.location);
                if (distance1 > 0 && distance1 < minDistance && distance2 > 0 && distance2 < minDistance) {
                    pApplet.fill(150, 28, 32);
                    pApplet.beginShape();
                    pApplet.noStroke();
                    pApplet.vertex(location.x, location.y, location.z);
                    pApplet.vertex(particle1.location.x, particle1.location.y, particle1.location.z);
                    pApplet.vertex(particle2.location.x, particle2.location.y, particle2.location.z);
                    pApplet.endShape(PConstants.CLOSE);
                }
            }
        }
    }
}

