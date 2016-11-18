package saschpe.birthdays;

import android.os.StrictMode;
import android.support.v7.app.AppCompatDelegate;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
