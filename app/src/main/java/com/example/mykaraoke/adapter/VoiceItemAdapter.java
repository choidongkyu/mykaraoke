package com.example.mykaraoke.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.example.mykaraoke.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

//음성녹음 목록 adapter
public class VoiceItemAdapter extends BaseAdapter {
    private ArrayList<String> voiceList;
    private Context context;
    private LayoutInflater layoutInflater;
    private ListType listType = ListType.NORMAL_LIST_VIEW;
    private Integer selectedPosition = -1; //select checkbox 구별 위한 flag
    private BottomSheetDialog bottomSheetDialog;


    public VoiceItemAdapter(ArrayList<String> voiceList, BottomSheetDialog bottomSheetDialog, Context context) {
        this.voiceList = voiceList;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context); //전달 받은 context로부터 layoutInflate 얻어옴.
        this.bottomSheetDialog = bottomSheetDialog;
    }

    @Override
    public int getCount() { // 데이터의 갯수를 반환
        return voiceList.size();
    }

    @Override
    public Object getItem(int position) { // position에 해당되는 아이템 반환
        return voiceList.get(position);
    }

    @Override
    public long getItemId(int i) { // position에 해당되는 아이템의 id 리턴
        return i; // id는 position으로 지정
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, final View convertView, ViewGroup viewGroup) {
        View view = layoutInflater.inflate(R.layout.voice_list_item, null); //커스텀된 item layout을 view에 지정
        TextView voiceTitle = view.findViewById(R.id.voice_text);
        voiceTitle.setText(voiceList.get(position));
        CheckBox checkBox = view.findViewById(R.id.voice_check);
        checkBox.setChecked(position == selectedPosition); //선택된 아이템이 check 표시 되도록 구현
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { //checkbox 클릭 리스너
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //single choice mode, checkbox를 하나만 선택 할 수 있도록 구현
                if (isChecked) {
                    selectedPosition = position;
                    bottomSheetDialog.show();
                } else {
                    selectedPosition = -1;
                }
                notifyDataSetChanged();
            }
        });

        ImageView imageView = view.findViewById(R.id.voice_image);
        if (listType == ListType.CHECK_LIST_VIEW) { //list의 type이 check list라면 checkbox가 보이도록 설정
            checkBox.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        } else { // list type이 normal 상태라면 check 박스가 보이지 않고 아이콘이 보이도록 설정
            checkBox.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            selectedPosition = -1; //모든 아이템 unchecked
        }
        return view;
    }

    //리스트의 type을 체크리스트로 바꿔주는 메소드
    public void changeListViewType(ListType listType) {
        this.listType = listType;
    }

    public ListType getListType() {
        return listType;
    }



    //모든 항목의 체크를 풀어주는 메소드
    public void uncheckedItem() {
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    //선택된 항목의 포지션 반환
    public int getSelectPosition() {
        return selectedPosition;
    }
}
