package com.example.mykaraoke.util;

import com.example.mykaraoke.adapter.SongItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;

//data 파싱을 도와주는 클래스, 모듈처럼 사용
public class Parsing {
    //youtube playlistItem의 snippet으로부터 songItem을 만드는 메소드
    public static SongItem createSongItemBySnippet(PlaylistItemSnippet snippet) {
        SongItem songItem = new SongItem();
        String description = snippet.getDescription(); //snippet으로부터 아이템의 description을 얻어옴
        description = description.replace(" ", ""); //description으로부터 제목,가수에 대한 정보를 parsing하기 위하여 공백제거
        description = description.replace("\n", "");//description으로부터 제목,가수에 대한 정보를 parsing하기 위하여 \n제거

        //'제목' 값을 얻기 위한 문자열 parsing
        String target = "제목";
        int targetNum = description.indexOf(target);
        if (targetNum == -1) { //targetNum이 -1인 경우는 private항목 이므로 아이템으로 만들수 없음 그러므로 null 리턴
            return null;
        }
        String title = description.substring(targetNum + 3, (description.substring(targetNum).indexOf("가수") + targetNum));
        songItem.setTitle(title);

        //'가수' 값을 얻기 위한 문자열 parsing
        target = "가수";
        targetNum = description.indexOf(target);
        String artist = description.substring(targetNum + 3, (description.substring(targetNum).indexOf("작사") + targetNum));
        songItem.setArtist(artist);

        //이미지 url
        songItem.setImage(snippet.getThumbnails().getDefault().getUrl());

        //video url
        songItem.setVideoId(snippet.getResourceId().getVideoId());

        return songItem;
    }
}
