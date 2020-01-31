package com.example.mymusic;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.FragmentManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
    AlertDialog dialog;

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


        // create the search dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);

        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_layout, null);

        // get the EditText from inflated layout
        final EditText editText = (EditText) content.findViewById(R.id.dialog_search);

        // Inflate and set the layout for the dialog
        builder.setView(content);

        builder.setTitle(R.string.string_search);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // searchQuery = editText.getText().toString();
                RxBus.publish("DATA_NOT_READY");
                // songList = dataService.getSongs();
            }
        });
        builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // dialog is created, not shown
        dialog = builder.create();

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

                            playerFragment.getArguments().putString("SELECTED_SONG", selectedSong.toJSON());
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

            case R.id.action_search:
                // User chose the "Search" item
                dialog.show();
                return true;

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