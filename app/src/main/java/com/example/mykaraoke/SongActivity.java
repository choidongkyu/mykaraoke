package com.example.mykaraoke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.thread.RecordThread;
import com.example.mykaraoke.util.Config;
import com.example.mykaraoke.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
    노래 선택시 노래를 부를수 있도록 동영상이 재생되는 activity
*/
public class SongActivity extends AppCompatActivity {
    private static final String TAG = SongActivity.class.getName();
    private Button recordButton;
    private RecordThread recordThread;
    private Toolbar toolbar;
    private SongItem songItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_song);
        toolbar = findViewById(R.id.toolbar);
        recordButton = findViewById(R.id.recordButton); //녹음 버튼
        recordThread = new RecordThread(this);

        Intent intent = getIntent();
        songItem = (SongItem) intent.getSerializableExtra("songItem"); // recyclerview로부터 선택된 SongItem

        if (songItem == null) { // 아이템을 제대로 받지 못한다면 activity 종료
            Log.e(TAG, "song Item does not exist. retry again");
            finish();
        }

        setSupportActionBar(toolbar);//툴바 생성
        getSupportActionBar().setDisplayShowTitleEnabled(false); //toolbar 제목 제거
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 생성
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);


        recordButton.setOnClickListener(new View.OnClickListener() { // 녹음버튼 클릭 리스너
            @Override
            public void onClick(View view) {
                if (!recordThread.isRecording()) {
                    Toast.makeText(getApplicationContext(), "녹음을 시작합니다", Toast.LENGTH_SHORT).show();
                    recordButton.setBackgroundColor(getColor(R.color.green));
                    recordThread = new RecordThread(SongActivity.this);
                    recordThread.start(); // 녹음 시작
                } else {
                    Toast.makeText(getApplicationContext(), "녹음을 종료합니다", Toast.LENGTH_SHORT).show();
                    recordButton.setBackgroundColor(getColor(R.color.gray));
                    recordThread.stopRecordThread(); // 녹음중일때 button 클릭시 녹음 일시 중지
                }
            }
        });


        //금영노래방 저작권으로 인해 youtubePlayer로 재생 불가능하여 웹뷰로 우회하여 재생
        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        WebSettings webSettings = webView.getSettings(); //세부 세팅 등록
        webSettings.setJavaScriptEnabled(true); // 웹페이지 자바스크립트 허용 여부
        webSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        webSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        webSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        webSettings.setSupportZoom(false); // 화면 줌 허용 여부
        webSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        webSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부
        webView.loadUrl("https://www.youtube.com/watch?v=" + songItem.getVideoID()); // video 재생 시작

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        recordButton.setBackgroundColor(getColor(R.color.gray));
        if (recordThread.isRecording()) { // 녹음중이라면
            Toast.makeText(getApplicationContext(), "녹음을 종료합니다", Toast.LENGTH_SHORT).show();
            recordThread.stopRecordThread(); // activity가 사용자에게 보이지 않으면 녹음 중지 후 파일 저장
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // toolbar item이 select 되었을때
        if (item.getItemId() == android.R.id.home) {//뒤로가기를 눌렀을 경우 activity 종료
            finish();
            return true;
        } else if (item.getItemId() == R.id.book_mark) { // 즐겨찾기 클릭시
            try {
                if (isBookMark(songItem)) { // 즐겨찾기가 되어 있다면
                    item.setIcon(R.drawable.ic_baseline_bookmark_border_24); // 즐겨찾기 아이콘 이미지 변경
                    JsonUtil.removeSongItemFromSharedPref(songItem, this); // 즐겨찾기 sharedPref에서 노래 삭제
                } else { // 즐겨 찾기가 되있지 않다면
                    item.setIcon(R.drawable.ic_baseline_bookmark_24); // 즐겨찾기 아이콘 변경
                    JsonUtil.addSongItemToSharedPref(songItem, this); // 즐겨찾기 sharedPref에 노래 추가
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // 커스텀 toolbar 메뉴 inflate
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.custom_toolbar, menu);
        try {
            if (isBookMark(songItem)) { //즐겨찾기가 되어있다면
                menu.getItem(0).setIcon(R.drawable.ic_baseline_bookmark_24);// 아이콘을 즐겨찾기 완료로 이미지 변경
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }


    //즐겨찾기에 저장된 songItem인지 판단하는 메소드
    private boolean isBookMark(SongItem songItem) throws JSONException {
        JSONArray jsonArray;
        SharedPreferences sharedPreferences = getSharedPreferences("Bookmark", MODE_PRIVATE);//즐겨찾기를 담당하는 shared preference를 가져옴
        if (sharedPreferences.getString(Config.BOOKMARKS_KEY, null) == null) { //만약 기존에 저장된 sharedPref가 없다면
            return false;
        } else { //저장된 sharedPref가 존재한다면
            jsonArray = new JSONArray(sharedPreferences.getString(Config.BOOKMARKS_KEY, null));
        }
        int index = JsonUtil.getIndexFromJsonArray(JsonUtil.songItemToJsonObject(songItem), jsonArray);//songitem을 jsonObject로 변환시켜 인덱스 반환
        if (index == -1) { //index가 -1 이라면 전달 받은 아이템은 즐겨찾기에 포함되어 있지 않으므로 false 반환
            return false;
        }
        return true;
    }
}