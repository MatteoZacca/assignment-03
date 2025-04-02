package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsModel {
    
    private final List<Boid> boids;
    private double separationWeight; 
    private double alignmentWeight; 
    private double cohesionWeight; 
    private final double width;
    private final double height;
    private final double maxSpeed;
    private final double perceptionRadius;
    private final double avoidRadius;
    private boolean isModelPaused = true;

    public BoidsModel(int nboids,  
    						double initialSeparationWeight, 
    						double initialAlignmentWeight, 
    						double initialCohesionWeight,
    						double width, 
    						double height,
    						double maxSpeed,
    						double perceptionRadius,
    						double avoidRadius){
        separationWeight = initialSeparationWeight;
        alignmentWeight = initialAlignmentWeight;
        cohesionWeight = initialCohesionWeight;
        this.width = width;
        this.height = height;
        this.maxSpeed = maxSpeed;
        this.perceptionRadius = perceptionRadius;
        this.avoidRadius = avoidRadius;

        /* Il ciclo for riempie la lista di boids con un numero di boid specificato dalla
        variabile nboids (1500). A ciascuno di questi viene assegnata:
        - posizione iniziale casuale all'interno dei limiti definiti dalla larghezza e
        dall'altezza dell'ambiente simulato
        - velocità iniziale casuale */
    	boids = new ArrayList<>();
        for (int i = 0; i < nboids; i++) {
        	P2d pos = new P2d(-width/2 + Math.random() * width, -height/2 + Math.random() * height);
        	V2d vel = new V2d(Math.random() * maxSpeed/2 - maxSpeed/4, Math.random() * maxSpeed/2 - maxSpeed/4);
        	boids.add(new Boid(pos, vel));
        }

    }
    
    public synchronized List<Boid> getBoids(){
    	return boids;
    }
    
    public synchronized double getMinX() {
    	return -width/2;
    }

    public synchronized double getMaxX() {
    	return width/2;
    }

    public synchronized double getMinY() {
    	return -height/2;
    }

    public synchronized double getMaxY() {
    	return height/2;
    }
    
    public synchronized double getWidth() {
    	return width;
    }
 
    public synchronized double getHeight() {
    	return height;
    }

    public synchronized void setSeparationWeight(double value) {
    	this.separationWeight = value;
    }

    public synchronized void setAlignmentWeight(double value) {
    	this.alignmentWeight = value;
    }

    public synchronized void setCohesionWeight(double value) {
    	this.cohesionWeight = value;
    }

    public synchronized double getSeparationWeight() {
    	return separationWeight;
    }

    public synchronized double getCohesionWeight() {
    	return cohesionWeight;
    }

    public synchronized double getAlignmentWeight() {
    	return alignmentWeight;
    }
    
    public synchronized double getMaxSpeed() {
    	return maxSpeed;
    }

    public synchronized double getAvoidRadius() {
    	return avoidRadius;
    }

    public synchronized double getPerceptionRadius() {
    	return perceptionRadius;
    }

    public synchronized void toggleSimulationPause() {
        this.isModelPaused = !this.isModelPaused;
    }

    public synchronized boolean isModelPaused() {
        return this.isModelPaused;
    }

}
