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

package saschpe.birthdays.activity.callbacks;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import saschpe.birthdays.R;

/**
 * Lifecycle callbacks that deal with {@link AdView}.
 *
 * Allows to keep the {@link saschpe.birthdays.activity.MainActivity} free of
 * AdMob / Play Services / Firebase code.
 */
public class AdViewActivityLifeCycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private AdView adView;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        // Google Mobile Ads. Look up the AdView as a resource and load a request.
        MobileAds.initialize(activity.getApplicationContext(),
                // Not a real secret. If you use that AdMob banner ID in your
                // projects I will receive the money instead :-)
                "ca-app-pub-9045162269320751~5472371821");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // Belongs to onActivityCreated, do it here to ensure inflated layout
        adView = (AdView) activity.findViewById(R.id.ad_view);
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Those numbers are hardly any secret. If you use them in your own
                // projects I won't get annoying ads, so go ahead :-)
                .addTestDevice("CB380BC5777E545490CF0D4A435348D7") // Sascha's OnePlus One
                .addTestDevice("6017F914680B8E8A9B332558F8E53245") // Sascha's Galaxy S2
                .addTestDevice("284D104E238AB19474B60214A41288B9") // Sascha's Pixel C
                .build());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        adView.resume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        adView.pause();
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        adView.destroy();
    }
}
