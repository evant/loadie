package me.tatarka.loadie.component;

import me.tatarka.loadie.Loader;

public class TestLoader extends Loader {

    public static final Create<TestLoader> CREATE = new Create<TestLoader>() {
        @Override
        public TestLoader create() {
            return new TestLoader();
        }
    };

    @Override
    protected void onStart(Receiver receiver) {

    }
}
