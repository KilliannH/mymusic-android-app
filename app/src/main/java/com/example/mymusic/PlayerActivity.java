package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mymusic.models.Song;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.logging.Logger;

public class PlayerActivity extends AppCompatActivity {

    public String URL = "http://";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

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
        Glide.with(this).load(song.getAlbum_img_url()).into(album);


    }
}
