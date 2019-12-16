package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.mymusic.MESSAGE";

    String api_host = Helper.getConfigValue(this, "api_host");
    String api_port = Helper.getConfigValue(this, "api_port");
    String api_endpoint = Helper.getConfigValue(this, "api_endpoint");
    String url ="http://" + api_host + ":" + api_port + api_endpoint + "/songs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listview = (ListView) findViewById(R.id.listview);
        final ArrayList<String> songList = new ArrayList<String>();

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest arrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // textView.setText("Response: " + response.toString());
                        for(int i = 0; i < response.length(); i++) {

                            JSONObject jsonobject = response.optJSONObject(i);
                            String title = jsonobject.optString("title");

                            songList.add(title);
                        }
                        Log.e("Reponse", songList.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());
                    }
                }
        );
        requestQueue.add(arrayRequest);
    }
}