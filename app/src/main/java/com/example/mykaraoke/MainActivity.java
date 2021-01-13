package com.example.mykaraoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.adapter.SongItemAdapter;
import com.example.mykaraoke.util.PagerSnapWithSpanCountHelper;
import com.example.mykaraoke.util.RecyclerViewDecoration;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private static final int HOME = 0;
    private static final int SEARCH = 1;
    private static final int LIBRARY = 2;
    private static final int SONG = 3;
    private static final int SETTING = 4;
    private TabLayout tabLayout;
    private static final int SPAN_COUNT = 5; // 한 화면에 보이는 data 수
    private final String latestSongUrlAddress = "https://www.music-flo.com/api/meta/v1/track/KPOP/new?page=1&size=100&timestamp=1581420059879";
    private final String trotSongUrlAddress = "https://www.music-flo.com/api/display/v1/browser/chart/3554/track/list?size=100&timestamp=1609790664606";
    private URL latestSongUrl = null;
    private URL trotSongUrl = null;
    private ArrayList<SongItem> latestSongItemArrayList;
    private ArrayList<SongItem> trotSongItemArrayList;
    private RecyclerView latestSongRecyclerView;
    private RecyclerView popularSongRecyclerView;
    private RecyclerView trotSongRecyclerView;
    protected SnapHelper snapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.mainTabLayout);
        latestSongItemArrayList = new ArrayList<>(); // 최신가요를 담을 list
        trotSongItemArrayList = new ArrayList<>(); // 트로트를 담을 list
        try {
            latestSongUrl = new URL(latestSongUrlAddress); //최신가요 받기위한  url
            trotSongUrl = new URL(trotSongUrlAddress); // 트로트 목록을 받기위한 url
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        latestSongRecyclerView = findViewById(R.id.latestSongList);
        popularSongRecyclerView = findViewById(R.id.popularMusicList);
        trotSongRecyclerView = findViewById(R.id.trotMusicList);
        GetDataTask getDataTask = new GetDataTask(getApplicationContext()); //url api를 통해 데이터를 받을 쓰레드 생성
        getDataTask.execute(latestSongUrl, trotSongUrl);//데이터를 받는 쓰레드 백그라운드 실행


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
                    case SONG: // 노래 tab을 선택 했을때 노래 activity로 화면 전환
                        startActivity(new Intent(MainActivity.this, SongActivity.class));
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
    public class GetDataTask extends AsyncTask<URL, Void, String> {
        HttpURLConnection urlConnection = null;
        String result;
        Context context;

        public GetDataTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(URL... urls) {
            for (int i = 0; i < urls.length; ++i) { //들어온 url 전부에게 data를 받을수 있도록 반복문으로 구현
                try {
                    urlConnection = (HttpURLConnection) urls[i].openConnection();//api url 접속
                    urlConnection.setRequestMethod("GET");//HTTP 프로토콜 GET 으로 설정
                    urlConnection.connect(); // 연결
                    int responseStateCode = urlConnection.getResponseCode(); //접속이 잘되었는지 코드 반환
                    InputStream inputStream;
                    if (responseStateCode == HttpURLConnection.HTTP_OK) {//접속이 잘되었다면
                        inputStream = urlConnection.getInputStream();//접속된 url의 inputStream을 얻어옴
                    } else {
                        inputStream = urlConnection.getErrorStream();
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    bufferedReader.close();
                    urlConnection.disconnect();
                    result = sb.toString();
                } catch (Exception e) {
                    result = e.toString();
                }

                JSONObject jsonObj = null;
                JSONArray jsonArray = null;
                boolean isLatestSong = urls[i].getPath().equals("/api/meta/v1/track/KPOP/new"); // 현재 url이 최신가요인지 트로트인지 구분하는 flag
                String list = isLatestSong ? "list" : "trackList"; //jsonObject가 트로트 리스트는 tracklist, 최신가요는 list로 구성되어있으므로 url에 따라 구분
                try {
                    jsonObj = new JSONObject(result);
                    jsonObj = new JSONObject(jsonObj.getString("data"));
                    jsonArray = (JSONArray) jsonObj.get(list);
                    for (int j = 0; j < jsonArray.length(); ++j) {
                        SongItem songItem = new SongItem();
                        JSONObject parseJsonObject = ((JSONObject) jsonArray.get(j));
                        songItem.setTitle((String) parseJsonObject.get("name"));// 곡 이름 json parsing

                        JSONArray parseJsonArray = parseJsonObject.getJSONArray("artistList"); // artistList array 얻어온 후
                        JSONObject artistJsonObject = (JSONObject) parseJsonArray.get(0); // artistList는 1개의 요소밖에 없으므로 index 0를 사용한다.
                        songItem.setArtist((String) artistJsonObject.get("name"));// list의 가수 이름 parsing

                        parseJsonObject = (JSONObject) parseJsonObject.get("album");
                        parseJsonArray = parseJsonObject.getJSONArray("imgList"); //imgList array 얻어온 후
                        parseJsonObject = (JSONObject) parseJsonArray.get(1); // 첫번째 요소에 접근
                        songItem.setImage((String) parseJsonObject.get("url")); // 이미지 url parsing

                        if (isLatestSong) { // 최신가요면 최신가요 리스트에 추가 / 트로트면 트로트 리스트에 추가
                            latestSongItemArrayList.add(songItem);
                        } else {
                            trotSongItemArrayList.add(songItem);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) { // 백그라운드 작업이 끝나면 불리는 메소드 / UI Thread
            super.onPostExecute(s);
            //최신가요 list Adapter생성
            SongItemAdapter latestSongAdapter = new SongItemAdapter(latestSongItemArrayList, context, SongItemAdapter.ItemType.LATEST_SONG_ITEM);

            //<지워질 코드> 인기가요와 최신가요가 안겹치게 하기위해서 같은곡 리스트지만 reverse하여 인기곡 리스트로 설정
            ArrayList<SongItem> popularSongItemArrayList = (ArrayList<SongItem>) latestSongItemArrayList.clone();
            Collections.reverse(popularSongItemArrayList);
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


    public void setRecyclerViewLayoutManager() {
        int scrollPosition = 0;
        //최신가요 recyclerView는 gridLayout으로 설정
        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), SPAN_COUNT, GridLayoutManager.HORIZONTAL, false);
        latestSongRecyclerView.setLayoutManager(gridLayoutManager);
        latestSongRecyclerView.scrollToPosition(scrollPosition);
        //한 페이지씩 넘어 갈수 있도록 recyclerView에 snapHelper를 붙여줌
        snapHelper = new PagerSnapWithSpanCountHelper(SPAN_COUNT);
        snapHelper.attachToRecyclerView(latestSongRecyclerView);

        //인기가요/트로트 recyclerView는 한줄 씩 보여야 하므로 linearLayout으로 설정
        RecyclerView.LayoutManager popularLinearLayoutManger = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        popularSongRecyclerView.setLayoutManager(popularLinearLayoutManger);
        RecyclerView.LayoutManager trotLinearLayoutManger = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        trotSongRecyclerView.setLayoutManager(trotLinearLayoutManger);
    }
}