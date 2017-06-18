/*
 * Copyright (C) 2016 Sascha Peilicke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.lang.ref.WeakReference;

import saschpe.birthdays.R;
import saschpe.birthdays.fragment.BirthdaysFragment;
import saschpe.birthdays.fragment.SourcesFragment;
import saschpe.birthdays.helper.PreferencesHelper;
import saschpe.birthdays.service.BirthdaysIntentService;

import static saschpe.birthdays.service.BirthdaysIntentService.MESSAGE_WHAT_STARTED;

public final class MainActivity extends AppCompatActivity {
    private static final String ACTION_OPEN_EVENT = "saschpe.birthdays.action.OPEN_EVENT";
    public static final String ACTION_SYNC_REQUESTED = "saschpe.birthdays.action.SYNC_REQUESTED";
    private static final String EXTRA_EVENT_ID = "saschpe.birthdays.extra.EVENT_ID";

    private CalendarSyncHandler calendarSyncHandler;
    private CoordinatorLayout coordinatorLayout;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_OPEN_EVENT:
                    long eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1);
                    if (eventId >= 0) {
                        Intent intent1 = new Intent(Intent.ACTION_VIEW)
                                .setData(ContentUris
                                        .withAppendedId(CalendarContract.Events.CONTENT_URI,
                                                eventId));
                        startActivity(intent1);
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

        // Set up nested scrollview
        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.nested_scroll);
        scrollView.setFillViewport(true);

        // Set up view pager
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new MainFragmentPagerAdapter(this, getSupportFragmentManager()));
        if (PreferencesHelper.getFirstRun(this)) {
            viewPager.setCurrentItem(1); // Only show setup on first run
        }

        // Set up  tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);

        // Open calendar FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.calendar_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(CalendarContract.CONTENT_URI
                                .buildUpon()
                                .appendPath("time")
                                .build());
                startActivity(intent);
            }
        });
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
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.help_and_feedback:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static final class CalendarSyncHandler extends Handler {
        private final WeakReference<MainActivity> ref;

        CalendarSyncHandler(final MainActivity activity) {
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
            }
        }
    }

    private static final class MainFragmentPagerAdapter extends FragmentPagerAdapter {
        private final String[] pageTitles;

        MainFragmentPagerAdapter(final Context context, final FragmentManager fm) {
            super(fm);
            pageTitles = new String[] {
                    context.getString(R.string.birthdays),
                    context.getString(R.string.sources)
            };
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                default:
                    return new BirthdaysFragment();
                case 1:
                    return new SourcesFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }
    }
}
