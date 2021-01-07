package com.example.mykaraoke.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mykaraoke.MainActivity;
import com.example.mykaraoke.R;

import java.util.ArrayList;

public class SongItemAdapter extends RecyclerView.Adapter<SongItemAdapter.ViewHolder> {

    public enum ItemType { // 최신가요 list에 들어가는 item과, 인기가요 list에 들어가는 item layout이 다르므로 enumType으로 구분
        LATEST_SONG_ITEM,
        POPULAR_SONG_ITEM
    }

    private ArrayList<SongItem> songItemArrayList;
    private Context context;
    private ItemType itemType;

    //최신가요를
    public SongItemAdapter(ArrayList<SongItem> songItemArrayList, Context context, ItemType itemType) {
        this.songItemArrayList = songItemArrayList;
        this.context = context;
        this.itemType = itemType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if(itemType == ItemType.LATEST_SONG_ITEM) {// 최신가요에 들어가는 item과 인기가요에 들어가는 item의 layout이 다르므로 itemType으로 구분
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.latest_list_item, parent, false);//custom한 list의 아이템 layout inflate
        }else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_list_item, parent, false);
        }

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
        holder.txtArtist.setText(songItem.getArtist());
    }

    @Override
    public int getItemCount() {
        return songItemArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout itemPanel;
        private ImageView imageView;
        private TextView txtArtist;
        private TextView txtTitle;
        public ViewHolder(View v) {
            super(v);
            if(itemType == ItemType.LATEST_SONG_ITEM) { //최신가요 리스트에 들어갈 item인지 인기가요에 들어갈 item인지 구분하여 resource 구분
                imageView = v.findViewById(R.id.latest_image);
                txtArtist = v.findViewById(R.id.latest_txtArtist);
                txtTitle = v.findViewById(R.id.latest_txtTitle);
            }else{
                imageView = v.findViewById(R.id.popular_image);
                txtArtist = v.findViewById(R.id.popular_txtArtist);
                txtTitle = v.findViewById(R.id.popular_txtTitle);
            }

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(MainActivity.TAG, "Element " + getAdapterPosition() + " clicked.");
                }
            });
        }
    }
}
