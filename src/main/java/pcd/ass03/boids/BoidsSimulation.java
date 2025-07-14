package pcd.ass03.boids;

import akka.actor.*;
import pcd.ass03.boids.BoidsProtocol.*;

import java.util.ArrayList;
import java.util.List;

public class BoidsSimulation {
    private final static int N_BOIDS = 1500;

    private final static double SEPARATION_WEIGHT = 1.0;
    private final static double ALIGNMENT_WEIGHT = 1.0;
    private final static double COHESION_WEIGHT = 1.0;

    private final static int ENVIRONMENT_WIDTH = 600;
    private final static int ENVIRONMENT_HEIGHT = 600;

    private final static double MAX_SPEED = 4.0;
    private final static double PERCEPTION_RADIUS = 50.0;
    private final static double AVOID_RADIUS = 20.0;

    private final static int SCREEN_WIDTH  = 700;
    private final static int SCREEN_HEIGHT = 700;

    public static void main(String[] args) {

        BoidsModel model = new BoidsModel(N_BOIDS,
                SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED, PERCEPTION_RADIUS, AVOID_RADIUS);

        ActorSystem system  = ActorSystem.create("boids-system");

        ActorRef boidsView = system.actorOf(Props.create(
                BoidsViewActor.class,
                () -> new BoidsViewActor(model, SCREEN_WIDTH, SCREEN_HEIGHT, N_BOIDS)),
                "boids-view-actor"
        );

        BoidsView view = new BoidsView(model, SCREEN_WIDTH, SCREEN_HEIGHT, N_BOIDS);

        ActorRef master = system.actorOf(Props.create(
                BoidMasterActor.class,
                () -> new BoidMasterActor(model, N_BOIDS, view)),
                "boid-master-actor"
        );

        boidsView.tell(new SetMasterActorMsg(master), ActorRef.noSender());
        master.tell(new BootMsg(model), ActorRef.noSender());
    }

    private static void log(String print) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + print);
    }


}
