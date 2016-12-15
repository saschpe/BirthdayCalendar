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

import android.accounts.AccountAuthenticatorActivity;
import android.os.Bundle;

import saschpe.birthdays.helper.AccountHelper;

public class AccountCreateActivity extends AccountAuthenticatorActivity {
    /**
     * Retrieves the AccountAuthenticatorResponse from either the intent of the
     * icicle, if the icicle is non-zero.
     *
     * @param icicle the save instance data of this Activity, may be null
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle result = AccountHelper.addAccountAndSync(this, null);
        if (result != null) {
            setAccountAuthenticatorResult(result);
        }
        finish();
    }
}
