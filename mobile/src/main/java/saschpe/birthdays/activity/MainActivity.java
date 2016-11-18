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

import android.Manifest;
import android.accounts.Account;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import saschpe.birthdays.BuildConfig;
import saschpe.birthdays.R;
import saschpe.birthdays.adapter.AccountArrayAdapter;
import saschpe.birthdays.helper.DisplayHelper;
import saschpe.birthdays.helper.PreferencesHelper;
import saschpe.birthdays.model.AccountModel;
import saschpe.birthdays.provider.AccountProviderHelper;
import saschpe.birthdays.service.BirthdaysIntentService;
import saschpe.birthdays.service.loader.ContactAccountListLoader;
import saschpe.versioninfo.widget.VersionInfoDialogFragment;

import static saschpe.birthdays.service.BirthdaysIntentService.MESSAGE_WHAT_DONE;
import static saschpe.birthdays.service.BirthdaysIntentService.MESSAGE_WHAT_STARTED;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String[] RUNTIME_PERMISSIONS = new String[] {
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS
    };
    private static final int PERMISSION_REQUEST_ALL = 1;

    private AccountArrayAdapter adapter;
    private CalendarSyncHandler calendarSyncHandler;
    ProgressBar progressBar;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarSyncHandler = new CalendarSyncHandler(this);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Init recycler adapter
        adapter = new AccountArrayAdapter(this, null);
        adapter.setOnAccountSelectedListener(new AccountArrayAdapter.OnAccountSelectedListener() {
            @Override
            public void onAccountSelected(AccountModel accountModel) {
                storeSelectedAccountsAndSync();
            }
        });

        // Init recycler view
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(DisplayHelper.getSuitableLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        if (checkRuntimePermissions()) {
            loadContactAccounts();
        } else {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, PERMISSION_REQUEST_ALL);
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ALL:
                if (checkRuntimePermissions()) {
                    loadContactAccounts();
                }
                break;
        }
    }

    private boolean checkRuntimePermissions() {
        for (String permission : RUNTIME_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Missing runtime permission: " + permission);
                return false;
            }
        }
        return true;
    }

    private void loadContactAccounts() {
        Log.d(TAG, "Restarting contact account list loader...");
        getSupportLoaderManager().restartLoader(0, null, contactAccountLoaderCallbacks);
    }

    private LoaderManager.LoaderCallbacks<List<AccountModel>> contactAccountLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<AccountModel>>() {
        @Override
        public Loader<List<AccountModel>> onCreateLoader(int i, Bundle bundle) {
            return new ContactAccountListLoader(MainActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<AccountModel>> loader, List<AccountModel> objects) {
            adapter.replaceAll(objects);
            MainActivity.this.recyclerView.setVisibility(View.VISIBLE);
            MainActivity.this.progressBar.setVisibility(View.GONE);

            storeSelectedAccountsAndSync();
        }

        @Override
        public void onLoaderReset(Loader<List<AccountModel>> loader) {
            adapter.clear();
            MainActivity.this.recyclerView.setVisibility(View.GONE);
            MainActivity.this.progressBar.setVisibility(View.VISIBLE);
        }
    };

    private void storeSelectedAccountsAndSync() {
        Log.i(TAG, "Store selected accounts and sync...");
        List<Account> contactAccountWhiteList = new ArrayList<>();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            AccountModel item = adapter.getItem(i);
            if (!item.isSelected()) {
                contactAccountWhiteList.add(item.getAccount());
            }
        }

        if (PreferencesHelper.getFirstRun(this) || !contactAccountWhiteList.equals(AccountProviderHelper.getAccountList(this))) {
            PreferencesHelper.setFirstRun(this, false);
            AccountProviderHelper.setAccountList(this, contactAccountWhiteList);
            // Trigger Google Calendar Provider sync after account database update
            BirthdaysIntentService.startActionSync(this, calendarSyncHandler);
        }
    }

    private void openCalendar() {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(CalendarContract.CONTENT_URI
                        .buildUpon()
                        .appendPath("time")
                        .build());
        startActivity(intent);
    }

    private static class CalendarSyncHandler extends Handler {
        private final WeakReference<MainActivity> ref;

        CalendarSyncHandler(MainActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = ref.get();
            if (activity == null || activity.isFinishing() /*|| activity.isDestroyed()*/) {
                removeCallbacksAndMessages(null);
                return;
            }

            switch (msg.what) {
                case MESSAGE_WHAT_STARTED:
                    Snackbar.make(activity.recyclerView, R.string.birthday_calendar_create, Snackbar.LENGTH_SHORT).show();
                    break;
                case MESSAGE_WHAT_DONE:
                    Snackbar.make(activity.recyclerView, R.string.birthday_calendar_ready, Snackbar.LENGTH_LONG)
                            .setAction(R.string.open, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    MainActivity activity = ref.get();
                                    if (activity == null || activity.isFinishing()) {
                                        return;
                                    }
                                    activity.openCalendar();
                                }
                            }).show();
                    break;
            }
        }
    }
}
