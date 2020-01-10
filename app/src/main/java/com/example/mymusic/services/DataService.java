package com.example.mymusic.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.models.Song;
import com.example.mymusic.utils.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataService {

    private String API_KEY;
    private String URL;
    private Context context;

    public DataService(Context context) {
        this.context = context;
    }

    public ArrayList<Song> getSongs() {
        final ArrayList<Song> songList = new ArrayList<Song>();

        URL = Util.buildUrl("/songs", context);
        API_KEY = Util.getAPI_KEY(context);

        RequestQueue requestQueue = Volley.newRequestQueue(context);

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
                        RxBus.publish("MAIN_READY");
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
        return songList;
    }
}
