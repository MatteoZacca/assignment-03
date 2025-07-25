package pcd.ass03.boids;

import java.util.ArrayList;
import java.util.List;

public class Boid {

    private P2d pos;
    private V2d vel;
    private V2d separation;
    private V2d alignment;
    private V2d cohesion;

    private List<Boid> nearbyBoids;

    public Boid(P2d pos, V2d vel) {
    	this.pos = pos;
    	this.vel = vel;
    }
    
    public P2d getPos() {
    	return pos;
    }

    public V2d getVel() {
    	return vel;
    }

    public void calculateVelocity(BoidsModel model) {
        /* change velocity vector according to separation, alignment, cohesion */
        this.nearbyBoids =  getNearbyBoids(model);

        this.separation = calculateSeparation(nearbyBoids, model);
        this.alignment = calculateAlignment(nearbyBoids, model);
        this.cohesion = calculateCohesion(nearbyBoids, model);
    }

    public void updateVelocity(BoidsModel model, String boidName) {
        //System.out.println("[" + Thread.currentThread().getName() + "]: inside updateVelocity - " + boidName);
        this.vel = vel.sum(alignment.mul(model.getAlignmentWeight()))
    			.sum(separation.mul(model.getSeparationWeight()))
    			.sum(cohesion.mul(model.getCohesionWeight()));
        
        /* Limit speed to MAX_SPEED */
        double speed = vel.abs();
        
        if (speed > model.getMaxSpeed()) {
            vel = vel.getNormalized().mul(model.getMaxSpeed());
        }
    }    
    
    public void updatePosition(BoidsModel model, String boidName) {
        //System.out.println("[" + Thread.currentThread().getName() + "]: inside updatePosition - " + boidName);

        /* Update position */
        this.pos = pos.sum(vel);
        
        /* environment wrap-around */
        if (pos.x() < model.getMinX()) pos = pos.sum(new V2d(model.getWidth(), 0));
        if (pos.x() >= model.getMaxX()) pos = pos.sum(new V2d(-model.getWidth(), 0));
        if (pos.y() < model.getMinY()) pos = pos.sum(new V2d(0, model.getHeight()));
        if (pos.y() >= model.getMaxY()) pos = pos.sum(new V2d(0, -model.getHeight()));
    }     

    private List<Boid> getNearbyBoids(BoidsModel model) {
    	var list = new ArrayList<Boid>();
        for (Boid boid : model.getBoids()) {
        	if (boid != this) {
        		P2d otherPos = boid.getPos();
        		double distance = pos.distance(otherPos);
        		if (distance < model.getPerceptionRadius()) {
        			list.add(boid);
        		}
        	}
        }
        return List.copyOf(list);
    }
    
    private V2d calculateAlignment(List<Boid> nearbyBoids, BoidsModel model) {
        double avgVx = 0;
        double avgVy = 0;
        if (nearbyBoids.size() > 0) {
	        for (Boid other : nearbyBoids) {
	        	V2d otherVel = other.getVel();
	            avgVx += otherVel.x();
	            avgVy += otherVel.y();
	        }	        
	        avgVx /= nearbyBoids.size();
	        avgVy /= nearbyBoids.size();
	        return new V2d(avgVx - vel.x(), avgVy - vel.y()).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }

    private V2d calculateCohesion(List<Boid> nearbyBoids, BoidsModel model) {
        double centerX = 0;
        double centerY = 0;
        if (nearbyBoids.size() > 0) {
	        for (Boid other: nearbyBoids) {
	        	P2d otherPos = other.getPos();
	            centerX += otherPos.x();
	            centerY += otherPos.y();
	        }
            centerX /= nearbyBoids.size();
            centerY /= nearbyBoids.size();
            return new V2d(centerX - pos.x(), centerY - pos.y()).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }
    
    private V2d calculateSeparation(List<Boid> nearbyBoids, BoidsModel model) {
        double dx = 0;
        double dy = 0;
        int count = 0;
        for (Boid other: nearbyBoids) {
        	P2d otherPos = other.getPos();
    	    double distance = pos.distance(otherPos);
    	    if (distance < model.getAvoidRadius()) {
    	    	dx += pos.x() - otherPos.x();
    	    	dy += pos.y() - otherPos.y();
    	    	count++;
    	    }
    	}
        if (count > 0) {
            dx /= count;
            dy /= count;
            return new V2d(dx, dy).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }
}
