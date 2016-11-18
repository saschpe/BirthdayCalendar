package saschpe.birthdays.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Handle the transfer of data between a server and the
 * app, using the Android sync adapter framework.
 */
class CalendarSyncAdapter extends AbstractThreadedSyncAdapter {
    /**
     * Set up the sync adapter
     */
    CalendarSyncAdapter(Context context)
    {
        super(context, true);
    }

    /**
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        CalendarSyncService.performSync(getContext());
    }
}
