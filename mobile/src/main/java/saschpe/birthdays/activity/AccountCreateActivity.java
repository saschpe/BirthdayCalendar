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
