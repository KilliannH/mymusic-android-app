package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    String[] listItem;

    public String URL = "http://";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<String> songList = new ArrayList<String>();
        final Context activityContext = this;

        // get api url path from config file
        try {
            String api_host = Util.getProperty("api_host", getApplicationContext());
            String api_port = Util.getProperty("api_port", getApplicationContext());
            String api_endpoint = Util.getProperty("api_endpoint", getApplicationContext());

            URL += api_host + ":" + api_port + api_endpoint + "/songs";
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
                        // textView.setText("Response: " + response.toString());
                        for(int i = 0; i < response.length(); i++) {

                            JSONObject jsonobject = response.optJSONObject(i);
                            String title = jsonobject.optString("title");

                            songList.add(title);
                        }

                        listItem = songList.toArray(new String[songList.size()]);

                        listView=(ListView)findViewById(R.id.listview);

                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activityContext,
                                android.R.layout.simple_list_item_1, android.R.id.text1, listItem);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                                // TODO Auto-generated method stub
                                String value=adapter.getItem(position);
                                Toast.makeText(getApplicationContext(),value, Toast.LENGTH_SHORT).show();

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
        );
        requestQueue.add(arrayRequest);
    }
}