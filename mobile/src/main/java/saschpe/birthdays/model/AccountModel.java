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

package saschpe.birthdays.model;

import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Describes an item in an account list view
 */
public class AccountModel {
    private static final String TAG = AccountModel.class.getSimpleName();

    private final Account account;
    private final Drawable icon;
    private final String label;
    private boolean selected;

    public AccountModel(Context context, Account account, AuthenticatorDescription description, boolean selected) {
        this.account = account;
        this.selected = selected;

        PackageManager pm = context.getPackageManager();
        label = getAccountLabel(pm, description);
        icon = pm.getDrawable(description.packageName, description.iconId, null);
    }

    private static String getAccountLabel(PackageManager pm, AuthenticatorDescription description)
    {
        try {
            return pm.getResourcesForApplication(description.packageName).getString(description.labelId);
        } catch (Resources.NotFoundException | PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error retrieving label", e);
        }
        return description.packageName;
    }

    public Account getAccount() {
        return account;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggleSelected() { this.selected = !this.selected; }

    @Override
    public String toString() {
        return account.name + " - " + account.type;
    }
}
