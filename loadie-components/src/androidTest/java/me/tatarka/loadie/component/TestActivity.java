package me.tatarka.loadie.component;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import me.tatarka.loadie.LoaderManager;

public class TestActivity extends FragmentActivity {

    public LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = LoaderManagerProvider.forActivity(this);
    }
}
