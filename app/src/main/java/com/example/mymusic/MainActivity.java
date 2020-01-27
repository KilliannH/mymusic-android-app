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
import com.google.gson.Gson;

import java.util.ArrayList;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    AlertDialog dialog;
    String searchQuery;
    ArrayList<Song> songList = new ArrayList<>();
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Context activityContext = this;

         final DataService dataService = new DataService(activityContext);
         RxBus.publish("MAIN_NOT_READY");

        songList = dataService.getSongs("");


        // create the search dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);

        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_layout, null);

        // get the EditText from inflated layout
        final EditText editText = (EditText) content.findViewById(R.id.dialog_search);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(content);

        builder.setTitle(R.string.string_search);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                searchQuery = editText.getText().toString();
                RxBus.publish("DATA_NOT_READY");
                songList = dataService.getSongs(searchQuery);
            }
        });
        builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        dialog = builder.create();

        final TextView textView = (TextView) findViewById(R.id.query);
        final Button clearButton = (Button) findViewById(R.id.clear_button);

        textView.setText(R.string.string_search);
        textView.setVisibility(View.GONE);
        clearButton.setVisibility(View.GONE);

        // Fires at startup & after a search query
        disposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "DATA_READY") {

                    if(searchQuery != null) {
                        String query = getResources().getString(R.string.string_search) + " : " + searchQuery;
                        textView.setText(query);
                        textView.setVisibility(View.VISIBLE);
                        clearButton.setVisibility(View.VISIBLE);
                    }
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
                            Bundle bundle = new Bundle();
                            bundle.putString("SELECTED_SONG", selectedSong.toJSON());
                            final Fragment playerFragment = new PlayerFragment();
                            playerFragment.setArguments(bundle);
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.add(R.id.fragment_container, playerFragment);
                            transaction.commit();
                        }
                    });
                }
            }
        });

       clearButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               RxBus.publish("DATA_NOT_READY");
               textView.setText(R.string.string_search);
               textView.setVisibility(View.GONE);
               clearButton.setVisibility(View.GONE);
               searchQuery = null;
               songList = dataService.getSongs("");
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