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

package saschpe.birthdays.provider;

import android.accounts.Account;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccountProviderHelper {
    private static final String TAG = AccountProviderHelper.class.getSimpleName();

    public static void setAccountList(Context context, List<Account> accounts) {
        // Clear table
        context.getContentResolver().delete(AccountContract.AccountList.CONTENT_URI, null, null);

        ContentValues[] values = new ContentValues[accounts.size()];

        // Build values array based on HashSet
        Iterator<Account> iterator = accounts.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Account account = iterator.next();
            Log.d(TAG, "Select account: " + account);
            values[i] = new ContentValues();
            values[i].put(AccountContract.AccountList.ACCOUNT_NAME, account.name);
            values[i].put(AccountContract.AccountList.ACCOUNT_TYPE, account.type);
            i++;
        }

        // Insert as bulk operation
        context.getContentResolver().bulkInsert(AccountContract.AccountList.CONTENT_URI, values);
    }

    public static List<Account> getAccountList(Context context) {
        List<Account> accounts = new ArrayList<>();
        Cursor cursor = AccountContract.getAccountListCursor(context, null, null);

        try {
            while (cursor != null && cursor.moveToNext()) {
                Account account = new Account(
                        cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.AccountList.ACCOUNT_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.AccountList.ACCOUNT_TYPE)));
                accounts.add(account);
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }

        return accounts;
    }

}
