package components;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {
    private final Consumer<Observer<T>> source;

    public Observable(Consumer<Observer<T>> source) {
        this.source = source;
    }

    public static <T> Observable<T> create(Consumer<Observer<T>> source) {
        return new Observable<>(source);
    }

    public Disposable subscribe(Observer<T> observer) {
        AtomicBoolean disposed = new AtomicBoolean(false);
        try {
            source.accept(new Observer<T>() {
                @Override
                public void onNext(T t) {
                    if (!disposed.get()) {
                        observer.onNext(t);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    if (!disposed.get()) {
                        observer.onError(e);
                    }
                }

                @Override
                public void onComplete() {
                    if (!disposed.get()) {
                        observer.onComplete();
                    }
                }
            });

        } catch (Exception e) {
            if (!disposed.get()) {
                observer.onError(e);
            }
        }

        return new Disposable() {
            @Override
            public void dispose() {
                disposed.set(true);
            }

            @Override
            public boolean isDisposed() {
                return disposed.get();
            }
        };
    }

    public Disposable subcribe(Consumer<T> onNext, Consumer<Throwable> onError, Runnable onComplete) {
        return subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                onNext.accept(item);
            }

            @Override
            public void onError(Throwable e) {
                onError.accept(e);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        });
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return new Observable<>(observer -> subcribe(
                item -> {
                    try {
                        observer.onNext(mapper.apply(item));
                    } catch (Exception e) {
                        observer.onError(e);
                    }
                },
                observer::onError,
                observer::onComplete
        ));
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return new Observable<>(observer -> subcribe(
                item -> {
                    try {
                        if (predicate.test(item)) {
                            observer.onNext(item);
                        }
                    } catch (Exception e) {
                        observer.onError(e);
                    }
                },
                observer::onError,
                observer::onComplete
        ));
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return new Observable<>(observer -> {
            AtomicBoolean disposed = new AtomicBoolean(false);
            subcribe(
                    item -> {
                        if (!disposed.get()) {
                            try {
                                Observable<R> innerObservable = mapper.apply(item);
                                innerObservable.subcribe(
                                        observer::onNext,
                                        observer::onError,
                                        () -> {
                                        }
                                );
                            } catch (Exception e) {
                                observer.onError(e);
                            }
                        }
                    },
                    observer::onError,
                    observer::onComplete
            );
        });
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(observer -> scheduler.execute(() -> subscribe(observer)));
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(observer -> subcribe(
                item -> scheduler.execute(() -> observer.onNext(item)),
                error -> scheduler.execute(() -> observer.onError(error)),
                () -> scheduler.execute(observer::onComplete)
        ));
    }
}
