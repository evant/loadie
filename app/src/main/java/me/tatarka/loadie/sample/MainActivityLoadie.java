package me.tatarka.loadie.sample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import me.tatarka.loadie.AsyncTaskLoader;
import me.tatarka.loadie.Loader;
import me.tatarka.loadie.LoaderManager;
import me.tatarka.loadie.component.LoaderManagerProvider;

/**
 * A few examples of loaders using loadie.
 */
public class MainActivityLoadie extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoaderManager loaderManager = LoaderManagerProvider.forActivity(this);

        setContentView(R.layout.activity_main);

        final TextView loader1Text = (TextView) findViewById(R.id.loader1);
        final TextView loader2Text = (TextView) findViewById(R.id.loader2);
        final TextView loader3Text = (TextView) findViewById(R.id.loader3);

        MyLoader loader1 = loaderManager.init(0, MyLoader.CREATE, new Loader.CallbacksAdapter<String>() {
            @Override
            public void onLoaderStart() {
                loader1Text.setText("Loading....");
            }

            @Override
            public void onLoaderResult(String result) {
                loader1Text.setText(result);
            }
        });
        final MyLoader loader2 = loaderManager.init(1, MyLoader.CREATE, new Loader.CallbacksAdapter<String>() {
            @Override
            public void onLoaderStart() {
                loader2Text.setText("Loading...");
            }

            @Override
            public void onLoaderResult(String result) {
                loader2Text.setText(result);
            }
        });
        CurrentTimeLoader loader3 = loaderManager.init(2, CurrentTimeLoader.CREATE, new Loader.CallbacksAdapter<Long>() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm:ss a", Locale.US);

            @Override
            public void onLoaderResult(Long result) {
                loader3Text.setText(timeFormat.format(new Date(result)));
            }
        });

        loader1.start();

        findViewById(R.id.loader2_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader2.restart();
            }
        });

        loader3.start();
    }

    public static class MyLoader extends AsyncTaskLoader<String> {

        public static Create<MyLoader> CREATE = new Create<MyLoader>() {
            @Override
            public MyLoader create() {
                return new MyLoader();
            }
        };

        @Override
        protected String doInBackground() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return null;
            }
            return "complete";
        }
    }

    public static class CurrentTimeLoader extends Loader<Long> {

        public static Create<CurrentTimeLoader> CREATE = new Create<CurrentTimeLoader>() {
            @Override
            public CurrentTimeLoader create() {
                return new CurrentTimeLoader();
            }
        };

        Handler handler = new Handler(Looper.getMainLooper());
        TimerTask timerTask;

        @Override
        protected void onStart(final Receiver receiver) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            receiver.result(System.currentTimeMillis());
                        }
                    });
                }
            };
            new Timer().schedule(timerTask, 0, 1000);
        }

        @Override
        protected void onCancel() {
            timerTask.cancel();
        }
    }
}
