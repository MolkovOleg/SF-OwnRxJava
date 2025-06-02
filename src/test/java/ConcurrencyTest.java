import components.Observable;
import org.junit.jupiter.api.Test;
import schedulers.CachedThreadScheduler;
import schedulers.ComputationScheduler;
import schedulers.SingleThreadScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyTest {

    @Test
    void testSubscribeWithDifSchedulers() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> executionThread = new AtomicReference<>();
        String mainThread = Thread.currentThread().getName();

        Observable.create(observer -> {
                    executionThread.set(Thread.currentThread().getName());
                    observer.onNext(1);
                    observer.onComplete();
                    latch.countDown();
                })
                .subscribeOn(new CachedThreadScheduler())
                .subscribe(
                        item -> {
                        },
                        error -> fail("Unexpected error"),
                        () -> {
                        }
                );

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertNotEquals(mainThread, executionThread.get());
    }

    @Test
    void testObserveOnWithDifferentSchedulers() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> observationThread = new AtomicReference<>();
        String mainThread = Thread.currentThread().getName();

        Observable.create(observer -> {
                    observer.onNext(1);
                    observer.onComplete();
                })
                .observeOn(new ComputationScheduler())
                .subscribe(
                        item -> observationThread.set(Thread.currentThread().getName()),
                        error -> fail("Unexpected error"),
                        () -> latch.countDown()
                );

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertNotEquals(mainThread, observationThread.get());
    }

    @Test
    void testSingleThreadSchedulerSequentialExecution() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        List<String> threadNames = new ArrayList<>();
        SingleThreadScheduler scheduler = new SingleThreadScheduler();

        Observable.create(observer -> {
                    threadNames.add(Thread.currentThread().getName());
                    observer.onNext(1);
                    observer.onComplete();
                    latch.countDown();
                })
                .subscribeOn(scheduler)
                .subscribe(
                        item -> {
                        },
                        error -> fail("Unexpected error"),
                        () -> {
                        }
                );

        Observable.create(observer -> {
                    threadNames.add(Thread.currentThread().getName());
                    observer.onNext(2);
                    observer.onComplete();
                    latch.countDown();
                })
                .subscribeOn(scheduler)
                .subscribe(
                        item -> {
                        },
                        error -> fail("Unexpected error"),
                        () -> {
                        }
                );

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(2, threadNames.size());
        assertEquals(threadNames.get(0), threadNames.get(1));
    }

    @Test
    void testErrorHandlingInConcurrentOperations() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> receivedError = new AtomicReference<>();
        String errorMessage = "Concurrent error";

        Observable.create(observer -> {
                    observer.onNext(1);
                    throw new RuntimeException(errorMessage);
                })
                .subscribeOn(new CachedThreadScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(
                        item -> fail("Should not receive items after error"),
                        error -> {
                            receivedError.set(error);
                            latch.countDown();
                        },
                        () -> fail("Should not complete")
                );

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertNotNull(receivedError.get());
        assertEquals(errorMessage, receivedError.get().getMessage());
    }
}
