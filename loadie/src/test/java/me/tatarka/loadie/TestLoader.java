package me.tatarka.loadie;

public class TestLoader<T> extends Loader<T> {
    public static <T> Loader.Create<TestLoader<T>> create() {
        return new Loader.Create<TestLoader<T>>() {
            @Override
            public TestLoader<T> create() {
                return new TestLoader<>();
            }
        };
    }

    private Receiver receiver;
    private boolean isCanceled;

    @Override
    protected void onStart(Receiver receiver) {
        this.receiver = receiver;
        isCanceled = false;
    }

    @Override
    protected void onCancel() {
        isCanceled = true;
    }

    public void result(T result) {
        if (receiver == null) {
            throw new IllegalStateException("Loader isn't running");
        }
        receiver.result(result);
    }

    public void success() {
        if (receiver == null) {
            throw new IllegalStateException("Loader isn't running");
        }
        receiver.success();
    }

    public void error(Throwable e) {
        if (receiver == null) {
            throw new IllegalStateException("Loader isn't running");
        }
        receiver.error(e);
    }

    public boolean isCanceled() {
        return isCanceled;
    }
}
