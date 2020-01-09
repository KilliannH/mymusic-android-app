package com.example.mymusic.threads;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.mymusic.bus.RxBus;

import java.io.IOException;
import java.util.HashMap;

import io.reactivex.rxjava3.functions.Consumer;

public class PlayerThread extends Thread {

    private String URL;
    private String API_KEY;
    private ProgressBar loadingBar;
    private Context context;
    private MediaPlayer mPlayer;
    private RxBus rxBus;

    public PlayerThread(MediaPlayer mPlayer, String url, String api_key, ProgressBar loadingBar, Context context) {
        this.URL = url;
        this.API_KEY = api_key;
        this.loadingBar = loadingBar;
        this.context = context;
        this.mPlayer = mPlayer;
    }

    @Override public void run() {

        // Set the media player audio stream type
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //Try to play music/audio from url
        try {
            // Set the audio data source

            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Authorization", API_KEY);

            Uri uri = Uri.parse(URL);

            mPlayer.setDataSource(context, uri, headers);
            mPlayer.prepare();

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    loadingBar.setVisibility(View.GONE);
                    rxBus.publish("PLAYER_READY");
                }
            });
        } catch (IOException e){
            // Catch the exception
            e.printStackTrace();
        }
    }
}
