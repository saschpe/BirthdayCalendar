/*
 * Copyright (C) 2016 Sascha Peilicke
 * Copyright (C) 2012-2013 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (C) 2010 Sam Steele
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

import android.accounts.Account;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import saschpe.birthdays.BuildConfig;
import saschpe.birthdays.R;
import saschpe.birthdays.helper.PreferencesHelper;
import saschpe.birthdays.provider.AccountProviderHelper;

public class CalendarSyncService extends Service {
    public static final String ACTION_SYNC_DONE = "saschpe.birthdays.service.action.SYNC_DONE";

    private static final String TAG = CalendarSyncService.class.getSimpleName();
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd",   // Most used
            "--MM-dd",      // Most used format without year
            "yyyyMMdd",     // HTC Desire
            "dd.MM.yyyy",
            "yyyy.MM.dd",
            "MM/dd/yyyy",   // Facebook
            "MM/dd",        // Facebook
            "dd/MM/yyyy",
            "dd/MM",
    };

    // Storage for an instance of the sync adapter
    private CalendarSyncAdapter syncAdapter = null;

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
        if (syncAdapter == null) {
            syncAdapter = new CalendarSyncAdapter(this);
        }
        return syncAdapter.getSyncAdapterBinder();
    }

    /**
     * Syncing work-horse.
     *
     * Simply runs in current thread if invoked directly.
     */
    public static void performSync(Context context) {
        Log.d(TAG, "Perform sync...");

        ContentResolver cr = context.getContentResolver();
        if (cr == null) {
            Log.e(TAG, "Unable to get content resolver!");
            return;
        }

        long calendarId = getCalendar(context);
        int deletedRows = cr.delete(getCalendarUri(context, CalendarContract.Events.CONTENT_URI),
                CalendarContract.Events.CALENDAR_ID + " = ?", new String[]{String.valueOf(calendarId)});
        Log.i(TAG, "Removed " + deletedRows + " rows from calendar " + calendarId);
        final long[] reminderMinutes = PreferencesHelper.getReminderMinutes(context);
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        Cursor cursor = getContactsEvents(context, cr);
        if (cursor == null) {
            Log.e(TAG, "Failed to parse contacts events");
        }

        try {
            final int eventDateColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE);
            final int displayNameColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            final int eventTypeColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE);
            final int eventCustomLabelColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.LABEL);
            final int eventLookupKeyColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.LOOKUP_KEY);

            int backRef = 0;

            // Loop over all events
            while (cursor != null && cursor.moveToNext()) {
                final String eventDateString = cursor.getString(eventDateColumn);
                final String displayName = cursor.getString(displayNameColumn);
                final int eventType = cursor.getInt(eventTypeColumn);
                final String eventLookupKey = cursor.getString(eventLookupKeyColumn);
                final Date eventDate = parseEventDateString(eventDateString);

                if (eventDate != null) {
                    // Get year from event
                    Calendar eventCalendar = Calendar.getInstance();
                    eventCalendar.setTime(eventDate);
                    int eventYear = eventCalendar.get(Calendar.YEAR);
                    //Log.debug("Event year: " + eventYear);

                    // If year < 1800 don't show brackets with age behind name. When no year is defined
                    // parseEventDateString() sets it to 1700. Also iCloud for example sets year to
                    // 1604 if no year is defined in their user interface.
                    boolean hasYear = false;
                    if (eventYear >= 1800) {
                        hasYear = true;
                    }

                    // Get current year
                    Calendar currentCalendar = Calendar.getInstance();
                    final int currentYear = currentCalendar.get(Calendar.YEAR);

                    // Insert events for the past 2 years and the next 5 years. Events are not inserted
                    // as recurring events to have different titles with birthday age in it.
                    final int startYear = currentYear - 1;
                    final int endYear = currentYear + 3;

                    for (int year = startYear; year <= endYear; year++) {
                        // Calculate age
                        final int age = year - eventYear;
                        // If birthday has year and age of this event >= 0, display age in title
                        boolean includeAge = false;
                        if (hasYear && age >= 0) {
                            includeAge = true;
                        }
                        //Log.debug("Year: " + year + " age: " + age + " include age: " + includeAge);

                        String title = null;
                        String description = "";
                        if (displayName != null) {
                            switch (eventType) {
                                case ContactsContract.CommonDataKinds.Event.TYPE_CUSTOM:
                                    String eventCustomLabel = cursor.getString(eventCustomLabelColumn);
                                    if (eventCustomLabel != null) {
                                        title = context.getString(R.string.event_custom_title_template, displayName, eventCustomLabel);
                                    } else {
                                        title = context.getString(R.string.event_other_title_template, displayName);
                                    }
                                    break;
                                case ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY:
                                    title = context.getString(R.string.event_anniversary_title_template, displayName);
                                    if (age == 1) {
                                        description = context.getString(R.string.event_anniversary_description_singular_template, displayName, age);
                                    } else {
                                        description = context.getString(R.string.event_anniversary_description_plural_template, displayName, age);
                                    }
                                    break;
                                case ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY:
                                    title = context.getString(R.string.event_birthday_title_template, displayName);
                                    if (age == 1) {
                                        description = context.getString(R.string.event_birthday_description_singular_template, displayName, eventYear, age);
                                    } else {
                                        description = context.getString(R.string.event_birthday_description_plural_template, displayName, eventYear, age);
                                    }
                                    break;
                                default:
                                    // Includes ContactsContract.CommonDataKinds.Event.TYPE_OTHER
                                    title = String.format(context.getString(R.string.event_other_title_template), displayName);
                                    break;
                            }
                        }

                        if (title != null) {
                            Log.d(TAG, "Title: " + title + " backref: " + backRef);
                            operations.add(insertEvent(context, calendarId, eventDate, year, title, description, eventLookupKey));

                            // Gets ContentProviderOperation to insert new reminder to the ContentProviderOperation
                            // with the given backRef. This is done using "withValueBackReference"
                            int reminderOperations = 0;
                            for (long reminderMinute : reminderMinutes) {
                                if (reminderMinute >= -1) {
                                    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(getCalendarUri(context, CalendarContract.Reminders.CONTENT_URI));
                                    // Add reminder to last added event identified by backRef
                                    // see http://stackoverflow.com/questions/4655291/semantics-of-withvaluebackreference
                                    builder.withValueBackReference(CalendarContract.Reminders.EVENT_ID, backRef);
                                    builder.withValue(CalendarContract.Reminders.MINUTES, reminderMinute);
                                    builder.withValue(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                                    operations.add(builder.build());
                                    reminderOperations += 1;
                                }
                            }
                            // Back reference for the next reminders, 1 is for the event
                            backRef += 1 + reminderOperations;
                        } else {
                            Log.d(TAG, "Event title empty, won't insert event and reminder");
                        }

                        // Intermediate commit, otherwise the binder transaction fails on large operation list
                        if (operations.size() > 200) {
                            applyBatchOperation(cr, operations);
                            backRef = 0;
                            operations.clear();
                        }
                    }
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }

        // Create remaining events (that haven't been created by intermediate commits):
        if (operations.size() > 0) {
            applyBatchOperation(cr, operations);
        }

        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(ACTION_SYNC_DONE));
        Log.d(TAG, "Done performing sync...");
    }

    private static void applyBatchOperation(ContentResolver cr, ArrayList<ContentProviderOperation> operations) {
        try {
            Log.d(TAG, "Applying calendar batch operation");
            cr.applyBatch(CalendarContract.AUTHORITY, operations);
            Log.d(TAG, "Successfully applied calendar batch operation");
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply calendar batch operation");
        }
    }

    private static ContentProviderOperation insertEvent(Context context, long calendarId, Date eventDate, int year, String title, String description, String lookupKey) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(getCalendarUri(context, CalendarContract.Events.CONTENT_URI));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(eventDate);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        /* Define over entire day.
         *
         * Note: ALL_DAY is enough on original Android calendar, but some calendar apps (Business
         * Calendar) do not display the event if time between dtstart and dtend is 0
         */
        final long dtstart = calendar.getTimeInMillis();
        final long dtend = dtstart + DateUtils.DAY_IN_MILLIS;

        builder.withValue(CalendarContract.Events.CALENDAR_ID, calendarId);
        builder.withValue(CalendarContract.Events.DTSTART, dtstart);
        builder.withValue(CalendarContract.Events.DTEND, dtend);
        builder.withValue(CalendarContract.Events.EVENT_TIMEZONE, Time.TIMEZONE_UTC);
        builder.withValue(CalendarContract.Events.ALL_DAY, 1);
        builder.withValue(CalendarContract.Events.TITLE, title);
        builder.withValue(CalendarContract.Events.DESCRIPTION, description);
        builder.withValue(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);

        /* Enable reminders for this event
         * Note: Need to be explicitly set on Android < 4 to enable reminders
         */
        builder.withValue(CalendarContract.Events.HAS_ALARM, 1);

        // Set availability to free.
        if (Build.VERSION.SDK_INT >= 14) {
            builder.withValue(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
        }
        // Add button to open contact
        if (Build.VERSION.SDK_INT >= 16 && lookupKey != null) {
            builder.withValue(CalendarContract.Events.CUSTOM_APP_PACKAGE, context.getPackageName());
            final Uri contactLookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            builder.withValue(CalendarContract.Events.CUSTOM_APP_URI, contactLookupUri.toString());
        }

        return builder.build();
    }

    private static Cursor getContactsEvents(Context context, ContentResolver contentResolver) {
        // Account blacklist from our provider
        List<Account> accountBlacklist = AccountProviderHelper.getAccountList(context);

        List<String> addedEventsIdentifiers = new ArrayList<>();

        /* 1. Get all raw contacts with their corresponding Account name and type (only raw
         *    contacts get Account affiliation) */
        Uri rawContactsUri = ContactsContract.RawContacts.CONTENT_URI;
        String[] rawContactsProjection = new String[] {
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.CONTACT_ID,
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.RawContacts.ACCOUNT_NAME,
                ContactsContract.RawContacts.ACCOUNT_TYPE
        };
        Cursor rawContacts = contentResolver.query(rawContactsUri, rawContactsProjection, null, null, null);

        /* 2. Go over all raw contacts and check if the Account is allowed. If account is allowed,
         *    get display name and lookup key and all events for this contact. Build a new
         *    MatrixCursor out of this data that can be used. */
        String[] columns = new String[] {
                BaseColumns._ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.CommonDataKinds.Event.LABEL,
        };
        MatrixCursor mc = new MatrixCursor(columns);
        int mcIndex = 0;
        try {
            while (rawContacts != null && rawContacts.moveToNext()) {
                long rawId = rawContacts.getLong(rawContacts.getColumnIndex(ContactsContract.RawContacts._ID));
                String accountType = rawContacts.getString(rawContacts.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                String accountName = rawContacts.getString(rawContacts.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));

                // 2a. Check if Account is allowed (not in blacklist)
                boolean addEvent;
                if (TextUtils.isEmpty(accountType) || TextUtils.isEmpty(accountName)) {
                    // Workaround: Simply add events without proper Account
                    addEvent = true;
                } else {
                    Account account = new Account(accountName, accountType);
                    addEvent = !accountBlacklist.contains(account);
                }

                if (addEvent) {
                    String displayName = null;
                    String lookupKey = null;

                    // 2b. Get display name and lookup key from normal contact table
                    String[] displayProjection = new String[] {
                            ContactsContract.Data.RAW_CONTACT_ID,
                            ContactsContract.Data.DISPLAY_NAME,
                            ContactsContract.Data.LOOKUP_KEY
                    };
                    String displayWhere = ContactsContract.Data.RAW_CONTACT_ID + "= ?";
                    String[] displaySelectionArgs = new String[] {
                            String.valueOf(rawId)
                    };
                    Cursor displayCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, displayProjection, displayWhere, displaySelectionArgs, null);
                    try {
                        if (displayCursor != null && displayCursor.moveToNext()){
                            displayName = displayCursor.getString(displayCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                            lookupKey = displayCursor.getString(displayCursor.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
                        }
                    } finally {
                        if (displayCursor != null && !displayCursor.isClosed()) {
                            displayCursor.close();
                        }
                    }

                    /* 2c. Get all events for this raw contact. We don't get this information for
                     *     the (merged) contact table, but from the raw contact. If we would query
                     *     this information from the contact table, we would also get events that
                     *     should have been filtered. */
                    Uri thisRawContactUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawId);
                    Uri entityUri = Uri.withAppendedPath(thisRawContactUri, ContactsContract.RawContacts.Entity.CONTENT_DIRECTORY);
                    String[] eventsProjection = new String[] {
                            ContactsContract.RawContacts._ID,
                            ContactsContract.RawContacts.Entity.DATA_ID,
                            ContactsContract.CommonDataKinds.Event.START_DATE,
                            ContactsContract.CommonDataKinds.Event.TYPE,
                            ContactsContract.CommonDataKinds.Event.LABEL
                    };
                    String eventsWhere = ContactsContract.RawContacts.Entity.MIMETYPE + " = ? AND " + ContactsContract.RawContacts.Entity.DATA_ID + " IS NOT NULL";
                    String[] eventsSelectionArgs = new String[] {
                            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                    };
                    Cursor eventsCursor = contentResolver.query(entityUri, eventsProjection, eventsWhere, eventsSelectionArgs, null);
                    try {
                        while (eventsCursor != null && eventsCursor.moveToNext()) {
                            String startDate = eventsCursor.getString(eventsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                            int type = eventsCursor.getInt(eventsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE));
                            String label = eventsCursor.getString(eventsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.LABEL));

                            /* 2d. Add this information to our MatrixCursor if not already added
                             *     previously. If two Event Reminder accounts have the same contact
                             *     with duplicated events, the event will already be in the HashSet
                             *     addedEventsIdentifiers.
                             *
                             *     eventIdentifier does not include startDate, because the
                             *     String formats of startDate differ between accounts. */
                            //String eventIdentifier = lookupKey + type + label;
                            String eventIdentifier = lookupKey + type;
                            if (addedEventsIdentifiers.contains(eventIdentifier)) {
                                Log.d(TAG, "Duplicate event was not added!");
                            } else {
                                Log.d(TAG, "Event was added with identifier " + eventIdentifier);

                                addedEventsIdentifiers.add(eventIdentifier);

                                mc.newRow().add(mcIndex).add(displayName).add(lookupKey).add(startDate).add(type).add(label);
                                mcIndex++;
                            }

                        }
                    } finally {
                        if (eventsCursor != null && !eventsCursor.isClosed()) {
                            eventsCursor.close();
                        }
                    }
                }
            }
        } finally {
            if (rawContacts != null && !rawContacts.isClosed()) {
                rawContacts.close();
            }
        }
        /*if (BuildConfig.DEBUG) {
            DatabaseUtils.dumpCursor(mc);
        }*/
        return mc;
    }

    private static Date parseEventDateString(String eventDateString) {
        if (eventDateString != null) {
            Date eventDate = null;

            for (String dateFormat : DATE_FORMATS) {
                if (eventDate == null) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
                    simpleDateFormat.setTimeZone(TimeZone.getDefault());

                    eventDate = simpleDateFormat.parse(eventDateString, new ParsePosition(0));
                    if (eventDate != null && !dateFormat.contains("yyyy")) {
                        // Because no year is defined in address book, set year to 1700. When
                        // year < 1800, the age will be not displayed in brackets
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(eventDate);
                        cal.set(Calendar.YEAR, 1700);
                    }
                }
            }
            // Unix timestamp - Some Motorola devices
            if (eventDate == null) {
                try {
                    eventDate = new Date(Long.parseLong(eventDateString));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing event date string " + eventDateString);
                }
            }
            /*if (eventDate != null) {
                Log.debug("Parsed event date string: " + eventDate.toString());
            }*/
            return eventDate;
        } else {
            return null;
        }
    }

    /**
     * Updates the calendar color
     */
    public static void updateCalendarColor(Context context) {
        int color = PreferencesHelper.getCalendarColor(context);
        ContentResolver cr = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(getCalendarUri(context, CalendarContract.Calendars.CONTENT_URI),
                getCalendar(context));

        Log.d(TAG, "Updating calendar " + uri.toString() + " color " + color);

        ContentProviderClient client = cr.acquireContentProviderClient(CalendarContract.AUTHORITY);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, color);
        try {
            client.update(uri, values, null, null);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to update calendar color!", e);
        }
        client.release();
    }

    /**
     * Returns birthday calendar ID.
     *
     * If no calendar is present, a new one is created.
     *
     * @return calendar id
     */
    public static long getCalendar(Context context) {
        final Uri calenderUri = getCalendarUri(context, CalendarContract.Calendars.CONTENT_URI);

        final String selection = CalendarContract.Calendars.ACCOUNT_NAME + " = ? AND " +
                                 CalendarContract.Calendars.ACCOUNT_TYPE + " = ? AND " +
                                 CalendarContract.Calendars.NAME + " = ?";
        final String calendarName = context.getString(R.string.calendar_name);

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(calenderUri,
                new String[] { BaseColumns._ID },
                selection,
                new String[] {
                        context.getString(R.string.app_name),
                        context.getString(R.string.account_type),
                        calendarName
                }, null);

        if (cursor != null && cursor.moveToNext()) {
            // Yay, calendar exists already. Just return it's id.
            long calendarId = cursor.getLong(0);
            cursor.close();
            return calendarId;
        } else {
            if (cursor != null) {
                cursor.close();
            }

            // So we've got to create a calendar first before we can return it's id.
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(calenderUri);
            builder.withValue(CalendarContract.Calendars.ACCOUNT_NAME, context.getString(R.string.app_name));
            builder.withValue(CalendarContract.Calendars.ACCOUNT_TYPE, context.getString(R.string.account_type));
            builder.withValue(CalendarContract.Calendars.NAME, calendarName);
            builder.withValue(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, context.getString(R.string.birthdays_and_anniversaries));
            builder.withValue(CalendarContract.Calendars.CALENDAR_COLOR, PreferencesHelper.getCalendarColor(context));
            builder.withValue(CalendarContract.Calendars.SYNC_EVENTS, PreferencesHelper.isCalendarSynced(context));
            builder.withValue(CalendarContract.Calendars.VISIBLE, 1);
            if (BuildConfig.DEBUG) {
                builder.withValue(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_EDITOR);
            } else {
                builder.withValue(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_READ);
            }
            builder.withValue(CalendarContract.Calendars.OWNER_ACCOUNT, context.getString(R.string.app_name));

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(builder.build());
            try {
                cr.applyBatch(CalendarContract.AUTHORITY, operations);
                Log.d(TAG, "Created calendar " + calendarName);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to create new calendar!", e);
                return -1;
            } catch (OperationApplicationException e) {
                Log.e(TAG, "Unable to create new calendar!", e);
                return -2;
            }

            // Try once again, this time we should find something in the database
            return getCalendar(context);
        }
    }

    /**
     * Builds URI based on account.
     */
    private static Uri getCalendarUri(Context context, Uri contentUri) {
        return contentUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, context.getString(R.string.app_name))
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, context.getString(R.string.account_type))
                .build();
    }
}