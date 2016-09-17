package me.tatarka.loadie;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;

/**
 * A {@link Loader} that wraps an rxjava {@link Observable}.
 */
public final class RxLoader<T> extends Loader<T> {

    /**
     * Returns a new {@code RxLoader} creator with the given observable to give to the {@link
     * LoaderManager}. The given observable must be cold since it's not expected to run until
     * {@link #start()} is called. You can turn a hot observable into a cold one with {@link
     * rx.Observable#defer(Func0)}. It is also important to note that the loader will <em>not</em>
     * change the scheduler that the observable is run with. For any heavy work you need to ensure
     * it happens off the main thread (for example, with {@link rx.Observable#subscribeOn(Scheduler)}.
     */
    public static <T> Create<RxLoader<T>> create(final Observable<T> observable) {
        return new Create<RxLoader<T>>() {
            @Override
            public RxLoader<T> create() {
                return new RxLoader<>(observable);
            }
        };
    }

    private final rx.Observable<T> observable;
    private Subscription subscription;

    public RxLoader(rx.Observable<T> observable) {
        this.observable = observable;
    }

    @Override
    protected void onStart(final Receiver receiver) {
        subscription = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T value) {
                        receiver.result(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        receiver.error(e);
                    }

                    @Override
                    public void onCompleted() {
                        receiver.success();
                    }
                });
    }

    @Override
    protected void onCancel() {
        subscription.unsubscribe();
        subscription = null;
    }
}
