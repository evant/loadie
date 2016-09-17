package me.tatarka.loadie;

import android.support.annotation.MainThread;
import android.util.SparseArray;

/**
 * Manges a set of loaders in the same scope. You should retain this instance across configuration
 * changes, calling {@link #start()}, {@link #stop()}, {@link #detach()}, and {@link #destroy()}
 * as needed.
 * <pre>
 * init()+-->start()+-->stop()+-->detach()+-->destroy()
 *   ^         ^----------+          +
 *   +-------------------------------+
 * </pre>
 */
public class LoaderManager {

    private SparseArray<Loader<?>> loaders = new SparseArray<>(1);
    private SparseArray<Loader.Callbacks<?>> loaderCallbacks = new SparseArray<>(1);

    /**
     * Initializes a loader, creating it if it doesn't already exist.
     *
     * @param id        The id to init the loader with, this must be unique for this loader
     *                  manager.
     * @param create    Method for creating the loader if it does not already exist.
     * @param callbacks The loader callbacks.
     */
    @MainThread
    public <T, L extends Loader<T>> L init(int id, Loader.Create<L> create, Loader.Callbacks<T> callbacks) {
        @SuppressWarnings("unchecked")
        L loader = (L) loaders.get(id);
        if (loader == null) {
            loader = create.create();
            loaders.put(id, loader);
        }
        if (loader.isAttached()) {
            throw new IllegalStateException("Loader " + loader + " already has callbacks. Make sure you are using unique ids and the LoaderManager was properly detached.");
        }
        if (loaderCallbacks != null) {
            if (loaderCallbacks.get(id) != null)  {
                throw new IllegalStateException("Loader " + loader + " already has callbacks. Make sure you are using unique ids and the LoaderManager was properly detached.");
            }
            loaderCallbacks.put(id, callbacks);
        } else {
            loader.setCallbacks(callbacks);
        }
        return loader;
    }

    /**
     * Destroys and removes the loader with the given id.
     */
    @MainThread
    public void remove(int id) {
        Loader<?> loader = loaders.get(id);
        if (loaderCallbacks != null) {
            loaderCallbacks.remove(id);
        }
        if (loader != null) {
            loader.setCallbacks(null);
            loader.destroy();
            loaders.remove(id);
        }
    }

    /**
     * Starts delivering results to it's callbacks.
     */
    @MainThread
    @SuppressWarnings("unchecked")
    public void start() {
        if (loaderCallbacks == null) {
            return;
        }
        for (int i = 0, size = loaders.size(); i < size; i++) {
            Loader loader = loaders.get(i);
            if (loader != null) {
                Loader.Callbacks<?> callbacks = this.loaderCallbacks.get(loaders.keyAt(i));
                loader.setCallbacks(callbacks);
            }
        }
        loaderCallbacks = null;
    }

    /**
     * Stops delivering loader results to it's callbacks. Unlike {@link #detach()} this does
     * <em>not</em> destroy references to the callbacks so they you can start delivering results to
     * them again without another {@link #init(int, Loader.Create, Loader.Callbacks)}.
     */
    @MainThread
    public void stop() {
        if (loaderCallbacks != null) {
           return; 
        }
        loaderCallbacks = new SparseArray<>(loaders.size());
        for (int i = 0, size = loaders.size(); i < size; i++) {
            Loader<?> loader = loaders.get(i);
            if (loader != null) {
                loaderCallbacks.put(loaders.keyAt(i), loader.getCallbacks());
                loader.setCallbacks(null);
            }
        }
    }

    /**
     * Detaches the callbacks from the loaders. You must call then when your context is being
     * destroyed to prevent leaks.
     */
    @MainThread
    public void detach() {
        loaderCallbacks = new SparseArray<>(loaders.size());
        for (int i = 0, size = loaders.size(); i < size; i++) {
            Loader<?> loader = loaders.get(i);
            if (loader != null) {
                loader.setCallbacks(null);
            }
        }
    }

    /**
     * Detaches and destroys all loaders. You should call this when you know you won't need any
     * anymore like when your activity is finishing.
     */
    @MainThread
    public void destroy() {
        loaderCallbacks = null;
        for (int i = 0, size = loaders.size(); i < size; i++) {
            Loader<?> loader = loaders.get(i);
            if (loader != null) {
                loader.destroy();
            }
        }
        loaders.clear();
    }
}
