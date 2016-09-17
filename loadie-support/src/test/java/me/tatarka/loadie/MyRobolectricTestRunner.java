package me.tatarka.loadie;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkEnvironment;

public class MyRobolectricTestRunner extends RobolectricTestRunner {
    public MyRobolectricTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void configureShadows(SdkEnvironment sdkEnvironment, Config config) {
        super.configureShadows(sdkEnvironment, config);
    }
}
