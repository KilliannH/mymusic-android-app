package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mymusic.models.Song;
import com.google.gson.Gson;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    String URL = "http://";

    ProgressBar progressBar;
    MediaPlayer mPlayer;
    Button mPlayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        final Context activityContext = this;
        mPlayButton = findViewById(R.id.playButton);
        RelativeLayout layout = new RelativeLayout(this);
        progressBar = findViewById(R.id.progressbar);

        progressBar.setVisibility(View.VISIBLE);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String songJson = intent.getStringExtra(MainActivity.SONG_JSON);

        Song song = new Gson().fromJson(songJson, Song.class);

        Log.e("song", song.toString());

        TextView title = findViewById(R.id.title);
        TextView artist = findViewById(R.id.artist);
        ImageView album = findViewById(R.id.album);

        title.setText(song.getTitle());
        artist.setText(song.getArtist());
        Glide.with(this).load(song.getAlbum_img()).into(album);

        // get api url path from config file
        try {
            String api_host = Util.getProperty("api_host", getApplicationContext());
            String api_port = Util.getProperty("api_port", getApplicationContext());
            String api_endpoint = Util.getProperty("api_endpoint", getApplicationContext());

            URL += api_host + ":" + api_port + api_endpoint + "/stream/" + song.getFilename();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize a new media player instance
        mPlayer = new MediaPlayer();

        // Set the media player audio stream type
        // mPlayer.setAudioStreamType(AudioManager.);
        //Try to play music/audio from url
        try{
            // Set the audio data source
            mPlayer.setDataSource(URL);
            mPlayer.prepareAsync();

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    progressBar.setVisibility(View.GONE);
                    mp.start();
                }
            });
        }catch (IOException e){
            // Catch the exception
            e.printStackTrace();
        }
    }
}
