package me.tatarka.loadie;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.OperationCanceledException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.Scheduler;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AsyncTaskLoaderTest {

    Scheduler background;
    BackgroundThreadSchedulerExecutor backgroundExecutor;
    TestAsyncTaskLoader<String> loader;

    @Before
    public void setup() {
        background = ShadowApplication.getInstance().getBackgroundThreadScheduler();
        backgroundExecutor = new BackgroundThreadSchedulerExecutor();
        loader = new TestAsyncTaskLoader<>(backgroundExecutor);
    }

    @Test
    public void returningFromDoInBackgroundCallsCallbacks() {
        background.pause();

        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.setResult("test");

        background.unPause();

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderResult(eq("test"));
        verify(callbacks).onLoaderSuccess();
    }

    @Test
    public void cancelDoesNotCallCallbacks() {
        background.pause();

        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.setResult("test");
        loader.cancel();

        background.unPause();

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void cancelWithCanceledExceptionDoesNotPropagate() {
        background.pause();

        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.cancel();
        loader.throwCanceledException();

        background.unPause();

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void canceledExceptionPropagates() {
        background.pause();

        loader.start();
        loader.throwCanceledException();

        background.unPause();

        assertNotNull(backgroundExecutor.exception);
        assertEquals(OperationCanceledException.class, backgroundExecutor.exception.getCause().getClass());
    }

    @Test
    public void otherExceptionPropagates() {
        background.pause();

        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.throwException();

        background.unPause();

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderError(isA(RuntimeException.class));
        verifyNoMoreInteractions(callbacks);
    }

    private static class BackgroundThreadSchedulerExecutor implements Executor {
        @Nullable
        Throwable exception;

        @Override
        public void execute(@NonNull final Runnable command) {
            ShadowApplication.getInstance().getBackgroundThreadScheduler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        command.run();
                    } catch (Throwable e) {
                        exception = e;
                    }
                }
            });
        }
    }
}
