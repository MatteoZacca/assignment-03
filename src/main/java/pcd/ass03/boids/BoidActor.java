package pcd.ass03.boids;

import akka.actor.*;
import pcd.ass03.boids.BoidsProtocol.*;


public class BoidActor extends AbstractActor{

    private Boid boid;
    private BoidsModel model;

    public BoidActor(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(CalculateVelocityMsg.class, this::onCalculateVelocity)
                .match(BeforeUpdateBoidMsg.class, this::onBeforeUpdateBoid)
                .match(UpdateSeparationWeightMsg.class, msg -> {
                    this.model.setSeparationWeight(msg.weight());
                })
                .match(UpdateAlignmentWeightMsg.class, msg -> {
                    this.model.setAlignmentWeight(msg.weight());
                })
                .match(UpdateCohesionWeightMsg.class, msg -> {
                    this.model.setCohesionWeight(msg.weight());
                })
                .build();
    }

    private void onCalculateVelocity(CalculateVelocityMsg msg) {
        model.setBoids(msg.boids());
        boid.calculateVelocity(model);
        getSender().tell(new AfterCalculateVelocityMsg(), ActorRef.noSender());
    }


    /* 1. Akka receives a BeforeUpdateBoidMsg
    2. it picks a thread from the dispatcher and uses it to call onBeforeUpdateBoid
    3. That threads runs all lines of code in order: updateVelocity(...), updatePosition(...)
    and then getSender().tell(...)
    Java runs the methods sequentially on the same thread (every method call is blocking unless
    you explicitly make it asynchronous */
    private void onBeforeUpdateBoid(BeforeUpdateBoidMsg msg) {
        //log(getSelf().path().name() + " received BeforeUpdateBoidMsg");
        boid.updateVelocity(model, getSelf().path().name());
        boid.updatePosition(model, getSelf().path().name());
        getSender().tell(new AfterUpdateBoidMsg(
                new Boid(
                        new P2d(boid.getPos().x(), boid.getPos().y()),
                        new V2d(boid.getVel().x(), boid.getVel().y())
                )),
                ActorRef.noSender()
        );
    }

    private static void log(String print) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + print);
    }

}
