package com.example.mymusic.fragments;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mymusic.R;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.models.Song;
import com.example.mymusic.threads.PlayerThread;
import com.example.mymusic.utils.Util;
import com.google.gson.Gson;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class PlayerFragment extends Fragment {

    private MediaPlayer mPlayer;
    private SeekBar mSeekBar;
    private Disposable disposable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle bundle = getArguments();

        Song song = new Gson().fromJson(bundle.getString("SELECTED_SONG"), Song.class);
        mPlayer = new MediaPlayer();
        String URL = Util.buildUrl("/stream/" + song.getFilename(), getContext());
        String API_KEY = Util.getAPI_KEY(getContext());

        PlayerThread playerThread = new PlayerThread(mPlayer, URL, API_KEY, getContext());
        playerThread.start();

        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);

        disposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "PLAYER_READY") {
                    mSeekBar.setMax(mPlayer.getDuration() / 1000);
                    mPlayer.start();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }
}