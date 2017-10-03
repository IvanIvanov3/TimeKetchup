package com.bytebazar.timeketchup.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.text.format.DateUtils;

import com.bytebazar.timeketchup.IModel;

import java.util.ArrayList;
import java.util.List;

public class SettingsModel implements IModel {

    private static final String SESSION_DURATION = "session_duration";
    private static final String SHORT_BREAK = "short_break";
    private static final String LONG_BREAK = "long_break";
    private static final String SESSIONS = "sessions";
    private static final String VIBRATION = "vibration";
    private static final String NOTIF_SOUND_URI = "notif_uri";

    private final static String MY_PREF = "pref";
    private static final String NOTIF_SOUND_TITLE = "notif_title";
    private static final String DAILY_WORK_SESSION = "sessions_today";
    private static final String DAILY_WORK_SESSION_TIME = "sessions_time";


    final private SharedPreferences mPreferences;

    public SettingsModel(Context context) {
        mPreferences = context.getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
    }

    @Override
    public int getSessionDuration() {
        return mPreferences.getInt(SESSION_DURATION, 25);
    }

    @Override
    public int getShortBreakDuration() {
        return mPreferences.getInt(SHORT_BREAK, 5);
    }

    @Override
    public int getLongBreakDuration() {
        return mPreferences.getInt(LONG_BREAK, 15);
    }

    @Override
    public int getSessionsBeforeBreak() {
        return mPreferences.getInt(SESSIONS, 4);
    }

    @Override
    public boolean isVibrationEnabled() {
        return mPreferences.getBoolean(VIBRATION, false);
    }

    @Override
    public String getNotificationSoundUri() {
        return mPreferences.getString(NOTIF_SOUND_URI, "");
    }

    @Override
    public String getNotificationSoundTitle() {
        return mPreferences.getString(NOTIF_SOUND_TITLE, "");
    }

    @Override
    public void setSessionDuration(int duration) {
        mPreferences.edit().putInt(SESSION_DURATION, duration).apply();
    }

    @Override
    public void setShortBreakDuration(int duration) {
        mPreferences.edit().putInt(SHORT_BREAK, duration).apply();
    }

    @Override
    public void setLongBreakDuration(int duration) {
        mPreferences.edit().putInt(LONG_BREAK, duration).apply();
    }

    @Override
    public void setSessionsBeforeBreak(int sessionStreak) {
        mPreferences.edit().putInt(SESSIONS, sessionStreak).apply();
    }

    @Override
    public void setVibration(boolean vibration) {
        mPreferences.edit().putBoolean(VIBRATION, vibration).apply();
    }

    @Override
    public void setNotificationSoundUri(String sound) {
        mPreferences.edit().putString(NOTIF_SOUND_URI, sound).apply();
    }


    @Override
    public void setNotificationSoundTitle(String currentTitle) {
        mPreferences.edit().putString(NOTIF_SOUND_TITLE, currentTitle).apply();
    }

    @Override
    public int getDailyWorkSession() {
        if (DateUtils.isToday(mPreferences.getLong(DAILY_WORK_SESSION_TIME, 0))) {
            return mPreferences.getInt(DAILY_WORK_SESSION, 0);
        } else return 0;
    }

    @Override
    public void setDailyWorkSession(int sessions) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(DAILY_WORK_SESSION, sessions);
        editor.putLong(DAILY_WORK_SESSION_TIME, System.currentTimeMillis());
        editor.apply();
    }

    @Override
    public List<Ringtone> getRingtones(Context context) {
        final RingtoneManager mRingtoneMgr = new RingtoneManager(context);

        mRingtoneMgr.setType(RingtoneManager.TYPE_RINGTONE);
        final Cursor cursor = mRingtoneMgr.getCursor();
        final List<Ringtone> ringtones = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            final String title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            final String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/"
                    + cursor.getString(RingtoneManager.ID_COLUMN_INDEX);

            ringtones.add(new Ringtone(uri, title));
        }
        cursor.close();
        return ringtones;
    }

}
