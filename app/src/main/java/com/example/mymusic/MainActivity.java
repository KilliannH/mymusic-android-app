package com.example.mymusic;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mymusic.PlayerActivity;

import com.example.mymusic.adapters.SongsAdapter;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.models.Song;
import com.example.mymusic.services.DataService;
import com.example.mymusic.services.ShuffleService;
import com.example.mymusic.utils.Util;
import com.google.gson.Gson;

import java.util.ArrayList;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    DataService dataService;

    ProgressBar progressBar;

    ArrayList<Song> songList = new ArrayList<>();
    private Disposable dataDisposable;
    private Disposable addSuccessDisposable;
    private Disposable addErrorDisposable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Context activityContext = this;

        listView = (ListView) findViewById(R.id.listview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        runLoadingState();

         dataService = new DataService(activityContext);
         RxBus.publish("MAIN_NOT_READY");

        songList = dataService.getSongs();

        // Fires at startup
        dataDisposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "DATA_READY") {
                    runLoadedState();
                    // init shuffleService by adding all songs
                    if(ShuffleService.getSongs().size() == 0) {
                        for (int i = 0; i < songList.size(); i++) {
                            ShuffleService.addSong(songList.get(i));
                        }
                    }

                    // init the list view
                    final SongsAdapter adapter = new SongsAdapter(activityContext, songList);
                    listView.setAdapter(adapter);
                    registerForContextMenu(listView);

                    // callback when an item is clicked
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            // TODO Auto-generated method stub
                            Song selectedSong = adapter.getItem(position);
                            dispatchSong(selectedSong);
                        }
                    });
                }
            }
        });

        addSuccessDisposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "DATA_RECEIVED") {

                    dataService.getSongs();
                    Toast.makeText(getApplicationContext(), "success!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addErrorDisposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "DATA_ERROR") {

                    runLoadedState();
                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // send song to the player activity
    public void dispatchSong(Song selectedSong) {
        Intent intent = new Intent(this, PlayerActivity.class);

        String songJson = new Gson().toJson(selectedSong);

        String songJsonArr = new Gson().toJson(songList);

        intent.putExtra("SONG_JSON", songJson);
        intent.putExtra("SONG_JSON_ARR", songJsonArr);
        startActivity(intent);
    }

    private void runLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
    }

    private void runLoadedState() {
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        dataDisposable.dispose();
        addSuccessDisposable.dispose();
        addErrorDisposable.dispose();
        RxBus.publish("DATA_NOT_READY");
    }

    // this is the longClick context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.song_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Song optSong = (Song) listView.getItemAtPosition(info.position);
        String songJson = "";
        for(int i = 0; i < songList.size(); i++) {
            Song song = songList.get(i);
            if(song.getId().equals(optSong.getId())) {
                songJson = song.toJSON("");
            }
        }

        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent intent = new Intent(this, AddEditActivity.class);
                intent.putExtra("SONG_JSON", songJson);
                intent.putExtra("activity", "MAIN");
                startActivity(intent);
                return true;
            case R.id.action_remove:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // this is the appBar Menu
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
                intent.putExtra("activity", "MAIN");
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}