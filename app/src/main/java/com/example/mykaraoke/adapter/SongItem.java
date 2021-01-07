package com.example.mykaraoke.adapter;

public class SongItem {
    private String image; //image 주소
    private String artist; // 가수 이름
    private String title;  // 노래 제목


    public SongItem(String image, String title, String artist) {
        this.image = image;
        this.artist = artist;
        this.title = title;
    }

    public SongItem() {
    }

    public String getImage() {
        return image;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
