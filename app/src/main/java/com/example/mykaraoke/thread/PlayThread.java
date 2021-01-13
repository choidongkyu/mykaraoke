package com.example.mykaraoke.thread;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PlayThread extends Thread {
    private boolean isPlaying = false;
    private final int sampleRate = 16000;
    private final int channelCount = AudioFormat.CHANNEL_IN_STEREO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final int bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelCount, audioFormat);
    private AudioTrack audioTrack;
    private final Context context;

    public PlayThread(Context context) {
        this.context = context;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelCount, audioFormat, bufferSize, AudioTrack.MODE_STREAM); // AudioTrack 생성
    }

    @Override
    public void run() {
        isPlaying = true;
        audioTrack.play();  // write 하기 전에 play 를 먼저 수행해 주어야 함
        byte[] writeData = new byte[bufferSize];
        FileInputStream fis = null;
        String fileName = "record.pcm";
        try {
            fis = context.openFileInput(fileName);//내부저장소에 record.pcm 파일에 접근
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        DataInputStream dis = new DataInputStream(fis);
        while(isPlaying) {
            try {
                int ret = dis.read(writeData, 0, bufferSize);
                if(ret <= 0) {
                    isPlaying = false;
                    break;
                }
                audioTrack.write(writeData, 0, ret); // AudioTrack 에 write 를 하면 스피커로 송출됨
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;

        try {
            dis.close();
            fis.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlayThread() {
        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

}
