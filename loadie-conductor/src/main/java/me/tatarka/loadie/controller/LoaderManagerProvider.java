package me.tatarka.loadie.controller;

import android.support.annotation.NonNull;
import android.view.View;

import com.bluelinelabs.conductor.Controller;

import me.tatarka.loadie.LoaderManager;

public class LoaderManagerProvider {

    /**
     * Obtains a {@link LoaderManager} for the given {@link Controller}.
     */
    public static LoaderManager forController(Controller controller) {
        final LoaderManager loaderManager = new LoaderManager();
        controller.addLifecycleListener(new Controller.LifecycleListener() {

            @Override
            public void preAttach(@NonNull Controller controller, @NonNull View view) {
                loaderManager.start();
            }

            @Override
            public void postDetach(@NonNull Controller controller, @NonNull View view) {
                loaderManager.stop();
            }

            @Override
            public void postDestroy(@NonNull Controller controller) {
                loaderManager.destroy();
            }
        });
        return loaderManager;
    }
}
