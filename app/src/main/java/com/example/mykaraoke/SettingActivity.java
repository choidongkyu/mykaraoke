package com.example.mykaraoke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.mykaraoke.util.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*
    설정창을 보여주는 액티비티
 */
public class SettingActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private HashSet<String> settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);//툴바 생성
        getSupportActionBar().setDisplayShowTitleEnabled(false); //toolbar 제목 제거
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 생성
        SwitchCompat recordSetting = findViewById(R.id.autoRecord);
        SwitchCompat volumeSetting = findViewById(R.id.autoVolume);
        SwitchCompat pushSetting = findViewById(R.id.autoPush);
        SwitchCompat waveFormSetting = findViewById(R.id.waveForm);

        settings = (HashSet<String>) loadSettingsFromSharedPref(); //기존에 저장된 settings 들을 가져옴
        if(settings == null) { //만약 저장된 값이 없다면, settings에 새로 메모리 할당
            settings = new HashSet<>(4); // setting 값을 담기위한 hashSet, 초기용량 4로 지정
        }

        if(settings.contains(Config.AUTO_RECORD_SETTING)) { //자동녹음이 설정되었다면
            recordSetting.setChecked(true); // 자동녹음 스위치 활성화
        }

        if(settings.contains(Config.AUTO_VOLUME_SETTING)) { // 볼륨 최대치 설정이 저장되어 있다면
            volumeSetting.setChecked(true); // 볼륨설정 스위치 활성화
        }

        if(settings.contains(Config.AUTO_PUSH_SETTING)) { // 푸쉬 알림 설정이 저장되어 있다면
            pushSetting.setChecked(true);
        }

        if(settings.contains(Config.AUTO_WAVEFORM_SETTING)) { //오디오 파형 비활성화가 설정이 저장되어 있다면
            waveFormSetting.setChecked(true);
        }

        //설정 스위치들의 리스너 구현
        recordSetting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) { //자동녹음 설정이 활성화 되었다면
                    settings.add(Config.AUTO_RECORD_SETTING); //auto record config 추가
                } else { // 자동녹음이 활성화가 되지 않았다면
                    settings.remove(Config.AUTO_RECORD_SETTING); // auto record config 제거
                }
            }
        });
        volumeSetting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) { // 볼륨 최대치 설정이 활성화 되었다면
                    settings.add(Config.AUTO_VOLUME_SETTING); // volume config 저장
                } else { // 볼륨 최대치 설정이 활성화 되지 않았다면
                    settings.remove(Config.AUTO_VOLUME_SETTING); // volume config 삭제
                }
            }
        });

        pushSetting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) { // 푸쉬 알림 설정이 활성화 되었다면
                    settings.add(Config.AUTO_PUSH_SETTING); // push config 저장
                } else { // 활성화 되지 않았다면
                    settings.remove(Config.AUTO_PUSH_SETTING); // push config 삭제
                }
            }
        });

        waveFormSetting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) { // 웨이브폼 비활성화 설정이 활성화 되었다면
                    settings.add(Config.AUTO_WAVEFORM_SETTING); // waveform config 저장
                } else { //활성화 되지 않았다면
                    settings.remove(Config.AUTO_WAVEFORM_SETTING); // waveform config 삭제
                }
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // toolbar item이 select 되었을때
        if (item.getItemId() == android.R.id.home) {//뒤로가기를 눌렀을 경우 activity 종료
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSettingsToSharedPref(settings); //액티비티가 사용자에게 보이지 않을때 설정들을 sharedPref에 저장
    }

    //스위치에 체크여부에 따라 설정을 sharedPref에 저장 하는 메소드
    private void saveSettingsToSharedPref(Set<String> settings) {
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE); //setting에 관련된 pref를 얻어옴
        SharedPreferences.Editor editor = sharedPreferences.edit(); //setting을 수정하기 위한 editor 불러옴
        editor.putStringSet(Config.SETTINGS_KEY, settings); //config들이 저장된 hashSet을 sharedPref에 저장
        editor.apply();
    }

    //sharedPref에 저장된 값들을 불러오는 메소드
    private Set<String> loadSettingsFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE); //setting에 관련된 pref를 얻어옴
        return sharedPreferences.getStringSet(Config.SETTINGS_KEY, null);
    }
}