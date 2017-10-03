package com.bytebazar.timeketchup;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.bytebazar.timeketchup.common.Constants;
import com.bytebazar.timeketchup.settings.SettingsModel;
import com.bytebazar.timeketchup.state.TimerState;

import java.util.concurrent.TimeUnit;

import static android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP;
import static android.media.AudioManager.STREAM_ALARM;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.bytebazar.timeketchup.common.Constants.ACTION_BREAK_LONG_FINISHED;
import static com.bytebazar.timeketchup.common.Constants.ACTION_BREAK_SHORT_FINISHED;
import static com.bytebazar.timeketchup.common.Constants.ACTION_FINISHED;
import static com.bytebazar.timeketchup.common.Constants.ACTION_SKIP_BREAK;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_BREAK;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_LONG_BREAK_UI;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_SHORT_BREAK_UI;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_WORK;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_WORK_UI;
import static com.bytebazar.timeketchup.common.Constants.ACTION_STOP;
import static com.bytebazar.timeketchup.common.Constants.ACTION_WORK_FINISHED;
import static com.bytebazar.timeketchup.common.Constants.NOTIFICATION_ID;
import static com.bytebazar.timeketchup.common.Constants.PENDING_INTENT_REQUEST;
import static com.bytebazar.timeketchup.state.TimerState.BREAK_LONG;
import static com.bytebazar.timeketchup.state.TimerState.BREAK_SHORT;
import static com.bytebazar.timeketchup.state.TimerState.INACTIVE;
import static com.bytebazar.timeketchup.state.TimerState.WORK;

public class TimerService extends Service {

    private NotificationUtils notificationUtils;
    private AlarmManager alarmManager;
    private LocalBroadcastManager broadcastManager;
    private NotificationManager notificationManager;
    final private Binder binder = new TimerBinder();
    @TimerState
    private int timerState;

    private long finishedTime;
    private int sessionsBeforeBreak;
    private int dailyWorkSessions;

    private boolean isServiceForeground;
    private IModel settings;

    class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    public int getRemainingTime() {
        if (checkStatus()) {
            return (int) TimeUnit.MILLISECONDS.toSeconds(finishedTime - SystemClock.elapsedRealtime());
        }
        return 0;

    }

