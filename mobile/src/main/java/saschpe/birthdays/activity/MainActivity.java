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

package saschpe.birthdays.activity;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.ref.WeakReference;

import saschpe.birthdays.BuildConfig;
import saschpe.birthdays.R;
import saschpe.birthdays.adapter.ViewPagerAdapter;
import saschpe.birthdays.fragment.BirthdaysFragment;
import saschpe.birthdays.fragment.SourcesFragment;
import saschpe.birthdays.helper.PreferencesHelper;
import saschpe.birthdays.service.BirthdaysIntentService;
import saschpe.versioninfo.widget.VersionInfoDialogFragment;

import static saschpe.birthdays.service.BirthdaysIntentService.MESSAGE_WHAT_DONE;
import static saschpe.birthdays.service.BirthdaysIntentService.MESSAGE_WHAT_STARTED;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_OPEN_EVENT = "saschpe.birthdays.action.OPEN_EVENT";
    public static final String ACTION_SYNC_REQUESTED = "saschpe.birthdays.action.SYNC_REQUESTED";

    public static final String EXTRA_EVENT_ID = "saschpe.birthdays.extra.EVENT_ID";

    private CalendarSyncHandler calendarSyncHandler;
    private CoordinatorLayout coordinatorLayout;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_OPEN_EVENT:
                    long eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1);
                    if (eventId >= 0) {
                        openEvent(eventId);
                    }
                    break;
                case ACTION_SYNC_REQUESTED:
                    // Trigger Google Calendar Provider sync after account database update
                    BirthdaysIntentService.startActionSync(MainActivity.this, calendarSyncHandler);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        calendarSyncHandler = new CalendarSyncHandler(this);

        // Set up fragment pager adapter
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new BirthdaysFragment(), getString(R.string.birthdays));
        pagerAdapter.addFragment(new SourcesFragment(), getString(R.string.sources));

        // Set up nested scrollview
        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.nested_scroll);
        scrollView.setFillViewport(true);

        // Set up view pager
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);
        if (PreferencesHelper.getFirstRun(this)) {
            viewPager.setCurrentItem(1); // Only show setup on first run
        }

        // Set up  tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SYNC_REQUESTED);
        intentFilter.addAction(ACTION_OPEN_EVENT);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.calendar:
                openCalendar();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.version_info:
                VersionInfoDialogFragment
                        .newInstance(
                                getString(R.string.app_name),
                                BuildConfig.VERSION_NAME,
                                "Sascha Peilicke",
                                R.mipmap.ic_launcher)
                        .show(getFragmentManager(), "version_info");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCalendar() {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(CalendarContract.CONTENT_URI
                        .buildUpon()
                        .appendPath("time")
                        .build());
        startActivity(intent);
    }

    private void openEvent(long eventId) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(ContentUris
                        .withAppendedId(CalendarContract.Events.CONTENT_URI,
                                eventId));
        startActivity(intent);
    }

    private static class CalendarSyncHandler extends Handler {
        private final WeakReference<MainActivity> ref;

        CalendarSyncHandler(MainActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = ref.get();
            if (activity == null) {
                removeCallbacksAndMessages(null);
                return;
            }

            switch (msg.what) {
                case MESSAGE_WHAT_STARTED:
                    Snackbar.make(activity.coordinatorLayout,
                            R.string.birthday_calendar_create,
                            Snackbar.LENGTH_SHORT).show();
                    break;
                case MESSAGE_WHAT_DONE:
                    BirthdaysFragment fragment = (BirthdaysFragment) activity
                            .getSupportFragmentManager()
                            // TODO: Find a better way!
                            .getFragments().get(0);
                    if (fragment != null) {
                        fragment.reloadAdapter();
                    }
                    break;
            }
        }
    }
}
