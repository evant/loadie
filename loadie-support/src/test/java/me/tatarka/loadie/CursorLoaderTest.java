package me.tatarka.loadie;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ShadowContentResolverCompatJellybean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.util.Scheduler;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, shadows = ShadowContentResolverCompatJellybean.class)
public class CursorLoaderTest {
    static final String AUTHORITY = "me.tatarka.loader.TestContentProvider";
    static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    Scheduler background;
    ContentResolver resolver;
    Cursor cursor;

    @Before
    public void setup() {
        background = ShadowApplication.getInstance().getBackgroundThreadScheduler();
        resolver = RuntimeEnvironment.application.getContentResolver();
        ShadowContentResolver shadowResolver = shadowOf(resolver);
        MyRoboCursor cursor = new MyRoboCursor();
        cursor.setNotificationUri(resolver, CONTENT_URI);
        shadowResolver.setCursor(CONTENT_URI, cursor);
        this.cursor = cursor;
    }

    @Test
    public void emptyUriReturnsNullCursor() {
        CursorLoader loader = new CursorLoader.Builder(resolver, Uri.EMPTY).build();
        Loader.Callbacks<Cursor> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderResult(eq((Cursor) null));
    }

    @Test
    public void correctUriReturnsCursor() {
        CursorLoader loader = new CursorLoader.Builder(resolver, CONTENT_URI).build();
        Loader.Callbacks<Cursor> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();

        verify(callbacks).onLoaderStart();
        verify(callbacks).onLoaderResult(eq(cursor));
    }

    @Test
    public void notifyDataChangeCallsCallbackAgain() {
        CursorLoader loader = new CursorLoader.Builder(resolver, CONTENT_URI).build();
        Loader.Callbacks<Cursor> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();

        ShadowApplication.runBackgroundTasks();
        RuntimeEnvironment.application.getContentResolver().notifyChange(CONTENT_URI, null);

        verify(callbacks, times(2)).onLoaderStart();
        verify(callbacks, times(2)).onLoaderResult(eq(cursor));
    }

    @Test
    public void cancelClosesCursorIfLoading() {
        CursorLoader loader = new CursorLoader.Builder(resolver, CONTENT_URI).build();
        Loader.Callbacks<Cursor> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);

        background.pause();

        loader.start();
        loader.cancel();

        background.unPause();
        ShadowApplication.runBackgroundTasks();

        assertTrue(cursor.isClosed());
    }

    @Test
    public void destroyClosesCursor() {
        CursorLoader loader = new CursorLoader.Builder(resolver, CONTENT_URI).build();
        Loader.Callbacks<Cursor> callbacks = mock(Loader.Callbacks.class);
        loader.setCallbacks(callbacks);
        loader.start();
        loader.destroy();

        ShadowApplication.runBackgroundTasks();

        assertTrue(cursor.isClosed());
    }
}
