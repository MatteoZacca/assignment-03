package pcd.ass03.boids.actors;

import akka.actor.*;
import pcd.ass03.boids.domain.Boid;
import pcd.ass03.boids.actors.BoidsProtocol.*;
import pcd.ass03.boids.domain.P2d;
import pcd.ass03.boids.domain.V2d;


public class BoidActor extends AbstractActor{

    private final int id;
    private final Boid boid; // strictly encapsulated state
    private final BoidsConfig config;

    public BoidActor(int id, BoidsConfig config) {
        this.id = id;
        this.config = config;

        double width = config.width();
        double height = config.height();
        double maxSpeed = config.maxSpeed();

        P2d pos = new P2d(Math.random() * width - width / 2, Math.random() * height - height / 2);
        V2d vel = new V2d(Math.random() * maxSpeed / 2 - maxSpeed / 4, Math.random() * maxSpeed / 2 - maxSpeed / 4);

        this.boid = new Boid(pos, vel);
    }

    @Override
    public void preStart() {
        // Check in with the Master so it knows the starting position
        BoidState initialState = new BoidState(
                this.id,
                new P2d(boid.getPos().x(), boid.getPos().y()),
                new V2d(boid.getVel().x(), boid.getVel().y())
        );

        getContext().getParent().tell(new BoidsInitializationMsg(initialState), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ComputeStepMsg.class, this::onComputeStep)
                .build();
    }

    private void onComputeStep(ComputeStepMsg msg) {
        boid.calculateVelocity(
                this.id,
                msg.flockState(),
                config
        );

        boid.updateVelocity(
                config,
                msg.separationWeight(),
                msg.alignmentWeight(),
                msg.cohesionWeight()
        );
        boid.updatePosition(config);

        BoidState newState = new BoidState(
                this.id,
                new P2d(boid.getPos().x(), boid.getPos().y()),
                new V2d(boid.getVel().x(), boid.getVel().y())
        );

        getSender().tell(new StepDoneMsg(newState), getSelf());
    }

    private static void log(String print) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + print);
    }

}
