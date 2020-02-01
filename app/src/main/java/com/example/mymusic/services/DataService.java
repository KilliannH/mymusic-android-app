package com.example.mymusic.services;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.models.Song;
import com.example.mymusic.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
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

                            String id = jsonObject.optString("_id");
                            String title = jsonObject.optString("title");
                            String artist = jsonObject.optString("artist");
                            String album = jsonObject.optString("album");
                            String album_img = jsonObject.optString("album_img");
                            String filename = jsonObject.optString("filename");

                            Song song = new Song(id, title, artist, album, album_img, filename);

                            songList.add(song);
                        }
                        RxBus.publish("DATA_READY");
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
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(arrayRequest);
        return songList;
    }

    public void addSong(Song song, String youtube_url) {
        URL = Util.buildUrl("/songs", context);
        API_KEY = Util.getAPI_KEY(context);

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject = new JSONObject(song.toJSON(youtube_url));
        } catch (JSONException err){
            Log.d("Error", err.toString());
        }

        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.e("Response", "" + response.toString());

                        boolean success = response.optBoolean("success");
                        if(success) {
                            RxBus.publish("DATA_RECEIVED");
                        } else {
                            RxBus.publish("DATA_ERROR");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());
                        RxBus.publish("DATA_ERROR");
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", API_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        objectRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000, // it actually takes about 30 sec on my bad connexion
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(objectRequest);
    }
}
