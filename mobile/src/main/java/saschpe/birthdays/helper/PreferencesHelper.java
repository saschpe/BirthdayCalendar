package saschpe.birthdays.helper;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import saschpe.birthdays.R;

public class PreferencesHelper {
    public static boolean getFirstRun(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_first_run_key),
                        context.getResources().getBoolean(R.bool.pref_first_run_default));
    }

    public static void setFirstRun(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.pref_first_run_key), value)
                .apply();
    }

    public static int getCalendarColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.pref_calendar_color_key),
                        context.getResources().getColor(R.color.pref_calendar_color_default));
    }

    public static int isCalendarSynced(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_birthdays_sync_key),
                        context.getResources().getBoolean(R.bool.pref_birthdays_sync_default))) {
            return 1;
        }
        return 0;
    }

    public static long getPeriodicSyncFrequency(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.pref_periodic_sync_frequency_key),
                        AlarmManager.INTERVAL_DAY);
    }

    /**
     * Return minute count for all reminders
     */
    public static long[] getReminderMinutes(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // rather hard-wired but good enough
        long[] minutes = new long[2];
        minutes[0] = Long.valueOf(prefs.getString(context.getString(R.string.pref_reminder_time_1_key), context.getResources().getString(R.string.pref_reminder_time_1_default)));
        minutes[1] = Long.valueOf(prefs.getString(context.getString(R.string.pref_reminder_time_2_key), context.getResources().getString(R.string.pref_reminder_time_2_default)));
        return minutes;
    }
}
