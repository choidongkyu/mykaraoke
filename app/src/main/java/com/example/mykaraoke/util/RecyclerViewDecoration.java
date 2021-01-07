package com.example.mykaraoke.util;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mykaraoke.MainActivity;

public class RecyclerViewDecoration extends RecyclerView.ItemDecoration { //리사이클러뷰 가로 간격 조절하기 위한 클래스
    private final int divWidth;

    public RecyclerViewDecoration(int divWidth) {
        this.divWidth = divWidth;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.right = divWidth;
        outRect.left = divWidth;
    }
}


