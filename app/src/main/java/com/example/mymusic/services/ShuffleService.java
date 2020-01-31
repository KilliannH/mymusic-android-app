package com.example.mymusic.services;

import com.example.mymusic.models.Song;

import java.util.ArrayList;

public class ShuffleService {

    private static ArrayList<Song> songs = new ArrayList<>();
    private static Boolean isShuffle = false;

    public static void addSong(Song song) {
        songs.add(song);
    }

    public static ArrayList<Song> getSongs() {
        return songs;
    }

    public static void removeSong(Song song) {
        songs.remove(song);
    }

    public static Boolean getIsShuffle() {
        return isShuffle;
    }

    public static void setIsShuffle(Boolean bool) {
        isShuffle = bool;
    }
}
