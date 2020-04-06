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

public class AddEditActivity extends AppCompatActivity {

    String songJson;
    String prevActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        final Context activityContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        prevActivity = intent.getStringExtra("activity");
        if(prevActivity.equals("PLAYER")) {
            songJson = intent.getStringExtra("SONG_JSON");
        }
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

            case android.R.id.home:
                Intent intent;
                if(prevActivity.equals("MAIN")) {
                    intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(this, PlayerActivity.class);
                    intent.putExtra("SONG_JSON", songJson);
                    startActivity(intent);
                }

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
