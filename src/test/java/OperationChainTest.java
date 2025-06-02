import components.Observable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class OperationChainTest {

    @Test
    void testOperatorChain() {
        List<Integer> received = new ArrayList<>();

        Observable.create(observer -> {
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onNext(3);
                    observer.onNext(4);
                    observer.onComplete();
                })
                .filter(x -> (int) x % 2 == 0)
                .map(x -> (int) x * 2)
                .subscribe(
                        received::add,
                        error -> fail("Unexpected error"),
                        () -> {
                        }
                );

        assertEquals(2, received.size());
        assertEquals(4, received.get(0));  // 2 * 2
        assertEquals(8, received.get(1));  // 4 * 2
    }
}
