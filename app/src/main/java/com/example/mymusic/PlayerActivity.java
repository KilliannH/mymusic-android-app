package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mymusic.models.Song;
import com.example.mymusic.threads.PlayerThread;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

public class PlayerActivity extends AppCompatActivity {

    String URL;
    String API_KEY;

    ProgressBar progressBar;
    FloatingActionButton floatingPlayButton;
    MediaPlayer mPlayer;

    Drawable playDrawable;
    Drawable pauseDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        final Context activityContext = this;

        playDrawable = getResources().getDrawable(android.R.drawable.ic_media_play, null);
        pauseDrawable = getResources().getDrawable(android.R.drawable.ic_media_pause, null);

        floatingPlayButton = findViewById(R.id.floatingPlayButton);
        RelativeLayout layout = new RelativeLayout(this);
        progressBar = findViewById(R.id.progressbar);

        progressBar.setVisibility(View.VISIBLE);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String songJson = intent.getStringExtra(MainActivity.SONG_JSON);

        Song song = new Gson().fromJson(songJson, Song.class);

        Log.e("song", song.toString());

        URL = Util.buildUrl("/stream/" + song.getFilename(), activityContext);
        API_KEY = Util.getAPI_KEY(activityContext);

        TextView title = findViewById(R.id.title);
        TextView artist = findViewById(R.id.artist);
        ImageView album = findViewById(R.id.album);

        title.setText(song.getTitle());
        artist.setText(song.getArtist());
        Glide.with(this).load(song.getAlbum_img()).into(album);

        mPlayer = new MediaPlayer();

        PlayerThread playerThread = new PlayerThread(mPlayer, URL, API_KEY, progressBar, activityContext);
        playerThread.start();

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

    @Override
    public void onPause() {
        super.onPause();
        mPlayer.stop();
        mPlayer.release();
    }
}
