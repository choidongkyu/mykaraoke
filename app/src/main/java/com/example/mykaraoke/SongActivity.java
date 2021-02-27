package com.example.mykaraoke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.thread.RecordThread;
import com.example.mykaraoke.util.Config;
import com.example.mykaraoke.util.JavascriptInterface;
import com.example.mykaraoke.util.JsonUtil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/*
    노래 선택시 노래를 부를수 있도록 동영상이 재생되는 activity
*/
public class SongActivity extends AppCompatActivity {
    private static final String TAG = SongActivity.class.getName();
    private RecordThread recordThread;
    private SongItem songItem;

    //fcm 토큰을 얻기 위한 firebase database
    private final DatabaseReference fcmRef = FirebaseDatabase.getInstance().getReference("users");
    //영상통화 연결을 위한 firbase database;
    private DatabaseReference callRef = FirebaseDatabase.getInstance().getReference("callUsers");

    private String username = "";
    private String friendsUsername = "";
    private boolean isPeerConnected = false;
    private boolean isAudio = true;
    private boolean isVideo = true;

    private RelativeLayout callLayout;
    private WebView webRtcWebView;
    private TextView incomingCallTxt;
    private ImageView acceptBtn;
    private ImageView rejectBtn;
    private LinearLayout callControlLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_song);
        toolbar = findViewById(R.id.toolbar);
        MaterialButton recommendButton = findViewById(R.id.recommendButton);
        MaterialButton recordButton = findViewById(R.id.recordButton);
        Button callButton = findViewById(R.id.callButton);
        ImageView toggleAudioBtn = findViewById(R.id.toggleAudioBtn);
        ImageView toggleVideoBtn = findViewById(R.id.toggleVideoBtn);
        callLayout = findViewById(R.id.callLayout);
        incomingCallTxt = findViewById(R.id.incomingCallTxt);
        acceptBtn = findViewById(R.id.acceptBtn);
        rejectBtn = findViewById(R.id.rejectBtn);
        callControlLayout = findViewById(R.id.callControlLayout);
        webRtcWebView = findViewById(R.id.webRtcWebView);
        webRtcWebView.setBackgroundColor(Color.TRANSPARENT);
        username = Config.USER_NAME;

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
                    recordButton.setText("녹음중지");
                    recordThread = new RecordThread(SongActivity.this);
                    recordThread.start(); // 녹음 시작
                } else {
                    Toast.makeText(getApplicationContext(), "녹음을 종료합니다", Toast.LENGTH_SHORT).show();
                    recordThread.stopRecordThread(); // 녹음중일때 button 클릭시 녹음 일시 중지
                    recordButton.setText("녹음시작");
                }
            }
        });

        recommendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFcmDialog();
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
        webView.loadUrl("https://www.youtube.com/watch?v=" + songItem.getVideoId()); // video 재생 시작

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCallDialog();
            }
        });

        toggleAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavascriptFunction("toggleAudio('" + isAudio + "')");
                if(isAudio) {
                    toggleAudioBtn.setImageResource(R.drawable.ic_baseline_mic_24);
                }else {
                    toggleAudioBtn.setImageResource(R.drawable.ic_baseline_mic_off_24);
                }
            }
        });

        toggleVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideo = !isVideo;
                callJavascriptFunction("toggleVideo('" + isVideo + "')");
                if(isVideo) {
                    toggleVideoBtn.setImageResource(R.drawable.ic_baseline_videocam_24);
                }else {
                    toggleVideoBtn.setImageResource(R.drawable.ic_baseline_videocam_off_24);
                }
            }
        });
        //webrtc 동작을 위한 init 메소드
        setupWebView();

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
        if (recordThread.isRecording()) { // 녹음중이라면
            Toast.makeText(getApplicationContext(), "녹음을 종료합니다", Toast.LENGTH_SHORT).show();
            recordThread.stopRecordThread(); // activity가 사용자에게 보이지 않으면 녹음 중지 후 파일 저장
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        callRef.child(username).setValue(null);
        webRtcWebView.loadUrl("about:blank");
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

    private void showFcmDialog() {
        final EditText editText = new EditText(this);
        editText.setHint("닉네임을 입력하세요");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("친구에게 곡 추천하기");
        builder.setView(editText);
        builder.setPositiveButton("보내기",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        fcmRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                //users에 해당하는 모든 데이터를 탐색
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    //입력된 닉네임이 데이터베이스에 있다면
                                    if (snapshot.getKey().equals(editText.getText().toString())) {
                                        String token = (String) snapshot.child("token").getValue(); //닉네임에 등록된 토큰값 얻어오기
                                        sendFcm(token);//상대방에게 fcm메시지 보내기
                                        Toast.makeText(getApplicationContext(), "전송완료.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                Toast.makeText(getApplicationContext(), "해당 닉네임이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    private void showCallDialog() {
        final EditText editText = new EditText(this);
        editText.setHint("닉네임을 입력하세요");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("친구랑 영상통화 하기");
        builder.setView(editText);
        builder.setPositiveButton("요청하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                friendsUsername = editText.getText().toString();
                sendCallRequest();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.show();
    }

    private void sendCallRequest() {
        Log.d(TAG, "sendCallRequest Called");
        if (!isPeerConnected) {
            Toast.makeText(this, "인터넷 연결을 체크 해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        callRef.child(friendsUsername).child("incoming").setValue(username);
        //상대방이 전화를 받으면 isAvailable의 값이 true 변경 되므로 해당값이 true로 변하는지 check
        callRef.child(friendsUsername).child("isAvailable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null) return;
                if (snapshot.getValue().toString().equals("true")) {
                    Log.d("dkchoi","test = " + snapshot.getValue().toString());
                    listenForConnId();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void listenForConnId() {
        Log.d(TAG, "listenForConnId Called");
        callRef.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) return;
                switchToControls();
                callJavascriptFunction("startCall('" + snapshot.getValue() + "')");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //상대방에게 fcm메시지를 보내는 메소드
    private void sendFcm(String token) {
        fcmRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // FCM 메시지 생성
                            JSONObject root = new JSONObject();
                            JSONObject notification = new JSONObject();
                            JSONObject data = new JSONObject();
                            notification.put("body", songItem.getTitle() + "을 부르러 가보실레요?");
                            notification.put("title", Config.USER_NAME + "님이 아래의 곡을 추천하였습니다!");
                            data.put("videoId", songItem.getVideoId());
                            data.put("songTitle", songItem.getTitle());
                            data.put("songArtist", songItem.getArtist());
                            data.put("songImage", songItem.getImage());
                            root.put("notification", notification);
                            root.put("data", data);
                            root.put("to", token);


                            //http 연결
                            URL Url = new URL("https://fcm.googleapis.com/fcm/send");
                            HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);
                            conn.addRequestProperty("Authorization", "key=" + Config.SERVER_KEY);
                            conn.setRequestProperty("Accept", "application/json");
                            conn.setRequestProperty("Content-type", "application/json");
                            OutputStream os = conn.getOutputStream();
                            os.write(root.toString().getBytes("utf-8"));
                            os.flush();
                            conn.getResponseCode();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupWebView() {
        Log.d(TAG, "setupWebView called!");
        webRtcWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if(request == null) return;
                request.grant(request.getResources());
            }
        });
        //web에 javaScript 기능을 사용하기 위해 해당 값을 true로 변경
        webRtcWebView.getSettings().setJavaScriptEnabled(true);
        //WebView 설정을 변경하여 동영상을 자동 재생 가능하도록 함
        webRtcWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        //웹에서 안드로이드 메소드를 부르기위한 bridge 생성
        webRtcWebView.addJavascriptInterface(new JavascriptInterface(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        Log.d(TAG, "loadVideoCall called!");
        String filePath = "file:android_asset/call.html";
        webRtcWebView.loadUrl(filePath);
        webRtcWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) { //페이지 로드가 정상적으로 끝난다면
                super.onPageFinished(view, url);
                //peerjs라이브러리 init
                initializePeer();
            }
        });
    }

    String uniqueId = "";

    private void initializePeer() {
        Log.d(TAG, "initializePeer called!");
        uniqueId = getUniqueID(); // 랜덤하고 유니크한 conId 생성
        //webrtc init
        callJavascriptFunction("init('" + uniqueId + "')");

        //전화가 온다면 incoming 값이 상대방 아이디로 변경됨
        callRef.child(username).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    onCallRequest(snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void onCallRequest(String caller) {
        if (caller == null) {
            return;
        }
        callLayout.setVisibility(View.VISIBLE);
        incomingCallTxt.setText(caller + "님이 같이 노래부르기를 요청합니다");
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callRef.child(username).child("connId").setValue(uniqueId);
                callRef.child(username).child("isAvailable").setValue(true);
                callLayout.setVisibility(View.GONE);
                switchToControls();
            }
        });

        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callRef.child(username).child("incoming").setValue(null);
                callLayout.setVisibility(View.GONE);
            }
        });
    }

    private void switchToControls() {
        callControlLayout.setVisibility(View.VISIBLE);
    }

    private void callJavascriptFunction(String functionString) {
        Log.d(TAG, "callJavascriptFunction Called");
        webRtcWebView.post(new Runnable() {
            @Override
            public void run() {
                webRtcWebView.evaluateJavascript(functionString, null);
            }
        });
    }

    private String getUniqueID() {
        return UUID.randomUUID().toString();
    }

    //서버와의 통신이 정상적으로 된다면 불리는 메소드
    public void onPeerConnected() {
        Log.d(TAG, "onPeerConnected called");
        isPeerConnected = true;
    }


}