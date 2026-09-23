package pcd.ass03.boids;

import pcd.ass03.boids.BoidsProtocol.*;

import java.util.ArrayList;
import java.util.List;

public class Boid {

    private P2d pos;
    private V2d vel;
    private V2d separation;
    private V2d alignment;
    private V2d cohesion;

    private List<BoidState> nearbyBoids;

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

    public void calculateVelocity(int BoidID, List<BoidState> flock, BoidsConfig config) {
        this.nearbyBoids =  getNearbyBoids(BoidID, flock, config.perceptionRadius());

        this.separation = calculateSeparation(nearbyBoids, config.avoidRadius());
        this.alignment = calculateAlignment(nearbyBoids);
        this.cohesion = calculateCohesion(nearbyBoids);
    }

    public void updateVelocity(BoidsConfig config, double sepWeight, double aliWeight, double cohWeight) {
        //System.out.println("[" + Thread.currentThread().getName() + "]: inside updateVelocity - " + boidName);

        this.vel = vel.sum(alignment.mul(aliWeight))
    			.sum(separation.mul(sepWeight))
    			.sum(cohesion.mul(cohWeight));
        
        /* Limit speed to MAX_SPEED */
        double speed = vel.abs();

        if (speed > config.maxSpeed()) {
            vel = vel.getNormalized().mul(config.maxSpeed());
        }
    }    
    
    public void updatePosition(BoidsConfig config) {
        //System.out.println("[" + Thread.currentThread().getName() + "]: inside updatePosition - " + boidName);

        /* Update position */
        this.pos = pos.sum(vel);

        double width = config.width();
        double height = config.height();
        double minX = -width / 2;
        double maxX = width / 2;
        double minY = -height / 2;
        double maxY = height / 2;
        
        /* environment wrap-around */
        if (pos.x() < minX) pos = pos.sum(new V2d(width, 0));
        if (pos.x() >= maxX) pos = pos.sum(new V2d(-width, 0));
        if (pos.y() < minY) pos = pos.sum(new V2d(0, height));
        if (pos.y() >= maxY) pos = pos.sum(new V2d(0, -height));
    }     

    private List<BoidState> getNearbyBoids(int boidId, List<BoidState> flock, double perceptionRadius) {
    	var list = new ArrayList<BoidState>();
        for (BoidState other : flock) {
        	if (other.id() != boidId) {
        		double distance = pos.distance(other.pos());
        		if (distance < perceptionRadius) {
        			list.add(other);
        		}
        	}
        }
        return list;
    }
    
    private V2d calculateAlignment(List<BoidState> nearbyBoids) {
        double avgVx = 0;
        double avgVy = 0;
        if (!nearbyBoids.isEmpty()) {
	        for (BoidState other : nearbyBoids) {
	            avgVx += other.vel().x();
	            avgVy += other.vel().y();
	        }	        
	        avgVx /= nearbyBoids.size();
	        avgVy /= nearbyBoids.size();
	        return new V2d(avgVx - vel.x(), avgVy - vel.y()).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }

    private V2d calculateCohesion(List<BoidState> nearbyBoids) {
        double centerX = 0;
        double centerY = 0;
        if (!nearbyBoids.isEmpty()) {
            for (BoidState other: nearbyBoids) {
                centerX += other.pos().x();
                centerY += other.pos().y();
            }
            centerX /= nearbyBoids.size();
            centerY /= nearbyBoids.size();
            return new V2d(centerX - pos.x(), centerY - pos.y()).getNormalized();
        } else {
            return new V2d(0, 0);
        }
    }

    private V2d calculateSeparation(List<BoidState> nearbyBoids, double avoidRadius) {
        double dx = 0;
        double dy = 0;
        int count = 0;
        for (BoidState other: nearbyBoids) {
            double distance = pos.distance(other.pos());
            if (distance < avoidRadius) {
                dx += pos.x() - other.pos().x();
                dy += pos.y() - other.pos().y();
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
