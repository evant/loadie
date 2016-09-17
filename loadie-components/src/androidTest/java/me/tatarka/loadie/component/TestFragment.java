package me.tatarka.loadie.component;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import me.tatarka.loadie.LoaderManager;

public class TestFragment extends Fragment {

    public LoaderManager loaderManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = LoaderManagerProvider.forFragment(this);
    }
}
