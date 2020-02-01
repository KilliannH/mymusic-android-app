package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.mymusic.adapters.SongsAdapter;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.fragments.PlayerFragment;
import com.example.mymusic.models.Song;
import com.example.mymusic.services.DataService;
import com.example.mymusic.services.ShuffleService;

import java.util.ArrayList;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    ArrayList<Song> songList = new ArrayList<>();
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Context activityContext = this;

        final Fragment playerFragment = new PlayerFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, playerFragment);
        transaction.commit();

         final DataService dataService = new DataService(activityContext);
         RxBus.publish("MAIN_NOT_READY");

        songList = dataService.getSongs();

        // Fires at startup
        disposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "DATA_READY") {

                    // init shuffleService by adding all songs
                    if(ShuffleService.getSongs().size() == 0) {
                        for (int i = 0; i < songList.size(); i++) {
                            ShuffleService.addSong(songList.get(i));
                        }
                    }

                    Log.e("suffleService", ShuffleService.getSongs().toString());

                    // init the list view
                    listView = (ListView) findViewById(R.id.listview);
                    final SongsAdapter adapter = new SongsAdapter(activityContext, songList);
                    listView.setAdapter(adapter);
                    registerForContextMenu(listView);

                    // callback when an item is clicked
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            // TODO Auto-generated method stub
                            Song selectedSong = adapter.getItem(position);

                            playerFragment.getArguments().putString("SELECTED_SONG", selectedSong.toJSON(null));
                            if(ShuffleService.getIsShuffle()) {
                                ShuffleService.removeSong(selectedSong);
                            }
                            RxBus.publish("PLAYER_REQUEST");
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        disposable.dispose();
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
        switch (item.getItemId()) {
            case R.id.action_edit:
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
                Intent intent = new Intent(this, AddSongActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_settings:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}