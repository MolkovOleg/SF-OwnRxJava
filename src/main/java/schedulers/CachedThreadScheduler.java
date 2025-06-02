package schedulers;

import components.Scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThreadScheduler implements Scheduler {

    private final ExecutorService executor;

    public CachedThreadScheduler() {
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
}
