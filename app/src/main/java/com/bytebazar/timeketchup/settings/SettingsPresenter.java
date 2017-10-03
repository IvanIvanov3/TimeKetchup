package com.bytebazar.timeketchup.settings;

import com.bytebazar.timeketchup.IModel;
import com.bytebazar.timeketchup.state.DialogType;

class SettingsPresenter {

    final private IModel mModel;
    final private SettingsView mView;
    private @DialogType
    int showingDialogType;

    SettingsPresenter(IModel model, SettingsView view) {
        mModel = model;
        mView = view;
    }


    void onDurationSettingClick(int dialogType) {
        showingDialogType = dialogType;
        String title = "";
        int minValue = 0;
        int maxValue = 0;
        int currentValue = 0;

        switch (dialogType) {
            case DialogType.SESSION_DURATION:
                title = "session duration";
                minValue = 1;
                maxValue = 60;
                currentValue = mModel.getSessionDuration();
                break;
            case DialogType.SHORT_BREAK_DURATION:
                title = "short break duration";
                minValue = 1;
                maxValue = 10;
                currentValue = mModel.getShortBreakDuration();
                break;
            case DialogType.LONG_BREAK_DURATION:
                title = "long break duration";
                minValue = 1;
                maxValue = 60;
                currentValue = mModel.getLongBreakDuration();
                break;
            case DialogType.SESSION_STREAK:
                title = "sessions before long break";
                minValue = 1;
                maxValue = 10;
                currentValue = mModel.getSessionsBeforeBreak();
                break;
        }
        mView.showDurationDialog(title, minValue, maxValue, currentValue);
    }

    void setCurrentValue(int value) {
        switch (showingDialogType) {
            case DialogType.SESSION_DURATION:
                mModel.setSessionDuration(value);
                mView.showSessionDuration(String.valueOf(value));
                break;
            case DialogType.SHORT_BREAK_DURATION:
                mModel.setShortBreakDuration(value);
                mView.showShortBreakDuration(String.valueOf(value));
                break;
            case DialogType.LONG_BREAK_DURATION:
                mModel.setLongBreakDuration(value);
                mView.showLongBreakDuration(String.valueOf(value));
                break;
            case DialogType.SESSION_STREAK:
                mModel.setSessionsBeforeBreak(value);
                mView.showSessionStreak(String.valueOf(value));
                break;
        }
    }


    void initValue() {
        mView.showSessionDuration(String.valueOf(mModel.getSessionDuration()));
        mView.showShortBreakDuration(String.valueOf(mModel.getShortBreakDuration()));
        mView.showLongBreakDuration(String.valueOf(mModel.getLongBreakDuration()));
        mView.showSessionStreak(String.valueOf(mModel.getSessionsBeforeBreak()));
        mView.showVibration(mModel.isVibrationEnabled());
        mView.showNotificationSoundTitle(mModel.getNotificationSoundTitle());
    }

    void vibrationClick(boolean isEnabled) {
        mModel.setVibration(isEnabled);
        mView.showVibration(isEnabled);
    }

    void notifSoundClick() {
        mView.showSoundPicker();
    }

}
