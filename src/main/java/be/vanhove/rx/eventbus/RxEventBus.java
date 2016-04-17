package be.vanhove.rx.eventbus;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import rx.Observable;
import rx.Subscriber;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RxEventBus {

    private final EventBus eventBus;

    public RxEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void register(Object object) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (isRegistrable(method)) {
                final RxSubscribe annotation = method.getAnnotation(RxSubscribe.class);
                Observable<Object> observable = createObservable(annotation);
                invokeMethod(object, method, observable);
            }
        }
    }

    private boolean isRegistrable(Method method) {
        if (method.isAnnotationPresent(RxSubscribe.class)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            return parameterTypes.length == 1
                    && parameterTypes[0].equals(Observable.class);
        }
        return false;
    }

    private Observable<Object> createObservable(final RxSubscribe annotation) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(final Subscriber<? super Object> subscriber) {
                eventBus.register(new Object() {
                    @Subscribe
                    void onNext(Object event) {
                        if (annotation.value().isInstance(event)) {
                            subscriber.onNext(event);
                        }
                    }
                });
            }
        });
    }

    private void invokeMethod(Object object, Method method, Observable<Object> observable) {
        try {
            method.invoke(object, observable);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
