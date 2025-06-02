package components;

public interface Scheduler {

    void execute(Runnable task);

    void shutdown();
}
