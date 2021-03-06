package me.tatarka.loadie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LoaderManagerTest {

    LoaderManager loaderManager;

    @Before
    public void setup() {
        loaderManager = new LoaderManager();
    }

    @Test
    public void init() {
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), null);

        assertNotNull(loader);
    }

    @Test
    public void initTwice() {
        TestLoader<String> loader1 = loaderManager.init(0, TestLoader.<String>create(), null);
        TestLoader<String> loader2 = loaderManager.init(0, TestLoader.<String>create(), null);

        assertTrue(loader1 == loader2);
    }

    @Test
    public void initDifferentIds() {
        TestLoader<String> loader1 = loaderManager.init(0, TestLoader.<String>create(), null);
        TestLoader<String> loader2 = loaderManager.init(1, TestLoader.<String>create(), null);

        assertFalse(loader1 == loader2);
    }

    @Test
    public void start_before_init() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), callbacks);
        loaderManager.start();
        loader.start();

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void start_after_init() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), callbacks);
        loader.start();
        loaderManager.start();

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void start_after_init1() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(1, TestLoader.<String>create(), callbacks);
        loader.start();
        loaderManager.start();

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void stop() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), callbacks);
        loaderManager.start();
        loaderManager.stop();
        loader.start();

        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void stop_and_start() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), callbacks);
        loaderManager.start();
        loaderManager.stop();
        loader.start();
        loaderManager.start();

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void detach() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), callbacks);
        loader.start();
        loaderManager.start();
        loaderManager.detach();
        loader.result("test");

        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void destroy() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), callbacks);
        loader.start();
        loaderManager.start();
        loaderManager.destroy();
        loader.result("test");

        assertTrue(loader.isCanceled());
        verify(callbacks).onLoaderStart();
        verifyNoMoreInteractions(callbacks);
    }

    @Test
    public void detachAndReattach() {
        Loader.Callbacks<String> callbacks1 = mock(Loader.Callbacks.class);
        Loader.Callbacks<String> callbacks2 = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), callbacks1);
        loader.start();
        loaderManager.start();
        loaderManager.detach();
        loader.result("test");

        loader = loaderManager.init(0, TestLoader.<String>create(), callbacks2);
        loaderManager.start();

        verify(callbacks1).onLoaderStart();
        verifyNoMoreInteractions(callbacks1);
        verify(callbacks2).onLoaderResult(eq("test"));
        verifyNoMoreInteractions(callbacks2);
    }

    @Test
    public void attachTwice() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), callbacks);

        try {
            loaderManager.init(0, TestLoader.<String>create(), null);
            fail();
        } catch (IllegalStateException e) {
            // pass
        }
    }

    @Test
    public void attachTwiceAfterStart() {
        Loader.Callbacks<String> callbacks = mock(Loader.Callbacks.class);
        TestLoader<String> loader = loaderManager.init(0, TestLoader.<String>create(), callbacks);
        loaderManager.start();

        try {
            loaderManager.init(0, TestLoader.<String>create(), null);
            fail();
        } catch (IllegalStateException e) {
            // pass
        }
    }
}
