package com.example.mymusic;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mymusic.adapters.SongsAdapter;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.fragments.PlayerFragment;
import com.example.mymusic.models.Song;
import com.example.mymusic.services.DataService;
import com.example.mymusic.services.ShuffleService;
import com.example.mymusic.utils.Util;

import java.util.ArrayList;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    private boolean loadingState = false;

    EditText title;
    EditText artist;
    EditText album;
    EditText album_img;
    EditText filename;
    EditText youtube_url;

    DataService dataService;

    ProgressBar progressBar;

    private AlertDialog dialog_add;

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

        // on builder, we don't want the activity context, but it class instead
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View content = inflater.inflate(R.layout.dialog_add, null);

        title = (EditText) content.findViewById(R.id.add_title);
        artist = (EditText) content.findViewById(R.id.add_artist);
        album = (EditText) content.findViewById(R.id.add_album);
        album_img = (EditText) content.findViewById(R.id.add_album_img);
        filename = (EditText) content.findViewById(R.id.add_filename);
        youtube_url = (EditText) content.findViewById(R.id.add_youtube_url);

        builder.setView(content);
        builder.setTitle(R.string.action_add);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                validateAndAddSong();
                if(loadingState) {
                    runLoadingState();
                }
            }
        });
        builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        dialog_add = builder.create();

        final Fragment playerFragment = new PlayerFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, playerFragment);
        transaction.commit();

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

                            playerFragment.getArguments().putString("SELECTED_SONG", selectedSong.toJSON(null));
                            RxBus.publish("PLAYER_REQUEST");
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

    private void runLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
    }

    private void runLoadedState() {
        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
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
                dialog_add.show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}