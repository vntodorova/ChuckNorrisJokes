package com.example.venetatodorova.chucknorrisjokes.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.venetatodorova.chucknorrisjokes.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CountdownView extends View {

    private static final int REFRESH_INTERVAL_IN_MILLIS = 20;
    private int size;
    private int startingDegree;
    private int startingColor;
    private int endColor;
    private boolean directionClockwise;
    private double progress;
    private RectF bounds;
    private Paint paint;
    private Timer timer;

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountdownView, 0, 0);
        try {
            size = array.getDimensionPixelSize(R.styleable.CountdownView_size, 0);
            startingDegree = array.getInteger(R.styleable.CountdownView_starting_degree, -90);
            startingColor = array.getColor(R.styleable.CountdownView_start_color, 0);
            endColor = array.getColor(R.styleable.CountdownView_end_color, -1);
            directionClockwise = array.getBoolean(R.styleable.CountdownView_direction_clockwise, true);
        } finally {
            array.recycle();
        }
        init();
    }

    private void init() {
        bounds = new RectF(0, 0, size, size);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(startingColor);
        timer = new Timer();
        setProgress(1);
    }

    public void start(int seconds) {
        long numberOfSteps =  TimeUnit.SECONDS.toMillis(seconds) / REFRESH_INTERVAL_IN_MILLIS;
        final double step = 1.0 / numberOfSteps;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        progress -= step;
                        if (progress <= 0) {
                            setProgress(1);
                            cancel();
                        }
                        setProgress(progress);
                    }
                });
            }
        }, 0, REFRESH_INTERVAL_IN_MILLIS);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float startAngle = startingDegree;
        float sweepAngle = (float) (progress * 360);
        startAngle += (directionClockwise ? -1 : 0) * sweepAngle;
        if (endColor != -1) {
            getNewPaint(progress);
        }
        if (startAngle < 360) {
            canvas.drawArc(bounds, startAngle, sweepAngle, true, paint);
        } else {
            canvas.drawOval(bounds, paint);
        }
    }

    private void getNewPaint(double progress) {
        int newRed = interpolate(progress, Color.red(startingColor), Color.red(endColor));
        int newGreen = interpolate(progress, Color.green(startingColor), Color.green(endColor));
        int newBlue = interpolate(progress, Color.blue(startingColor), Color.blue(endColor));
        paint.setColor(Color.rgb(newRed, newGreen, newBlue));
    }

    private int interpolate(double progress, double startColor, double endColor) {
        return (int) (endColor + ((startColor - endColor) * progress));
    }

    public void setProgress(double progress) {
        this.progress = progress;
        invalidate();
        requestLayout();
    }
}
