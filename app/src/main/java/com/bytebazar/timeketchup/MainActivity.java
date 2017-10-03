package com.bytebazar.timeketchup;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.bytebazar.timeketchup.common.Constants;
import com.bytebazar.timeketchup.settings.SettingsFragment;
import com.bytebazar.timeketchup.settings.SettingsModel;

import java.util.concurrent.TimeUnit;

import static com.bytebazar.timeketchup.common.Constants.ACTION_BREAK_LONG_FINISHED;
import static com.bytebazar.timeketchup.common.Constants.ACTION_BREAK_SHORT_FINISHED;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_LONG_BREAK_UI;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_SHORT_BREAK_UI;
import static com.bytebazar.timeketchup.common.Constants.ACTION_START_WORK_UI;
import static com.bytebazar.timeketchup.common.Constants.ACTION_WORK_FINISHED;
import static com.bytebazar.timeketchup.common.Constants.NOTIFICATION_ID;
import static com.bytebazar.timeketchup.state.TimerState.BREAK_LONG;
import static com.bytebazar.timeketchup.state.TimerState.BREAK_SHORT;
import static com.bytebazar.timeketchup.state.TimerState.INACTIVE;
import static com.bytebazar.timeketchup.state.TimerState.WORK;

public class MainActivity extends AppCompatActivity implements ActivityConnection {

    private boolean isServiceBound;
    private TimerService timerService;

    private AlertDialog alertDialog;
    private IModel settings;

    private TextView toolbarTitle;

    final private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;

