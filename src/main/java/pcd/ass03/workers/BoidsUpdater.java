package pcd.ass03.workers;

import pcd.ass03.BoidsModel;
import pcd.ass03.synchronizers.MyCyclicBarrier;

import java.util.ArrayList;
import java.util.List;

public class BoidsUpdater {

    private final int availableProcessors = Runtime.getRuntime().availableProcessors();

    private final List<Agent> agents = new ArrayList<>();

    private final MyCyclicBarrier startUpdateBarrier;
    private final MyCyclicBarrier readingDoneBarrier;
    private final MyCyclicBarrier endUpdateBarrier;

    public BoidsUpdater() {

        this.startUpdateBarrier = new MyCyclicBarrier(availableProcessors + 1);
        this.readingDoneBarrier = new MyCyclicBarrier(availableProcessors);
        this.endUpdateBarrier = new MyCyclicBarrier(availableProcessors + 1);


        for (int index = 0; index < availableProcessors; index++) {
            Agent agent = new Agent(index, this.startUpdateBarrier, this.endUpdateBarrier, this.readingDoneBarrier);
            agents.add(agent);
            agent.start();
        }
    }

    public void update(BoidsModel model) {
        this.agents.forEach(it -> it.setModel(model));

        try {
            this.startUpdateBarrier.await(); // Quando viene premuto il tasto 'Play'
            // nella GUI, si entra nel primo ciclo if all'interno del metodo runSimulation.
            // Una volta che si entra nell'if, viene chiamato il metodo 'update', e una
            // volta eseguita l'istruzione di cui sopra, anche il main raggiunge
            // 'startUpdateBarrier', dove gli altri thread sono già stati soggetti alla
            // 'wait()', ed essendo il main l'ultimo thread necessario per fare crollare
            // la barriera, una volta arrivato, 'startUpdateBarrier' crolla.
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        try {
            this.endUpdateBarrier.await(); // Analogia con il ragionamento sopra sulla
            // 'startUpdatebarrier'
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

}
