package com.example.mymusic.models;

public class Song {

    private String id;
    private String title;
    private String artist;
    private String album;
    private String album_img;
    private String filename;

    public Song (String id, String title, String artist, String album, String album_img, String filename) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.album_img = album_img;
        this.filename = filename;

    }

    /*** getters & setters ***/

    public String getId() {
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

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbum_img() {
        return album_img;
    }

    public void setAlbum_img(String album_img) {
        this.album_img = album_img;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album_img='" + album_img + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}
