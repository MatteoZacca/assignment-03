package pcd.ass03.boids;

import akka.actor.*;
import pcd.ass03.boids.BoidsProtocol.*;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;

public class BoidMasterActor extends AbstractActor {

    private long t0;
    private int framerate;
    private static final int FRAMERATE = 60;

    private BoidsModel model;
    private BoidsConfig config;
    private BoidsView view;

    private int nStartingBoids;
    private int countUpdate;
    private boolean isPaused = true;

    private List<ActorRef> boidsActors;
    private List<BoidState> currentStates;
    private List<BoidState> nextStates;

    private double currentSeparationWeight;
    private double currentAlignmentWeight;
    private double currentCohesionWeight;

    public BoidMasterActor(BoidsModel model, int nStartingBoids, BoidsView view) {
        this.model = model;
        this.nStartingBoids = nStartingBoids;
        this.view = view;
        this.boidsActors = new ArrayList<>();
        this.currentStates = new ArrayList<>();
        this.nextStates = new ArrayList<>();
    }

    /* --------------------------------- BEHAVIOURS --------------------------------- */

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BootMsg.class, this::onBoot)
                .match(StartSimulationMsg.class, msg -> {
                    log("[" + getSelf().path().name() + "] received StartSimulationMsg");
                    isPaused = false;
                    triggerNextFrame();
                    getContext().become(runningBehaviour());
                })
                .match(ResetSimulationMsg.class, this::onResetSimulation) // This enables processing two successive
                // updates to nBoids through the JTextField without needing to press the 'Play' button in between.
                // Safely catch and ignore delayed messages from the previous run
                .match(Tick.class, msg -> {})
                .match(StepDoneMsg.class, msg -> {})
                .build();
    }

    public Receive runningBehaviour() {
        return receiveBuilder()
                .match(StepDoneMsg.class, this::onStepDone)
                .match(Tick.class, msg -> {
                    // Only start a new frame if we aren't paused
                    if (!isPaused) {
                        triggerNextFrame();
                    }
                })
                .match(PauseSimulationMsg.class, msg -> {
                    log("[" + getSelf().path().name() + "] received PauseSimulationMsg");
                    isPaused = true;
                })
                .match(StartSimulationMsg.class, msg -> {
                    log("[" + getSelf().path().name() + "] received StartSimulationMsg");
                    if (isPaused) {
                        isPaused = false;
                        if (countUpdate == 0) {
                            triggerNextFrame();
                        }
                    }
                })
                .match(ResetSimulationMsg.class, this::onResetSimulation)
                .match(UpdateSeparationWeightMsg.class, msg -> this.currentSeparationWeight = msg.weight())
                .match(UpdateAlignmentWeightMsg.class, msg -> this.currentAlignmentWeight = msg.weight())
                .match(UpdateCohesionWeightMsg.class, msg -> this.currentCohesionWeight = msg.weight())
                .build();
    }

    /* --------------------------------- METHODS --------------------------------- */

    private void onBoot(BootMsg msg) {
        log("[" + this.getSelf().path().name() + "] received BootMsg");
        this.model = msg.model(); // the first BootMsg sent in BoidsSimulation wouldn't need this,
        // but every time we push the Reset button, this actor receive a BootMsg, so we have to
        // ri-initialize this.model

        this.config = new BoidsConfig(
                model.getWidth(), model.getHeight(), model.getMaxSpeed(),
                model.getPerceptionRadius(), model.getAvoidRadius()
        );

        this.currentSeparationWeight = model.getSeparationWeight();
        this.currentAlignmentWeight = model.getAlignmentWeight();
        this.currentCohesionWeight = model.getCohesionWeight();

        List<Boid> startingBoids = model.getBoids();
        this.currentStates.clear();

        for (int i = 0; i < startingBoids.size(); i++) {
            Boid boid = startingBoids.get(i);

            this.currentStates.add(new BoidState(
                    i, new P2d(boid.getPos().x(), boid.getPos().y()), new V2d(boid.getVel().x(), boid.getVel().y())
            ));

            final int boidId = i;

            ActorRef boidActor = getContext().actorOf(Props.create(
                    BoidActor.class,
                    () -> new BoidActor(boidId, boid, config)),
                    "boid-" + i + "-" + System.currentTimeMillis()); // in this way i create a unique name
            // for every actor, and I won't have problem inside onResetSimulation
            this.boidsActors.add(boidActor);
        }
    }

    private void triggerNextFrame() {
        this.t0 = System.currentTimeMillis();
        this.countUpdate = boidsActors.size();
        this.nextStates.clear();

        // Send a single ComputeStepMsg to all boids with the immutable list and current weights
        ComputeStepMsg computeMsg = new ComputeStepMsg(
                this.currentStates,
                this.currentSeparationWeight,
                this.currentAlignmentWeight,
                this.currentCohesionWeight
        );

        for (ActorRef boid : boidsActors) {
            boid.tell(computeMsg, getSelf());
        }
    }

    private void scheduleTick() {
        long dtElapsed = System.currentTimeMillis() - t0;
        long frameratePeriod = 1000 / FRAMERATE;
        long delay = Math.max(0, frameratePeriod - dtElapsed);

        if (dtElapsed < frameratePeriod) {
            this.framerate = FRAMERATE;
        } else {
            this.framerate = (int) (1000 / dtElapsed);
        }

        getContext().system().scheduler().scheduleOnce(
                Duration.create(delay, java.util.concurrent.TimeUnit.MILLISECONDS),
                self(), new Tick(), getContext().getSystem().dispatcher(), self()
        );
    }

    private void onStepDone(StepDoneMsg msg) {
        this.nextStates.add(msg.updatedBoid());
        this.countUpdate--;

        if (countUpdate == 0) {
            // Swap snapshot lists for the next frame
            this.currentStates = new ArrayList<>(this.nextStates);

            // Update the model so the View can render it (we will fix the view logic next)
            this.model.setBoids(convertSnapshotsToBoids(this.currentStates));
            this.view.update(framerate);

            scheduleTick();
        }
    }

    private void onResetSimulation(ResetSimulationMsg msg) {
        log("[" + this.getSelf().path().name() + "] received ResetSimulationMsg");
        this.nStartingBoids = msg.nStartingBoids();
        this.model.generateBoids(nStartingBoids);

        for (ActorRef boid : boidsActors) {
            boid.tell(PoisonPill.getInstance(), ActorRef.noSender());
        }

        this.boidsActors.clear();
        this.getContext().become(createReceive());
        this.getSelf().tell(new BootMsg(this.model), ActorRef.noSender());
    }

    // Helper method to keep BoidsView rendering correctly until we update the View logic
    private List<Boid> convertSnapshotsToBoids(List<BoidState> snapshots) {
        List<Boid> boids = new ArrayList<>();
        for (BoidState snap : snapshots) {
            boids.add(new Boid(snap.pos(), snap.vel()));
        }
        return boids;
    }

    private static void log(String print) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + print);
    }
}
