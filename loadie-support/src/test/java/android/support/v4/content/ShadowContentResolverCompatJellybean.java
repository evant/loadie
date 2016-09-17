package android.support.v4.content;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowApplication;

import static org.robolectric.Shadows.shadowOf;

/**
 * Shadow to work around issue with using compat content resolver support lib
 * https://github.com/robolectric/robolectric/issues/2020
 */
@Implements(value = ContentResolverCompatJellybean.class, inheritImplementationMethods = true)
public class ShadowContentResolverCompatJellybean {
    @Implementation
    public static Cursor query(ContentResolver resolver, Uri uri, String[] projection,
                               String selection, String[] selectionArgs, String sortOrder,
                               Object cancellationSignalObj) {
        return shadowOf(ShadowApplication.getInstance().getApplicationContext().getContentResolver())
                .query(uri, projection, selection, selectionArgs, sortOrder);
    }
}