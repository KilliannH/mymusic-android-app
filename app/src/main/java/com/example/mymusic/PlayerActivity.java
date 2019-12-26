package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
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
import com.example.mymusic.threads.PlayerThread;
import com.google.gson.Gson;

public class PlayerActivity extends AppCompatActivity {

    String URL;
    String API_KEY;

    ProgressBar progressBar;
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

        URL = Util.buildUrl("/stream/" + song.getFilename(), activityContext);
        API_KEY = Util.getAPI_KEY(activityContext);

        TextView title = findViewById(R.id.title);
        TextView artist = findViewById(R.id.artist);
        ImageView album = findViewById(R.id.album);

        title.setText(song.getTitle());
        artist.setText(song.getArtist());
        Glide.with(this).load(song.getAlbum_img()).into(album);

        PlayerThread playerThread = new PlayerThread(URL, API_KEY, progressBar, activityContext);
        playerThread.start();
    }
}
