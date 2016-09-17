package me.tatarka.loadie;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class LoaderTesterTest {

    @Test
    public void runSynchronously_waits_for_loader_to_complete() throws Throwable {
        Loader<Integer> loader = new TestAsyncLoader();
        final boolean[] complete = new boolean[1];
        loader.setCallbacks(new Loader.CallbacksAdapter<Integer>() {
            @Override
            public void onLoaderSuccess() {
                complete[0] = true;
            }
        });
        LoaderTester.runSynchronously(loader);

        assertTrue(complete[0]);
    }

    @Test
    public void runSynchronously_throws_delivered_error() throws Throwable {
        TestAsyncLoader loader = new TestAsyncLoader();
        loader.deliverError = true;

        try {
            LoaderTester.runSynchronously(loader);
            fail();
        } catch (TestAsyncLoader.TestException e) {
            // pass
        }
    }

    @Test
    public void getResultSynchronously_returns_the_first_result() throws Throwable {
        Loader<Integer> loader = new TestAsyncLoader();
        Integer result = LoaderTester.getResultSynchronously(loader);

        assertEquals((Integer) 0, result);
    }

    @Test
    public void getResultSynchronously_throws_delivered_error() throws Throwable {
        TestAsyncLoader loader = new TestAsyncLoader();
        loader.deliverError = true;

        try {
            LoaderTester.getResultSynchronously(loader);
            fail();
        } catch (TestAsyncLoader.TestException e) {
            // pass
        }
    }

    @Test
    public void getResultsSynchronously_returns_all_the_results() throws Throwable {
        Loader<Integer> loader = new TestAsyncLoader();
        Iterator<Integer> results = LoaderTester.getResultsSynchronously(loader);
        int[] resultArray = new int[5];
        int i = 0;
        while (results.hasNext()) {
            resultArray[i++] = results.next();
        }

        assertArrayEquals(new int[]{0, 1, 2, 3, 4}, resultArray);
    }

    @Test
    public void getResultsSynchronously_returns_all_the_results_without_calling_hasNext() throws Throwable {
        Loader<Integer> loader = new TestAsyncLoader();
        Iterator<Integer> results = LoaderTester.getResultsSynchronously(loader);
        int[] resultArray = new int[]{
                results.next(),
                results.next(),
                results.next(),
                results.next(),
                results.next()
        };

        assertArrayEquals(new int[]{0, 1, 2, 3, 4}, resultArray);
        assertFalse(results.hasNext());
    }

    @Test
    public void getResultsSynchronously_throws_delivered_error_on_next() throws Throwable {
        TestAsyncLoader loader = new TestAsyncLoader();
        Iterator<Integer> results = LoaderTester.getResultsSynchronously(loader);
        results.next();
        results.next();
        loader.deliverError = true;

        try {
            results.next();
            fail();
        } catch (RuntimeException e) {
            assertEquals(TestAsyncLoader.TestException.class, e.getCause().getClass());
        }
    }
}
