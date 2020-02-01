package com.example.mymusic;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mymusic.adapters.SongsAdapter;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.models.Song;
import com.example.mymusic.services.DataService;
import com.example.mymusic.services.ShuffleService;
import com.example.mymusic.utils.Util;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class AddSongActivity extends AppCompatActivity {

    private EditText title;
    private EditText artist;
    private EditText album;
    private EditText album_img;
    private EditText filename;
    private EditText youtube_url;

    private DataService dataService;

    private AlertDialog dialog;

    private Disposable succesDisposable;
    private Disposable errorDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar

        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        final DataService dataService = new DataService(getApplicationContext());

        title = (EditText) findViewById(R.id.add_title);
        artist = (EditText) findViewById(R.id.add_artist);
        album = (EditText) findViewById(R.id.add_album);
        album_img = (EditText) findViewById(R.id.add_album_img);
        filename = (EditText) findViewById(R.id.add_filename);
        youtube_url = (EditText) findViewById(R.id.add_youtube_url);

        Button addButton = (Button) findViewById(R.id.add_button);

        // on builder, we don't want the activity context, but it class instead
        AlertDialog.Builder builder = new AlertDialog.Builder(AddSongActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_layout, null);

        builder.setView(content);
        builder.setTitle(R.string.loading);
        dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    if(!Util.validateUrl(opt_album_img)) {
                        Toast.makeText(getApplicationContext(), "you must use a valid image url", Toast.LENGTH_SHORT).show();
                    } else if(!Util.validateUrl(opt_youtube_url)) {
                        Toast.makeText(getApplicationContext(), "you must use a valid youtube url", Toast.LENGTH_SHORT).show();
                    } else if(!Util.validateMp3(opt_filename)) {
                        Toast.makeText(getApplicationContext(), "invalid filename", Toast.LENGTH_SHORT).show();
                    } else {
                        Song newSong = new Song(opt_title, opt_artist, opt_album, opt_album_img, opt_filename);
                        Log.e("opt_yt_url", opt_youtube_url);
                        dataService.addSong(newSong, opt_youtube_url);
                        dialog.show();
                    }
                }
            }
        });

        succesDisposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "DATA_RECEIVED") {

                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "success!", Toast.LENGTH_SHORT).show();
                    RxBus.publish("");
                }
            }
        });

        errorDisposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "DATA_ERROR") {

                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    RxBus.publish("");
                }
            }
        });
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

    @Override
    public void onPause() {
        super.onPause();
        succesDisposable.dispose();
        errorDisposable.dispose();
        RxBus.publish("DATA_NOT_READY");
    }
}
