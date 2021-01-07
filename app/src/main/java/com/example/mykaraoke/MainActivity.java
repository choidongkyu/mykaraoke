package com.example.mykaraoke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.mykaraoke.fragment.HomeFragment;
import com.example.mykaraoke.fragment.LibraryFragment;
import com.example.mykaraoke.fragment.SearchFragment;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "dkchoi";
    private static final int HOME = 0;
    private static final int SEARCH = 1;
    private static final int LIBRARY = 2;
    private TabLayout tabLayout;
    private Fragment fragment = null;

    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.mainTabLayout);
        FrameLayout frameLayout = findViewById(R.id.frameLayout);

        fragment = new HomeFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment); // frameLayout을 Home 화면으로 교체
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN); // fragment를 교체 할때 animation set
        fragmentTransaction.commit(); // commit을 해주어야 화면에서 fragmnet가 보임

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() { // tab item 클릭 리스너 설정
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case HOME: // home Tab을 선택했을때
                        fragment = new HomeFragment();
                        break;
                    case SEARCH: // search Tab을 선택 했을때
                        fragment = new SearchFragment();
                        break;
                    case LIBRARY: // 보관함 Tab을 선택 했을때
                        fragment = new LibraryFragment();
                        break;
                    default:
                        startActivity(new Intent(MainActivity.this, SongActivity.class));
                        return;
                }
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, fragment);
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
}