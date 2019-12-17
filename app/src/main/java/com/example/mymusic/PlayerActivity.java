package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    public String URL = "http://";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        // String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        // TextView textView = findViewById(R.id.textView);
        // textView.setText(message);

        // get api url path from config file
        try {
            String api_host = Util.getProperty("api_host", getApplicationContext());
            String api_port = Util.getProperty("api_port", getApplicationContext());
            String api_endpoint = Util.getProperty("api_endpoint", getApplicationContext());

            URL += api_host + ":" + api_port + api_endpoint + "/songs/";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
