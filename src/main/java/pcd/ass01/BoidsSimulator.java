package pcd.ass01;

import pcd.ass01.workers.BoidsUpdater;

import java.util.Optional;

public class BoidsSimulator {

    private static final int FRAMERATE = 25;

    private final BoidsUpdater boidsUpdater;

    private BoidsModel model;
    private int framerate;
    private Optional<BoidsView> view;


    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        this.boidsUpdater = new BoidsUpdater();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() {
        while (true) {

            if (!model.isModelPaused()) { // Se il modello NON è in pausa, aggiorna la simulazione
                long t0 = System.currentTimeMillis();

                boidsUpdater.update(model);

                if (view.isPresent()) {
                    view.get().update(framerate);
                    regulateFramerate(t0);
                }
            }
        }
    }

    private void regulateFramerate(long t0) {
        long t1 = System.currentTimeMillis();
        var dtElapsed = t1 - t0;
        var framratePeriod = 1000 / FRAMERATE;

        if (dtElapsed < framratePeriod) {
            try {
                Thread.sleep(framratePeriod - dtElapsed);
            } catch (Exception ex) {}
            framerate = FRAMERATE;
        } else {
            framerate = (int) (1000 / dtElapsed);
        }
    }
}


