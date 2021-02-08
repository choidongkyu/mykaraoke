package com.example.mykaraoke.thread;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mykaraoke.R;
import com.example.mykaraoke.SongActivity;
import com.example.mykaraoke.WaveFormView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*
    음성녹음을 하여 pcm data를 생성해주는 thread
 */
public class RecordThread extends Thread {
    private boolean isRecording = false;
    private final int sampleRate = 16000;
    private final int channelCount = AudioFormat.CHANNEL_IN_STEREO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelCount, audioFormat);
    private AudioRecord audioRecord;
    private Context context;
    private Handler handler;
    private WaveFormView waveFormView;

    public RecordThread(Context context) {
        this.context = context;
        waveFormView = ((SongActivity) context).findViewById(R.id.waveform_view);
        audioRecord = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelCount)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                short[] readData = (short[]) msg.obj;
                waveFormView.updateAudioData(readData);
            }
        };
    }

    @Override
    public void run() {
        isRecording = true;
        audioRecord.startRecording(); // 녹음 시작

        short[] readData = new short[bufferSize];
        int size;
        size = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).list().length; // 파일 뒤에 붙는 숫자를 사이즈로 지정
        String pcmFileName = "음성" + size / 2 + ".pcm"; // 음성0.pcm, 음성1.pcm ... 식으로 저장, wav pcm 두개의 파일이 저장되므로 2를 나눠줌
        String wavFileName = "음성" + size / 2 + ".wav";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString(), pcmFileName)); // app의 music 외부저장소에 저장
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (isRecording) {
            int ret = audioRecord.read(readData, 0, bufferSize);  //  AudioRecord의 read 함수를 통해 pcm data 를 읽어옴
            //데이터를 핸들러를 통해 처리
            Message msg = handler.obtainMessage(0, readData);
            handler.sendMessage(msg);
            ByteBuffer byteBuffer = ByteBuffer.allocate(2 * bufferSize).order(ByteOrder.LITTLE_ENDIAN);
            for (short data : readData) {
                byteBuffer.putShort(data);
            }
            try {
                fos.write(byteBuffer.array());    //  읽어온 readData를 byteArray로 변환한 후 파일에 write 함
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //pcm data를 콘텐트프로바이더를 통한 파일 공유를 위하여 wav파일로 변환
            rawToWave(new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), pcmFileName),
                    new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), wavFileName));

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

    //pcm 데이터를 wav파일로 바꾸기 위한 메소드
    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            int channel = 2;
            int bitDepth = 16;
            int sampleRate = 16000;
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) channel); // number of channels
            writeInt(output, sampleRate); // sample rate
            writeInt(output, sampleRate * channel * (bitDepth / 8)); // byte rate
            writeShort(output, (short) (channel * (bitDepth / 8))); // block align
            writeShort(output, (short) bitDepth); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }

            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis = new FileInputStream(f);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    //shortArray를 ByteArray로 변환해주는 메소드
    public byte[] shortArrayToByteArray(short[] shortArray) {
        int shortArraySize = shortArray.length;
        byte[] byteArray = new byte[shortArraySize * 2];
        for (int i = 0; i < shortArraySize; i++) {
            byteArray[i * 2] = (byte) (shortArray[i] & 0x00FF);
            byteArray[(i * 2) + 1] = (byte) (shortArray[i] >> 8);
        }
        return byteArray;
    }
}
