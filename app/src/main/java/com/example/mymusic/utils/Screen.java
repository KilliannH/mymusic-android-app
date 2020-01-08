package com.example.mymusic.utils;
import com.example.mymusic.models.Song;

public class Screen {
    private String name;
    private Song songData;

    public Screen(String name) {
        this.name = name;
    }

    // overrides constuctor
    public Screen(String name, Song songData) {
        this.name = name;
        this.songData = songData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Song getSongData() {
        return songData;
    }

    public void setSongData(Song songData) {
        this.songData = songData;
    }

    @Override
    public String toString() {
        return "Screen{" +
                "name='" + name + '\'' +
                ", songData=" + songData +
                '}';
    }
}
