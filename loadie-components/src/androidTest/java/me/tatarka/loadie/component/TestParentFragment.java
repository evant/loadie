package me.tatarka.loadie.component;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class TestParentFragment extends Fragment {

    public static final String FRAGMENT_TAG = "tag";

    public TestFragment fragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            fragment = new TestFragment();
            getChildFragmentManager().beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commitNow();
        } else {
            fragment = (TestFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
    }
}
