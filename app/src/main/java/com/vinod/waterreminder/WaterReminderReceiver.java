package com.vinod.waterreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

public class WaterReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "water_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, 0);
        String title = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_TITLE);
        if (reminderId == 0) {
            return;
        }

        showNotification(context, reminderId, title == null ? "Drink water" : title);
    }

    private void showNotification(Context context, int reminderId, String title) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri sound = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Water reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Hourly and custom reminders to drink water");
            channel.setSound(sound, audioAttributes);
            manager.createNotificationChannel(channel);
        }

        Intent openApp = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        android.app.Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new android.app.Notification.Builder(context, CHANNEL_ID)
                : new android.app.Notification.Builder(context);

        builder
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText("Time for a glass of water.")
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setDefaults(android.app.Notification.DEFAULT_SOUND);

        manager.notify(2000 + reminderId, builder.build());
    }
}
