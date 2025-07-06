package pcd.ass03.synchronizers;

public class MyCyclicBarrier {
    private final int totalThreads;
    private int currentThreads = 0;

    private boolean barrierReleased = false;

    public MyCyclicBarrier(final int totalThreads) {
        this.totalThreads = totalThreads;
    }

    public synchronized void await() throws InterruptedException {
        this.barrierReleased = false;
        this.currentThreads++;

        System.out.println("[" + Thread.currentThread().getName() + "] entrato nella barriera: " +
                "Posizione in coda: (" + getQueuePosition() + ")");

        if (this.currentThreads == this.totalThreads) {
            this.barrierReleased = true;
            // Tutti i thread hanno raggiunto la barriera
            notifyAll();
            this.currentThreads = 0;
        } else {
            while(this.currentThreads < this.totalThreads && !barrierReleased){
                // Aspetto che tutti i thread giungano allo stesso punto
                wait();
            }
        }
    }

    public synchronized String getQueuePosition() {
        return this.currentThreads + "/" + this.totalThreads;
    }

}
