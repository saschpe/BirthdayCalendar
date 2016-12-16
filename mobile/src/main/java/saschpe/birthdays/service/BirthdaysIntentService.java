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

package saschpe.birthdays.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import saschpe.birthdays.helper.AccountHelper;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class BirthdaysIntentService extends IntentService {
    private static final String TAG = BirthdaysIntentService.class.getSimpleName();
    public static final String ACTION_SYNC = "saschpe.birthdays.service.action.SYNC";
    public static final String ACTION_CHANGE_COLOR = "saschpe.birthdays.service.action.CHANGE_COLOR";
    public static final String EXTRA_ERROR_CODE = "saschpe.birthdays.service.extra.ERROR_CODE";
    public static final String EXTRA_ERROR_THROWABLE = "saschpe.birthdays.service.extra.ERROR_EXCEPTION";
    public static final String EXTRA_ERROR_MESSAGE = "saschpe.birthdays.service.extra.ERROR_MESSAGE";
    public static final String EXTRA_MESSENGER = "saschpe.birthdays.service.extra.MESSENGER";
    public static final int MESSAGE_WHAT_STARTED = 1;
    public static final int MESSAGE_WHAT_DONE = 2;
    public static final int MESSAGE_WHAT_ERROR = 3;

    private Messenger messenger;

    /**
     * Starts this service to perform action Sync with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    public static void startActionSync(@NonNull Context context, @Nullable Handler handler) {
        Log.d(TAG, "Start ACTION_SYNC");
        Intent intent = new Intent(context, BirthdaysIntentService.class);
        if (handler != null) {
            // Create a new Messenger for back-channel communication
            intent.putExtra(EXTRA_MESSENGER, new Messenger(handler));
        }
        intent.setAction(ACTION_SYNC);
        context.startService(intent);
    }

    public static void startActionChangeColor(@NonNull Context context) {
        Log.d(TAG, "Start ACTION_CHANGE_COLOR");
        Intent intent = new Intent(context, BirthdaysIntentService.class);
        intent.setAction(ACTION_CHANGE_COLOR);
        context.startService(intent);
    }

    public BirthdaysIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(EXTRA_MESSENGER)) {
            messenger = (Messenger) extras.get(EXTRA_MESSENGER);
        }

        sendMessage(MESSAGE_WHAT_STARTED);

        switch(intent.getAction()) {
            case ACTION_SYNC:
                CalendarSyncService.performSync(this);
                break;
            case ACTION_CHANGE_COLOR:
                // Update calendar color if enabled
                if (AccountHelper.isAccountActivated(this)) {
                    CalendarSyncService.updateCalendarColor(this);
                }
                break;
        }

        sendMessage(MESSAGE_WHAT_DONE);
    }

    private void sendMessage(int message) {
        sendMessage(message, null);
    }

    private void sendMessage(int message, @Nullable Bundle data) {
        if (messenger != null) {
            Message msg = Message.obtain();
            msg.what = message;
            msg.setData(data);
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.v(TAG, "Unable to send message", e);
            }
        }
    }
}
