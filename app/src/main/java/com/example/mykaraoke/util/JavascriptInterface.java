package com.example.mykaraoke.util;

import com.example.mykaraoke.SongActivity;

public class JavascriptInterface {
    SongActivity songActivity;
    public JavascriptInterface(SongActivity songActivity) {
        this.songActivity = songActivity;
    }

    @android.webkit.JavascriptInterface
    public void onPeerConnected() {
        songActivity.onPeerConnected();
    }
}
