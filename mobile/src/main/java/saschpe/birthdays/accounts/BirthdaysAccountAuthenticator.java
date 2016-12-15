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

package saschpe.birthdays.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

import saschpe.birthdays.activity.AccountCreateActivity;

public final class BirthdaysAccountAuthenticator extends AbstractAccountAuthenticator {
    private static final String TAG = BirthdaysAccountAuthenticator.class.getSimpleName();

    private Context context;

    public BirthdaysAccountAuthenticator(Context context) {
        super(context);
        this.context = context.getApplicationContext();
    }

    /**
     * Returns a Bundle that contains the Intent of the activity that can be used to edit the
     * properties. In order to indicate success the activity should call response.setResult()
     * with a non-null Bundle.
     *
     * @param response    used to set the result for the request. If the Constants.INTENT_KEY
     *                    is set in the bundle then this response field is to be used for sending future
     *                    results if and when the Intent is started.
     * @param accountType the AccountType whose properties are to be edited.
     * @return a Bundle containing the result or the Intent to date to continue the request.
     * If this is null then the request is considered to still be active and the result should
     * sent later using response.
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.e(TAG, "AccountAuthenticator.editProperties not implemented");
        // Editing properties is not supported
        throw new UnsupportedOperationException();
    }

    /**
     * Adds an account of the specified accountType.
     *
     * @param response         to send the result back to the AccountManager, will never be null
     * @param accountType      the type of account to add, will never be null
     * @param authTokenType    the type of auth token to retrieve after adding the account, may be null
     * @param requiredFeatures a String array of account_authenticator-specific features that the added
     *                         account must support, may be null
     * @param options          a Bundle of account_authenticator-specific options, may be null
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     * <ul>
     * <li> {@link android.accounts.AccountManager#KEY_INTENT}, or
     * <li> {@link android.accounts.AccountManager#KEY_ACCOUNT_NAME} and {@link android.accounts.AccountManager#KEY_ACCOUNT_TYPE} of
     * the account that was added, or
     * <li> {@link android.accounts.AccountManager#KEY_ERROR_CODE} and {@link android.accounts.AccountManager#KEY_ERROR_MESSAGE} to
     * indicate an error
     * </ul>
     * @throws android.accounts.NetworkErrorException if the account_authenticator could not honor the request due to a
     *                                                network error
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Intent intent = new Intent(context, AccountCreateActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        Bundle result = new Bundle();
        result.putParcelable(AccountManager.KEY_INTENT, intent);
        return result;
    }

    /**
     * Checks that the user knows the credentials of an account.
     *
     * @param response to send the result back to the AccountManager, will never be null
     * @param account  the account whose credentials are to be checked, will never be null
     * @param options  a Bundle of account_authenticator-specific options, may be null
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     * <ul>
     * <li> {@link android.accounts.AccountManager#KEY_INTENT}, or
     * <li> {@link android.accounts.AccountManager#KEY_BOOLEAN_RESULT}, true if the check succeeded, false otherwise
     * <li> {@link android.accounts.AccountManager#KEY_ERROR_CODE} and {@link android.accounts.AccountManager#KEY_ERROR_MESSAGE} to
     * indicate an error
     * </ul>
     * @throws android.accounts.NetworkErrorException if the account_authenticator could not honor the request due to a
     *                                                network error
     */
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        Log.e(TAG, "AccountAuthenticator.confirmCredentials not implemented");
        // Ignore attempts to confirm credential
        return null;
    }

    /**
     * Gets the authtoken for an account.
     *
     * @param response      to send the result back to the AccountManager, will never be null
     * @param account       the account whose credentials are to be retrieved, will never be null
     * @param authTokenType the type of auth token to retrieve, will never be null
     * @param options       a Bundle of account_authenticator-specific options, may be null
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     * <ul>
     * <li> {@link android.accounts.AccountManager#KEY_INTENT}, or
     * <li> {@link android.accounts.AccountManager#KEY_ACCOUNT_NAME}, {@link android.accounts.AccountManager#KEY_ACCOUNT_TYPE},
     * and {@link android.accounts.AccountManager#KEY_AUTHTOKEN}, or
     * <li> {@link android.accounts.AccountManager#KEY_ERROR_CODE} and {@link android.accounts.AccountManager#KEY_ERROR_MESSAGE} to
     * indicate an error
     * </ul>
     * @throws android.accounts.NetworkErrorException if the account_authenticator could not honor the request due to a
     *                                                network error
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.e(TAG, "AccountAuthenticator.getAuthToken not implemented");
        // Getting an authentication token is not supported
        throw new UnsupportedOperationException();
    }

    /**
     * Ask the account_authenticator for a localized label for the given authTokenType.
     *
     * @param authTokenType the authTokenType whose label is to be returned, will never be null
     * @return the localized label of the auth token type, may be null if the type isn't known
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.e(TAG, "AccountAuthenticator.getAuthTokenLabel not implemented");
        // Getting a label for the auth token is not supported
        throw new UnsupportedOperationException();
    }

    /**
     * Update the locally stored credentials for an account.
     *
     * @param response      to send the result back to the AccountManager, will never be null
     * @param account       the account whose credentials are to be updated, will never be null
     * @param authTokenType the type of auth token to retrieve after updating the credentials,
     *                      may be null
     * @param options       a Bundle of account_authenticator-specific options, may be null
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     * <ul>
     * <li> {@link android.accounts.AccountManager#KEY_INTENT}, or
     * <li> {@link android.accounts.AccountManager#KEY_ACCOUNT_NAME} and {@link android.accounts.AccountManager#KEY_ACCOUNT_TYPE} of
     * the account that was added, or
     * <li> {@link android.accounts.AccountManager#KEY_ERROR_CODE} and {@link android.accounts.AccountManager#KEY_ERROR_MESSAGE} to
     * indicate an error
     * </ul>
     * @throws android.accounts.NetworkErrorException if the account_authenticator could not honor the request due to a
     *                                                network error
     */
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.e(TAG, "AccountAuthenticator.updateCredentials not implemented");
        // Updating user credentials is not supported
        throw new UnsupportedOperationException();
    }

    /**
     * Checks if the account supports all the specified account_authenticator specific features.
     *
     * @param response to send the result back to the AccountManager, will never be null
     * @param account  the account to check, will never be null
     * @param features an array of features to check, will never be null
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     * <ul>
     * <li> {@link android.accounts.AccountManager#KEY_INTENT}, or
     * <li> {@link android.accounts.AccountManager#KEY_BOOLEAN_RESULT}, true if the account has all the features,
     * false otherwise
     * <li> {@link android.accounts.AccountManager#KEY_ERROR_CODE} and {@link android.accounts.AccountManager#KEY_ERROR_MESSAGE} to
     * indicate an error
     * </ul>
     * @throws android.accounts.NetworkErrorException if the account_authenticator could not honor the request due to a
     *                                                network error
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        Log.e(TAG, "AccountAuthenticator.hasFeatures: " + Arrays.toString(features));
        // Checking features for the account is not supported
        return null;
    }
}
