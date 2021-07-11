package com.example.mykaraoke.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mykaraoke.adapter.SongItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

//json 데이터를 필요에 맞게 가공하기 위해 만든 클래스
public class JsonUtil {

    //songItem 리스트를 jsonArray로 변환하는 메소드
    public static JSONArray songItemListToJsonArray(ArrayList<SongItem> list) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (SongItem songItem : list) { //리스트의 모든 songItem을 jsonObject로 변환
            jsonArray.put(songItemToJsonObject(songItem)); //songItem을 jsonObject로 변환 후 jsonArray에 put
        }
        return jsonArray;
    }

    //jsonArray를 songItemList로 변경해주는 메소드
    public static ArrayList<SongItem> jsonArrayToSongItemList(String jsonArrayData) throws JSONException {
        ArrayList<SongItem> songItemList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonArrayData); //String data를 jsonArray 객체로 변환
        for (int i = 0; i < jsonArray.length(); ++i) {
            SongItem songItem = new SongItem();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            //jsonObject를 songItem으로 변환
            songItem.setTitle(jsonObject.getString("title"));
            songItem.setArtist(jsonObject.getString("artist"));
            songItem.setImage(jsonObject.getString("Image"));
            songItem.setVideoId(jsonObject.getString("VideoId"));

            songItemList.add(songItem); //만들어진 songItem을 반환 될 list에 저장
        }
        return songItemList;
    }

    //SongItem을 JsonObject로 변환시켜주는 메소드
    public static JSONObject songItemToJsonObject(SongItem songItem) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", songItem.getTitle());
        jsonObject.put("artist", songItem.getArtist());
        jsonObject.put("Image", songItem.getImage());
        jsonObject.put("VideoId", songItem.getVideoId());

        return jsonObject;
    }

    //jsonArray에 해당 jsonObject의 색인을 반환해주는 메소드
    public static int getIndexFromJsonArray(JSONObject jsonObject, JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); ++i) {
            if (jsonObject.toString().equals(jsonArray.getString(i))) { //jsonArray에 Object가 있다면
                return i; // 색인반환
            }
        }
        return -1; //jsonArray에 object가 없다면 -1 반환
    }

    //songItem을 sharedPref에 저장하는 메소드
    public static void addSongItemToSharedPref(SongItem songItem, Context context) throws JSONException {
        JSONArray jsonArray;

        SharedPreferences sharedPreferences = context.getSharedPreferences("Bookmark", Context.MODE_PRIVATE);//즐겨찾기를 담당하는 shared preference를 가져옴
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getString(Config.BOOKMARKS_KEY, null) == null) {//만약 기존에 저장된 sharedPref가 없다면
            jsonArray = new JSONArray(); //새로운 jsonArray객체 생성
        } else { //기존에 저장된 shraredPref가 있다면
            jsonArray = new JSONArray(sharedPreferences.getString(Config.BOOKMARKS_KEY, null));//기존에 있는 값들을 JsonArray로 다시 생성
        }
        jsonArray.put(JsonUtil.songItemToJsonObject(songItem)); //songItem을 jsonObject로 변환 후 jsonArray에 put
        editor.putString(Config.BOOKMARKS_KEY, jsonArray.toString()); //아이템이 추가된 jsonArray를 sharedPref에 put
        editor.apply();//저장
    }

    //songItem을 sharedPref로부터 삭제하는 메소드
    public static void removeSongItemFromSharedPref(SongItem songItem, Context context) throws JSONException {
        JSONArray jsonArray;
        SharedPreferences sharedPreferences = context.getSharedPreferences("Bookmark", Context.MODE_PRIVATE);//즐겨찾기를 담당하는 shared preference를 가져옴
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getString(Config.BOOKMARKS_KEY, null) == null) { //만약 기존에 저장된 sharedPref가 없다면
            return;
        } else { //저장된 sharedPref가 존재한다면
            jsonArray = new JSONArray(sharedPreferences.getString(Config.BOOKMARKS_KEY, null));
        }
        JSONObject jsonObject = JsonUtil.songItemToJsonObject(songItem);//지워질 songItem을 jsonObject로 변환
        int index;//지워질 오브젝트의 색인을 저장하는 변수
        index = JsonUtil.getIndexFromJsonArray(jsonObject, jsonArray);//object가 있는지 확인 후 해당 object의 색인 반환
        if (index != -1) { //index가 정상적으로 반환되면 해당 object 삭제 후 sharedPref에 저장
            jsonArray.remove(index);
            editor.putString(Config.BOOKMARKS_KEY, jsonArray.toString());
            editor.apply();
        }
    }

    public static ArrayList<SongItem> loadBookmarkList(Context context) throws JSONException {
        ArrayList<SongItem> songItemList;
        SharedPreferences sharedPreferences = context.getSharedPreferences("Bookmark", MODE_PRIVATE);//즐겨찾기를 담당하는 shared preference를 가져옴
        String jsonData = sharedPreferences.getString(Config.BOOKMARKS_KEY, null); //만약 기존에 저장된 sharedPref를 불러옴
        if (jsonData == null) { //만약 기존에 저장된 즐겨찾기 목록이 없다면
            songItemList = new ArrayList<>();//빈 arrayList를 만듬
        } else { //기존에 저장된 즐겨찾기 목록이 있다면
            songItemList = JsonUtil.jsonArrayToSongItemList(jsonData);//jsonArray data를 songitemList로 변환
        }
        return songItemList;
    }

    //SharedPref에 저장된 songItem의 title을 수정하는 메소드
    public static void setTitleToSharedPref(SongItem songItem, String title, Context context) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Bookmark", MODE_PRIVATE);//즐겨찾기를 담당하는 shared preference를 가져옴
        String jsonData = sharedPreferences.getString(Config.BOOKMARKS_KEY, null); //만약 기존에 저장된 sharedPref를 불러옴
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray(jsonData);
        JSONObject jsonObject = songItemToJsonObject(songItem); //songItem을 JsonObject로 변환
        int index = getIndexFromJsonArray(jsonObject, jsonArray);//해당 아이템의 인덱스를 반환
        jsonObject = (JSONObject) jsonArray.get(index); //해당 인덱스의 jsonObject를 얻어옴
        jsonObject.put("title", title);//변경하고자 하는 title로 수정
        editor.putString(Config.BOOKMARKS_KEY, jsonArray.toString());
        editor.apply();
    }
}
