package me.tatarka.loadie;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;

import org.robolectric.fakes.RoboCursor;

public class MyRoboCursor extends RoboCursor {
    private ContentResolver resolver;
    private Uri notificationUri;
    private boolean closedWasCalled;

    @Override
    public void setExtras(Bundle extras) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        resolver = cr;
        notificationUri = uri;
    }

    @Override
    public Uri getNotificationUri() {
        return notificationUri;
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        resolver.registerContentObserver(getNotificationUri(), false, observer);
    }

    @Override
    public void close() {
        closedWasCalled = true;
        super.close();
    }

    @Override
    public boolean isClosed() {
        return closedWasCalled;
    }
}
