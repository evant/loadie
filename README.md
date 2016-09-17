Loadie
======

Loaders for the rest of us.

The concept of loaders in Android is pretty great: a way to do async work in a lifecycle-aware way.
Unfortunately, the implementation is pretty bad. Loadie attempts to fix this in several ways:

* Very simple loader interface, you only need to implement 1 method, or up to 3 if really necessary.
Compare that to Android loader's 6.
* A clear separation between creating loaders and starting them.
* Not tied into any component, you just need to call the 4 lifecycle methods on LoaderManager at the
correct time, though default implementations for Activities and Fragments are provided.
* Not coupled to content providers in any way, though there is a CursorLoader if you need that.

## Creating a Loader

To create a loader, you subclass `Loader` and implement `onStart()` and optionally `onCancel()` and
`onDestroy()`.

```java
public class MyLoader extends Loader<String> {
    // Convenience creator for loaderManager.init()
    public static final Create<MyLoader> CREATE = new Create<MyLoader>() {
        @Override
        public MyLoader create() {
            return new MyLoader();
        }
    };

    @Override
    protected void onStart(Receiver receiver) {
        // Note loader doesn't handle threading, you have to do that yourself.
        api.doAsync(new ApiCallback() {
            public void onResult(String result) {
                // Make sure this happens on the main thread!
                receiver.success(result);
            }
          
            public void onError(Error error) {
                receiver.error(error); 
            }
        });
    }

    // Overriding this method is optional, but if you can cancel your call when it's no longer needed, you should.
    @Override
    protected void onCancel() {
        api.cancel();
    }

    // Overriding this method is optional and allows you to clean up any resources.
    @Override
    protected void onDestroy() {

    }
}
```

In your async operation you call `Receiver#result(value)` as many times as you have results and then
either `Receiver#success()` or `Receiver#error(error)` exactly once to notify the work has ended or
there was an error. If you have used rxjava this contract should seem familer. There is also a
convenience `Receiver#success(value)` when you only have a single value to deliver.

As mentioned above, loaders don't to any threading for you. You are responsible for getting the work
off the main thread and delivering the results back to the main thread.

## Using a Loader

It most cases you will manage loaders through a `LoaderManager` that you obtain from a 
`LoaderManagerProvider`. This will manage the lifecycle for you, ensuring your callbacks happen when 
they need to. For example, using `me.tatarka.loadie:loadie-components` and in an Activity, you would 
do:

```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LoaderManager loaderManager = LoaderManagerProvider.forActivity(this);
        // Creates a new loader or returns an existing one if it's already created.
        final MyLoader myLoader = loaderManager.init(0, MyLoader.CREATE, new Loader.CallbacksAdapter<String>() {
            @Override
            public void onLoaderStart() {
                // Update your ui to show you are loading something
            }

            @Override
            public void onLoaderResult(String result) {
                // Update your ui with the result
            }
            
            @Override
            public void onLoaderError(Throwable error) {
                // Update your ui with the error
            }

            @Override
            public void onLoaderComplete() {
                // Optionally do something when the loader has completed
            }
        });
        // The loader won't actually run until you call this!
        myLoader.start();
        
        setContentView(R.layout.activity_main);
        // view setup stuff...
        
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // The separation between initialization and starting, means it's easy to react to user events!
                myLoader.restart();
            }
        });
    }
}
```

`me.tatarka.loadie:loadie-conductor` has a `LoaderManagerProvider` for [Conductor](https://github.com/bluelinelabs/Conductor)
if that's your thing. It will ensure that the callbacks are not run when the view is not attached.

```java
public class MyController extends Controller {

    LoaderManager loaderManager;

    public TestController() {
        loaderManager = LoaderManagerProvider.forController(this);
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return ...;
    }
}
```

## Built-in Loaders

There are a few built-in loaders for some common cases. `me.tatarka.loadie:loadie-support` has 
loaders that mirror the ones in the support lib.

```java
public class MyAsyncTaskLoader extends AsyncTaskLoader<String> {
    @Override
    protected String doInBackground() {
        // Do lots of work to get a string.
        return "Cool!";
    }
}
```

```java
loaderManager.init(0, new CursorLoader.Builder(getContentResolver(), MY_TABLE_URI)
    .projection(...)
    .selection(...)
    .sortOrder(...), ...);
```

`me.tatarka.loadie:loadie-rx` contains an `RxLoader` to easily accept an observable.

```java
loaderManager.init(0, RxLoader.create(myObservable), ...);
```

## Providing your own LoaderManager

`LoaderManager` isn't tied to any specific component. You can make your own by retaining it across
configuration changes and calling the 4 lifecycle methods (`start()`, `stop()`, `detach()`, and 
`destroy()`). For example, here is a simple one using an activity's `onRetainNonConfigurationInstance()`.

```java
public class MainActivity extends Activity {

    LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get a retained instance or create a new one.
        loaderManager = (LoaderManager) getLastNonConfigurationInstance();
        if (loaderManager == null) {
            loaderManager = new LoaderManager();
        }
        // We immediately have views, start delivering callbacks.
        loaderManager.start();
        setContentView(R.layout.activity_main);
        // we don't need to call stop() because the views are never detached. It would be necessary
        // in, ex a fragment where the views could be destroyed but the fragment is still around.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            // Activity is done, cancel any loaders.
            loaderManager.destroy();
        } else {
            // Otherwise, just detach callbacks.
            loaderManager.detach();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return loaderManager;
    }
}
```

## Testing Loaders

You can test loaders synchronously with `LoaderTester` in `me.tatarka.loadie:loadie-test`.

```java
@Test
public void my_loader_test() {
    // Just wait for loader to complete.
    LoaderTester.runSynchronously(new MyLoader());
    // Get the first result.
    String result = LoaderTester.getResultSynchronously(new MyLoader());
    // Get all results.
    Iterator<String> results = LoaderTester.getResultsSynchronously(new MyLoader());
    String result1 = results.next();
    String result2 = results.next();
    String result3 = results.next();
}
```
