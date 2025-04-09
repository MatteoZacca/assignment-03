package pcd.ass01;

public class BoidsSimulation {

	final static int N_BOIDS = 1500;
	static final double SEPARATION_WEIGHT = 1.0;
    static final double ALIGNMENT_WEIGHT = 1.0;
    static final double COHESION_WEIGHT = 1.0;

    static final int ENVIRONMENT_WIDTH = 600;
	static final int ENVIRONMENT_HEIGHT = 600;
    static final double MAX_SPEED = 4.0;
    static final double PERCEPTION_RADIUS = 50.0;
    static final double AVOID_RADIUS = 20.0;

	static final int SCREEN_WIDTH = 700;
	static final int SCREEN_HEIGHT = 700;
	

    public static void main(String[] args) {      
    	var model = new BoidsModel(
    					N_BOIDS, 
    					SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT, 
    					ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
    					MAX_SPEED,
    					PERCEPTION_RADIUS,
    					AVOID_RADIUS); 
    	var sim = new BoidsSimulator(model);
    	var view = new BoidsView(model, SCREEN_WIDTH, SCREEN_HEIGHT);
    	sim.attachView(view);
    	sim.runSimulation();
    }
}
