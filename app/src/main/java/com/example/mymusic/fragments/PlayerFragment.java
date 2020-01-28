package com.example.mymusic.fragments;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mymusic.R;
import com.example.mymusic.bus.RxBus;
import com.example.mymusic.models.Song;
import com.example.mymusic.threads.PlayerThread;
import com.example.mymusic.utils.Util;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class PlayerFragment extends Fragment {

    private MediaPlayer mPlayer;
    private SeekBar mSeekBar;
    private TextView noSongTextView;
    private TextView timer;

    private Boolean isShuffle = false;

    private ImageView playImg;
    private ImageView shuffleImg;

    private Disposable playerRequestDisposable;
    private Disposable playerCbDisposable;

    private Handler seekHandler;
    private Runnable seekRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.setArguments(new Bundle());
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mSeekBar.setVisibility(View.GONE);

        playImg = (ImageView) view.findViewById(R.id.play_img);
        playImg.setVisibility(View.GONE);
        shuffleImg = (ImageView) view.findViewById(R.id.shuffle_img);
        shuffleImg.setVisibility(View.GONE);

        final Drawable playDrawable = getResources().getDrawable(R.drawable.ic_play_black_36dp, null);
        final Drawable pauseDrawable = getResources().getDrawable(R.drawable.ic_pause_black_36dp, null);

        timer = view.findViewById(R.id.timer);
        timer.setVisibility(View.GONE);

        noSongTextView = (TextView) view.findViewById(R.id.no_song_textView);

        playerRequestDisposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "PLAYER_REQUEST") {

                    if(mSeekBar.getVisibility() == View.GONE) {
                        mSeekBar.setVisibility(View.VISIBLE);
                    }
                    if(noSongTextView.getVisibility() == View.VISIBLE) {
                        noSongTextView.setVisibility(View.GONE);
                    }

                    if(mPlayer != null) {
                        if(mPlayer.isPlaying()) {
                            mPlayer.stop();
                        }
                        mPlayer.release();
                    }

                    mPlayer = new MediaPlayer();
                    Bundle bundle = getArguments();
                    Song song = new Gson().fromJson(bundle.getString("SELECTED_SONG"), Song.class);

                    String URL = Util.buildUrl("/stream/" + song.getFilename(), getContext());
                    String API_KEY = Util.getAPI_KEY(getContext());

                    PlayerThread playerThread = new PlayerThread(mPlayer, URL, API_KEY, getContext());
                    playerThread.start();
                }
            }
        });

        playerCbDisposable = RxBus.subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o == "PLAYER_READY") {
                    mSeekBar.setMax(mPlayer.getDuration() / 1000);
                    timer.setVisibility(View.VISIBLE);
                    playImg.setVisibility(View.VISIBLE);
                    shuffleImg.setVisibility(View.VISIBLE);

                    timer.setText("00:00");
                    if(!mPlayer.isPlaying()) {
                        mPlayer.start();
                    }
                    playImg.setImageDrawable(pauseDrawable);

                    seekHandler = new Handler(Looper.getMainLooper());
                    seekRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (mPlayer != null) {
                                int mCurrentPosition = mPlayer.getCurrentPosition() / 1000;
                                mSeekBar.setProgress(mCurrentPosition);
                            }
                            seekHandler.postDelayed(this, 1000);
                        }
                    };

                    getActivity().runOnUiThread(seekRunnable);

                    playImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mPlayer.isPlaying()) {
                                mPlayer.pause();
                                playImg.setImageDrawable(playDrawable);
                            } else {
                                mPlayer.start();
                                playImg.setImageDrawable(pauseDrawable);
                            }
                        }
                    });

                    shuffleImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                    mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (mPlayer != null && fromUser) {
                                mPlayer.seekTo(progress * 1000);
                            }

                            int millis = progress * 1000;

                            timer.setText(String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(millis),
                                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                            ));
                        }
                    });
                }
            }
        });
    }

    /*public void shuffleClick(View v) {
        isShuffle = !isShuffle;

        if(isShuffle) {
            shuffleView.setImageDrawable(blueShuffleDrawable);
        } else {
            shuffleView.setImageDrawable(blackShuffleDrawable);
        }
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        playerRequestDisposable.dispose();
        playerCbDisposable.dispose();
        seekHandler.removeCallbacks(seekRunnable);
        RxBus.publish("PLAYER_NOT_READY");
    }
}