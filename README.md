# Water Reminder Android App

A simple native Android app that reminds you to drink water at a custom interval.

## Features

- Start and stop water reminders.
- Change the reminder interval in minutes.
- Sends Android notifications.
- Reschedules after phone restart if reminders were active.
- Uses plain Java and Android system APIs. No external app libraries.

## How to Run

1. Open Android Studio.
2. Choose **Open**.
3. Select this folder:

   `C:\Users\vinod\Documents\Codex\2026-05-14\can-you-create-a-take-salary\water-reminder-android`

4. Let Android Studio sync Gradle.
5. Run the app on an emulator or Android phone.

On Android 13 and newer, allow notification permission when the app asks.

## Build APK Without Android Studio

You can build the APK online with GitHub Actions:

1. Create a GitHub repository.
2. Upload this whole `water-reminder-android` folder contents to the repository root.
3. Open the repository on GitHub.
4. Go to **Actions**.
5. Choose **Build Android APK**.
6. Click **Run workflow**.
7. After it finishes, open the completed run and download the artifact named `water-reminder-debug-apk`.

The APK inside will be:

`app-debug.apk`

For personal use, the debug APK is enough. For Play Store upload, you need a signed release APK or AAB.

## Default Reminder

The app defaults to `60` minutes. Change the number and tap **Start reminder**.
