package me.tatarka.loadie;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A loader helps connect async operations to your views. It is retained across configuration
 * changes with {@link LoaderManager}. You run your operation in {@link #onStart(Receiver)}} and
 * deliver the result with {@link Receiver#result(Object)}. Note that {@link #onStart(Receiver)}}
 * is <em>not</em> run in a background thread. You should handle threading yourself with an {@link
 * android.os.AsyncTask} or other mechanism. The result will be cached and re-delivered after a
 * configuration change. You may also optionally implement {@link #onCancel()} if you can cancel
 * your work when it is no longer needed.
 *
 * @param <T> The type of result that the loader will deliver
 */
public abstract class Loader<T> {
    private static final int STATE_RUNNING = 1;
    private static final int STATE_HAS_RESULT = 1 << 1;
    private static final int STATE_SUCCESS = 1 << 2;
    private static final int STATE_ERROR = 1 << 3;
    private static final int STATE_DESTROYED = 1 << 4;

    private static final int CALLBACKS_START = 1;
    private static final int CALLBACKS_RESULT = 2;
    private static final int CALLBACKS_SUCCESS = 4;
    private static final int CALLBACKS_ERROR = 5;

    private static boolean isRunning(int state) {
        return (state & STATE_RUNNING) == STATE_RUNNING;
    }

    private static boolean isSuccess(int state) {
        return (state & STATE_SUCCESS) == STATE_SUCCESS;
    }

    private static boolean isError(int state) {
        return (state & STATE_ERROR) == STATE_ERROR;
    }

    private static boolean isDestroyed(int state) {
        return (state & STATE_DESTROYED) == STATE_DESTROYED;
    }

    /**
     * Throws an {@link IllegalStateException} if loader is destroyed.
     */
    private static void checkDestroyed(String method, int state) {
        if (isDestroyed(state)) {
            throw new IllegalStateException("cannot call " + method + "() after destroy()");
        }
    }

    @Nullable
    Callbacks<T> callbacks;
    @Nullable
    Receiver receiver;
    T cachedResult;
    Throwable cachedError;
    AtomicInteger state = new AtomicInteger();

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int callback = msg.arg1;
            @SuppressWarnings("unchecked")
            HandlerArgs<Object> args = (HandlerArgs<Object>) msg.obj;
            if ((callback & CALLBACKS_RESULT) == CALLBACKS_RESULT) {
                args.callbacks.onLoaderResult(args.cachedResult);
            } else if ((callback & CALLBACKS_START) == CALLBACKS_START) {
                args.callbacks.onLoaderStart();
            }
            if ((callback & CALLBACKS_SUCCESS) == CALLBACKS_SUCCESS) {
                args.callbacks.onLoaderSuccess();
            } else if ((callback & CALLBACKS_ERROR) == CALLBACKS_ERROR) {
                args.callbacks.onLoaderError(args.cachedError);
            }
        }
    };

    /**
     * Starts the loader if it has not already been started, calling {@link #onStart(Receiver)}}
     * and triggering {@link Callbacks#onLoaderStart()}. This must be called on the main thread.
     */
    @MainThread
    public final void start() {
        int s = state.get();
        checkDestroyed("start", s);
        if (!isRunning(s) && !isSuccess(s) && !isError(s)) {
            state.set(STATE_RUNNING);
            if (callbacks != null) {
                callbacks.onLoaderStart();
            }
            receiver = new Receiver();
            receiver.myState |= Receiver.SYNCHRONOUS;
            onStart(receiver);
            receiver.myState &= ~Receiver.SYNCHRONOUS;
        }
    }

    /**
     * Cancels the loader if it's running, calling {@link #onCancel()} and removing any cached
     * data. This must be called on the main thread.
     */
    @MainThread
    public final void cancel() {
        int s = state.get();
        checkDestroyed("cancel", s);
        cachedResult = null;
        if (receiver != null) {
            receiver.myState = Receiver.CANCELED;
            receiver = null;
        }
        handler.removeMessages(0);
        if (isRunning(s)) {
            onCancel();
        }
        state.set(0);
    }

    /**
     * Forces the loader to restart. This is a convenience method for calling {@link #cancel()}
     * followed by {@link #start()}. This must be called on the main thread.
     */
    @MainThread
    public final void restart() {
        cancel();
        start();
    }

    /**
     * Destroys the loader, canceling it if necessary. This is normally called for you by {@link
     * LoaderManager}. This will trigger {@link #onDestroy()} allowing you to clean up any
     * resources if necessary. You should not call any other methods on the loader after this.
     */
    @MainThread
    public final void destroy() {
        int s = state.get();
        checkDestroyed("destroy", s);
        cancel();
        state.set(STATE_DESTROYED);
        callbacks = null;
        onDestroy();
    }

    /**
     * Returns true if the loader is running. That is, if it has been started and not stopped and
     * {@link Receiver#success()} has not been called. If this is true than you may expect one or
     * more results to be delivered.
     */
    public final boolean isRunning() {
        return isRunning(state.get());
    }

    /**
     * Returns true if the loader has a result. If it does, the result will be delivered
     * immediately after the loader has been re-attached.
     */
    public final boolean hasResult() {
        return (state.get() & STATE_HAS_RESULT) == STATE_HAS_RESULT;
    }

    /**
     * Returns true if the loader has been completed successfully. If it has, then it has been
     * started but no more results may be delivered and it's no longer running.
     */
    public final boolean isSuccess() {
        return isSuccess(state.get());
    }

    /**
     * Returns true if the loader has completed with an error. If it has, then no more results may
     * be delivered and it is no longer running.
     */
    public final boolean isError() {
        return isError(state.get());
    }

    /**
     * Returns true if the loader has been destroyed.
     */
    public final boolean isDestroyed() {
        return isDestroyed(state.get());
    }

    /**
     * Returns true if the loader has non-null callbacks attached with {@link
     * #setCallbacks(Callbacks)}.
     */
    public final boolean isAttached() {
        return callbacks != null;
    }

    /**
     * Do your loader work. This is run on the main thread so you are expected to handle threading
     * yourself either by using an {@link android.os.AsyncTask} or other mechanism. When you have
     * one or more results you should call {@link Receiver#result(Object)} and then call {@link
     * Receiver#success()} when you are done.
     */
    protected abstract void onStart(final Receiver receiver);

    /**
     * Optionally cancel doing work because the result is no longer needed. This will only be
     * called if the loader has been started and is running. This is run on the main thread.
     */
    protected void onCancel() {
    }

    /**
     * Optionally clean up any resources the loader is using when it is destroyed. No methods will
     * be called on the loader after this.
     */
    protected void onDestroy() {
    }

    /**
     * Set the callbacks for the loader. This is normally called for you by {@link LoaderManager}.
     * Data will be delivered of if the loader already has it. Otherwise, {@link
     * Callbacks#onLoaderStart()} will be called to give you the opportunity to show any loading
     * ui. You may pass in null to clear the callbacks. This must be called on the main thread.
     */
    @MainThread
    public final void setCallbacks(@Nullable final Callbacks<T> callbacks) {
        this.callbacks = callbacks;
        handler.removeMessages(0);
        if (callbacks != null) {
            int methods = 0;
            if (hasResult()) {
                methods |= CALLBACKS_RESULT;
            } else if (isRunning()) {
                methods |= CALLBACKS_START;
            }
            if (isSuccess()) {
                methods |= CALLBACKS_SUCCESS;
            } else if (isError()) {
                methods |= CALLBACKS_ERROR;
            }
            dispatchCallbacks(callbacks, methods);
        }
    }

    /**
     * Returns the callbacks that was set by {@link #setCallbacks(Callbacks)}, or null if none are
     * set.
     */
    @Nullable
    @MainThread
    public final Callbacks<T> getCallbacks() {
        return callbacks;
    }

    /**
     * So that callback methods are consistently async, we post them to a handler.
     */
    void dispatchCallbacks(Callbacks<T> callbacks, int methods) {
        Message message = handler.obtainMessage(0);
        message.obj = new HandlerArgs<>(cachedResult, cachedError, callbacks);
        message.arg1 = methods;
        handler.dispatchMessage(message);
    }

    /**
     * Receives results from the loader and notifies the loader's callbacks.
     */
    public final class Receiver {
        /**
         * If the receiver is canceled, ignore any delivered results.
         */
        private static final int CANCELED = 1;
        /**
         * If the receiver is success, any calls to {@link #result(Object)} is an error.
         */
        private static final int SUCCESS = 2;
        /**
         * If the receiver is error, any calls to {@link #result(Object)} is an error.
         */
        private static final int ERROR = 3;

        /**
         * It's possible that a result is immediately delivered inside {@link
         * #onStart(Loader.Receiver)}. Because we don't want to surprise our consumer with
         * immediate results, we should post them to a handler in this case.
         */
        private static final int SYNCHRONOUS = 4;

        int myState = 0;

        /**
         * Deliver a result to {@link Callbacks#onLoaderResult(Object)}. When you are done
         * delivering results you should call {@link #success()}. If the loader has already been
         * canceled, then the result is ignored as it is not expected to be used. This must be run
         * on the main thread.
         */
        @MainThread
        public final void result(T result) {
            if ((myState & CANCELED) == CANCELED) {
                return;
            }
            if ((myState & SUCCESS) == SUCCESS) {
                throw new IllegalStateException("cannot call result() after success()");
            }
            if ((myState & ERROR) == ERROR) {
                throw new IllegalStateException("cannot call result() after error()");
            }

            int s = state.get();
            state.set(s | STATE_HAS_RESULT);

            cachedResult = result;
            if (callbacks != null) {
                if ((myState & SYNCHRONOUS) == SYNCHRONOUS) {
                    dispatchCallbacks(callbacks, CALLBACKS_RESULT);
                } else {
                    callbacks.onLoaderResult(result);
                }
            }
        }

        /**
         * Deliver a result to {@link Callbacks#onLoaderResult(Object)}. If there is an error, you
         * should not deliver any more results, nor call {@link #success()}. If the loader has
         * already been canceled, then the result is ignored as it is not expected to be used. This
         * must be run on the main thread.
         */
        @MainThread
        public final void error(Throwable error) {
            if ((myState & CANCELED) == CANCELED) {
                return;
            }
            if ((myState & SUCCESS) == SUCCESS) {
                throw new IllegalStateException("cannot call result() after success()");
            }
            if ((myState & ERROR) == ERROR) {
                throw new IllegalStateException("error() already called");
            }

            int s = state.get();
            state.set((s & ~STATE_RUNNING) | STATE_ERROR);

            cachedError = error;
            if (callbacks != null) {
                if ((myState & SYNCHRONOUS) == SYNCHRONOUS) {
                    dispatchCallbacks(callbacks, CALLBACKS_ERROR);
                } else {
                    callbacks.onLoaderError(error);
                }
            }
        }

        /**
         * Marks the loader as success and triggers {@link Callbacks#onLoaderSuccess()} If the
         * loader has already been canceled then the call will be ignored. This must be called on
         * the main thread.
         */
        @MainThread
        public final void success() {
            if ((myState & CANCELED) == CANCELED) {
                return;
            }
            if ((myState & SUCCESS) == SUCCESS) {
                throw new IllegalStateException("success() already called");
            }
            if ((myState & ERROR) == ERROR) {
                throw new IllegalStateException("cannot call success() after error()");
            }
            myState = SUCCESS;

            int s = state.get();
            state.set((s & ~STATE_RUNNING) | STATE_SUCCESS);

            if (callbacks != null) {
                if ((myState & SYNCHRONOUS) == SYNCHRONOUS) {
                    dispatchCallbacks(callbacks, CALLBACKS_SUCCESS);
                } else {
                    callbacks.onLoaderSuccess();
                }
            }
        }

        /**
         * Delivers a result and completes the loader. This is a convenience for calling {@link
         * #result(Object)} followed by {@link #success()}. This must be called on the main thread.
         */
        @MainThread
        public void success(T result) {
            result(result);
            success();
        }
    }

    /**
     * Creates a {@link Loader}. This is passed into {@link LoaderManager#init(int, Create,
     * Callbacks)}
     * instead of an instance of the loader itself because a new loader may not need to be created
     * if one already exists.
     *
     * It is common for implementations of {@link Loader} to provide an implementation of this for
     * ease of use for consumers.
     */
    public interface Create<L extends Loader> {
        L create();
    }

    /**
     * Implement this callback to listen to data from the loader.
     */
    public interface Callbacks<T> {
        /**
         * Called when the loader is started and when the loader is running but does not yet have a
         * result when the callback is attached.
         */
        void onLoaderStart();

        /**
         * Called when the loader delivers a result and with the last result when the callback is
         * attached if it exists.
         */
        void onLoaderResult(T result);

        /**
         * Called when the loader terminates with an error. No more results will be delivered and
         * {@link #onLoaderSuccess()} will not be called.
         */
        void onLoaderError(Throwable error);

        /**
         * Called when the loader completes successfully and will deliver no more results and when
         * the loader was completed when the callback is attached.
         */
        void onLoaderSuccess();
    }

    public static abstract class CallbacksAdapter<T> implements Callbacks<T> {
        @Override
        public void onLoaderStart() {

        }

        @Override
        public void onLoaderResult(T result) {

        }

        @Override
        public void onLoaderError(Throwable error) {

        }

        @Override
        public void onLoaderSuccess() {

        }
    }

    private static final class HandlerArgs<T> {
        final T cachedResult;
        final Throwable cachedError;
        final Callbacks<T> callbacks;

        HandlerArgs(T cachedResult, Throwable cachedError, Callbacks<T> callbacks) {
            this.cachedResult = cachedResult;
            this.cachedError = cachedError;
            this.callbacks = callbacks;
        }
    }
}
