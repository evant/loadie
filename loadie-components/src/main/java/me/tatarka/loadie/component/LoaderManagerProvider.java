package me.tatarka.loadie.component;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.tatarka.loadie.LoaderManager;

public class LoaderManagerProvider {

    private static final String LOADER_FRAGMENT_TAG = "me.tatarka.loadie.Fragment";

    /**
     * Obtains a {@link LoaderManager} for the given activity that will be retained across
     * configuration changes.
     */
    public static LoaderManager forActivity(FragmentActivity activity) {
        return forFragmentManager(activity.getSupportFragmentManager());
    }

    /**
     * Obtains a {@link LoaderManager} for the given fragment that will be retained across
     * configuration changes.
     */
    public static LoaderManager forFragment(Fragment fragment) {
        return forFragmentManager(fragment.getChildFragmentManager());
    }

    private static LoaderManager forFragmentManager(FragmentManager fm) {
        return getLoaderFragment(fm).loader.loaderManager;
    }

    private static LoaderFragment getLoaderFragment(FragmentManager fm) {
        LoaderFragment fragment = (LoaderFragment) fm.findFragmentByTag(LOADER_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new LoaderFragment();
            fm.beginTransaction()
                    .add(fragment, LOADER_FRAGMENT_TAG)
                    .commitNow();
        }
        return fragment;
    }

    /**
     * @hide
     */
    public static class LoaderFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks {
        LoaderLoader loader;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            loader = (LoaderLoader) getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            return new LoaderLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {

        }

        @Override
        public void onLoaderReset(Loader loader) {

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            loader.loaderManager.start();
            return null;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            loader.loaderManager.stop();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            loader.loaderManager.detach();
        }
    }

    static class LoaderLoader extends android.support.v4.content.Loader {
        final LoaderManager loaderManager = new LoaderManager();

        LoaderLoader(Context context) {
            super(context);
        }

        @Override
        protected void onReset() {
            loaderManager.destroy();
        }
    }
}
