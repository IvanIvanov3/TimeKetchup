package com.bytebazar.timeketchup;

import android.content.Context;

import com.bytebazar.timeketchup.settings.Ringtone;

import java.util.List;

public interface IModel {

    int getSessionDuration();

    int getShortBreakDuration();

    int getLongBreakDuration();

    int getSessionsBeforeBreak();

    boolean isVibrationEnabled();

    String getNotificationSoundUri();

    String getNotificationSoundTitle();

    void setSessionDuration(int duration);

    void setShortBreakDuration(int duration);

    void setLongBreakDuration(int duration);

    void setSessionsBeforeBreak(int sessionStreak);

    void setVibration(boolean vibration);

    void setNotificationSoundUri(String sound);

    void setNotificationSoundTitle(String currentTitle);

    int getDailyWorkSession();

    void setDailyWorkSession(int sessions);

    List<Ringtone> getRingtones(Context context);

}
