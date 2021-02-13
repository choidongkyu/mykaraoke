package com.example.mykaraoke;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.util.Parsing;
import com.example.mykaraoke.util.Snippet;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //firebase realtime database에 연결
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("songs");

        //app실행시 한번만 data받을수 있도록 singleValueEvent를 리스너로 설정
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //songItem 리스트 형태로 받기위한 genericTypeIndicator 생성
                GenericTypeIndicator<List<SongItem>> genericTypeIndicator = new GenericTypeIndicator<List<SongItem>>() {
                };

                songItemList = (ArrayList<SongItem>) snapshot.getValue(genericTypeIndicator);
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
