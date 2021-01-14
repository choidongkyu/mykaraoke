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
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.example.mykaraoke.thread.RecordThread;
import com.example.mykaraoke.util.Config;

//TODO activity에 대한 설명
public class SongActivity extends AppCompatActivity {
    private static final String TAG = SongActivity.class.getName();
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private SharedPreferences sharedPref;
    private Button recordButton;
    private RecordThread recordThread;
    private WebView webView;
    private WebSettings webSettings;
    private String videoID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_song);
        Toolbar toolbar = findViewById(R.id.toolbar);
        recordButton = findViewById(R.id.recordButton); //녹음 버튼
        recordThread = new RecordThread(this);

        Intent intent = getIntent();
        videoID = intent.getStringExtra(Config.VIDEO_ID);//recyclerview로부터 VideoID를 intent로 전달 받음

        toolbar.setTitle(""); //toolbar 제목 제거
        setSupportActionBar(toolbar);//툴바 생성
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 생성

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this); // 설정값을 갖고오기위한 Preference 매니저

        recordButton.setOnClickListener(new View.OnClickListener() { // 녹음버튼 클릭 리스너
            @Override
            public void onClick(View view) {
                if (!recordThread.isRecording()) { //조건문에 부정문 넣는것 피하기
                    if (hasPermissions(permissions)) { // 녹음시 필요한 권한이 있다면
                        recordButton.setBackgroundColor(getColor(R.color.green));
                        recordThread = new RecordThread(SongActivity.this);
                        recordThread.start(); // 녹음 시작
                    } else { //권한이 없다면 권한 요청
                        ActivityCompat.requestPermissions(SongActivity.this, permissions, 1);
                    }
                } else {
                    recordButton.setBackgroundColor(getColor(R.color.gray));
                    recordThread.stopRecordThread(); // 녹음중일때 button 클릭시 녹음 일시 중지
                }
            }
        });

        // 자동 녹음으로 설정되어 있다면 액티비티 시작 후 녹음 Thread 시작
        if (sharedPref.getBoolean("autoRecord", false)) {
            if (hasPermissions(permissions)) { // 녹음시 필요한 권한이 있다면
                recordButton.setBackgroundColor(getColor(R.color.green));
                recordThread = new RecordThread(SongActivity.this);
                recordThread.start(); // 녹음 시작
            } else { //권한이 없다면 권한 요청
                ActivityCompat.requestPermissions(SongActivity.this, permissions, 1);
            }
        }

        //금영노래방 저작권으로 인해 youtubePlayer로 재생 불가능하여 웹뷰로 우회하여 재생
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        webSettings = webView.getSettings(); //세부 세팅 등록
        webSettings.setJavaScriptEnabled(true); // 웹페이지 자바스크립트 허용 여부
        webSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        webSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        webSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        webSettings.setSupportZoom(false); // 화면 줌 허용 여부
        webSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        webSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        webView.loadUrl("https://www.youtube.com/watch?v=" + videoID); // video 재생 시작

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
            recordThread.stopRecordThread(); // activity가 사용자에게 보이지 않으면 녹음 중지후 파일 저장
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
        }
        return super.onOptionsItemSelected(item);
    }

    // 해당 기능의 권한이 있는지 확인할 수 있는 메소드
    private boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}