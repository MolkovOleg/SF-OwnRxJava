import schedulers.CachedThreadScheduler;
import schedulers.ComputationScheduler;
import schedulers.SingleThreadScheduler;

public class Main {
    public static void main(String[] args) {

        // Создание планировщиков
        CachedThreadScheduler cachedScheduler = new CachedThreadScheduler();
        ComputationScheduler computationScheduler = new ComputationScheduler();
        SingleThreadScheduler singleThreadScheduler = new SingleThreadScheduler();
    }
}
