package com.bytebazar.timeketchup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Trace;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import java.util.Locale;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static com.bytebazar.timeketchup.common.Constants.DELAY;
import static java.lang.String.format;

public class TimerFragment extends Fragment {
    public static final String TAG = "TimerFragment";
    private TextView sessionCounter;
    private TextView sessionType;
    private TimerView timerView;
    private ActivityConnection activityConnection;
    final private Handler updateHandler = new Handler();
    private int durationTime;

    private boolean shouldContinue;
    final private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!shouldContinue) {
                return;
            }
            updateTimerView();
            updateHandler.postDelayed(this, DELAY);
        }
    };

    private void updateTimerView() {
        final int remainingTime = activityConnection.getRemainingTime();
        if (remainingTime == 0) {
            stopTimer();
        }

        final int minutes = remainingTime / 60;
        final int seconds = remainingTime % 60;

        final String currentTick = (minutes > 0 ? minutes : "") + "."
                + format(Locale.US, "%02d", seconds);

        timerView.updateTime(currentTick, remainingTime, durationTime);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    public static TimerFragment newInstance() {
        return new TimerFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityConnection = (ActivityConnection) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_timer, container, false);

        initToolbar();
        initView(v);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.app_name);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!activityConnection.checkStatus()) {
            durationTime = activityConnection.getDurationTime();
            updateTimerView();
        }
        setSessionsCounter(activityConnection.getTodaySessions());
    }

    private void setSessionsCounter(int todaySessions) {
        sessionCounter.setText(String.valueOf(todaySessions));
    }

    private void initToolbar() {
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void initView(View v) {
        sessionType = v.findViewById(R.id.session_type);
        sessionType.setLayerType(LAYER_TYPE_HARDWARE, null);
        sessionType.setAlpha(0f);
        sessionCounter = v.findViewById(R.id.counter);

        timerView = v.findViewById(R.id.timer_view);
        timerView.setOnClickListener(v1 -> activityConnection.onTimerClick());
        timerView.setOnLongClickListener(v1 -> {
            activityConnection.onTimerLongClick();
            return true;
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            activityConnection.openSettings();
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    public void startTimer(String state) {
        shouldContinue = true;
        updateHandler.removeCallbacks(runnable);
        durationTime = activityConnection.getDurationTime();
        updateHandler.post(runnable);
        updateSessionType(state);
    }

    private void updateSessionType(String state) {
        sessionType.setText(state);
        if (!state.isEmpty()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    Trace.beginSection("alpha session animation");
                }
                sessionType.setAlpha(0f);
                sessionType.animate().alpha(1f).setDuration(1000).start();
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    Trace.endSection();
                }
            }
        }

    }

    private void stopTimer() {
        activityConnection.getTodaySessions();
        shouldContinue = false;
        updateHandler.removeCallbacks(runnable);
    }

    public void updateSessionsCounter(int sessions) {
        animateSessionCounter(sessionCounter, sessions);
    }

    private void animateSessionCounter(TextView view, int value) {
        view.animate().setDuration(500).rotationX(90f)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setText(String.valueOf(value));
                        view.animate().setDuration(500).rotationX(0f).setListener(null).start();
                    }
                }).start();
    }
}
