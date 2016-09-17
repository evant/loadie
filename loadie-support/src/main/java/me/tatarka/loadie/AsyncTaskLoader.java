package me.tatarka.loadie;

import android.os.AsyncTask;
import android.support.v4.os.OperationCanceledException;
import android.support.v4.util.Pair;

import java.util.concurrent.Executor;

/**
 * A {@link Loader} that runs your some work in an {@link AsyncTask} similar to {@link
 * android.content.AsyncTaskLoader}.
 */
public abstract class AsyncTaskLoader<T> extends Loader<T> {

    private final Executor executor;
    private AsyncTask<Void, T, Pair<T, Throwable>> task;

    public AsyncTaskLoader() {
        this.executor = AsyncTask.THREAD_POOL_EXECUTOR;
    }

    public AsyncTaskLoader(Executor executor) {
        this.executor = executor;
    }

    /**
     * Called on a worker thread to perform the load and return the result. To support
     * cancellation, this method should periodically check {@link #isRunning()} and return early or
     * throw an {@link android.support.v4.os.OperationCanceledException} if it returns false.
     * Throwing any other exception will be propagated to {@link me.tatarka.loadie.Loader.Callbacks#onLoaderError(Throwable)}.
     */
    protected abstract T doInBackground();

    @Override
    protected final void onStart(final Receiver receiver) {
        task = new AsyncTask<Void, T, Pair<T, Throwable>>() {
            @Override
            protected Pair<T, Throwable> doInBackground(Void... params) {
                try {
                    return Pair.create(AsyncTaskLoader.this.doInBackground(), null);
                } catch (OperationCanceledException e) {
                    if (!isRunning()) {
                        return null;
                    } else {
                        // Thrown when not actually canceled, just propagate exception.
                        throw e;
                    }
                } catch (Exception e) {
                    return Pair.<T, Throwable>create(null, e);
                }
            }

            @Override
            protected void onPostExecute(Pair<T, Throwable> result) {
                if (result.second == null) {
                    receiver.success(result.first);
                } else {
                    receiver.error(result.second);
                }
            }
        };
        task.executeOnExecutor(executor);
    }

    @Override
    protected final void onCancel() {
        task.cancel(false);
        task = null;
    }
}
