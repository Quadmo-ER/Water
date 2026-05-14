package com.vinod.waterreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

final class ReminderScheduler {
    static final String PREFS = "water_reminder_prefs";
    static final String KEY_ENABLED = "enabled";
    static final String KEY_INTERVAL_MINUTES = "interval_minutes";
    static final int DEFAULT_INTERVAL_MINUTES = 60;

    private static final int REQUEST_CODE = 1001;

    private ReminderScheduler() {
    }

    static void saveSettings(Context context, boolean enabled, int intervalMinutes) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ENABLED, enabled)
                .putInt(KEY_INTERVAL_MINUTES, intervalMinutes)
                .apply();
    }

    static boolean isEnabled(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, false);
    }

    static int getIntervalMinutes(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_INTERVAL_MINUTES, DEFAULT_INTERVAL_MINUTES);
    }

    static void scheduleNext(Context context) {
        int intervalMinutes = Math.max(1, getIntervalMinutes(context));
        long triggerAt = SystemClock.elapsedRealtime() + intervalMinutes * 60_000L;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = reminderIntent(context);
        alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent);
    }

    static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(reminderIntent(context));
        }
    }

    private static PendingIntent reminderIntent(Context context) {
        Intent intent = new Intent(context, WaterReminderReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
