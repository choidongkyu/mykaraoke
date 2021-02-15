package com.example.mykaraoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.adapter.SongItemAdapter;
import com.example.mykaraoke.util.Config;
import com.example.mykaraoke.util.PagerSnapWithSpanCountHelper;
import com.example.mykaraoke.util.Parsing;
import com.example.mykaraoke.util.RecyclerViewDecoration;
import com.google.android.material.tabs.TabLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;


import java.io.IOException;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int SEARCH = 1;
    private static final int LIBRARY = 2;
    private static final int SETTING = 3;
    private static final int SPAN_COUNT = 5; // 한 화면에 보이는 data 수
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private ArrayList<SongItem> latestSongItemArrayList;
    private ArrayList<SongItem> popularSongItemArrayList;
    private ArrayList<SongItem> trotSongItemArrayList;
    private RecyclerView latestSongRecyclerView;
    private RecyclerView popularSongRecyclerView;
    private RecyclerView trotSongRecyclerView;


    private YouTube youTubeDataApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latestSongItemArrayList = new ArrayList<>(); // 최신가요를 담을 list
        popularSongItemArrayList = new ArrayList<>(); // 인기가요를 담을 list
        trotSongItemArrayList = new ArrayList<>(); // 트로트를 담을 list

        TabLayout tabLayout = findViewById(R.id.mainTabLayout);
        latestSongRecyclerView = findViewById(R.id.latestSongList);
        popularSongRecyclerView = findViewById(R.id.popularMusicList);
        trotSongRecyclerView = findViewById(R.id.trotMusicList);


        if (!hasPermissions(permissions)) { //녹음시 필요한 권한이 없다면 권한요청
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        GsonFactory gsonFactory = new GsonFactory(); // youtube api build 하기위한 gsonFactory
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport(); // youtube api build 하기위한 httpTransport

        youTubeDataApi = new YouTube.Builder(httpTransport, gsonFactory, null)
                .setApplicationName(getResources().getString(R.string.app_name))
                .build();

        YoutubeAsyncTask youtubeAsyncTask = new YoutubeAsyncTask(this); //youtube api를 통해 데이터를 받는 쓰레드 생성
        youtubeAsyncTask.execute(Config.TROT_SONG_ID, Config.LATEST_SONG_ID, Config.POPULAR_SONG_ID); //쓰레드 백그라운드 실행, 데이터를 받기위해 재생목록 아이디를 전달

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() { // tab item 클릭 리스너 설정
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case SEARCH: // search Tab을 선택 했을때 검색 activity로 화면 전환
                        startActivity(new Intent(MainActivity.this, SearchActivity.class));
                        break;
                    case LIBRARY: // 보관함 Tab을 선택 했을때 보관함 activity로 화면 전환
                        startActivity(new Intent(MainActivity.this, LibraryActivity.class));
                        break;
                    case SETTING: // setting tab을 선택 했을때 노래 activity로 화면 전환
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        break;
                    default:
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //api로부터 백그라운드에서 data를 받는 asyncTask Thread
    public class YoutubeAsyncTask extends AsyncTask<String, Void, Void> {
        Context context;

        public YoutubeAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... configs) {
            for (String config : configs) { //매개변수로 playlist 받은 수 만큼 데이터 받기 위해 반복문 실행
                PlaylistItemListResponse playlistItems = null;
                try {
                    playlistItems = youTubeDataApi.playlistItems()
                            .list(Config.PART) // 많은 정보를 받기 위해 snippet으로 설정
                            .setPlaylistId(config) // 재생목록 아이디 지정
                            .setMaxResults((long) 50) // 최대 받을 아이템 갯수 지정
                            .setKey(Config.API_KEY) // api key 설정
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (playlistItems == null) { // api로부터 데이터를 받지 못했다면 null 반환
                    return null;
                }

                for (PlaylistItem playlistItem : playlistItems.getItems()) {
                    SongItem songItem = Parsing.createSongItemBySnippet(playlistItem.getSnippet());
                    if (songItem == null) { // songItem이 null인 경우 private 아이템이므로 항목에 추가하지 않음.
                        continue;
                    }
                    if (config.equals(Config.LATEST_SONG_ID)) { //재생목록이 최신가요 목록이라면
                        latestSongItemArrayList.add(songItem); //최신가요 리스트에 추가
                    } else if (config.equals(Config.TROT_SONG_ID)) { // 재생목록이 트로트 목록이라면
                        trotSongItemArrayList.add(songItem); //트롯 가요 리스트에 추가
                    } else if (config.equals(Config.POPULAR_SONG_ID)) { // 재생목록이 인기가요 목록이라면
                        popularSongItemArrayList.add(songItem); // 인기가요 리스트에 추가
                    }
                }
            }
            return null;

        }


        @Override
        protected void onPostExecute(Void aVoid) { // 백그라운드 작업이 끝나면 불리는 메소드 / UI Thread
            super.onPostExecute(aVoid);
            //최신가요 list Adapter생성
            SongItemAdapter latestSongAdapter = new SongItemAdapter(latestSongItemArrayList, context, SongItemAdapter.ItemType.LATEST_SONG_ITEM);

            //인기가요  list Adapter 생성
            SongItemAdapter popularSongAdapter = new SongItemAdapter(popularSongItemArrayList, context, SongItemAdapter.ItemType.POPULAR_SONG_ITEM);

            //트로트 list Adapter 생성
            SongItemAdapter trotSongAdapter = new SongItemAdapter(trotSongItemArrayList, context, SongItemAdapter.ItemType.POPULAR_SONG_ITEM);

            latestSongRecyclerView.setAdapter(latestSongAdapter); // 최신가요 리스트에 Adapter 설정
            popularSongRecyclerView.setAdapter(popularSongAdapter); // 인기가요 리스트에 Adapter 설정
            trotSongRecyclerView.setAdapter(trotSongAdapter);//트로트 리스트에 adapter 설정

            //아이템간의 간격을 20으로 지정
            trotSongRecyclerView.addItemDecoration(new RecyclerViewDecoration(20));
            popularSongRecyclerView.addItemDecoration(new RecyclerViewDecoration(20));

            setRecyclerViewLayoutManager(); // 각 recyclerView의 layoutManager 설정하는 메소드
        }
    }


    private void setRecyclerViewLayoutManager() {
        int scrollPosition = 0;
        //최신가요 recyclerView는 gridLayout으로 설정
        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), SPAN_COUNT, GridLayoutManager.HORIZONTAL, false);
        latestSongRecyclerView.setLayoutManager(gridLayoutManager);
        latestSongRecyclerView.scrollToPosition(scrollPosition);

        //한 페이지씩 넘어 갈수 있도록 recyclerView에 snapHelper를 붙여줌
        SnapHelper snapHelper = new PagerSnapWithSpanCountHelper(SPAN_COUNT);
        snapHelper.attachToRecyclerView(latestSongRecyclerView);

        //인기가요/트로트 recyclerView는 한줄 씩 보여야 하므로 linearLayout으로 설정
        RecyclerView.LayoutManager popularLinearLayoutManger = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        popularSongRecyclerView.setLayoutManager(popularLinearLayoutManger);
        RecyclerView.LayoutManager trotLinearLayoutManger = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        trotSongRecyclerView.setLayoutManager(trotLinearLayoutManger);
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