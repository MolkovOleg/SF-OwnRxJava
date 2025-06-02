import components.Disposable;
import components.Observable;
import schedulers.CachedThreadScheduler;
import schedulers.ComputationScheduler;
import schedulers.SingleThreadScheduler;

public class Main {
    public static void main(String[] args) {

        // Создание планировщиков
        CachedThreadScheduler cachedScheduler = new CachedThreadScheduler();
        ComputationScheduler computationScheduler = new ComputationScheduler();
        SingleThreadScheduler singleThreadScheduler = new SingleThreadScheduler();

        try {
            // Пример 1: Использование flatMap
            System.out.println("\nПример 1: Использование flatMap");
            Observable<Integer> numbers = Observable.create(observer -> {
                try {
                    for (int i = 1; i <= 5; i++) {
                        observer.onNext(i);
                        observer.onComplete();
                    }
                } catch (Exception e) {
                    observer.onError(e);
                }
            });

            Disposable disposable = numbers.
                    flatMap(number -> Observable.create(observer -> {
                        try {
                            Thread.sleep(100);
                            observer.onNext(number * 10);
                            observer.onNext(number * 20);
                            observer.onComplete();
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    }))
                    .subscribe(
                            item -> System.out.println("FlatMap result: " + item),
                            error -> System.out.println("Error: " + error),
                            () -> System.out.println("FlatMap completed")
                    );

            // Пример 2: Обработка ошибок
            System.out.println("\nПример 2: Обработка ошибок");
            Observable.create(observer -> {
                        try {
                            observer.onNext(1);
                            observer.onNext(2);
                            throw new RuntimeException("Симуляция ошибки");
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    })
                    .subscribe(
                            item -> System.out.println("Получены: " + item),
                            error -> System.out.println("Обработка ошибок: " + error),
                            () -> System.out.println("Это не будет вызвано из-за ошибки")
                    );

            // Пример 3: Проверка disposable
            System.out.println("\nПример 3: Проверка disposable");
            Observable<Integer> infinite = Observable.create(observer -> {
                int i = 0;
                while (true) {
                    observer.onNext(i++);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            Disposable infiniteDisposable = infinite
                    .subscribeOn(cachedScheduler)
                    .observeOn(computationScheduler)
                    .subscribe(
                            item -> System.out.println("Infinite: " + item),
                            error -> System.out.println("Error: " + error.getMessage()),
                            () -> System.out.println("This won't be called")
                    );

            // Подождем цикла и отключить
            try {
                Thread.sleep(500);
                infiniteDisposable.dispose();
                System.out.println("Infinite stream disposed");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Ждем остальные операции для завершения
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            System.out.println("Завершение работы планировщиков...");
            cachedScheduler.shutdown();
            computationScheduler.shutdown();
            singleThreadScheduler.shutdown();
            System.out.println("Планировщики завершены");
        }
    }
}