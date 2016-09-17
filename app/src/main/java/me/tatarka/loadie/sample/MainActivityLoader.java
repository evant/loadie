package me.tatarka.loadie.sample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A few examples of loaders using framework loaders.
 */
public class MainActivityLoader extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final LoaderManager loaderManager = getSupportLoaderManager();

        setContentView(R.layout.activity_main);

        final TextView loader1Text = (TextView) findViewById(R.id.loader1);
        final TextView loader2Text = (TextView) findViewById(R.id.loader2);
        final TextView loader3Text = (TextView) findViewById(R.id.loader3);

        loader1Text.setText("Loading...");
        boolean loader1Running = loaderManager.getLoader(0) != null;
        MyLoader loader1 = (MyLoader) loaderManager.initLoader(0, null, new LoaderCallbacksAdapter<String>() {
            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                return new MyLoader(MainActivityLoader.this);
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
                loader1Text.setText(data);
            }
        });
        if (!loader1Running) {
            loader1.forceLoad();
        }

        boolean loader2Running = loaderManager.getLoader(1) != null;
        final LoaderCallbacksAdapter<String> loader2Callbacks = new LoaderCallbacksAdapter<String>() {
            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                return new MyLoader(MainActivityLoader.this);
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
                loader2Text.setText(data);
            }
        };
        if (loader2Running) {
            loader2Text.setText("Loading...");
            loaderManager.initLoader(1, null, loader2Callbacks);
        }

        findViewById(R.id.loader2_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader2Text.setText("Loading...");
                loaderManager.restartLoader(1, null, loader2Callbacks).forceLoad();
            }
        });

        loaderManager.initLoader(2, null, new LoaderCallbacksAdapter<Long>() {

            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm:ss a", Locale.US);

            @Override
            public Loader<Long> onCreateLoader(int id, Bundle args) {
                return new CurrentTimeLoader(MainActivityLoader.this);
            }

            @Override
            public void onLoadFinished(Loader<Long> loader, Long data) {
                loader3Text.setText(timeFormat.format(new Date(data)));
            }
        });
    }

    // This loader doesn't start until forceLoad() is called.
    public static class MyLoader extends AsyncTaskLoader<String> {

        public MyLoader(Context context) {
            super(context);
        }

        @Override
        public String loadInBackground() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return null;
            }
            return "complete";
        }
    }

    // This loader will start delivering results immediately.
    public static class CurrentTimeLoader extends Loader<Long> {

        Handler handler = new Handler(Looper.getMainLooper());
        TimerTask timerTask;

        public CurrentTimeLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            if (timerTask != null) {
                timerTask.cancel();
            }
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isStarted()) {
                                deliverResult(System.currentTimeMillis());
                            }
                        }
                    });
                }
            };
            new Timer().schedule(timerTask, 0, 1000);
        }

        @Override
        protected void onStopLoading() {
            if (timerTask != null) {
                timerTask.cancel();
            }
        }
    }

    public static abstract class LoaderCallbacksAdapter<D> implements LoaderManager.LoaderCallbacks<D> {

        @Override
        public void onLoadFinished(Loader<D> loader, D data) {

        }

        @Override
        public void onLoaderReset(Loader<D> loader) {

        }
    }
}
