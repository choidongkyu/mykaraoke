package com.example.mykaraoke.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.mykaraoke.BuildConfig;
import com.example.mykaraoke.LibraryActivity;
import com.example.mykaraoke.MainActivity;
import com.example.mykaraoke.R;
import com.example.mykaraoke.adapter.ListType;
import com.example.mykaraoke.adapter.SongItem;
import com.example.mykaraoke.adapter.VoiceItemAdapter;
import com.example.mykaraoke.thread.PlayThread;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

//음성 녹음 목록을 보여주는 프래그먼트

public class VoiceListFragment extends Fragment implements LibraryActivity.IBackPressedListener {
    private ListView voiceListView;
    private VoiceItemAdapter voiceItemAdapter;
    private ArrayList<String> voiceList;
    private ArrayList<String> wavFileList;
    private BottomSheetDialog bottomSheetDialog;
    private Context context;
    private PlayThread playThread;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voice_list, container, false);
        voiceListView = view.findViewById(R.id.voiceList);
        voiceList = new ArrayList<>(); // 파일리스트를 배열에서 list 형태로 변경(추가,제거 용이하기 위하여)
        wavFileList = new ArrayList<>(); // wav 파일을 관리용 리스트

        File externalFiles = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        String[] files = externalFiles.list(); // 음성녹음 파일 리스트 get
        for (String file : files) {
            //녹음이 종료되면 wav파일과 pcm파일 2개가 나오게 되는데 녹음을 재생할시에는 pcm파일만 필요하므로 wav파일은 리스트에 담지 않음
            if (file.contains(".wav")) {
                continue;
            }
            voiceList.add(file);
        }
        View dialogView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout, null); //bottomsheet 다이어로그 inflate
        bottomSheetDialog = new BottomSheetDialog(context, R.style.Theme_MaterialComponents_BottomSheetDialog);
        bottomSheetDialog.setContentView(dialogView);

        Button editButton = dialogView.findViewById(R.id.editButton); // 리스트 수정 버튼
        Button deleteButton = dialogView.findViewById(R.id.deleteButton); // 리스트 삭제 버튼
        Button sharedButton = dialogView.findViewById(R.id.shareButton); // 리스트 공유 버튼

        playThread = new PlayThread(context);//음성 재생하기 위한 Thread

        voiceItemAdapter = new VoiceItemAdapter(voiceList, bottomSheetDialog, context); //음성녹음 리스트 adapter 생성
        voiceListView.setAdapter(voiceItemAdapter);//adapter set
        voiceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //아이템 롱클릭시 checkbox list로 변환
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                voiceItemAdapter.changeListViewType(ListType.CHECK_LIST_VIEW); //listType을 checkbox로 변경
                voiceListView.setAdapter(voiceItemAdapter);//리스트 ui초기화 위하여 다시 adapter 설정
                return true;
            }
        });

        voiceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (playThread.isPlaying()) { //음성이 재생중이라면 재생 멈춤
                    playThread.stopPlayThread();
                } else { // 재생 중이 아니라면 음성 재생
                    playThread = new PlayThread(context, voiceList.get(i));
                    playThread.start();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() { //삭제 버튼을 눌렀을때
            @Override
            public void onClick(View view) {
                String pcmFileName = voiceList.get(voiceItemAdapter.getSelectPosition());
                String wavFileName = pcmFileName.replace(".pcm", ".wav");
                File pcmFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), pcmFileName); //외부 저장소 pcm파일에 접근
                if (pcmFile.exists()) { //파일이 존재한다면 파일 삭제
                    pcmFile.delete();
                }
                File wavFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), wavFileName);//wav파일도 마찬가지로 삭제
                if (wavFile.exists()) {
                    wavFile.delete();
                }
                voiceList.remove(voiceList.get(voiceItemAdapter.getSelectPosition())); //data 삭제
                bottomSheetDialog.dismiss(); //다이어로그 닫기
                voiceItemAdapter.notifyDataSetChanged();
            }
        });

        sharedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //콘텐트 프로바이더는 pcm파일을 재생할 수 없으므로 wav로 변경
                String fileName = voiceList.get(voiceItemAdapter.getSelectPosition()).replace(".pcm", ".wav");
                File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName); //외부 저장소 파일에 접근

                Intent intent = new Intent(Intent.ACTION_SEND); //공유 데이터를 담을 인텐트 생성
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setDataAndType(uri, "audio/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent sharedIntent = Intent.createChooser(intent, "친구에게 공유하기"); //공유하기 위한 인텐트 생성
                context.startActivity(sharedIntent);
                bottomSheetDialog.dismiss(); //다이어로그 닫기
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() { // 수정버튼을 눌렀을때
            @Override
            public void onClick(View view) {
                showTextInputDialog(voiceItemAdapter.getSelectPosition()); // text 입력 다이어로그 띄우기
            }
        });

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //다이어로그가 사라지면 check된 항목 unCheck
                voiceItemAdapter.uncheckedItem();
            }
        });
        ((LibraryActivity) getActivity()).setBackPressedListener(this); // backbutton 이벤트 받기위한 리스너 설정
        return view;
    }

    private void showTextInputDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText input = new EditText(context);
        builder.setTitle("녹음 파일 이름 변경");
        builder.setView(input);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String pcmFileName = voiceList.get(voiceItemAdapter.getSelectPosition());
                String wavFileName = pcmFileName.replace(".pcm", ".wav");
                File wavFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), wavFileName); // 내부 저장소 wav 파일에 접근
                File pcmFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), pcmFileName); // 내부 저장소 pcm 파일에 접근
                pcmFile.renameTo(new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), input.getText() + ".pcm"));//파일명을 입력된 text로 수정
                wavFile.renameTo(new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), input.getText() + ".wav"));//wav파일도 마찬가지로 이름 수정
                voiceList.set(position, input.getText() + ".pcm");//data를 입력된 text로 수정
                bottomSheetDialog.dismiss(); //다이어로그 닫기
                voiceItemAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //nothing
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        //back 버튼을 누를시 list의 형태가 check list라면 normal list로 변경
        if (voiceItemAdapter.getListType() == ListType.CHECK_LIST_VIEW) {
            voiceItemAdapter.changeListViewType(ListType.NORMAL_LIST_VIEW); //listType을 normal type으로 변경
            voiceListView.setAdapter(voiceItemAdapter);//리스트 ui초기화 위하여 다시 adapter 설정
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playThread.isPlaying()) { //음성 재생중이라면 재생 멈춤
            playThread.stopPlayThread();
        }
    }
}
