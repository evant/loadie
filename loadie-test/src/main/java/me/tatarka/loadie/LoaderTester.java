package me.tatarka.loadie;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

public class LoaderTester {

    /**
     * Indicates the {@link ArrayBlockingQueue} has no more items.
     */
    static final Object END = new Object();

    /**
     * Runs the loader synchronously until it either completes or delivers an error, then destroys
     * the loader. No results will be returned but the error will be thrown if delivered.
     */
    @SuppressWarnings("unchecked")
    public static <T> void runSynchronously(final Loader<T> loader) throws Throwable {
        final Semaphore semaphore = new Semaphore(0);
        final Object[] maybeError = new Object[1];

        final Handler mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                loader.setCallbacks(new DelegateCallbacks<T>(loader.getCallbacks()) {
                    @Override
                    public void onLoaderSuccess() {
                        super.onLoaderSuccess();
                        loader.destroy();
                        semaphore.release();
                    }

                    @Override
                    public void onLoaderError(Throwable error) {
                        super.onLoaderError(error);
                        maybeError[0] = error;
                        loader.destroy();
                        semaphore.release();
                    }
                });
                loader.start();
            }
        };
        mainThreadHandler.sendEmptyMessage(0);
        semaphore.acquire();

        if (maybeError[0] != null) {
            throw (Throwable) maybeError[0];
        }
    }

    /**
     * Runs the loader synchronously and returns the result or throws in case of an error, then
     * destroys the loader. If the loader can return more than one result, only the first one will
     * be returned and the loader will then be immediately canceled.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getResultSynchronously(final Loader<T> loader) throws Throwable {
        final Semaphore semaphore = new Semaphore(0);
        final Object[] resultOrError = new Object[2];

        final Handler mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                loader.setCallbacks(new DelegateCallbacks<T>(loader.getCallbacks()) {
                    @Override
                    public void onLoaderResult(T result) {
                        super.onLoaderResult(result);
                        resultOrError[0] = result;
                        loader.destroy();
                        semaphore.release();
                    }

                    @Override
                    public void onLoaderError(Throwable error) {
                        super.onLoaderError(error);
                        resultOrError[1] = error;
                        loader.destroy();
                        semaphore.release();
                    }
                });
                loader.start();
            }
        };
        mainThreadHandler.sendEmptyMessage(0);
        semaphore.acquire();

        if (resultOrError[1] != null) {
            throw (Throwable) resultOrError[1];
        } else {
            return (T) resultOrError[0];
        }
    }

    /**
     * Returns an iterator that you may iterate over to get each result of the loader. While this
     * method returns immediately, calling {@link Iterator#hasNext()} or {@link Iterator#next()}
     * will block until the next result has arrived. The iterator will end when there are no more
     * results, and will throw an exception wrapped in a runtime exception if the loader delivers
     * an error.
     */
    public static <T> Iterator<T> getResultsSynchronously(final Loader<T> loader) {
        return new Iterator<T>() {

            final ArrayBlockingQueue<Object[]> queue = new ArrayBlockingQueue<>(1);
            boolean needsNext = true;
            Object value;

            final Handler mainThreadHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    loader.setCallbacks(new DelegateCallbacks<T>(loader.getCallbacks()) {
                        @Override
                        public void onLoaderResult(T result) {
                            super.onLoaderResult(result);
                            try {
                                queue.put(new Object[]{result, null});
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onLoaderError(Throwable error) {
                            super.onLoaderError(error);
                            try {
                                queue.put(new Object[]{null, error});
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public void onLoaderSuccess() {
                            super.onLoaderSuccess();
                            try {
                                queue.put(new Object[]{END, null});
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    loader.start();
                }
            };

            {
                mainThreadHandler.sendEmptyMessage(0);
            }

            @Override
            public boolean hasNext() {
                ensureNext();
                needsNext = false;
                return value != END;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                ensureNext();
                needsNext = true;
                if (value == END) {
                    throw new NoSuchElementException();
                }
                return (T) value;
            }

            private void ensureNext() {
                Object[] result;
                if (needsNext) {
                    try {
                        result = queue.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("waiting thread interrupted", e);
                    }
                    if (result[1] != null) {
                        throw new RuntimeException((Throwable) result[1]);
                    }
                    value = result[0];
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * It may be useful for the test to add it's own callbacks, delegate to them so they aren't
     * lost.
     */
    private static class DelegateCallbacks<T> implements Loader.Callbacks<T> {

        final Loader.Callbacks<T> delegate;

        DelegateCallbacks(Loader.Callbacks<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onLoaderStart() {
            if (delegate != null) {
                delegate.onLoaderStart();
            }
        }

        @Override
        public void onLoaderResult(T result) {
            if (delegate != null) {
                delegate.onLoaderResult(result);
            }
        }

        @Override
        public void onLoaderError(Throwable error) {
            if (delegate != null) {
                delegate.onLoaderError(error);
            }
        }

        @Override
        public void onLoaderSuccess() {
            if (delegate != null) {
                delegate.onLoaderSuccess();
            }
        }
    }
}
