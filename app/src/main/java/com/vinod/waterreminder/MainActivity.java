package com.vinod.waterreminder;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private static final int INK = Color.rgb(17, 31, 43);
    private static final int MUTED = Color.rgb(92, 108, 119);
    private static final int BLUE = Color.rgb(22, 119, 168);
    private static final int TEAL = Color.rgb(19, 139, 128);
    private static final int PAPER = Color.rgb(244, 249, 252);
    private static final int CARD = Color.WHITE;

    private EditText titleInput;
    private EditText intervalInput;
    private TextView statusText;
    private LinearLayout remindersContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermission();
        setContentView(createContentView());
        renderReminders();
    }

    private View createContentView() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(PAPER);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scrollView.addView(root, fullWidth());

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(20), dp(20), dp(20), dp(20));
        hero.setBackground(roundedGradient(Color.rgb(232, 248, 255), Color.rgb(221, 247, 241), dp(22)));
        root.addView(hero, fullWidthWithBottom(dp(16)));

        TextView appLabel = label("HYDRATION 2026");
        appLabel.setTextColor(TEAL);
        hero.addView(appLabel, fullWidth());

        TextView title = new TextView(this);
        title.setText("Drink water, on time.");
        title.setTextColor(INK);
        title.setTextSize(31);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setLetterSpacing(0);
        hero.addView(title, fullWidthWithTop(dp(6)));

        TextView subtitle = new TextView(this);
        subtitle.setText("Create multiple hourly reminders with notification sound. Uses battery-friendly Android alarms.");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(15);
        subtitle.setLineSpacing(2, 1.08f);
        hero.addView(subtitle, fullWidthWithTop(dp(10)));

        statusText = new TextView(this);
        statusText.setTextColor(BLUE);
        statusText.setTextSize(16);
        statusText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        hero.addView(statusText, fullWidthWithTop(dp(18)));

        LinearLayout addCard = card();
        root.addView(addCard, fullWidthWithBottom(dp(16)));

        TextView addTitle = sectionTitle("Add Reminder");
        addCard.addView(addTitle, fullWidth());

        TextView titleLabel = fieldLabel("Reminder name");
        addCard.addView(titleLabel, fullWidthWithTop(dp(14)));

        titleInput = input("Drink water");
        addCard.addView(titleInput, fullWidthWithTop(dp(8)));

        TextView intervalLabel = fieldLabel("Repeat every");
        addCard.addView(intervalLabel, fullWidthWithTop(dp(14)));

        intervalInput = input("1");
        intervalInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        addCard.addView(intervalInput, fullWidthWithTop(dp(8)));

        TextView hoursHint = new TextView(this);
        hoursHint.setText("hours. Use 1 for every hour.");
        hoursHint.setTextColor(MUTED);
        hoursHint.setTextSize(13);
        addCard.addView(hoursHint, fullWidthWithTop(dp(6)));

        Button addButton = primaryButton("Add hourly reminder");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReminder();
            }
        });
        addCard.addView(addButton, buttonParams(dp(16)));

        LinearLayout listCard = card();
        root.addView(listCard, fullWidth());

        TextView remindersTitle = sectionTitle("My Reminders");
        listCard.addView(remindersTitle, fullWidth());

        remindersContainer = new LinearLayout(this);
        remindersContainer.setOrientation(LinearLayout.VERTICAL);
        listCard.addView(remindersContainer, fullWidthWithTop(dp(8)));

        TextView batteryNote = new TextView(this);
        batteryNote.setText("Battery note: reminders use inexact repeating alarms, so Android can batch them efficiently. Battery saver may delay alerts slightly.");
        batteryNote.setTextColor(MUTED);
        batteryNote.setTextSize(13);
        batteryNote.setGravity(Gravity.CENTER);
        batteryNote.setLineSpacing(2, 1.08f);
        root.addView(batteryNote, fullWidthWithTop(dp(16)));

        return scrollView;
    }

    private void addReminder() {
        String reminderTitle = titleInput.getText().toString().trim();
        int intervalHours = parseHours(intervalInput.getText().toString());
        if (intervalHours < 1) {
            Toast.makeText(this, "Enter at least 1 hour.", Toast.LENGTH_SHORT).show();
            return;
        }

        ReminderScheduler.addReminder(this, reminderTitle, intervalHours);
        titleInput.setText("Drink water");
        intervalInput.setText("1");
        renderReminders();
        Toast.makeText(this, "Reminder added with sound.", Toast.LENGTH_SHORT).show();
    }

    private void renderReminders() {
        List<WaterReminder> reminders = ReminderScheduler.getReminders(this);
        remindersContainer.removeAllViews();

        int activeCount = 0;
        for (WaterReminder reminder : reminders) {
            if (reminder.enabled) {
                activeCount += 1;
            }
            remindersContainer.addView(reminderCard(reminder), fullWidthWithTop(dp(10)));
        }

        if (activeCount == 0) {
            statusText.setText("No active reminders");
        } else if (activeCount == 1) {
            statusText.setText("1 active reminder");
        } else {
            statusText.setText(activeCount + " active reminders");
        }
    }

    private View reminderCard(final WaterReminder reminder) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(roundedStroke(Color.rgb(250, 253, 255), reminder.enabled ? TEAL : Color.rgb(218, 228, 235), dp(18)));

        TextView name = new TextView(this);
        name.setText(reminder.title);
        name.setTextColor(INK);
        name.setTextSize(18);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(name, fullWidth());

        TextView detail = new TextView(this);
        detail.setText((reminder.enabled ? "Active" : "Paused") + " • every " + reminder.intervalHours + " hour" + (reminder.intervalHours == 1 ? "" : "s") + " • sound on");
        detail.setTextColor(MUTED);
        detail.setTextSize(14);
        card.addView(detail, fullWidthWithTop(dp(6)));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(actions, fullWidthWithTop(dp(12)));

        Button toggle = reminder.enabled ? secondaryButton("Pause") : primaryButton("Start");
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReminderScheduler.updateReminder(MainActivity.this, reminder.withEnabled(!reminder.enabled));
                renderReminders();
            }
        });
        actions.addView(toggle, actionButtonParams());

        Button delete = dangerButton("Delete");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReminderScheduler.deleteReminder(MainActivity.this, reminder.id);
                renderReminders();
            }
        });
        LinearLayout.LayoutParams deleteParams = actionButtonParams();
        deleteParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(delete, deleteParams);

        return card;
    }

    private int parseHours(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(rounded(CARD, dp(20)));
        return card;
    }

    private TextView sectionTitle(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextColor(INK);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return title;
    }

    private TextView fieldLabel(String text) {
        TextView label = label(text.toUpperCase());
        label.setTextColor(MUTED);
        return label;
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(12);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setLetterSpacing(0.08f);
        return label;
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setTextSize(16);
        input.setTextColor(INK);
        input.setHintTextColor(Color.rgb(142, 154, 164));
        input.setPadding(dp(14), 0, dp(14), 0);
        input.setBackground(roundedStroke(Color.WHITE, Color.rgb(208, 222, 232), dp(14)));
        return input;
    }

    private Button primaryButton(String text) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(15);
        button.setTextColor(Color.WHITE);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(rounded(BLUE, dp(14)));
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(15);
        button.setTextColor(BLUE);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(rounded(Color.rgb(235, 247, 252), dp(14)));
        return button;
    }

    private Button dangerButton(String text) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(text);
        button.setTextSize(15);
        button.setTextColor(Color.rgb(190, 68, 68));
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(rounded(Color.rgb(255, 240, 240), dp(14)));
        return button;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private GradientDrawable roundedGradient(int start, int end, int radius) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{start, end}
        );
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private GradientDrawable roundedStroke(int color, int stroke, int radius) {
        GradientDrawable drawable = rounded(color, radius);
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams fullWidthWithTop(int top) {
        LinearLayout.LayoutParams params = fullWidth();
        params.setMargins(0, top, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams fullWidthWithBottom(int bottom) {
        LinearLayout.LayoutParams params = fullWidth();
        params.setMargins(0, 0, 0, bottom);
        return params;
    }

    private LinearLayout.LayoutParams buttonParams(int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        params.setMargins(0, top, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams actionButtonParams() {
        return new LinearLayout.LayoutParams(0, dp(44), 1);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
