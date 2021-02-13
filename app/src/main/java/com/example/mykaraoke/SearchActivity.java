package com.example.mykaraoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.example.mykaraoke.adapter.SearchAdapter;
import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.util.RecyclerViewDecoration;

import java.util.ArrayList;

import static com.example.mykaraoke.SplashActivity.songItemList;

public class SearchActivity extends AppCompatActivity {
    private EditText editText;
    private RecyclerView recyclerView;
    private ArrayList<SongItem> searchList;
    private SearchAdapter searchAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        editText = findViewById(R.id.search_text);
        recyclerView = findViewById(R.id.search_list);
        searchList = new ArrayList<>();
        searchAdapter = new SearchAdapter(searchList, this);
        recyclerView.setAdapter(searchAdapter);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        // 아이템 간격을 20으로 지정
        recyclerView.addItemDecoration(new RecyclerViewDecoration(30));


        //한글자씩 검색어를 입력시 동작하는 리스너 정의
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editText.getText().toString();
                search(text);
            }
        });
    }

    private void search(String text) {
        //문자 입력시 리스트 초기화
        searchList.clear();
        //리스트의 모든 데이터를 검색
        for (SongItem songItem : songItemList) {
            //검색된 text가 제목에 포함되어 있다면
            if(songItem.getTitle().toLowerCase().contains(text)) {
                //데이터를 리스트에 추가
                searchList.add(songItem);
            }
        }
        searchAdapter.notifyDataSetChanged();
    }
}