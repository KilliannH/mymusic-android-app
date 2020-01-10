package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.example.mymusic.models.Song;
import com.example.mymusic.services.DataService;
import com.google.gson.Gson;

import java.util.ArrayList;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Context activityContext = this;

         DataService dataService = new DataService(activityContext);
         RxBus.publish("MAIN_NOT_READY");

        final ArrayList<Song> songList = dataService.getSongs();

        disposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "MAIN_READY") {

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
                            dispatchSong(selectedSong);
                        }
                    });
                }
            }
        });
    }

    // send song to the player activity
    public void dispatchSong(Song selectedSong) {
        Intent intent = new Intent(this, PlayerActivity.class);

        String songJson = new Gson().toJson(selectedSong);

        intent.putExtra("SONG_JSON", songJson);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        disposable.dispose();
        RxBus.publish("MAIN_NOT_READY");
    }

    // this menu is on longclick
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.song_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update:
                Log.e("Context", "update selected");
                return true;
            case R.id.action_remove:
                Log.e("Context", "remove selected");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // this menu is in appBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent myIntent;

        switch (item.getItemId()) {


            /* case R.id.action_artists:
                // User chose the "Settings" item, show the app settings UI...
                myIntent = new Intent(MainActivity.this,
                        ArtistsActivity.class);
                startActivity(myIntent);
                return true;

             */

            case R.id.action_add:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}