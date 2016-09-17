package me.tatarka.loadie;

import android.support.v4.os.OperationCanceledException;

import java.util.concurrent.Executor;

public class TestAsyncTaskLoader<T> extends AsyncTaskLoader<T> {

    public static <T> Loader.Create<TestAsyncTaskLoader<T>> create() {
        return new Loader.Create<TestAsyncTaskLoader<T>>() {
            @Override
            public TestAsyncTaskLoader<T> create() {
                return new TestAsyncTaskLoader<>();
            }
        };
    }

    public TestAsyncTaskLoader() {
        super();
    }

    public TestAsyncTaskLoader(Executor executor) {
        super(executor);
    }

    private T result;
    private boolean throwOperationCanceled;
    private boolean throwException;

    @Override
    protected T doInBackground() {
        if (throwException) {
            throw new RuntimeException();
        } else if (throwOperationCanceled) {
            throw new OperationCanceledException();
        } else {
            return result;
        }
    }

    public void setResult(T result) {
        this.result = result;
    }

    public void throwCanceledException() {
        this.throwOperationCanceled = true;
    }

    public void throwException() {
        throwException = true;
    }
}
