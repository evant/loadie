package me.tatarka.loadie.sample;

import android.content.Intent;
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

    static final int LOADER1 = 0;
    static final int LOADER2 = 1;
    static final int LOADER3 = 2;
    static final int LOADER4 = 3;

    LoaderManager loaderManager;
    CurrentTimeLoader loader3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = LoaderManagerProvider.forActivity(this);
        setContentView(R.layout.activity_main);
        loader1();
        loader2();
        loader3();
        loader4();
    }

    private void loader1() {
        final TextView loader1Text = (TextView) findViewById(R.id.loader1);
        MyLoader loader1 = loaderManager.init(LOADER1, MyLoader.CREATE, new Loader.CallbacksAdapter<String>() {
            @Override
            public void onLoaderStart() {
                loader1Text.setText("Loading....");
            }

            @Override
            public void onLoaderResult(String result) {
                loader1Text.setText(result);
            }
        });
        loader1.start();
    }

    private void loader2() {
        final TextView loader2Text = (TextView) findViewById(R.id.loader2);
        final MyLoader loader2 = loaderManager.init(LOADER2, MyLoader.CREATE, new Loader.CallbacksAdapter<String>() {
            @Override
            public void onLoaderStart() {
                loader2Text.setText("Loading...");
            }

            @Override
            public void onLoaderResult(String result) {
                loader2Text.setText(result);
            }
        });
        findViewById(R.id.loader2_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader2.restart();
            }
        });
    }

    private void loader3() {
        final TextView loader3Text = (TextView) findViewById(R.id.loader3);
        loader3 = loaderManager.init(LOADER3, CurrentTimeLoader.CREATE, new Loader.CallbacksAdapter<Long>() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm:ss a", Locale.US);

            @Override
            public void onLoaderResult(Long result) {
                loader3Text.setText(timeFormat.format(new Date(result)));
            }
        });
    }

    private void loader4() {
        final TextView loader4Text = (TextView) findViewById(R.id.loader4);
        final MyArgLoader loader4 = loaderManager.init(LOADER4, MyArgLoader.CREATE, new Loader.CallbacksAdapter<String>() {
            @Override
            public void onLoaderStart() {
                loader4Text.setText("Loading...");
            }

            @Override
            public void onLoaderResult(String result) {
                loader4Text.setText(result);
            }
        });
        findViewById(R.id.arg1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader4.restart(1);
            }
        });
        findViewById(R.id.arg2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader4.restart(2);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loader3.start(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isChangingConfigurations()) {
            loader3.cancel();
        }
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

    public static class MyArgLoader extends AsyncTaskLoader<String> {
        
        public static Create<MyArgLoader> CREATE = new Create<MyArgLoader>() {
            @Override
            public MyArgLoader create() {
                return new MyArgLoader();
            }
        };

        private int arg;

        @Override
        protected String doInBackground() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return null;
            }
            return "complete " + arg;
        }

        public void restart(int arg) {
            cancel();
            this.arg = arg;
            start();
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
