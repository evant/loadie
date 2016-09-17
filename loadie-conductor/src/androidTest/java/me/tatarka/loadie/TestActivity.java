package me.tatarka.loadie;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

public class TestActivity extends Activity {

    private static final String CONTROLLER_TAG = "tag";

    public Router router;
    public TestController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        router = Conductor.attachRouter(this, (ViewGroup) findViewById(android.R.id.content), savedInstanceState);
        if (!router.hasRootController()) {
            controller = new TestController();
            router.setRoot(RouterTransaction.with(controller).tag(CONTROLLER_TAG));
        } else {
            controller = (TestController) router.getControllerWithTag(CONTROLLER_TAG);
        }
    }

    @Override
    public void onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed();
        }
    }
}
