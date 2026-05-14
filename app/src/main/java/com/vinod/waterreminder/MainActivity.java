package com.vinod.waterreminder;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText intervalInput;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermission();
        setContentView(createContentView());
        refreshStatus();
    }

    private View createContentView() {
        int padding = dp(22);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(padding, padding, padding, padding);
        root.setBackgroundColor(Color.rgb(245, 249, 252));

        TextView title = new TextView(this);
        title.setText("Water Reminder");
        title.setTextSize(30);
        title.setTextColor(Color.rgb(20, 34, 45));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        root.addView(title, fullWidth());

        TextView subtitle = new TextView(this);
        subtitle.setText("Get a gentle reminder to drink water at your chosen interval.");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.rgb(83, 101, 112));
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = fullWidth();
        subtitleParams.setMargins(0, dp(10), 0, dp(28));
        root.addView(subtitle, subtitleParams);

        statusText = new TextView(this);
        statusText.setTextSize(18);
        statusText.setGravity(Gravity.CENTER);
        statusText.setTextColor(Color.rgb(22, 119, 168));
        LinearLayout.LayoutParams statusParams = fullWidth();
        statusParams.setMargins(0, 0, 0, dp(24));
        root.addView(statusText, statusParams);

        TextView label = new TextView(this);
        label.setText("Remind me every");
        label.setTextSize(15);
        label.setTextColor(Color.rgb(83, 101, 112));
        root.addView(label, fullWidth());

        intervalInput = new EditText(this);
        intervalInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        intervalInput.setText(String.valueOf(ReminderScheduler.getIntervalMinutes(this)));
        intervalInput.setHint("60");
        intervalInput.setTextSize(20);
        intervalInput.setSingleLine(true);
        intervalInput.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams inputParams = fullWidth();
        inputParams.setMargins(0, dp(8), 0, dp(6));
        root.addView(intervalInput, inputParams);

        TextView minutes = new TextView(this);
        minutes.setText("minutes");
        minutes.setTextSize(14);
        minutes.setGravity(Gravity.CENTER);
        minutes.setTextColor(Color.rgb(83, 101, 112));
        LinearLayout.LayoutParams minutesParams = fullWidth();
        minutesParams.setMargins(0, 0, 0, dp(26));
        root.addView(minutes, minutesParams);

        Button startButton = primaryButton("Start reminder");
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startReminder();
            }
        });
        root.addView(startButton, buttonParams());

        Button stopButton = secondaryButton("Stop reminder");
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopReminder();
            }
        });
        root.addView(stopButton, buttonParams());

        TextView note = new TextView(this);
        note.setText("Tip: keep notifications enabled for this app. Some phones may delay alarms while battery saver is active.");
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        note.setTextColor(Color.rgb(102, 116, 126));
        LinearLayout.LayoutParams noteParams = fullWidth();
        noteParams.setMargins(0, dp(20), 0, 0);
        root.addView(note, noteParams);

        return root;
    }

    private void startReminder() {
        int interval = parseInterval();
        if (interval < 1) {
            Toast.makeText(this, "Enter at least 1 minute.", Toast.LENGTH_SHORT).show();
            return;
        }

        ReminderScheduler.saveSettings(this, true, interval);
        ReminderScheduler.cancel(this);
        ReminderScheduler.scheduleNext(this);
        refreshStatus();
        Toast.makeText(this, "Reminder started.", Toast.LENGTH_SHORT).show();
    }

    private void stopReminder() {
        ReminderScheduler.saveSettings(this, false, parseInterval());
        ReminderScheduler.cancel(this);
        refreshStatus();
        Toast.makeText(this, "Reminder stopped.", Toast.LENGTH_SHORT).show();
    }

    private int parseInterval() {
        try {
            return Integer.parseInt(intervalInput.getText().toString().trim());
        } catch (NumberFormatException ignored) {
            return ReminderScheduler.DEFAULT_INTERVAL_MINUTES;
        }
    }

    private void refreshStatus() {
        int interval = ReminderScheduler.getIntervalMinutes(this);
        if (ReminderScheduler.isEnabled(this)) {
            statusText.setText("Active: every " + interval + " minutes");
        } else {
            statusText.setText("Reminder is off");
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }
    }

    private Button primaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(22, 119, 168));
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setTextColor(Color.rgb(22, 119, 168));
        button.setBackgroundColor(Color.WHITE);
        return button;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        params.setMargins(0, dp(8), 0, 0);
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
