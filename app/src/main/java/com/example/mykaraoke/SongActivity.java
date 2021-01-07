package com.example.mykaraoke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;

public class SongActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(""); //toolbar 제목 제거
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 생성
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // toolbar item이 select 되었을때
        switch (item.getItemId()) {
            case android.R.id.home : { //뒤로가기를 눌렀을 경우 activity 종료
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}