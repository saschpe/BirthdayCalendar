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

package saschpe.birthdays.helper;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import saschpe.birthdays.R;
import saschpe.birthdays.service.BirthdaysIntentService;

public class AccountHelper {
    private static final String TAG = AccountHelper.class.getSimpleName();

    public static Bundle addAccount(Context context) {
        Log.d(TAG, "AccountHelper.addAccount: Adding account...");

        final Account account = new Account(context.getString(R.string.app_name), context.getString(R.string.account_type));
        AccountManager manager = AccountManager.get(context);

        if (manager.addAccountExplicitly(account, null, null)) {
            // Enable automatic sync once per day
            ContentResolver.setSyncAutomatically(account, context.getString(R.string.content_authority), true);
            ContentResolver.setIsSyncable(account, context.getString(R.string.content_authority), 1);

            // Add periodic sync interval based on user preference
            final long freq = PreferencesHelper.getPeriodicSyncFrequency(context);
            ContentResolver.addPeriodicSync(account, context.getString(R.string.content_authority), new Bundle(), freq);

            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            Log.i(TAG, "Account added: " + account.name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.notifyAccountAuthenticated(account);
            }
            return result;
        } else {
            Log.e(TAG, "Adding account explicitly failed!");
            return null;
        }
    }

    /**
     * Adds account and forces manual sync afterwards if adding was successful
     */
    public static Bundle addAccountAndSync(Context context, Handler backgroundStatusHandler) {
        final Bundle result = addAccount(context);
        if (result != null) {
            if (result.containsKey(AccountManager.KEY_ACCOUNT_NAME)) {
                BirthdaysIntentService.startActionSync(context, backgroundStatusHandler);
                return result;
            } else {
                Log.e(TAG, "Unable to add account. Result did not contain KEY_ACCOUNT_NAME");
            }
        } else {
            Log.e(TAG, "Unable to add account. Result was null.");
        }
        return null;
    }

    /**
     * Remove account from Android system
     */
    public static boolean removeAccount(Context context) {
        Log.d(TAG, "Removing account...");
        AccountManager manager = AccountManager.get(context);
        final Account account = new Account(context.getString(R.string.app_name), context.getString(R.string.account_type));
        AccountManagerFuture<Boolean> future = manager.removeAccount(account, null, null);
        if (future.isDone()) {
            try {
                future.getResult();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Problem while removing account!", e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Checks whether the account is enabled or not
     */
    public static boolean isAccountActivated(Context context) {
        AccountManager manager = AccountManager.get(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Lacking permission GET_ACCOUNTS to query existing accounts!");
            return false;
        }
        Account[] availableAccounts = manager.getAccountsByType(context.getString(R.string.account_type));
        for (Account currentAccount : availableAccounts) {
            if (currentAccount.name.equals(context.getString(R.string.app_name))) {
                Log.i(TAG, "Account already present");
                return true;
            }
        }
        return false;
    }
}
