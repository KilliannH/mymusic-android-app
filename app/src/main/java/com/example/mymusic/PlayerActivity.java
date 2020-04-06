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
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.models.Song;
import com.example.mymusic.threads.PlayerThread;
import com.example.mymusic.utils.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class PlayerActivity extends AppCompatActivity {

    String URL;
    String API_KEY;

    ArrayList<Song> songList;
    String songJson;
    Song song;

    ProgressBar loadingBar;
    SeekBar mSeekBar;
    FloatingActionButton floatingPlayButton;
    FloatingActionButton floatingPrevButton;
    FloatingActionButton floatingNextButton;

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
        songJson = intent.getStringExtra("SONG_JSON");
        String songJsonArr = intent.getStringExtra("SONG_JSON_ARR");

        Type songListType = new TypeToken<ArrayList<Song>>(){}.getType();

        songList = new Gson().fromJson(songJsonArr, songListType);
        Log.e("songList", "" + songList);

        playDrawable = getResources().getDrawable(android.R.drawable.ic_media_play, null);
        pauseDrawable = getResources().getDrawable(android.R.drawable.ic_media_pause, null);

        floatingPlayButton = findViewById(R.id.floatingPlayButton);
        floatingPrevButton = findViewById(R.id.floatingPrevButton);
        floatingNextButton = findViewById(R.id.floatingNextButton);

        loadingBar = findViewById(R.id.loadingBar);
        mSeekBar = findViewById(R.id.seekBar);

        loadingBar.setVisibility(View.VISIBLE);

        song = new Gson().fromJson(songJson, Song.class);

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

        PlayerThread playerThread = new PlayerThread(mPlayer, URL, API_KEY, activityContext);
        playerThread.start();

        disposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "PLAYER_READY") {
                    loadingBar.setVisibility(View.GONE);
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

                            // if end of the song reached, skip to the next song if exists
                            if (mPlayer != null && progress >= (mPlayer.getDuration() / 1000)) {
                                mPlayer.pause();
                                floatingPlayButton.setImageDrawable(playDrawable);
                                skipNext();
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

                    floatingPrevButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mPlayer.isPlaying()) {
                                mPlayer.pause();
                                mPlayer.seekTo(0);
                                floatingPlayButton.setImageDrawable(playDrawable);
                            } else {
                                skipPrev();
                            }
                        }
                    });

                    floatingNextButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            skipNext();
                        }
                    });
                }
            }
        });
    }

    public void dispatchSong(Song selectedSong) {
        Intent intent = new Intent(this, PlayerActivity.class);

        String songJson = new Gson().toJson(selectedSong);

        String songJsonArr = new Gson().toJson(songList);

        intent.putExtra("SONG_JSON", songJson);
        intent.putExtra("SONG_JSON_ARR", songJsonArr);
        startActivity(intent);
    }

    public void skipNext() {
        for(int i = 0; i < songList.size(); i++) {
            if(song.getId().equals(songList.get(i).getId())) {
                if(i != songList.size() -1) {
                    Song nextSong = songList.get(i + 1);
                    dispatchSong(nextSong);
                }
            }
        }
    }

    public void skipPrev() {
        for(int i = 0; i < songList.size(); i++) {
            if(song.getId().equals(songList.get(i).getId())) {
                if(i != 0) {
                    Song prevSong = songList.get(i - 1);
                    dispatchSong(prevSong);
                }
            }
        }
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

            case R.id.action_add:
                Intent intent = new Intent(this, AddEditActivity.class);
                intent.putExtra("activity", "PLAYER");
                intent.putExtra("SONG_JSON", songJson);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}