package com.bytebazar.timeketchup.settings;

interface SettingsView {

    void showDurationDialog(String title, final int minValue, int maxValue, final int currentValue);

    void showSessionDuration(String duration);

    void showShortBreakDuration(String duration);

    void showLongBreakDuration(String duration);

    void showSessionStreak(String sessions);

    void showVibration(boolean isEnable);

    void showSoundPicker();

    void showNotificationSoundTitle(String title);
}
