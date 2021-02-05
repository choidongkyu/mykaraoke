package com.example.mykaraoke.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mykaraoke.LibraryActivity;
import com.example.mykaraoke.R;
import com.example.mykaraoke.adapter.BookMarkItemAdapter;
import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.util.Config;
import com.example.mykaraoke.util.JsonUtil;
import com.example.mykaraoke.util.RecyclerViewDecoration;

import org.json.JSONException;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class BookMarkListFragment extends Fragment implements LibraryActivity.IBackPressedListener{
    private Context context;
    private RecyclerView bookMarkRecyclerView;
    private BookMarkItemAdapter bookMarkListAdapter;
    private ArrayList<SongItem> bookmarkSongList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_mark_list, container, false);
        bookMarkRecyclerView = view.findViewById(R.id.book_mark_List);

        try {
            bookmarkSongList = JsonUtil.loadBookmarkList(context);//SharedPref에 저장되어 있는 즐겨찾기 목록 불러옴
        } catch (JSONException e) {
            e.printStackTrace();
        }

        bookMarkListAdapter = new BookMarkItemAdapter(bookmarkSongList, context);
        bookMarkRecyclerView.setAdapter(bookMarkListAdapter);

        //즐겨찾기 recyclerView을 LinearLayout으로 설정
        int scrollPosition = 0;
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        bookMarkRecyclerView.setLayoutManager(linearLayoutManager);
        bookMarkRecyclerView.scrollToPosition(scrollPosition);

        // 아이템 간격을 20으로 지정
        bookMarkRecyclerView.addItemDecoration(new RecyclerViewDecoration(30));
        ((LibraryActivity)getActivity()).setBackPressedListener(this); // backbutton 이벤트 받기위한 리스너 설정

        return view;
    }

    @Override
    public void onBackPressed() {
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    //즐겨찾기 목록에서 노래 화면에서 북마크 해제하고 다시 돌아오는 경우를 대비하여 notifyData를 다시 해줌
    @Override
    public void onStart() {
        super.onStart();
        try {
            bookMarkListAdapter.notifyAdapter();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}