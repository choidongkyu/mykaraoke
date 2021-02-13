package com.example.mykaraoke.adapter;

import java.io.Serializable;

public class SongItem implements Serializable {
    private String title;  // 노래 제목
    private String artist; // 가수 이름
    private String videoID; //비디오 재생시 필요한 ID
    private String image; //image 주소


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

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }
}
