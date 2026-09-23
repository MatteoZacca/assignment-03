package pcd.ass03.boids;

import akka.actor.*;
import pcd.ass03.boids.BoidsProtocol.*;


public class BoidActor extends AbstractActor{

    private final int id;
    private Boid boid; // only this actor touches this instance
    private final BoidsConfig config;

    public BoidActor(int id, Boid initialBoid, BoidsConfig config) {
        this.id = id;
        this.boid = new Boid(initialBoid.getPos(), initialBoid.getVel());
        this.config = config;
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
