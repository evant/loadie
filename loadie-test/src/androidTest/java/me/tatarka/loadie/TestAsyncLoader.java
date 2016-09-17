package me.tatarka.loadie;

import android.os.Handler;
import android.os.Looper;

public class TestAsyncLoader extends Loader<Integer> {

    public volatile boolean deliverError;

    @Override
    protected void onStart(final Receiver receiver) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler(Looper.getMainLooper());
                for (int i = 0; i < 5; i++) {
                    if (deliverError) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                receiver.error(new TestException());
                            }
                        });
                        return;
                    }
                    final int finalI = i;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            receiver.result(finalI);
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (deliverError) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            receiver.error(new TestException());
                        }
                    });
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        receiver.success();
                    }
                });
            }
        }).start();
    }

    public static class TestException extends Exception {
    }
}
