package components;

public interface Observer<T> {

    void onNext(T item);

    void onError(Throwable e);

    void onComplete();
}
