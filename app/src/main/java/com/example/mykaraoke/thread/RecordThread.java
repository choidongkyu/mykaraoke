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

        byte[] readData = new byte[bufferSize];
        String pcmFileName = "음성" + context.getApplicationContext().fileList().length / 2 + ".pcm"; // 음성0.pcm, 음성1.pcm ... 식으로 저장
        String wavFileName = "음성" + context.getApplicationContext().fileList().length / 2 + ".wav";
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(pcmFileName, Context.MODE_PRIVATE); // 내부저장소에 파일 저장


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
            rawToWave(new File(context.getFilesDir() + "/" + pcmFileName), new File(context.getFilesDir() + "/" + wavFileName));

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
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 16000); // sample rate
            writeInt(output, 16000 * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
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
}
