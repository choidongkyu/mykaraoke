package com.example.mykaraoke.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mykaraoke.R;
import com.example.mykaraoke.SongActivity;
import com.example.mykaraoke.util.JsonUtil;

import org.json.JSONException;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private final ArrayList<SongItem> songItemArrayList;
    private final Context context;

    public SearchAdapter(ArrayList<SongItem> songItemArrayList, Context context) {
        this.songItemArrayList = songItemArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
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
        private final ImageView imageView;
        private final TextView txtTitle;
        private final TextView txtArtist;

        public ViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.songitem_image);
            txtTitle = v.findViewById(R.id.songitem_txtTitle);
            txtArtist = v.findViewById(R.id.songitem_txtArtist);

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
        }
    }
}
