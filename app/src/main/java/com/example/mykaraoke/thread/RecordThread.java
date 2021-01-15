package com.example.mykaraoke.thread;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordThread extends Thread {
    private boolean isRecording = false;
    private final int sampleRate = 16000;
    private final int channelCount = AudioFormat.CHANNEL_IN_STEREO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelCount, audioFormat);
    private AudioRecord audioRecord;
    private Context context;

    public RecordThread(Context context) {
        this.context = context;
        audioRecord = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelCount)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();

    }

    @Override
    public void run() {
        isRecording = true;
        audioRecord.startRecording(); // 녹음 시작
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "녹음을 시작합니다", Toast.LENGTH_SHORT).show();
            }
        });

        byte[] readData = new byte[bufferSize];
        String fileName = "record.pcm";
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE); // 내부저장소에 파일 저장
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (isRecording) {
            int ret = audioRecord.read(readData, 0, bufferSize);  //  AudioRecord의 read 함수를 통해 pcm data 를 읽어옴
            try {
                fos.write(readData, 0, bufferSize);    //  읽어온 readData 를 파일에 write 함
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "녹음을 종료합니다", Toast.LENGTH_SHORT).show();
            }
        });
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecordThread() {
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }
}
