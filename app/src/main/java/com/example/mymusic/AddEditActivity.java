package com.example.mymusic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusic.bus.RxBus;
import com.example.mymusic.models.Song;
import com.example.mymusic.services.DataService;
import com.example.mymusic.utils.Util;
import com.google.gson.Gson;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class AddEditActivity extends AppCompatActivity {

    String songJson;
    String prevActivity;
    Song editedSong;

    TextView label;
    Button button;

    ProgressBar progressBar;

    EditText title;
    EditText artist;
    EditText album;
    EditText album_img;
    EditText filename;
    EditText youtube_url;

    private DataService dataService;

    Disposable disposable;

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

        dataService = new DataService(this);

        Intent intent = getIntent();
        prevActivity = intent.getStringExtra("activity");
        songJson = intent.getStringExtra("SONG_JSON");

        button = findViewById(R.id.add_edit_button);
        progressBar = findViewById(R.id.progressBar);

        if(songJson != null && prevActivity.equals("MAIN")) {
            Log.e("debug song", "" + songJson);
            editedSong = new Gson().fromJson(songJson, Song.class);

            label = findViewById(R.id.add_label);

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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndAddOrUpdateSong();
            }
        });

        disposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "DATA_RECEIVED") {

                    runLoadedState();
                    Toast.makeText(getApplicationContext(), "success!", Toast.LENGTH_SHORT).show();
                }

                if (o == "DATA_ERROR") {

                    runLoadedState();
                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void runLoadedState() {
        title.setActivated(true);
        artist.setActivated(true);
        album.setActivated(true);
        album_img.setActivated(true);
        filename.setActivated(true);

        if(songJson == null) {
            youtube_url.setActivated(true);
        }

        button.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void runLoadingState() {
        title.setActivated(false);
        artist.setActivated(false);
        album.setActivated(false);
        album_img.setActivated(false);
        filename.setActivated(false);

        if(songJson == null) {
            youtube_url.setActivated(false);
        }

        button.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void validateAndAddOrUpdateSong() {

        runLoadingState();

        String opt_title = title.getText().toString();
        String opt_artist = artist.getText().toString();
        String opt_album = album.getText().toString();
        String opt_album_img = album_img.getText().toString();
        String opt_filename = filename.getText().toString();
        String opt_youtube_url = youtube_url.getText().toString();

        if(opt_title.isEmpty() || opt_artist.isEmpty() || opt_album.isEmpty()
                || opt_album_img.isEmpty() || opt_filename.isEmpty() || (opt_youtube_url.isEmpty() && songJson == null)) {
            Toast.makeText(getApplicationContext(), "you must fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            if (!Util.validateUrl(opt_album_img)) {
                Toast.makeText(getApplicationContext(), "you must use a valid image url", Toast.LENGTH_SHORT).show();
            } else if (!Util.validateUrl(opt_youtube_url) && songJson == null) {
                Toast.makeText(getApplicationContext(), "you must use a valid youtube url", Toast.LENGTH_SHORT).show();
            } else if (!Util.validateMp3(opt_filename)) {
                Toast.makeText(getApplicationContext(), "invalid filename", Toast.LENGTH_SHORT).show();
            } else {
                if(songJson != null) {
                    // we are on update state
                    editedSong.setTitle(opt_title);
                    editedSong.setArtist(opt_artist);
                    editedSong.setAlbum(opt_album);
                    editedSong.setAlbum_img(opt_album_img);
                    editedSong.setFilename(opt_filename);
                    dataService.editSong(editedSong);
                } else {
                    Song newSong = new Song(opt_title, opt_artist, opt_album, opt_album_img, opt_filename);
                    dataService.addSong(newSong, opt_youtube_url);
                }
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
                if(prevActivity.equals("MAIN")) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, PlayerActivity.class);
                    intent.putExtra("SONG_JSON", songJson);
                    startActivity(intent);
                }

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disposable.dispose();
        RxBus.publish("DATA_NOT_READY");
    }
}
