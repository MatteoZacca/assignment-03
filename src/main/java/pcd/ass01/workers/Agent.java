package pcd.ass01.workers;

import pcd.ass01.Boid;
import pcd.ass01.BoidsModel;
import pcd.ass01.synchronizers.MyCyclicBarrier;

import java.util.ArrayList;
import java.util.List;

public class Agent extends Thread {

    private final int index;
    private final int availableProcessors = Runtime.getRuntime().availableProcessors();

    private final MyCyclicBarrier startUpdateBarrier;
    private final MyCyclicBarrier endUpdateBarrier;
    private final MyCyclicBarrier readingDoneBarrier;

    private List<Boid> myBoids = new ArrayList<>();
    private BoidsModel model;


    public Agent (final int index, final MyCyclicBarrier startUpdateBarrier,
                  final MyCyclicBarrier endUpdateBarrier,
                  final MyCyclicBarrier readingDoneBarrier) {
        this.index = index;
        this.startUpdateBarrier = startUpdateBarrier;
        this.readingDoneBarrier = readingDoneBarrier;
        this.endUpdateBarrier = endUpdateBarrier;
    }

    public void setModel(BoidsModel model) {
        this.model = model;

        myBoids.clear();

        for(int i = 0; i < model.getBoids().size(); i++){
            if(i % availableProcessors == index){
                myBoids.add(model.getBoids().get(i));
            }
        }
    }

    public void run() {

        while(true) {
            syncBeforeAgentsStart();

            readNearbyBoids();

            syncReadingBeforeWriting();

            updateBoidsState();

            syncBeforeAgentsEnd();
        }
    }

    private void syncBeforeAgentsStart() {
        try {
            log(": sincronizzo i thread con startUpdateBarrier");
            this.startUpdateBarrier.await();
            log(": startUpdateBarrier crolla");
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void readNearbyBoids() {
        log(": inizia la fase di lettura");
        for (var boid: myBoids) {
            boid.readNearbyBoids(this.model);
        }
        log(": termina la fase di lettura");
    }

    private void syncReadingBeforeWriting() {
        try {
            log(": sincronizzo i thread con readingDoneBarrier");
            this.readingDoneBarrier.await();
            log(": readingDoneBarrier crolla" );
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void updateBoidsState() {
        log(": inizia la fase di scrittura");
        for(var boid: myBoids) {
            boid.updateVelocity(model);
            boid.updatePos(model);
        }
        log(": termina la fase di scrittura");
    }

    private void syncBeforeAgentsEnd() {
        log(": sincronizzo i thread con endUpdateBarrier");
        try {
            this.endUpdateBarrier.await();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        log(": endUpdateBarrier crolla");
    }

    private synchronized void log(String msg) {
        System.out.println("[" + getName() + "]" + msg);
    }
}
