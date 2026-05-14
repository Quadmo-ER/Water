package com.vinod.waterreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

final class ReminderScheduler {
    static final String PREFS = "water_reminder_prefs";
    static final String KEY_REMINDERS = "reminders";
    static final String KEY_NEXT_ID = "next_id";
    static final String EXTRA_REMINDER_ID = "reminder_id";
    static final String EXTRA_REMINDER_TITLE = "reminder_title";
    static final int DEFAULT_INTERVAL_HOURS = 1;

    private static final int REQUEST_BASE = 4000;

    private ReminderScheduler() {
    }

    static List<WaterReminder> getReminders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_REMINDERS, "");
        List<WaterReminder> reminders = new ArrayList<>();

        if (saved == null || saved.isEmpty()) {
            WaterReminder defaultReminder = new WaterReminder(nextId(context), "Drink water", DEFAULT_INTERVAL_HOURS, false);
            reminders.add(defaultReminder);
            saveReminders(context, reminders);
            return reminders;
        }

        try {
            JSONArray array = new JSONArray(saved);
            for (int index = 0; index < array.length(); index += 1) {
                reminders.add(WaterReminder.fromJson(array.getJSONObject(index)));
            }
        } catch (JSONException ignored) {
            reminders.clear();
        }

        if (reminders.isEmpty()) {
            reminders.add(new WaterReminder(nextId(context), "Drink water", DEFAULT_INTERVAL_HOURS, false));
            saveReminders(context, reminders);
        }
        return reminders;
    }

    static void addReminder(Context context, String title, int intervalHours) {
        List<WaterReminder> reminders = getReminders(context);
        WaterReminder reminder = new WaterReminder(nextId(context), cleanTitle(title), Math.max(1, intervalHours), true);
        reminders.add(reminder);
        saveReminders(context, reminders);
        schedule(context, reminder);
    }

    static void updateReminder(Context context, WaterReminder reminder) {
        List<WaterReminder> reminders = getReminders(context);
        for (int index = 0; index < reminders.size(); index += 1) {
            if (reminders.get(index).id == reminder.id) {
                reminders.set(index, reminder);
                break;
            }
        }
        saveReminders(context, reminders);
        cancel(context, reminder.id);
        if (reminder.enabled) {
            schedule(context, reminder);
        }
    }

    static void deleteReminder(Context context, int reminderId) {
        List<WaterReminder> reminders = getReminders(context);
        for (int index = reminders.size() - 1; index >= 0; index -= 1) {
            if (reminders.get(index).id == reminderId) {
                reminders.remove(index);
            }
        }
        cancel(context, reminderId);
        saveReminders(context, reminders);
    }

    static void scheduleActiveReminders(Context context) {
        List<WaterReminder> reminders = getReminders(context);
        for (WaterReminder reminder : reminders) {
            if (reminder.enabled) {
                schedule(context, reminder);
            }
        }
    }

    static void schedule(Context context, WaterReminder reminder) {
        long intervalMillis = Math.max(1, reminder.intervalHours) * AlarmManager.INTERVAL_HOUR;
        long firstTrigger = SystemClock.elapsedRealtime() + intervalMillis;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTrigger,
                intervalMillis,
                reminderIntent(context, reminder)
        );
    }

    static void cancel(Context context, int reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, WaterReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_BASE + reminderId,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    static boolean hasEnabledReminders(Context context) {
        List<WaterReminder> reminders = getReminders(context);
        for (WaterReminder reminder : reminders) {
            if (reminder.enabled) {
                return true;
            }
        }
        return false;
    }

    private static PendingIntent reminderIntent(Context context, WaterReminder reminder) {
        Intent intent = new Intent(context, WaterReminderReceiver.class);
        intent.putExtra(EXTRA_REMINDER_ID, reminder.id);
        intent.putExtra(EXTRA_REMINDER_TITLE, reminder.title);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_BASE + reminder.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void saveReminders(Context context, List<WaterReminder> reminders) {
        JSONArray array = new JSONArray();
        for (WaterReminder reminder : reminders) {
            try {
                array.put(reminder.toJson());
            } catch (JSONException ignored) {
                // Ignore invalid reminder objects and keep saving the rest.
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_REMINDERS, array.toString())
                .apply();
    }

    private static int nextId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int id = prefs.getInt(KEY_NEXT_ID, 1);
        prefs.edit().putInt(KEY_NEXT_ID, id + 1).apply();
        return id;
    }

    private static String cleanTitle(String title) {
        String trimmed = title == null ? "" : title.trim();
        return trimmed.isEmpty() ? "Drink water" : trimmed;
    }
}
