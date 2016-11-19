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

package saschpe.birthdays.service.loader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import saschpe.birthdays.helper.AccountHelper;
import saschpe.birthdays.model.AccountModel;
import saschpe.birthdays.provider.AccountProviderHelper;

public class ContactAccountListLoader extends AsyncTaskLoader<List<AccountModel>> {
    private static final String TAG = ContactAccountListLoader.class.getSimpleName();

    private List<AccountModel> accounts;

    public ContactAccountListLoader(Context context) {
        super(context);
    }

    @Override
    public List<AccountModel> loadInBackground() {
        if (!AccountHelper.isAccountActivated(getContext())) {
            AccountHelper.addAccount(getContext());
        }

        // Retrieve all accounts that are actively used for contacts
        HashSet<Account> contactAccounts = new HashSet<>();
        Cursor cursor = null;
        try {
            cursor = getContext().getContentResolver().query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    new String[] {
                            ContactsContract.RawContacts.ACCOUNT_NAME,
                            ContactsContract.RawContacts.ACCOUNT_TYPE
                    }, null, null, null);

            while (cursor.moveToNext()) {
                String account_name = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                String account_type = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                Account account = new Account(account_name, account_type);
                contactAccounts.add(account);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving accounts", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        List<Account> accountBlacklist = AccountProviderHelper.getAccountList(getContext());
        Log.d(TAG, "Stored account list: " + accountBlacklist);

        AccountManager manager = AccountManager.get(getContext());
        AuthenticatorDescription[] descriptions = manager.getAuthenticatorTypes();

        ArrayList<AccountModel> items = new ArrayList<>();
        for (Account account : contactAccounts) {
            for (AuthenticatorDescription description : descriptions) {
                if (description.type.equals(account.type)) {
                    boolean disabled = accountBlacklist.contains(account);
                    items.add(new AccountModel(getContext(), account, description, !disabled));
                }
            }
        }

        // Sort the list
        Collections.sort(items, ALPHA_COMPARATOR);

        return items;
    }

    /**
     * Handles a request to date the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (accounts != null) {
            // If we currently have a result available, deliver it immediately.
            deliverResult(accounts);
        }

        if (takeContentChanged() || accounts == null) {
            // If the data has changed since the last time it was loaded or
            // is not currently available, date a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    public void stopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();

        if (accounts != null) {
            accounts = null;
        }
    }

    /**
     * Perform alphabetical comparison of account item objects.
     */
    private static final Comparator<AccountModel> ALPHA_COMPARATOR = new Comparator<AccountModel>() {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(AccountModel item1, AccountModel item2) {
            return collator.compare(item1.getLabel(), item2.getLabel());
        }
    };
}
