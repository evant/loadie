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

    static final int LOADER1 = 0;
    static final int LOADER2 = 1;
    static final int LOADER3 = 2;
    static final int LOADER4 = 3;

    LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = getSupportLoaderManager();
        setContentView(R.layout.activity_main);
        loader1();
        loader2();
        loader3();
        loader4();
    }

    private void loader1() {
        final TextView loader1Text = (TextView) findViewById(R.id.loader1);
        loader1Text.setText("Loading...");
        MyLoader loader1 = (MyLoader) loaderManager.initLoader(LOADER1, null, new LoaderCallbacksAdapter<String>() {
            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                return new MyLoader(MainActivityLoader.this);
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
                loader1Text.setText(data);
            }
        });
        loader1.start();
    }

    private void loader2() {
        final TextView loader2Text = (TextView) findViewById(R.id.loader2);
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
        MyLoader loader2 = (MyLoader) loaderManager.initLoader(LOADER2, null, loader2Callbacks);
        if (loader2.isLoading) {
            loader2Text.setText("Loading...");
        }
        findViewById(R.id.loader2_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader2Text.setText("Loading...");
                loaderManager.restartLoader(LOADER2, null, loader2Callbacks).forceLoad();
            }
        });
    }

    private void loader3() {
        final TextView loader3Text = (TextView) findViewById(R.id.loader3);
        loaderManager.initLoader(LOADER3, null, new LoaderCallbacksAdapter<Long>() {

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

    private void loader4() {
        final TextView loader4Text = (TextView) findViewById(R.id.loader4);
        final LoaderCallbacksAdapter<String> loader4Callbacks = new LoaderCallbacksAdapter<String>() {
            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                return new MyArgLoader(MainActivityLoader.this, args.getInt("arg"));
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
                loader4Text.setText(data);
            }
        };
        MyArgLoader loader4 = (MyArgLoader) loaderManager.<String>getLoader(LOADER4);
        if (loader4 != null) {
            loaderManager.initLoader(LOADER4, null, loader4Callbacks);
            if (loader4.isLoading) {
                loader4Text.setText("Loading...");
            }
        }

        findViewById(R.id.arg1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader4Text.setText("Loading...");
                Bundle args = new Bundle();
                args.putInt("arg", 1);
                loaderManager.restartLoader(LOADER4, args, loader4Callbacks).forceLoad();
            }
        });
        findViewById(R.id.arg2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader4Text.setText("Loading...");
                Bundle args = new Bundle();
                args.putInt("arg", 2);
                loaderManager.restartLoader(LOADER4, args, loader4Callbacks).forceLoad();
            }
        });
    }

    // This loader doesn't start until forceLoad() is called.
    public static class MyLoader extends AsyncTaskLoader<String> {

        boolean isLoading;
        boolean hasData;

        public MyLoader(Context context) {
            super(context);
        }

        public void start() {
            if (!hasData) {
                forceLoad();
            }
        }

        @Override
        public void forceLoad() {
            isLoading = true;
            super.forceLoad();
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

        @Override
        public void deliverResult(String data) {
            isLoading = false;
            hasData = true;
            super.deliverResult(data);
        }
    }

    public static class MyArgLoader extends AsyncTaskLoader<String> {
        boolean hasData;
        boolean isLoading;
        int arg;

        public MyArgLoader(Context context, int arg) {
            super(context);
            this.arg = arg;
        }

        public void start() {
            if (!hasData) {
                forceLoad();
            }
        }

        @Override
        public void forceLoad() {
            isLoading = true;
            super.forceLoad();
        }

        @Override
        public String loadInBackground() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return null;
            }
            return "complete " + arg;
        }

        @Override
        public void deliverResult(String data) {
            isLoading = false;
            hasData = true;
            super.deliverResult(data);
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
