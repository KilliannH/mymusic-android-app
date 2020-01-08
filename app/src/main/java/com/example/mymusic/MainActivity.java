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
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.mymusic.adapters.SongsAdapter;
import com.example.mymusic.models.Song;
import com.example.mymusic.services.SessionService;
import com.example.mymusic.utils.Screen;
import com.example.mymusic.utils.Util;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    public String URL;
    public String API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ArrayList<Screen> screens = SessionService.getScreens();

        // si screen has no length, then no up button, else impl up.
        if(screens != null && screens.size() > 0) {

            // Get a support ActionBar corresponding to this toolbar
            ActionBar ab = getSupportActionBar();

            // Enable the Up button
            if(ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }

            for(int i = 0; i < screens.size(); i++) {
                Screen s = screens.get(i);
                Log.e("screen", s.getName());
            }
        }

        SessionService.addScreen(new Screen("main"));

        final ArrayList<Song> songList = new ArrayList<Song>();
        final Context activityContext = this;

        URL = Util.buildUrl("/songs", activityContext);
        API_KEY = Util.getAPI_KEY(activityContext);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest arrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        for (int i = 0; i < response.length(); i++) {

                            JSONObject jsonObject = response.optJSONObject(i);

                            Integer id = jsonObject.optInt("id");
                            String title = jsonObject.optString("title");
                            String artist = jsonObject.optString("artist");
                            String album = jsonObject.optString("album");
                            String album_img = jsonObject.optString("album_img");
                            String filename = jsonObject.optString("filename");

                            Song song = new Song(id, title, artist, album, album_img, filename);

                            songList.add(song);
                        }

                        listView = (ListView) findViewById(R.id.listview);
                        final SongsAdapter adapter = new SongsAdapter(activityContext, songList);
                        listView.setAdapter(adapter);

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
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", API_KEY);
                return headers;
            }
        };

        requestQueue.add(arrayRequest);
    }

    // send song to the player activity
    public void dispatchSong(Song selectedSong) {
        Intent intent = new Intent(this, PlayerActivity.class);

        String songJson = new Gson().toJson(selectedSong);

        intent.putExtra("SONG_JSON", songJson);
        startActivity(intent);
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
            case R.id.action_albums:
                // User chose the "Settings" item, show the app settings UI...
                Intent myIntent = new Intent(MainActivity.this,
                        AlbumsActivity.class);
                startActivity(myIntent);
                return true;

            case R.id.action_artists:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            case R.id.action_songs:
                return true;

            case R.id.action_add:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            case R.id.action_remove:
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