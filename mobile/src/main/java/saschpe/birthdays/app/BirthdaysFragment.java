/*
 * Copyright (C) 2017 Sascha Peilicke
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

package saschpe.birthdays.app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.Calendar;

import saschpe.android.utils.helper.DisplayHelper;
import saschpe.android.utils.widget.SpacesItemDecoration;
import saschpe.birthdays.R;
import saschpe.birthdays.adapter.EventAdapter;
import saschpe.birthdays.service.CalendarSyncService;

public final class BirthdaysFragment extends Fragment {
    private static final String TAG = BirthdaysFragment.class.getSimpleName();
    private static final String[] RUNTIME_PERMISSIONS = new String[]{
            Manifest.permission.READ_CALENDAR,
    };
    private static final int PERMISSION_REQUEST_ALL = 1;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private int spacePx;
    private Cursor cursor;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case CalendarSyncService.ACTION_SYNC_DONE:
                        // TODO: This is rather bold. Ideally we would get notified about
                        // individual items and notify() them individually
                        // TODO: Needs restructuring of the cursor loader...
                        refreshAdapter();
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Compute spacing between recycler view items
        float marginDp = 4;
        spacePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                marginDp, getResources().getDisplayMetrics());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_birthdays, container, false);
        progressBar = rootView.findViewById(R.id.progress_bar);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (checkRuntimePermissions()) {
            refreshAdapter();
        } else {
            ActivityCompat.requestPermissions(getActivity(), RUNTIME_PERMISSIONS, PERMISSION_REQUEST_ALL);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView.LayoutManager layoutManager = DisplayHelper.getSuitableLayoutManager(getContext());

        if (layoutManager instanceof GridLayoutManager) {
            recyclerView.addItemDecoration(new SpacesItemDecoration(spacePx,
                    SpacesItemDecoration.HORIZONTAL));
        }
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacePx,
                SpacesItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(broadcastReceiver,
                        new IntentFilter(CalendarSyncService.ACTION_SYNC_DONE));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ALL:
                if (checkRuntimePermissions()) {
                    refreshAdapter();
                }
                break;
        }
    }

    private boolean checkRuntimePermissions() {
        for (String permission : RUNTIME_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Missing runtime permission: " + permission);
                return false;
            }
        }
        return true;
    }

    private void refreshAdapter() {
        String[] selectionArgs = new String[]{
                String.valueOf(CalendarSyncService.getCalendar(getContext()))
        };

        // Looking one year into the future is enough to display
        // everybody's birthday once...
        Calendar now = Calendar.getInstance();
        String nowString = Long.toString(now.getTimeInMillis());
        now.add(Calendar.YEAR, 1);
        String oneYearFromNow = Long.toString(now.getTimeInMillis());

        Uri eventsUri = CalendarContract.Instances.CONTENT_URI.buildUpon()
                .appendEncodedPath(nowString)
                .appendEncodedPath(oneYearFromNow)
                .build();

        //noinspection MissingPermission
        cursor = getContext().getContentResolver()
                .query(eventsUri, EventAdapter.PROJECTION, EventAdapter.SELECTION, selectionArgs,
                        CalendarContract.Instances.DTSTART + " ASC");

        EventAdapter adapter = new EventAdapter(getContext(), cursor);
        progressBar.setVisibility(View.GONE);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
    }
}
