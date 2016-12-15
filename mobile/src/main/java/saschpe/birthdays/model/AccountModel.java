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