            switch (intent.getAction()) {
                case ACTION_START_WORK_UI:
                    startTimerUI(WORK);
                    break;
                case ACTION_START_SHORT_BREAK_UI:
                    startTimerUI(BREAK_SHORT);
                    break;
                case ACTION_START_LONG_BREAK_UI:
                    startTimerUI(BREAK_LONG);
                    break;
                case ACTION_WORK_FINISHED:
                    updateSessionsCounter(intent.getIntExtra(Constants.EXTRA_DAILY_WORK_SESSIONS, 0));
                    alertDialog = buildStartBreakDialog();
                    showDialog(alertDialog);
                    break;
                case ACTION_BREAK_SHORT_FINISHED:
                case ACTION_BREAK_LONG_FINISHED:
                    if (timerService.getSessionsBeforeBreak() >= settings.getSessionsBeforeBreak()) {
                        timerService.resetSessionsBeforeBreak();
                    }
                    alertDialog = buildStartSessionDialog();
                    showDialog(alertDialog);
            }
        }
    };

    private void showDialog(AlertDialog alertDialog) {
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void startTimerUI(int timerState) {
        String state = "";
        if (timerState == WORK) {
            state = "work";
        } else if (timerState == BREAK_SHORT) {
            state = "short break";
        } else if (timerState == BREAK_LONG) {
            state = "long break";

        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TimerFragment.TAG);
        if (fragment != null) {
            ((TimerFragment) fragment).startTimer(state);
        }
    }

    final private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            isServiceBound = true;
            timerService.sendToBackground();
            timerService.checkStatus(true);
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
//            Intent intent = new Intent();
//            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
//            startActivity(intent);
//        }


        settings = new SettingsModel(this);
        initToolbar();
        addTimerFragment();
        createAndBindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, createIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private IntentFilter createIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_WORK_FINISHED);
        intentFilter.addAction(ACTION_BREAK_LONG_FINISHED);
        intentFilter.addAction(ACTION_BREAK_SHORT_FINISHED);
        intentFilter.addAction(ACTION_START_WORK_UI);
        intentFilter.addAction(ACTION_START_SHORT_BREAK_UI);
        intentFilter.addAction(ACTION_START_LONG_BREAK_UI);
        return intentFilter;
    }

    private void createAndBindService() {
        final Intent intent = new Intent(this, TimerService.class);
        startService(intent);
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void addTimerFragment() {
        final FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = TimerFragment.newInstance();
            manager.beginTransaction()
                    .add(R.id.fragment_container, fragment, TimerFragment.TAG)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    @Override
    public void setTitle(int titleId) {
        toolbarTitle.setText(titleId);
    }

    @Override
    public void onBackPressed() {
        final int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCount == 1) {
            showExitDialog();
        } else super.onBackPressed();
    }

    private void showExitDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.stop) + "?")
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    unbindAndStopService();
                    alertDialog.dismiss();
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> alertDialog.dismiss())
                .create();
        alertDialog.show();
    }

    @Override
    public void openSettings() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment.newInstance(), SettingsFragment.TAG)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public boolean checkStatus() {
        return isServiceBound && timerService.checkStatus(true);
    }

    @Override
    public int getRemainingTime() {
        if (isServiceBound && timerService.getTimerState() != INACTIVE) {
            return timerService.getRemainingTime();
        } else return (int) TimeUnit.MINUTES.toSeconds(settings.getSessionDuration());
    }

    @Override
    public int getDurationTime() {
        int duration = settings.getSessionDuration();
        if (isServiceBound) {
            final int timerState = timerService.getTimerState();
            if (timerState == BREAK_SHORT) {
                duration = settings.getShortBreakDuration();
            } else if (timerState == BREAK_LONG) {
                duration = settings.getLongBreakDuration();
            }
        }
        return duration;
    }

    @Override
    public int getTodaySessions() {
        return settings.getDailyWorkSession();
    }

    private void unbindAndStopService() {
        if (timerService.isServiceForeground()) {
            timerService.sendToBackground();
        }
        timerService.cancelAlarm();
        timerService.stopSession();

        final Intent intent = new Intent(this, TimerService.class);
        unbindService(serviceConnection);
        isServiceBound = false;
        stopService(intent);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceBound && timerService.getTimerState() != INACTIVE) {
            timerService.bringToForeground();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isServiceBound && timerService.isServiceForeground()) {
            timerService.sendToBackground();
        }
    }

    private AlertDialog buildStartSessionDialog() {
        return new AlertDialog.Builder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.dialog_break_complete))
                .setPositiveButton(getString(R.string.dialog_begin_session), (dialog, which) -> {
                    removeCompletionNotification();
                    startTimer(WORK);
                })
                .setNegativeButton(getString(R.string.dialog_session_close)
                        , (dialog, which) -> {
                            removeCompletionNotification();
                            initValue();
                        })
                .setOnCancelListener(dialogInterface -> {
                    removeCompletionNotification();
                    initValue();
                })
                .create();

    }

    private AlertDialog buildStartBreakDialog() {
        return new AlertDialog.Builder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.dialog_session_complete))
                .setPositiveButton(
                        getString(R.string.dialog_start_break),
                        (dialog, which) -> {
                            removeCompletionNotification();
                            startBreak();
                        })
                .setNegativeButton(
                        getString(R.string.dialog_skip_break),
                        (dialog, which) -> {
                            removeCompletionNotification();
                            startTimer(WORK);
                        })
                .setNeutralButton(getString(R.string.dialog_session_close), (dialog, which) -> {
                    removeCompletionNotification();
                    initValue();
                })
                .setOnCancelListener(dialogInterface -> {
                    removeCompletionNotification();
                    initValue();
                })
                .create();
    }

    private void initValue() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TimerFragment.TAG);
        if (fragment != null) {
            ((TimerFragment) fragment).startTimer("");
        }
    }

    private void showSkipWorkDialog() {
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.dialog_start_break) + "?")
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    stopSession();
                    startBreak();
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> {
                })
                .create();
        alertDialog.show();
    }

    private void showSkipBreakDialog() {
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialog)
                .setTitle(getString(R.string.dialog_skip_break) + "?")
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    stopSession();
                    startTimer(WORK);
                })
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> {

                })
                .create();
        alertDialog.show();
    }

    private void stopSession() {
        timerService.cancelAlarm();
        timerService.stopSession();
        if (timerService.getTimerState() == WORK) {
            updateSessionsCounter(timerService.getDailyWorkSessions());
        }
    }

    private void removeCompletionNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void startTimer(int sessionType) {
        timerService.startSession(sessionType);
    }

    private void startBreak() {
        startTimer(timerService.getSessionsBeforeBreak() >= settings.getSessionsBeforeBreak()
                ? BREAK_LONG
                : BREAK_SHORT
        );
    }

    @Override
    public void onTimerLongClick() {
        switch (timerService.getTimerState()) {
            case INACTIVE:
                break;
            case WORK:
                showSkipWorkDialog();
                break;
            default:
                showSkipBreakDialog();
        }
    }

    @Override
    public boolean onTimerClick() {
        boolean isStartTimer = false;
        switch (timerService.getTimerState()) {
            case INACTIVE:
                startTimer(WORK);
                isStartTimer = true;
                break;
        }
        return isStartTimer;
    }

    private void updateSessionsCounter(int sessions) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TimerFragment.TAG);
        if (fragment != null) {
            ((TimerFragment) fragment).updateSessionsCounter(sessions);
        }
    }


}
