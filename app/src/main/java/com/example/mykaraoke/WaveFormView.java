package com.example.mykaraoke;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaveFormView extends View {
    private static final String TAG = "WaveFormView";
    int sampleRate = 16000; // 1초에 들어오는 데이터의 수
    float second = 30f;//한줄이 이동하는 시간을 30초로 정함

    //1픽셀마다 들어갈수 있는 데이터의 양
    int samplePerPixel = ((int) second * sampleRate) / 1920;

    int dataRange = 65536; // pcm data는 short 변수의 범위를갖고 있으므로 dataRange를 65536으로 지정
    float magnification = 3f; //진폭의 크기
    short shortMaxValue = 32767; // pcmData의 최대값

    Bitmap bmp = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
    Bitmap tempBmp = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);

    private Paint waveFormPaint;//waveform을 그려줄 paint
    private float mAxisX = 0;//x좌표


    public WaveFormView(Context context) {
        super(context);
        init(context, null);
    }

    public WaveFormView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaveFormView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int waveformColor = Color.parseColor("#009FF7");
        waveFormPaint = new Paint();
        waveFormPaint.setColor(waveformColor);
        waveFormPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        waveFormPaint.setStrokeWidth(0);

        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorWaveBackground));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bmp, 0, 0, waveFormPaint);
    }

    public synchronized void updateAudioData(short[] buffer) {
        ArrayList<Short> newBuffer = new ArrayList<>();
        for (short data : buffer) {
            newBuffer.add(data);
        }
        drawWave(newBuffer);
        invalidate();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawWave(ArrayList<Short> buffer) {
        int height = getMeasuredHeight();//측정되는 높이값
        int originWidth = bmp.getWidth();//bitmap의 width
        int originHeight = bmp.getHeight();//bitmap의 height
        int dataLength = buffer.size();//데이터의 크기

        float axisX = 0;//x좌표
        int finalAxisX = -1;//목표 x좌표
        short low = 0;
        short high = 0;
        int lowY = 0;
        int highY = 0;


        if (this.mAxisX > getMeasuredWidth()) {//waveform이 화면끝까지 갔다면
            //2개의 bitmap을 교차하여 계속해서 waveform이 그려지도록 함
            Canvas secondCanvas = new Canvas(tempBmp);
            secondCanvas.drawBitmap(bmp, new Rect(dataLength / samplePerPixel, 0, originWidth, originHeight),
                    new Rect(0, 0, originWidth - (dataLength / samplePerPixel), originHeight), waveFormPaint);
            bmp = tempBmp;
            this.mAxisX -= (dataLength / samplePerPixel);
        }

        Canvas cashCanvas = new Canvas(bmp);

        //데이터길이와 1픽셀당 데이터 수를 계산하여 x좌표가 어디까지 갈지 정함
        while (axisX < dataLength / samplePerPixel) {
            List<Short> tempBuffer = buffer.subList(0, samplePerPixel);//받은 데이터를 samplePerpixel만큼 받음
            short[] data = new short[samplePerPixel];//1픽셀에 그려질 데이터를 받음
            for (int i = 0; i < tempBuffer.size(); ++i) {
                data[i] = tempBuffer.get(i);
            }
            Arrays.sort(data); //최댓값, 최솟값 구하기 위한 정렬
            int nearestAxisX = (int) this.mAxisX;
            if (nearestAxisX != finalAxisX) {
                finalAxisX = nearestAxisX;
                //1픽셀에 그려질 최대값과 최소 값을 구함
                low = (short) (data[0] * magnification);
                high = (short) (data[data.length - 1] * magnification);
                if (low == 0 && high == 0) {
                    low = -1;
                    high = 1;
                }
                lowY = height - (low + shortMaxValue) * height / dataRange;
                highY = height - (high + shortMaxValue) * height / dataRange;
                //데이터의 최소값과 최대값의 사이에 선을 그려줌
                cashCanvas.drawLine(finalAxisX, lowY, finalAxisX, highY, waveFormPaint);
            }
            float scale = 1f;//픽셀 단위
            //x좌표를 1픽셀만큼 움직여줌
            this.mAxisX += scale;
            axisX += scale;
            tempBuffer.clear();
        }
    }
}
