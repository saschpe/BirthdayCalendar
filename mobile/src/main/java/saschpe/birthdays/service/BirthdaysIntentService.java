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
