package me.tatarka.loadie;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;

import me.tatarka.loadie.controller.LoaderManagerProvider;

public class TestController extends Controller {

    public LoaderManager loaderManager;

    public TestController() {
        loaderManager = LoaderManagerProvider.forController(this);
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return new View(container.getContext());
    }
}
