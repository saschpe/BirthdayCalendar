/*
 * Copyright 2016 Sascha Peilicke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package saschpe.birthdays;

import android.os.StrictMode;
import android.support.v7.app.AppCompatDelegate;

import com.google.android.gms.ads.MobileAds;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }

        // Support vector drawable support for pre-Lollipop devices
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Google Mobile Ads. Look up the AdView as a resource and load a request.
        MobileAds.initialize(this,
                // Not a real secret. If you use that AdMob banner ID in your
                // projects I will receive the money instead :-)
                "ca-app-pub-9045162269320751~5472371821");
    }
}
