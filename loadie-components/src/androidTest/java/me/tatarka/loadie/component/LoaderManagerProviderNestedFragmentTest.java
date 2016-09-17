package me.tatarka.loadie.component;

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
public class LoaderManagerProviderNestedFragmentTest {

    @Rule
    public ActivityTestRule<TestActivityWithNestedFragment> activityRule = new ActivityTestRule<>(TestActivityWithNestedFragment.class);
    public Instrumentation i;

    @Before
    public void setup() {
        i = InstrumentationRegistry.getInstrumentation();
    }

    @Test
    public void loader_manager_is_retained_across_config_changes() {
        final LoaderManager firstLoaderManager = activityRule.getActivity().fragment.fragment.loaderManager;
        final TestActivityWithNestedFragment firstActivity = activityRule.getActivity();
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
                TestActivityWithNestedFragment secondActivity = (TestActivityWithNestedFragment) ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).iterator().next();
                LoaderManager secondLoaderManager = secondActivity.fragment.fragment.loaderManager;

                assertNotSame(firstActivity.fragment.fragment, secondActivity.fragment.fragment);
                assertSame(firstLoaderManager, secondLoaderManager);
            }
        });
    }

    @Test
    public void loader_manager_detaches_loaders_on_config_change() {
        LoaderManager loaderManager = activityRule.getActivity().fragment.fragment.loaderManager;
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
    public void loader_manager_stops_on_fragment_detach() {
        LoaderManager loaderManager = activityRule.getActivity().fragment.fragment.loaderManager;
        final TestLoader testLoader = loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activityRule.getActivity().fragment.getChildFragmentManager().beginTransaction()
                        .detach(activityRule.getActivity().fragment.fragment)
                        .commitNow();
            }
        });
        i.waitForIdleSync();

        assertFalse(testLoader.isAttached());
        assertFalse(testLoader.isDestroyed());
    }
    
    @Test
    public void loader_manager_stops_on_parent_fragment_detach() {
        LoaderManager loaderManager = activityRule.getActivity().fragment.fragment.loaderManager;
        final TestLoader testLoader = loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activityRule.getActivity().getSupportFragmentManager().beginTransaction()
                        .detach(activityRule.getActivity().fragment)
                        .commitNow();
            }
        });
        i.waitForIdleSync();

        assertFalse(testLoader.isAttached());
        assertFalse(testLoader.isDestroyed());
    }

    @Test
    public void loader_manager_starts_on_fragment_attach() {
        LoaderManager loaderManager = activityRule.getActivity().fragment.fragment.loaderManager;
        final TestLoader testLoader = loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activityRule.getActivity().fragment.getChildFragmentManager().beginTransaction()
                        .detach(activityRule.getActivity().fragment.fragment)
                        .commitNow();
                activityRule.getActivity().fragment.getChildFragmentManager().beginTransaction()
                        .attach(activityRule.getActivity().fragment.fragment)
                        .commitNow();
            }
        });
        i.waitForIdleSync();

        assertTrue(testLoader.isAttached());
        assertFalse(testLoader.isDestroyed());
    }
    
    @Test
    public void loader_manager_does_not_start_fragment_recreated_but_not_attached() {
        LoaderManager loaderManager = activityRule.getActivity().fragment.fragment.loaderManager;
        final TestLoader testLoader = loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activityRule.getActivity().fragment.getChildFragmentManager().beginTransaction()
                        .detach(activityRule.getActivity().fragment.fragment)
                        .commitNow();
                activityRule.getActivity().recreate();
            }
        });
        i.waitForIdleSync();
        loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });

        assertFalse(testLoader.isAttached());
        assertFalse(testLoader.isDestroyed());
    }

    @Test
    public void loader_manager_destroys_on_removing_fragment() {
        LoaderManager loaderManager = activityRule.getActivity().fragment.fragment.loaderManager;
        final TestLoader testLoader = loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activityRule.getActivity().fragment.getChildFragmentManager().beginTransaction()
                        .remove(activityRule.getActivity().fragment.fragment)
                        .commitNow();
                activityRule.getActivity().recreate();
            }
        });
        i.waitForIdleSync();
        loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });

        assertFalse(testLoader.isAttached());
        assertTrue(testLoader.isDestroyed());
    }

    @Test
    public void loader_manager_destroys_on_removing_parent_fragment() {
        LoaderManager loaderManager = activityRule.getActivity().fragment.fragment.loaderManager;
        final TestLoader testLoader = loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });
        i.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activityRule.getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(activityRule.getActivity().fragment)
                        .commitNow();
                activityRule.getActivity().recreate();
            }
        });
        i.waitForIdleSync();
        loaderManager.init(0, TestLoader.CREATE, new Loader.CallbacksAdapter() {
        });

        assertFalse(testLoader.isAttached());
        assertTrue(testLoader.isDestroyed());
    }

    @Test
    public void z_loader_manager_destroys_on_finish() {
        // We need a separate activity because the test framework will complain if we finish the original one.
        final TestActivityWithFragment activity = (TestActivityWithFragment) i.startActivitySync(new Intent(i.getContext(), TestActivityWithFragment.class));
        LoaderManager loaderManager = activity.fragment.loaderManager;
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
