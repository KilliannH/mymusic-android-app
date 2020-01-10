package com.example.mymusic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.models.Song;
import com.example.mymusic.threads.PlayerThread;
import com.example.mymusic.utils.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class PlayerActivity extends AppCompatActivity {

    String URL;
    String API_KEY;
    private String prevScreen;

    ProgressBar loadingBar;
    SeekBar mSeekBar;
    FloatingActionButton floatingPlayButton;
    MediaPlayer mPlayer;

    Drawable playDrawable;
    Drawable pauseDrawable;

    private Disposable disposable;
    private Handler seekHandler;
    private Runnable seekRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        final Context activityContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar

        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        String songJson = intent.getStringExtra("SONG_JSON");
        prevScreen = intent.getStringExtra("PREV_SCREEN");

        playDrawable = getResources().getDrawable(android.R.drawable.ic_media_play, null);
        pauseDrawable = getResources().getDrawable(android.R.drawable.ic_media_pause, null);

        floatingPlayButton = findViewById(R.id.floatingPlayButton);
        loadingBar = findViewById(R.id.loadingBar);
        mSeekBar = findViewById(R.id.seekBar);

        loadingBar.setVisibility(View.VISIBLE);

        Song song = new Gson().fromJson(songJson, Song.class);

        URL = Util.buildUrl("/stream/" + song.getFilename(), activityContext);
        API_KEY = Util.getAPI_KEY(activityContext);

        TextView title = findViewById(R.id.artist);
        TextView artist = findViewById(R.id.title);
        final TextView timer = findViewById(R.id.timer);
        ImageView album = findViewById(R.id.album);

        title.setText(song.getTitle());
        artist.setText(song.getArtist());
        timer.setText("--:--");
        Glide.with(this).load(song.getAlbum_img()).into(album);

        mPlayer = new MediaPlayer();

        PlayerThread playerThread = new PlayerThread(mPlayer, URL, API_KEY, loadingBar, activityContext);
        playerThread.start();

        disposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "PLAYER_READY") {
                    mSeekBar.setMax(mPlayer.getDuration() / 1000);
                    mPlayer.start();
                    timer.setText("00:00");

                    seekHandler = new Handler();
                    seekRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (mPlayer != null) {
                                int mCurrentPosition = mPlayer.getCurrentPosition() / 1000;
                                mSeekBar.setProgress(mCurrentPosition);
                            }
                            seekHandler.postDelayed(this, 1000);
                        }
                    };
                    //Make sure you update Seekbar on UI thread
                    PlayerActivity.this.runOnUiThread(seekRunnable);

                    mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (mPlayer != null && fromUser) {
                                mPlayer.seekTo(progress * 1000);
                            }

                            if (mPlayer != null && progress >= (mPlayer.getDuration() / 1000)) {
                                mPlayer.seekTo(0);
                                mPlayer.pause();
                                floatingPlayButton.setImageDrawable(playDrawable);
                            }

                            int millis = progress * 1000;

                            timer.setText(String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(millis),
                                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                            ));
                        }
                    });

                    floatingPlayButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (mPlayer.isPlaying()) {
                                mPlayer.pause();
                                floatingPlayButton.setImageDrawable(playDrawable);
                            } else {
                                mPlayer.start();
                                floatingPlayButton.setImageDrawable(pauseDrawable);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mPlayer.reset();
        mPlayer.release();
        disposable.dispose();
        seekHandler.removeCallbacks(seekRunnable);
        RxBus.publish("PLAYER_NOT_READY");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