    public int getDailyWorkSessions() {
        return dailyWorkSessions;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        settings = new SettingsModel(this);
        timerState = INACTIVE;
        dailyWorkSessions = settings.getDailyWorkSession();
        notificationUtils = new NotificationUtils();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (action != null) {
            switch (intent.getAction()) {
                case ACTION_START_BREAK:
                    startSession(getSessionsBeforeBreak() >= settings.getSessionsBeforeBreak()
                            ? BREAK_LONG
                            : BREAK_SHORT
                    );
                    createForegroundNotificationAndNotify();
                    break;
                case ACTION_FINISHED:
                    sendFinishedNotification();
                    stopSession();
                    onCountdownFinished();
                    setTimerStateInactive();
                    break;
                case ACTION_SKIP_BREAK:
                case ACTION_START_WORK:
                    startSession(WORK);
                    createForegroundNotificationAndNotify();
                    break;
                case ACTION_STOP:
                    cancelAlarm();
                    stopSession();
                    sendToBackground();
                    stopTimerService();
                    break;
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void onCountdownFinished() {
        if (timerState == WORK) {
            Intent intent = createWorkFinishedIntent();
            broadcastManager.sendBroadcast(intent);
        } else if (timerState == BREAK_SHORT) {
            broadcastManager.sendBroadcast(new Intent(ACTION_BREAK_SHORT_FINISHED));
        } else broadcastManager.sendBroadcast(new Intent(ACTION_BREAK_LONG_FINISHED));
    }

    @NonNull
    private Intent createWorkFinishedIntent() {
        Intent intent = new Intent(ACTION_WORK_FINISHED);
        intent.putExtra(Constants.EXTRA_DAILY_WORK_SESSIONS, dailyWorkSessions);
        return intent;
    }

    public boolean checkStatus(boolean sendBroadcast) {
        final boolean isActive = (timerState != INACTIVE);
        if (isActive && sendBroadcast) {
            sendStartBroadcast(timerState);
        }
        return isActive;
    }

    private boolean checkStatus() {
        return checkStatus(false);
    }

    void bringToForeground() {
        startForeground(NOTIFICATION_ID, notificationUtils.createForegroundNotification(this
                , timerState, getRemainingTime()));
        isServiceForeground = true;
    }

    public void sendToBackground() {
        stopForeground(true);
        isServiceForeground = false;
    }

    private void setAlarm(long countDownTime) {
        alarmManager = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
        if (alarmManager == null) return;

        Intent serviceIntent = createFinishedIntent();
        PendingIntent intent = createServicePendingIntent(this, serviceIntent);

        if (SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(ELAPSED_REALTIME_WAKEUP, countDownTime, intent);
        } else if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(ELAPSED_REALTIME_WAKEUP, countDownTime, intent);
        } else {
            alarmManager.set(ELAPSED_REALTIME_WAKEUP, countDownTime, intent);
        }
    }

    public void cancelAlarm() {
        Intent serviceIntent = createFinishedIntent();
        if (alarmManager != null) {
            alarmManager.cancel(createServicePendingIntent(this, serviceIntent));
        }
    }

    private Intent createFinishedIntent() {
        return new Intent(this, TimerService.class)
                .setAction(ACTION_FINISHED);
    }

    private PendingIntent createServicePendingIntent(Context context, Intent intent) {
        return PendingIntent.getService(context, PENDING_INTENT_REQUEST, intent, 0);
    }

    public void startSession(int timerState) {
        finishedTime = calculateEndTimeFor(timerState);
        sendStartBroadcast(timerState);
        this.timerState = timerState;
        setAlarm(finishedTime);
    }

    public void stopSession() {
        if (timerState == BREAK_LONG) {
            resetSessionsBeforeBreak();
        } else if (timerState == WORK) {
            sessionsBeforeBreak++;
            dailyWorkSessions++;
            settings.setDailyWorkSession(dailyWorkSessions);
        }
    }


    //-----------------------------------


    private void sendStartBroadcast(int sessionType) {
        switch (sessionType) {
            case WORK:
                broadcastManager.sendBroadcast(new Intent(ACTION_START_WORK_UI));
                break;
            case BREAK_SHORT:
                broadcastManager.sendBroadcast(new Intent(ACTION_START_SHORT_BREAK_UI));
                break;
            case BREAK_LONG:
                broadcastManager.sendBroadcast(new Intent(ACTION_START_LONG_BREAK_UI));
                break;
        }
    }

    private long calculateEndTimeFor(int sessionType) {
        long currentTime = SystemClock.elapsedRealtime();
        switch (sessionType) {
            case WORK:
                currentTime = currentTime + TimeUnit.MINUTES.toMillis(settings.getSessionDuration());
                break;
            case BREAK_SHORT:
                currentTime = currentTime + TimeUnit.MINUTES.toMillis(settings.getShortBreakDuration());
                break;
            case BREAK_LONG:
                currentTime = currentTime + TimeUnit.MINUTES.toMillis(settings.getLongBreakDuration());
                break;
        }
        return currentTime;
    }


    private void sendFinishedNotification() {
        Notification notification = notificationUtils.createCompletionNotification(this, timerState);

        if (settings.isVibrationEnabled()) {
            notification.vibrate = new long[]{0, 300, 700, 300};
        }

        if (SDK_INT >= LOLLIPOP) {
            notification.audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
        } else {
            notification.audioStreamType = STREAM_ALARM;
        }

        notification.sound = Uri.parse(settings.getNotificationSoundUri());
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void stopTimerService() {
        stopSelf();
    }

    private void createForegroundNotificationAndNotify() {
        notificationManager.notify(NOTIFICATION_ID, notificationUtils.createForegroundNotification(
                this, timerState, getRemainingTime()));
    }

    private void setTimerStateInactive() {
        timerState = INACTIVE;
    }


    public int getTimerState() {
        return timerState;
    }

    public boolean isServiceForeground() {
        return isServiceForeground;
    }

    public void resetSessionsBeforeBreak() {
        sessionsBeforeBreak = 0;
    }

    public int getSessionsBeforeBreak() {
        return sessionsBeforeBreak;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        sendToBackground();
        cancelAlarm();
    }
}
