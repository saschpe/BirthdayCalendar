package saschpe.birthdays;

import saschpe.birthdays.activity.callbacks.AdViewActivityLifeCycleCallbacks;

public class FreeApplication extends saschpe.birthdays.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.FLAVOR.equals("free")) {
            registerActivityLifecycleCallbacks(new AdViewActivityLifeCycleCallbacks());
        }
    }
}
