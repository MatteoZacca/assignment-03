package pcd.ass03.boids;

import java.util.ArrayList;
import java.util.List;

public class BoidsModel {

    private final double width;
    private final double height;
    private final double maxSpeed;
    private final double perceptionRadius;
    private final double avoidRadius;

    private List<Boid> boids;
    private double separationWeight;
    private double alignmentWeight;
    private double cohesionWeight;

    public BoidsModel(int nboids,
                      double initialSeparationWeight,
                      double initialAlignmentWeight,
                      double initialCohesionWeight,
                      double width,
                      double height,
                      double maxSpeed,
                      double perceptionRadius,
                      double avoidRadius) {
        this.separationWeight = initialSeparationWeight;
        this.alignmentWeight = initialAlignmentWeight;
        this.cohesionWeight = initialCohesionWeight;
        this.width = width;
        this.height = height;
        this.maxSpeed = maxSpeed;
        this.perceptionRadius = perceptionRadius;
        this.avoidRadius = avoidRadius;

        generateBoids(nboids);
    }

    public void generateBoids(int nboids) {
        this.boids = new ArrayList<>();
        for (int i = 0; i < nboids; i++) {
            P2d pos = new P2d(Math.random() * width - width / 2, Math.random() * height - height / 2);
            V2d vel = new V2d(Math.random() * maxSpeed / 2 - maxSpeed / 4, Math.random() * maxSpeed / 2 - maxSpeed / 4);
            boids.add(new Boid(pos, vel));
        }
    }

    public List<Boid> getBoids() {
        return this.boids;
    }

    public void setBoids(List<Boid> boids) {
        this.boids = boids;
    }

    public void setBoidsNumber(int boidsNumber) {
        generateBoids(boidsNumber);
    }

    public double getMinX() {
        return -width / 2;
    }

    public double getMaxX() {
        return width / 2;
    }

    public double getMinY() {
        return -height / 2;
    }

    public double getMaxY() {
        return height / 2;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setSeparationWeight(double value) {
        this.separationWeight = value;
    }

    public void setAlignmentWeight(double value) {
        this.alignmentWeight = value;
    }

    public void setCohesionWeight(double value) {
        this.cohesionWeight = value;
    }

    public double getSeparationWeight() {
        return separationWeight;
    }

    public double getAlignmentWeight() {
        return alignmentWeight;
    }

    public double getCohesionWeight() {
        return cohesionWeight;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getAvoidRadius() {
        return avoidRadius;
    }

    public double getPerceptionRadius() {
        return perceptionRadius;
    }

}
