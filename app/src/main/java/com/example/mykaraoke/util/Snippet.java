package com.example.mykaraoke.util;


//서버로부터 받는 데이터의 형태를 클래스화
public class Snippet {
    private String description; //곡에 대한 전반적인 정보
    private String image; // 썸네일
    private String videoId; // 비디오를 재생시킬수 있는 id


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
