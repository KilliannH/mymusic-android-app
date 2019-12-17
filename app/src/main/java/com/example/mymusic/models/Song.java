package com.example.mymusic.models;

public class Song {

    private Integer id;
    private String title;
    private String artist;
    private String album_img_url;

    public Song (Integer id, String title, String artist, String album_img_url) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album_img_url = album_img_url;
    }


    /*** getters & setters ***/

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum_img_url() {
        return album_img_url;
    }

    public void setAlbum_img_url(String album_img_url) {
        this.album_img_url = album_img_url;
    }
}
