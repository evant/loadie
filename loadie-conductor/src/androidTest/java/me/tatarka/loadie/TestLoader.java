package me.tatarka.loadie;

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
