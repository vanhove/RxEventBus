package be.vanhove.rx.eventbus;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;

import static org.assertj.core.api.Assertions.assertThat;
import static rx.schedulers.Schedulers.immediate;

public class RxEventBusTest {

    private RxEventBus rxEventBus;
    private EventBus eventBus;

    @Before
    public void setUp() {
        eventBus = new EventBus();
        rxEventBus = new RxEventBus(eventBus);
    }

    @Test
    public void register() {
        TestObject testObject = new TestObject();
        rxEventBus.register(testObject);

        eventBus.post("Test");

        assertThat(testObject.counterValue()).isEqualTo(1);
    }

    private class TestObject {

        private volatile int counter = 0;

        @RxSubscribe(String.class)
        public void handle(Observable<String> observable) {
            observable.subscribeOn(immediate()).forEach(new Action1<String>() {
                @Override
                public void call(String integer) {
                    counter++;
                }
            });
        }

        public int counterValue() {
            return counter;
        }
    }
}