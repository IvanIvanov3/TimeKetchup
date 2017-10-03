package com.bytebazar.timeketchup.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bytebazar.timeketchup.R;
import com.bytebazar.timeketchup.state.DialogType;


public class SettingsFragment extends Fragment implements View.OnClickListener, SettingsView {

    public static final String TAG = "SettingsFragment";

    private TextView mSessionDuration;
    private TextView mShortBreak;
    private TextView mLongBreak;
    private TextView mSessionStreak;

    private TextView mNotifSound;
    private SwitchCompat mVibration;

    private SettingsPresenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new SettingsPresenter(new SettingsModel(getActivity().getApplicationContext()), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_settings, container, false);

        final ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        initView(v);
        getActivity().setTitle(R.string.settings);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataToView();
    }

    private void loadDataToView() {
        mPresenter.initValue();
    }

    private void initView(View v) {
        final View mSessionDurationLayout = v.findViewById(R.id.session_duration_layout);
        mSessionDuration = v.findViewById(R.id.session_duration_tv);
        final View mShortBreakLayout = v.findViewById(R.id.sbreak_duration_layout);
        mShortBreak = v.findViewById(R.id.sbreak_duration);
        final View mLongBreakLayout = v.findViewById(R.id.lbreak_duration_layout);
        mLongBreak = v.findViewById(R.id.lbreak_duration_tv);
        final View mSessionStreakLayout = v.findViewById(R.id.session_streak_layout);
        mSessionStreak = v.findViewById(R.id.session_streak_tv);

        final View mNotifSoundLayout = v.findViewById(R.id.notif_sound_layout);
        mNotifSound = v.findViewById(R.id.notif_sound_title);
        final View mVibrationLayout = v.findViewById(R.id.vibration_layout);
        mVibration = v.findViewById(R.id.vibration_switch);

        mSessionDurationLayout.setOnClickListener(this);
        mShortBreakLayout.setOnClickListener(this);
        mLongBreakLayout.setOnClickListener(this);
        mSessionStreakLayout.setOnClickListener(this);
        mNotifSoundLayout.setOnClickListener(this);
        mVibrationLayout.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        @DialogType int dialogType = DialogType.Empty;
        switch (v.getId()) {
            case R.id.vibration_layout:
                mPresenter.vibrationClick(!mVibration.isChecked());
                return;
            case R.id.notif_sound_layout:
                mPresenter.notifSoundClick();
                return;
            case R.id.session_duration_layout:
                dialogType = DialogType.SESSION_DURATION;
                break;
            case R.id.sbreak_duration_layout:
                dialogType = DialogType.SHORT_BREAK_DURATION;
                break;
            case R.id.lbreak_duration_layout:
                dialogType = DialogType.LONG_BREAK_DURATION;
                break;
            case R.id.session_streak_layout:
                dialogType = DialogType.SESSION_STREAK;
                break;

        }
        mPresenter.onDurationSettingClick(dialogType);
    }

    public static Fragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void showDurationDialog(String title, final int minValue, int maxValue, final int currentValue) {

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.dialog_seekbar, null);
        final TextView currentValueText = v.findViewById(R.id.current_value);
        final TextView minValueText = v.findViewById(R.id.min_value);
        final TextView maxValueText = v.findViewById(R.id.max_value);
        final SeekBar seekBar = v.findViewById(R.id.seek_bar);


        minValueText.setText(String.valueOf(minValue));
        maxValueText.setText(String.valueOf(maxValue));
        currentValueText.setText(String.valueOf(currentValue));

        seekBar.setMax(maxValue - minValue);
        seekBar.setProgress(currentValue - minValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentValueText.setText(String.valueOf(progress + minValue));
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setView(v)
                .setPositiveButton(android.R.string.ok
                        , (dialog, which) -> mPresenter.setCurrentValue(seekBar.getProgress() + minValue))
                .setTitle(title);
        builder.create();
        builder.show();
    }

    @Override
    public void showSessionDuration(String duration) {
        mSessionDuration.setText(duration);
    }

    @Override
    public void showShortBreakDuration(String duration) {
        mShortBreak.setText(duration);
    }

    @Override
    public void showLongBreakDuration(String duration) {
        mLongBreak.setText(duration);
    }

    @Override
    public void showSessionStreak(String sessions) {
        mSessionStreak.setText(sessions);
    }

    @Override
    public void showVibration(boolean isEnable) {
        mVibration.setChecked(isEnable);
    }


    @Override
    public void showSoundPicker() {
        startActivity(new Intent(getActivity(), RingtonePickerActivity.class));
    }

    @Override
    public void showNotificationSoundTitle(String title) {
        mNotifSound.setText(title);
    }

}
