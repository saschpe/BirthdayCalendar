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
