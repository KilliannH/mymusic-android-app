package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    public static final String SONG_JSON = "com.example.mymusic.SONG_JSON";
    public String URL = "http://";
    public String API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<Song> songList = new ArrayList<Song>();
        final Context activityContext = this;

        // get api url path from config file
        try {
            String api_host = Util.getProperty("api_host", getApplicationContext());
            String api_port = Util.getProperty("api_port", getApplicationContext());
            String api_endpoint = Util.getProperty("api_endpoint", getApplicationContext());
            String api_key = Util.getProperty("api_key", getApplicationContext());

            URL += api_host + ":" + api_port + api_endpoint + "/songs";
            API_KEY = api_key;
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                            String album_img = jsonObject.optString("album_img");
                            String filename = jsonObject.optString("filename");

                            Song song = new Song(id, title, artist, album_img, filename);

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

        intent.putExtra(SONG_JSON, songJson);
        startActivity(intent);
    }
}