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

package saschpe.birthdays.fragment;

import android.Manifest;
import android.accounts.Account;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import saschpe.android.utils.helper.DisplayHelper;
import saschpe.birthdays.R;
import saschpe.birthdays.activity.MainActivity;
import saschpe.birthdays.adapter.AccountArrayAdapter;
import saschpe.birthdays.helper.PreferencesHelper;
import saschpe.birthdays.model.AccountModel;
import saschpe.birthdays.provider.AccountProviderHelper;
import saschpe.birthdays.service.loader.ContactAccountListLoader;

public class SourcesFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<AccountModel>> {
    private static final String TAG = SourcesFragment.class.getSimpleName();
    private static final String[] RUNTIME_PERMISSIONS = new String[] {
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.GET_ACCOUNTS
    };
    private static final int PERMISSION_REQUEST_ALL = 1;

    private AccountArrayAdapter adapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * <p>
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>Any restored child fragments will be created before the base
     * <code>Fragment.onCreate</code> method returns.</p>
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init recycler adapter
        adapter = new AccountArrayAdapter(getActivity(), null);
        adapter.setOnAccountSelectedListener(new AccountArrayAdapter.OnAccountSelectedListener() {
            @Override
            public void onAccountSelected(AccountModel accountModel) {
                storeSelectedAccountsAndSync();
            }
        });
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sources, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        return rootView;
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (checkRuntimePermissions()) {
            loadContactAccounts();
        } else {
            requestPermissions(RUNTIME_PERMISSIONS, PERMISSION_REQUEST_ALL);
        }
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Init recycler view
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(DisplayHelper.getSuitableLayoutManager(getActivity()));

        super.onViewCreated(view, savedInstanceState);
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

    @Override
    public Loader<List<AccountModel>> onCreateLoader(int i, Bundle bundle) {
        return new ContactAccountListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<AccountModel>> loader, List<AccountModel> objects) {
        adapter.replaceAll(objects);
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        storeSelectedAccountsAndSync();
    }

    @Override
    public void onLoaderReset(Loader<List<AccountModel>> loader) {
        adapter.clear();
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private boolean checkRuntimePermissions() {
        for (String permission : RUNTIME_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Missing runtime permission: " + permission);
                return false;
            }
        }
        return true;
    }

    private void loadContactAccounts() {
        Log.d(TAG, "Restarting contact account list loader...");
        getLoaderManager().restartLoader(0, null, this);
    }

    private void storeSelectedAccountsAndSync() {
        Log.i(TAG, "Store selected accounts and sync...");
        List<Account> contactAccountWhiteList = new ArrayList<>();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            AccountModel item = adapter.getItem(i);
            if (!item.isSelected()) {
                contactAccountWhiteList.add(item.getAccount());
            }
        }

        if (PreferencesHelper.getFirstRun(getActivity()) ||
                !contactAccountWhiteList.equals(AccountProviderHelper.getAccountList(getActivity()))) {
            PreferencesHelper.setFirstRun(getActivity(), false);
            AccountProviderHelper.setAccountList(getActivity(), contactAccountWhiteList);

            LocalBroadcastManager.getInstance(getActivity())
                    .sendBroadcast(new Intent(MainActivity.ACTION_SYNC_REQUESTED));
        }
    }
}
