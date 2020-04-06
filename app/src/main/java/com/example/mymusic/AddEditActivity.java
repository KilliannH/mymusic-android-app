package com.example.mymusic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusic.models.Song;
import com.example.mymusic.services.DataService;
import com.example.mymusic.utils.Util;
import com.google.gson.Gson;

public class AddEditActivity extends AppCompatActivity {

    String songJson;
    String prevActivity;
    Song editedSong;

    TextView label;
    Button button;

    EditText title;
    EditText artist;
    EditText album;
    EditText album_img;
    EditText filename;
    EditText youtube_url;

    private DataService dataService;
    private Boolean loadingState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        DataService dataService = new DataService(this);

        Intent intent = getIntent();
        prevActivity = intent.getStringExtra("activity");
        songJson = intent.getStringExtra("SONG_JSON");

        if(songJson != null && prevActivity.equals("MAIN")) {
            Log.e("debug song", "" + songJson);
            editedSong = new Gson().fromJson(songJson, Song.class);

            label = findViewById(R.id.add_label);
            button = findViewById(R.id.add_edit_button);

            title = findViewById(R.id.add_title);
            artist = findViewById(R.id.add_artist);
            album = findViewById(R.id.add_album);
            album_img = findViewById(R.id.add_album_img);
            filename = findViewById(R.id.add_filename);
            youtube_url = findViewById(R.id.add_youtube_url);

            label.setText(R.string.edit_label);
            button.setText(R.string.action_update);

            title.setText(editedSong.getTitle());
            artist.setText(editedSong.getArtist());
            album.setText(editedSong.getAlbum());
            album_img.setText(editedSong.getAlbum_img());
            filename.setText(editedSong.getFilename());
            youtube_url.setVisibility(View.GONE);
        }
    }

    private void validateAndAddSong() {
        String opt_title = title.getText().toString();
        String opt_artist = artist.getText().toString();
        String opt_album = album.getText().toString();
        String opt_album_img = album_img.getText().toString();
        String opt_filename = filename.getText().toString();
        String opt_youtube_url = youtube_url.getText().toString();

        if(opt_title.isEmpty() || opt_artist.isEmpty() || opt_album.isEmpty()
                || opt_album_img.isEmpty() || opt_filename.isEmpty() || opt_youtube_url.isEmpty()) {
            Toast.makeText(getApplicationContext(), "you must fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            if (!Util.validateUrl(opt_album_img)) {
                Toast.makeText(getApplicationContext(), "you must use a valid image url", Toast.LENGTH_SHORT).show();
            } else if (!Util.validateUrl(opt_youtube_url)) {
                Toast.makeText(getApplicationContext(), "you must use a valid youtube url", Toast.LENGTH_SHORT).show();
            } else if (!Util.validateMp3(opt_filename)) {
                Toast.makeText(getApplicationContext(), "invalid filename", Toast.LENGTH_SHORT).show();
            } else {
                Song newSong = new Song(opt_title, opt_artist, opt_album, opt_album_img, opt_filename);
                dataService.addSong(newSong, opt_youtube_url);
                loadingState = true;
            }
        }
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

            case android.R.id.home:
                Intent intent;
                if(prevActivity.equals("MAIN")) {
                    intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(this, PlayerActivity.class);
                    intent.putExtra("SONG_JSON", songJson);
                    startActivity(intent);
                }

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
