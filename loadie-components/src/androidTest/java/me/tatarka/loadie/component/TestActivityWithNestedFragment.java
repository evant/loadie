package me.tatarka.loadie.component;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

public class TestActivityWithNestedFragment extends FragmentActivity {

    public static final String FRAGMENT_TAG = "tag";

    public TestParentFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            fragment = new TestParentFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commitNow();
        } else {
            fragment = (TestParentFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        }
    }
}
