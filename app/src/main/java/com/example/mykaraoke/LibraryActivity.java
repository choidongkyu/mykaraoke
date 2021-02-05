package com.example.mykaraoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.mykaraoke.fragment.BookMarkListFragment;
import com.example.mykaraoke.fragment.VoiceListFragment;
import com.google.android.material.tabs.TabLayout;

/*
    음성녹음 / 즐겨찾기 list를 보여주는 activity
*/
public class LibraryActivity extends AppCompatActivity {
    private static final String TAG = LibraryActivity.class.getName();
    private IBackPressedListener listener;
    private static final int VOICE_CONTAINER = 0;
    private static final int BOOKMARK_CONTAINER = 1;
    FragmentManager fragmentManager;

    public interface IBackPressedListener { // fragment에서 back button 이벤트 받기위한 리스너 인터페이스
        void onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        TabLayout tabLayout = findViewById(R.id.libraryTabLayout);
        FrameLayout frameLayout = findViewById(R.id.listContainer);

        //기본적으로 액티비티 실행 시 음성녹음 list를 보여주기 위해 fragment replace 구현
        Fragment voiceListFragment = new VoiceListFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.listContainer, voiceListFragment);
        fragmentTransaction.commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                Fragment fragment = null;
                if(pos == VOICE_CONTAINER) { //음성 녹음 목록 탭 선택시
                    fragment = new VoiceListFragment();
                }else if(pos == BOOKMARK_CONTAINER) { // 즐겨찾기 목록 탭 선택시
                    fragment = new BookMarkListFragment();
                }

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.listContainer, fragment);
                fragmentTransaction.commit();
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
    public void onBackPressed() {
        listener.onBackPressed();
    }

    public void setBackPressedListener (IBackPressedListener listener) {//프래그먼트 전환시 리스너가 교체되도록 구현
        this.listener = listener;
    }
}