package com.example.mykaraoke.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mykaraoke.R;
import com.example.mykaraoke.SongActivity;
import com.example.mykaraoke.util.JsonUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

//즐겨 찾기 목록 Adapter

public class BookMarkItemAdapter extends RecyclerView.Adapter<BookMarkItemAdapter.ViewHolder> {
    private ArrayList<SongItem> songItemArrayList;
    private Context context;
    private BottomSheetDialog bottomSheetDialog;
    private int selectPostion = -1;
    Button editButton;
    Button deleteButton;
    Button sharedButton;

    public BookMarkItemAdapter(ArrayList<SongItem> songItemArrayList, Context context) {
        this.songItemArrayList = songItemArrayList;
        this.context = context;
        View dialogView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout, null); //bottomsheet 다이어로그 inflate

        bottomSheetDialog = new BottomSheetDialog(context, R.style.Theme_MaterialComponents_BottomSheetDialog);
        bottomSheetDialog.setContentView(dialogView);
        editButton = dialogView.findViewById(R.id.editButton); // 리스트 수정 버튼
        deleteButton = dialogView.findViewById(R.id.deleteButton); // 리스트 삭제 버튼
        sharedButton = dialogView.findViewById(R.id.shareButton); // 리스트 공유 버튼
    }

    @NonNull
    @Override
    public BookMarkItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongItem songItem = songItemArrayList.get(position);

        Glide.with(context)//url 이미지를 imageView에 연결시키기 위한 라이브러리
                .load(songItem.getImage()) // image 주소 얻어옴
                .thumbnail(0.5f) // 썸네일 비율 설정
                .into(holder.imageView);// imageView에 이미지 연결

        holder.txtTitle.setText(songItem.getTitle());
    }


    @Override
    public int getItemCount() {
        return songItemArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView txtTitle;
        private ImageView settingView;

        public ViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.bookmark_image);
            txtTitle = v.findViewById(R.id.bookmark_txtTitle);
            settingView = v.findViewById(R.id.bookmark_setting);


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SongActivity.class);
                    //노래 item을 Parcelable나 Serializable를 implement 하여 intent로 객체 전달시 값의 형태로 전달되는 이슈가 있음
                    //현재 영속성의 관련된 기능을 사용하지 못하여 어쩔수 없이 static 변수에 값을 넣어 참조 형태로 songactivity에서 사용해야함
                    //추후 영속성 관련 기능을 구현할 수 있다면 로직 다시 구현
                    SongItem songItem = songItemArrayList.get(getAdapterPosition());
                    intent.putExtra("songItem", songItem);
                    context.startActivity(intent);
                }
            });

            //아이템의 설정 클릭 시
            settingView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectPostion = getAdapterPosition();
                    bottomSheetDialog.show();
                }
            });

            //삭제 버튼 클릭시
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        JsonUtil.removeSongItemFromSharedPref(songItemArrayList.get(selectPostion), context);//SharedPref에 있는 songItem 삭제
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    songItemArrayList.remove(selectPostion); //list에 있는 data 삭제
                    notifyDataSetChanged();
                    bottomSheetDialog.dismiss(); // 바텀 다이어로그 사라짐
                }
            });

            //수정 버튼 클릭시
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTextInputDialog(selectPostion);//title 수정 다이어로그
                }
            });

            //공유하기 버튼 클릭시
            sharedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String videoId = songItemArrayList.get(selectPostion).getVideoID(); //선택된 아이템의 videoId를 얻어옴
                    Intent intent = new Intent(Intent.ACTION_SENDTO); //공유 데이터를 담을 인텐트 생성
                    intent.setType("text/plain"); //url을 공유하므로 text/plain을 선택
                    intent.putExtra(Intent.EXTRA_TEXT, "https://www.youtube.com/watch?v=" + videoId); //유튜브 동영상을 재생시킬 수 있는 url전송
                    Intent sharedIntent = Intent.createChooser(intent, "친구에게 공유하기"); //공유하기 위한 인텐트 생성
                    context.startActivity(sharedIntent);
                    bottomSheetDialog.dismiss(); //다이어로그 닫기
                }
            });
        }

        private void showTextInputDialog(final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final EditText input = new EditText(context);
            builder.setTitle("즐겨찾기 이름 변경");
            builder.setView(input);
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        //입력 받은 text를 SharedPref에 접근하여 수정
                        JsonUtil.setTitleToSharedPref(songItemArrayList.get(position), input.getText().toString(), context);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    songItemArrayList.get(position).setTitle(input.getText().toString());//data를 입력된 text로 수정
                    bottomSheetDialog.dismiss(); //다이어로그 닫기
                    notifyDataSetChanged();
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
    }

    //sharedPref의 data가 갱신될때 list와 sharedPref의 sync를 맞추기 위해 만든 메소드
    public void notifyAdapter() throws JSONException {
        songItemArrayList = JsonUtil.loadBookmarkList(context);
        notifyDataSetChanged();
    }

}
