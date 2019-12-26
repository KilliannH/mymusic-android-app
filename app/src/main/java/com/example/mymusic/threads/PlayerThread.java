package com.example.mymusic.threads;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.HashMap;

public class PlayerThread extends Thread {

    private String URL;
    private String API_KEY;
    private ProgressBar progressBar;
    private Context context;

    public PlayerThread(String url, String api_key, ProgressBar progressBar, Context context) {
        this.URL = url;
        this.API_KEY = api_key;
        this.progressBar = progressBar;
        this.context = context;
    }

    @Override public void run() {
        // Initialize a new media player instance
        MediaPlayer mPlayer = new MediaPlayer();

        // Set the media player audio stream type
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //Try to play music/audio from url
        try{
            // Set the audio data source

            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Authorization", API_KEY);

            Uri uri = Uri.parse(URL);

            mPlayer.setDataSource(context, uri, headers);
            mPlayer.prepare();

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    progressBar.setVisibility(View.GONE);
                    mp.start();
                }
            });
        }catch (IOException e){
            // Catch the exception
            e.printStackTrace();
        }
    }
}
