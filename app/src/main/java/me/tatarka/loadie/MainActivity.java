package me.tatarka.loadie;

import android.app.Activity;
import android.os.Bundle;

import me.tatarka.loadie.sample.R;

public class MainActivity extends Activity {

    LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get a retained instance or create a new one.
        loaderManager = (LoaderManager) getLastNonConfigurationInstance();
        if (loaderManager == null) {
            loaderManager = new LoaderManager();
        }
        // We immediately have views, start delivering callbacks.
        loaderManager.start();
        setContentView(R.layout.activity_main);
        // we don't need to call stop() because the views are never detached. It would be necessary
        // in, ex a fragment where the views could be destroyed but the fragment is still around.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            // Activity is done, cancel any loaders.
            loaderManager.destroy();
        } else {
            // Otherwise, just detach callbacks.
            loaderManager.detach();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return loaderManager;
    }
}
