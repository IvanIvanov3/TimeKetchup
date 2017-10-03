package com.bytebazar.timeketchup.settings;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bytebazar.timeketchup.IModel;
import com.bytebazar.timeketchup.R;

import java.io.IOException;


public class RingtonePickerActivity extends AppCompatActivity implements View.OnClickListener, RingtoneAdapter.OnRingtoneClickListener {

    private static final String TAG = "RingtonePickerActivity";

    private RingtoneAdapter ringtoneAdapter;
    private MediaPlayer mediaPlayer;
    private String currentRingtoneUri;
    private IModel settings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone_picker);
        settings = new SettingsModel(this);
        currentRingtoneUri = settings.getNotificationSoundUri();
        init(currentRingtoneUri);
        setTitle("choose ringtone");
    }

    private void init(String uri) {
        final Button okButton = findViewById(R.id.ok_btn);
        final Button cancelButton = findViewById(R.id.cancel_btn);
        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        ringtoneAdapter = new RingtoneAdapter(this, uri,settings.getRingtones(this));
        mediaPlayer = new MediaPlayer();

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ringtoneAdapter);

        final DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_btn:
                checkAndSave();
            default:
                stop();
                finish();
        }
    }

    @Override
    public void onRingtoneClick(String uri) {
        try {
            playRingtone(Uri.parse(uri));
        } catch (Exception e) {
            Log.e(TAG, "player error", e);
        }
    }

    private void playRingtone(@NonNull Uri uri) throws IOException,
            IllegalArgumentException,
            SecurityException,
            IllegalStateException {

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.reset();
        mediaPlayer.setDataSource(this, uri);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    private void stop() {
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
        mediaPlayer.release();
    }

    private void checkAndSave() {
        final String uri = ringtoneAdapter.getCurrentRingtoneUri();
        if (uri != null && !uri.equals(currentRingtoneUri)) {
            settings.setNotificationSoundUri(uri);
            settings.setNotificationSoundTitle(ringtoneAdapter.getCurrentTitle());
        }
    }
}
