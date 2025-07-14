package pcd.ass03.boids;

import akka.actor.*;
import pcd.ass03.boids.BoidsProtocol.*;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;

public class BoidMasterActor extends AbstractActorWithStash {

    private long t0;
    private int framerate;
    private static final int FRAMERATE = 60;

    private BoidsModel model;
    private BoidsView view;

    private int nStartingBoids;
    private int countUpdate;
    private int countUpdateWeight;
    private List<ActorRef> boidsActor;
    private List<Boid> updatedBoids;

    public BoidMasterActor(BoidsModel model, int nStartingBoids, BoidsView view) {
        this.model = model;
        this.nStartingBoids = nStartingBoids;
        this.view = view;
        this.boidsActor = new ArrayList<>();
        this.updatedBoids = new ArrayList<>();
    }

    /* --------------------------------- BEHAVIOURS --------------------------------- */

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BootMsg.class, this::onBoot)
                .match(StartSimulationMsg.class, this::onStartSimulation)
                .matchAny(msg -> {
                    //log("Unhandled (will stash): " + msg.getClass());
                    this.stash();
                })
                .build();
    }

    public Receive UpdateBehaviour() {
        return receiveBuilder()
                .match(AfterCalculateVelocityMsg.class, this::onAfterCalculateVelocity)
                .match(AfterUpdateBoidMsg.class, this::onAfterUpdateBoid)
                .match(Tick.class, this::onTick)
                .matchAny(msg -> {
                    //log("Unhandled (will stash): " + msg.getClass());
                    this.stash();
                })
                .build();
    }

    public Receive RunningSimulationBehaviour() {
        return receiveBuilder()
                .match(ContinueUpdatingSimulationMsg.class, this::onContinueUpdatingSimulation)
                .match(PauseSimulationMsg.class, this::onPauseSimulation)
                .match(ResetSimulationMsg.class, this::onResetSimulation) // that's only for ResetSimulationMsg received
                // from boidsSlider
                .match(UpdateSeparationWeightMsg.class, this::onBeforeUpdateSeparationWeight)
                .match(UpdateAlignmentWeightMsg.class, this::onBeforeUpdateAlignmentWeight)
                .match(UpdateCohesionWeightMsg.class, this::onBeforeUpdateCohesionWeight)
                .matchAny(msg -> {
                    //log("Unhandled (will stash): " + msg.getClass());
                    this.stash();
                })
                .build();
    }

    public Receive UpdateWeightBehaviour() {
        return receiveBuilder()
                .match(AfterUpdateSeparationWeightMsg.class, this::onAfterUpdateSeparationWeight)
                .match(AfterUpdateAlignmentWeight.class, this::onAfterUpdateAlignmentWeight)
                .match(AfterUpdateCohesionWeight.class, this::onAfterUpdateCohesionWeight)
                .matchAny(msg -> {
                            //log("Unhandled (will stash): " + msg.getClass());
                            stash();
                        })
                .build();
    }

    public Receive pausingBehaviour() {
        return receiveBuilder()
                .match(StartSimulationMsg.class, this::onStartSimulation)
                .match(ResetSimulationMsg.class, this::onResetSimulation)
                .matchAny(msg -> {
                    //log("Unhandled (will stash): " + msg.getClass());
                    stash();
                })
                .build();
    }

    /* --------------------------------- METHODS --------------------------------- */
    private void onBoot(BootMsg msg) {
        log("[" + this.getSelf().path().name() + "] received BootMsg");
        this.model = msg.model(); // the first BootMsg sent in BoidsSimulation wouldn't need this,
        // but every time we push the Reset button, this actor receive a BootMsg, so we have to
        // ri-initialize this.model

        List<Boid> startingBoids = model.getBoids();

        for (int i = 0; i < startingBoids.size(); i++) {
            Boid boid = startingBoids.get(i);
            ActorRef boidActor = getContext().actorOf(Props.create(
                    BoidActor.class,
                    () -> new BoidActor(boid, model)),
                    "boid-" + i + "-" + System.currentTimeMillis()); // in this way i create a unique name
            // for every actor, and I won't have problem inside onResetSimulation
            this.boidsActor.add(boidActor);
        }
    }

    private void onStartSimulation(StartSimulationMsg msg) {
        log("[" + this.getSelf().path().name() + "] received StartSimulationMsg - nBoids: " + this.boidsActor.size());
        this.t0 = System.currentTimeMillis();

        this.countUpdate = boidsActor.size();
        this.updatedBoids.clear();

        for(ActorRef boid : boidsActor) {
            boid.tell(new CalculateVelocityMsg(model.getBoids()), getSelf());
        }

        this.getContext().become(UpdateBehaviour());
        this.unstashAll();
    }

    private void onAfterCalculateVelocity(AfterCalculateVelocityMsg msg) {
        this.countUpdate--;
        if (this.countUpdate == 0) {
            log(this.getSelf().path().name() + " received " + this.boidsActor.size() +
                    " AfterCalculateVelocityMsg, now it can send BeforeUpdateBoidMsg()");
            for (ActorRef boid : boidsActor) {
                boid.tell(new BeforeUpdateBoidMsg(), getSelf());
            }
            this.countUpdate = this.boidsActor.size();
        }
    }

    private void onAfterUpdateBoid(AfterUpdateBoidMsg msg) {
        this.updatedBoids.add(msg.updatedBoid());
        this.countUpdate--;
        if (countUpdate == 0) {
            log(this.getSelf().path().name() + " received " + this.boidsActor.size() +
                    " AfterUpdateBoidMsg, now it can send update the GUI and send TickMsg");
            this.model.setBoids(new ArrayList<>(this.updatedBoids));
            //this.model.getPrintingBoids();
            this.view.update(framerate);

            long dtElapsed = System.currentTimeMillis() - t0;
            long frameratePeriod = 1000 / FRAMERATE;
            long delay = Math.max(0, frameratePeriod - dtElapsed);

            if (dtElapsed < frameratePeriod) {
                this.framerate = FRAMERATE;
            } else {
                this.framerate = (int) (1000 / dtElapsed);
            }

            /* This schedules a one-time message Tick to be sent to the actor after a delay,
            using the actor system's scheduler. */
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(delay, java.util.concurrent.TimeUnit.MILLISECONDS),
                    self(), // the recipient of the message
                    new Tick(), // message to send after the delay
                    getContext().getSystem().dispatcher(), // this is the Akka dispatcher that will run the delayed task
                    // (usually the default thread-pool for actors)
                    self() // this defines the sender of the message
            );
        }
    }

    private void onTick(Tick msg) {
        log("[" + this.getSelf().path().name() + "] received TickMsg");
        this.getContext().become(RunningSimulationBehaviour());
        this.unstashAll();
        getSelf().tell(new ContinueUpdatingSimulationMsg(), ActorRef.noSender());
    }

    private void onContinueUpdatingSimulation(ContinueUpdatingSimulationMsg msg) {
        log(this.getSelf().path().name() + " received ContinueUpdatingSimulationMsg");
        this.t0 = System.currentTimeMillis();

        this.countUpdate = boidsActor.size();
        this.updatedBoids.clear();

        for(ActorRef boid : boidsActor) {
            boid.tell(new CalculateVelocityMsg(model.getBoids()), getSelf());
        }

        this.getContext().become(UpdateBehaviour());
        this.unstashAll();
    }

    private void onPauseSimulation(PauseSimulationMsg msg) {
        log("[" + this.getSelf().path().name() + "] received PauseSimulationMsg");
        this.getContext().become(pausingBehaviour());
    }

    private void onResetSimulation(ResetSimulationMsg msg) {
        log("[" + this.getSelf().path().name() + "] received ResetSimulationMsg");
        this.nStartingBoids = msg.nStartingBoids();
        this.model.generateBoids(nStartingBoids);

        for (ActorRef boid : boidsActor) {
            boid.tell(PoisonPill.getInstance(), ActorRef.noSender());
        }

        this.boidsActor.clear();
        this.getContext().become(createReceive());
        this.getSelf().tell(new BootMsg(this.model), ActorRef.noSender());
        this.unstashAll();

    }

    private void onBeforeUpdateSeparationWeight(UpdateSeparationWeightMsg msg) {
        log("[" + this.getSelf().path().name() + "] received UpdateSeparationWeightMsg ---> " + String.format("%.1f", msg.weight()));

        this.countUpdateWeight = this.boidsActor.size();
        this.model.setSeparationWeight(msg.weight());

        for (ActorRef boid : boidsActor) {
            boid.tell(new UpdateSeparationWeightMsg(msg.weight()), getSelf());
        }

        this.getContext().become(UpdateWeightBehaviour());
        this.unstashAll();
    }

    private void onAfterUpdateSeparationWeight(AfterUpdateSeparationWeightMsg msg) {
        this.countUpdateWeight--;
        if(this.countUpdateWeight == 0) {
            log("[" + this.getSelf().path().name() + "] received " + this.boidsActor.size() +
                    " AfterUpdateSeparationWeightMsg");

            this.countUpdateWeight = this.boidsActor.size();
            this.getContext().become(RunningSimulationBehaviour());
            this.unstashAll();
        }
    }

    private void onBeforeUpdateAlignmentWeight(UpdateAlignmentWeightMsg msg) {
        log("[" + this.getSelf().path().name() + "] received UpdateAlignmentWeightMsg ---> " + String.format("%.1f", msg.weight()));

        this.countUpdateWeight = this.boidsActor.size();
        this.model.setAlignmentWeight(msg.weight());

        for (ActorRef boid : boidsActor) {
            boid.tell(new UpdateAlignmentWeightMsg(msg.weight()), getSelf());
        }

        this.getContext().become(UpdateWeightBehaviour());
        this.unstashAll();
    }

    private void onAfterUpdateAlignmentWeight(AfterUpdateAlignmentWeight msg) {
        this.countUpdateWeight--;
        if (this.countUpdateWeight == 0) {
            log("[" + this.getSelf().path().name() + "] received " + this.boidsActor.size() +
                    " AfterUpdateAlignmentWeightMsg");

            this.countUpdateWeight = this.boidsActor.size();
            this.getContext().become(RunningSimulationBehaviour());
            this.unstashAll();
        }
    }

    private void onBeforeUpdateCohesionWeight(UpdateCohesionWeightMsg msg) {
        log("[" + this.getSelf().path().name() + "] received UpdateCohesionWeightMsg ---> " + String.format("%.1f", msg.weight()));

        this.countUpdateWeight = this.boidsActor.size();
        this.model.setCohesionWeight(msg.weight());

        for (ActorRef boid : boidsActor) {
            boid.tell(new UpdateCohesionWeightMsg(msg.weight()), getSelf());
        }

        this.getContext().become(UpdateWeightBehaviour());
        this.unstashAll();
    }

    private void onAfterUpdateCohesionWeight(AfterUpdateCohesionWeight msg) {
        this.countUpdateWeight--;
        if (this.countUpdateWeight == 0) {
            log("[" + this.getSelf().path().name() + "] received " + this.boidsActor.size() +
                    " AfterUpdateCohesionWeightMsg");

            this.countUpdateWeight = this.boidsActor.size();
            this.getContext().become(RunningSimulationBehaviour());
            this.unstashAll();
        }
    }

    private static void log(String print) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + print);
    }

}
