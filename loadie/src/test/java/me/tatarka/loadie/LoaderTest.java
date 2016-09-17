package me.tatarka.loadie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LoaderTest {

    @Spy
    TestLoader<String> loader;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void newlyCreatedLoaderIsNotRunning() {
        assertFalse(loader.isRunning());
    }

    @Test
    public void newlyCreatedLoaderHasNoResult() {
        assertFalse(loader.hasResult());
    }

    @Test
    public void startedLoaderIsRunning() {
        loader.start();

        assertTrue(loader.isRunning());
    }

    @Test
    public void startedLoaderHasNoResult() {
        loader.start();

        assertFalse(loader.hasResult());
    }

    @Test
    public void startingLoaderCallsOnStart() {
        loader.start();

        verify(loader).onStart(any(Loader.Receiver.class));
    }

    @Test
    public void deliverResultIsRunning() {
        loader.start();
        loader.result("test");

        assertTrue(loader.isRunning());
    }

    @Test
    public void deliverResultsHasResult() {
        loader.start();
        loader.result("test");

        assertTrue(loader.hasResult());
    }

    @Test
    public void successIsNotRunning() {
        loader.start();
        loader.success();

        assertFalse(loader.isRunning());
    }

    @Test
    public void errorIsNotRunning() {
        loader.start();
        loader.error(new Exception());

        assertFalse(loader.isRunning());
    }

    @Test
    public void successHasNoResult() {
        loader.start();
        loader.success();

        assertFalse(loader.hasResult());
    }

    @Test
    public void errorHasNoResult() {
        loader.start();
        loader.error(new Exception());

        assertFalse(loader.hasResult());
    }

    @Test
    public void errorIsError() {
        loader.start();
        loader.error(new Exception());

        assertTrue(loader.isError());
    }

    public void errorIsNotSuccess() {
        loader.start();
        loader.error(new Exception());

        assertFalse(loader.isSuccess());
    }

    @Test
    public void deliverResultSuccessIsNotRunning() {
        loader.start();
        loader.result("test");
        loader.success();

        assertFalse(loader.isRunning());
    }

    @Test
    public void deliverResultSuccessHasResult() {
        loader.start();
        loader.result("test");
        loader.success();

        assertTrue(loader.hasResult());
    }

    @Test
    public void stopWithoutStartDoesNothing() {
        loader.cancel();

        verify(loader, never()).onCancel();
    }

    @Test
    public void startStopCallsOnStop() {
        loader.start();
        loader.cancel();
        verify(loader).onCancel();
    }

    @Test
    public void startStopIsNotRunning() {
        loader.start();
        loader.cancel();

        assertFalse(loader.isRunning());
    }

    @Test
    public void startStopHasNoResult() {
        loader.start();
        loader.cancel();

        assertFalse(loader.hasResult());
    }

    @Test
    public void startSuccessStopDoesNotCallOnStop() {
        loader.start();
        loader.success();
        loader.cancel();

        verify(loader, never()).onCancel();
    }

    @Test
    public void newlyCreatedDoesNotCallCallbacks() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);

        verifyZeroInteractions(callbacks);
    }

    @Test
    public void startCallsCallbacksOnLoadStart() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void runningCallsCallbacksOnLoadStart() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.start();
        loader.setCallbacks(callbacks);

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void deliverResultCallsCallbacksOnLoaderResult() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.result("test");

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderResult(eq("test"));
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void deliveredResultCallsCallbacksOnLoaderResult() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.start();
        loader.result("test");
        loader.setCallbacks(callbacks);

        verify(callbacks).onLoaderResult(eq("test"));
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void successCallsCallbacksLoaderSuccess() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.success();

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderSuccess();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void successCallsCallbacksLoaderSuccess2() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.start();
        loader.success();
        loader.setCallbacks(callbacks);

        verify(callbacks).onLoaderSuccess();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void deliverResultAfterSuccess() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.success();

        try {
            loader.result("test");
            fail();
        } catch (IllegalStateException e) {
            // pass
        }

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderSuccess();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void successTwice() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.success();

        try {
            loader.success();
            fail();
        } catch (IllegalStateException e) {
            // pass
        }

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderSuccess();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void deliverResultAfterCancel() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.cancel();
        loader.result("test");

        verify(callbacks, never()).onLoaderResult("test");
    }

    @Test
    public void destroyCallsOnDestroy() {
        loader.destroy();

        verify(loader).onDestroy();
    }

    @Test
    public void destroyClearsCallbacks() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.destroy();

        assertFalse(loader.isAttached());
    }

    @Test
    public void destroyCancelsRunning() {
        loader.start();
        loader.destroy();

        assertTrue(loader.isCanceled());
    }
}
