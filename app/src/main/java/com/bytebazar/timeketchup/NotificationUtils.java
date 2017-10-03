package com.bytebazar.timeketchup;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;

import com.bytebazar.timeketchup.state.TimerState;

import java.util.Calendar;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.graphics.Color.WHITE;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;
import static com.bytebazar.timeketchup.common.Constants.ACTION_SKIP_BREAK;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_BREAK;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_WORK;
import static com.bytebazar.timeketchup.common.Constants.ACTION_STOP;
import static com.bytebazar.timeketchup.common.Constants.PENDING_INTENT_REQUEST;
import static com.bytebazar.timeketchup.state.TimerState.BREAK_LONG;
import static com.bytebazar.timeketchup.state.TimerState.BREAK_SHORT;
import static com.bytebazar.timeketchup.state.TimerState.WORK;

class NotificationUtils {

    public Notification createCompletionNotification(Context context, int timerState) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_timer_done)
                .setPriority(PRIORITY_HIGH)
                .setVisibility(VISIBILITY_PUBLIC)
                .setLights(WHITE, 250, 750)
                .setContentTitle(context.getString(R.string.dialog_session_complete))
                .setContentText(buildCompletedNotificationText(context, timerState))
                .setContentIntent(getActivity(
                        context,
                        PENDING_INTENT_REQUEST,
                        new Intent(context.getApplicationContext(), MainActivity.class),
                        FLAG_ONE_SHOT
                ))
                .setAutoCancel(true);

        if (timerState == WORK) {
            builder.addAction(createStartBreakAction(context))
                    .addAction(createSkipBreakAction(context));
        } else {
            builder.addAction(createStartWorkAction(context));
        }
        return builder.build();
    }

    private CharSequence buildCompletedNotificationText(Context context, int timerState) {
        switch (timerState) {
            case BREAK_SHORT:
            case BREAK_LONG:
                return context.getString(R.string.notification_break_complete);
            case WORK:
            default:
                return context.getString(R.string.notification_work_complete);
        }
    }

    public Notification createForegroundNotification(
            Context context,
            @TimerState int timerState,
            int remainingTime
    ) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_timer)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(buildForegroundNotificationText(context, timerState, remainingTime))
//                .setOngoing(isTimerActive(timerState))
                .setShowWhen(false)
                .setContentIntent(
                        getActivity(
                                context,
                                PENDING_INTENT_REQUEST,
                                new Intent(context.getApplicationContext(), MainActivity.class),
                                FLAG_UPDATE_CURRENT
                        ));

        builder.addAction(createStopAction(context));
        if (timerState == WORK) {
            builder.addAction(
                    isTimerActive(timerState) ? createBreakAction(context)
                            : createStopAction(context));
        } else if (timerState == BREAK_LONG || timerState == BREAK_SHORT) {
            builder.addAction(createStartWorkAction(context));
        }

        return builder.build();
    }

    private NotificationCompat.Action createBreakAction(Context context) {
        Intent startBreakIntent = new Intent(context, TimerService.class)
                .setAction(ACTION_START_BREAK);
        PendingIntent startBreakPendingIntent = createPendingIntentFromIntent(context, startBreakIntent);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                context.getString(R.string.dialog_start_break),
                startBreakPendingIntent).build();
    }

    private CharSequence buildForegroundNotificationText(
            Context context,
            int sessionType,
            int remainingTime) {
        switch (sessionType) {
            case BREAK_SHORT:
            case BREAK_LONG:
                return context.getString(R.string.notification_break)
                        + buildNotificationCountdownTime(context, remainingTime);
            case WORK:
            default:
                return context.getString(R.string.notification_session)
                        + buildNotificationCountdownTime(context, remainingTime);
        }
    }

    private CharSequence buildNotificationCountdownTime(Context context, int remainingTime) {

        boolean is24HourFormat = DateFormat.is24HourFormat(context);

        String inFormat = is24HourFormat ? " k:mm" : " h:mm aa";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, remainingTime);

        return DateFormat.format(inFormat, calendar) + ".";
    }

    private boolean isTimerActive(@TimerState int timerState) {
        return timerState != TimerState.INACTIVE;
    }

    private NotificationCompat.Action createStartBreakAction(Context context) {
        Intent startBreakIntent = new Intent(context, TimerService.class)
                .setAction(ACTION_START_BREAK);
        PendingIntent startBreakPendingIntent = createPendingIntentFromIntent(context, startBreakIntent);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                context.getString(R.string.dialog_start_break),
                startBreakPendingIntent).build();
    }

    private NotificationCompat.Action createSkipBreakAction(Context context) {
        Intent skipBreakIntent = new Intent(context, TimerService.class)
                .setAction(ACTION_SKIP_BREAK);
        PendingIntent skipBreakPendingIntent = createPendingIntentFromIntent(context, skipBreakIntent);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_skip,
                context.getString(R.string.dialog_skip_break),
                skipBreakPendingIntent).build();
    }

    private NotificationCompat.Action createStartWorkAction(Context context) {
        Intent startWorkIntent = new Intent(context, TimerService.class)
                .setAction(ACTION_START_WORK);
        PendingIntent startWorkPendingIntent = createPendingIntentFromIntent(context, startWorkIntent);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                context.getString(R.string.dialog_begin_session),
                startWorkPendingIntent).build();
    }

    private NotificationCompat.Action createStopAction(Context context) {
        Intent stopIntent = new Intent(context, TimerService.class)
                .setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = createPendingIntentFromIntent(context, stopIntent);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_stop, context.getString(R.string.stop),
                stopPendingIntent).build();
    }

//    private static int getIconDependsApi() {
//        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
//        return useWhiteIcon ? R.drawable.icon_silhouette : R.drawable.ic_launcher;;
//    }

    private PendingIntent createPendingIntentFromIntent(Context context, Intent intent) {
        return PendingIntent.getService(context, PENDING_INTENT_REQUEST, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
