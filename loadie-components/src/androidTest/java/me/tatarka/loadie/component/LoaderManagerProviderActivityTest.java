package me.tatarka.loadie.component;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import me.tatarka.loadie.Loader;
import me.tatarka.loadie.LoaderManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoaderManagerProviderActivityTest {

    @Rule
    public ActivityTestRule<TestActivity> activityRule = new ActivityTestRule<>(TestActivity.class);
    public Instrumentation i;

    @Before
    public void setup() {
        i = InstrumentationRegistry.getInstrumentation();
    }

    @Test
    public void loader_manager_is_retained_across_config_changes() {
        final LoaderManager firstLoaderManager = activityRule.getActivity().loaderManager;
        final Activity firstActivity = activityRule.getActivity();
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                firstActivity.recreate();
            }
        });
        i.waitForIdleSync();
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                TestActivity secondActivity = (TestActivity) ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).iterator().next();
                LoaderManager secondLoaderManager = secondActivity.loaderManager;

                assertNotSame(firstActivity, secondActivity);
                assertSame(firstLoaderManager, secondLoaderManager);
            }
        });
    }

    @Test
    public void loader_manager_detaches_loaders_on_config_change() {
        LoaderManager loaderManager = activityRule.getActivity().loaderManager;
        final TestLoader testLoader = loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activityRule.getActivity().recreate();
            }
        });
        i.waitForIdleSync();

        assertFalse(testLoader.isAttached());
        assertFalse(testLoader.isDestroyed());
    }

    @Test
    public void z_loader_manager_destroys_on_finish() {
        // We need a separate activity because the test framework will complain if we finish the original one.
        final TestActivity activity = (TestActivity) i.startActivitySync(new Intent(i.getContext(), TestActivity.class));
        LoaderManager loaderManager = activity.loaderManager;
        final TestLoader testLoader = loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                i.callActivityOnDestroy(activity);
            }
        });
        i.waitForIdleSync();

        assertFalse(testLoader.isAttached());
        assertTrue(testLoader.isDestroyed());
    }
}
