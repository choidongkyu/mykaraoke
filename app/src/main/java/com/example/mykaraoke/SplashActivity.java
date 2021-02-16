package com.example.mykaraoke;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.ui.login.LoginActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//fireBase로부터 data를 받는 splash activity
public class SplashActivity extends AppCompatActivity {
    public static ArrayList<SongItem> songItemList;
    private SongItem recommendSongItem = null; //추천된 songItem
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //firebase realtime database에 연결
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("songs");
        if(getIntent().getExtras() != null){ // fcm push에서 activity 실행된다면 song data가 존재
            recommendSongItem = new SongItem();
            recommendSongItem.setVideoId(getIntent().getExtras().getString("videoId"));
            recommendSongItem.setTitle(getIntent().getExtras().getString("songTitle"));
            recommendSongItem.setArtist(getIntent().getExtras().getString("songArtist"));
            recommendSongItem.setImage(getIntent().getExtras().getString("songImage"));
        }


        //app실행시 한번만 data받을수 있도록 singleValueEvent를 리스너로 설정
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //songItem 리스트 형태로 받기위한 genericTypeIndicator 생성
                GenericTypeIndicator<List<SongItem>> genericTypeIndicator = new GenericTypeIndicator<List<SongItem>>() {
                };

                songItemList = (ArrayList<SongItem>) snapshot.getValue(genericTypeIndicator);
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                if(recommendSongItem != null) {//fcm에서 app이 시작되었다면 songItem 존재함
                    intent.putExtra("recommendSongItem", recommendSongItem);//songItem 전달
                }
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
