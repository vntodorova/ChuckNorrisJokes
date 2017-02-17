package com.example.venetatodorova.chucknorrisjokes.activities;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.venetatodorova.chucknorrisjokes.R;
import com.example.venetatodorova.chucknorrisjokes.services.DatabaseReader;
import com.example.venetatodorova.chucknorrisjokes.services.DatabaseWriter;
import com.example.venetatodorova.chucknorrisjokes.services.DownloadThread;
import com.example.venetatodorova.chucknorrisjokes.views.CountdownView;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements DatabaseWriter.DatabaseWriterListener{

    public static final int SET_NEW_JOKE = 1;
    public static final int DATABASE_IS_FULL = 2;

    private ArrayList<DownloadThread> downloadThreads = new ArrayList<>();
    private ArrayList<Animation> exitAnimations;
    private TextView textView;
    private ScheduledExecutorService scheduler;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_NEW_JOKE: {
                    textView.setText((String) msg.obj);
                    textView.startAnimation(getRandomAnimation());
                    break;
                }
                case DATABASE_IS_FULL: {
                    for (DownloadThread thread : downloadThreads) {
                        thread.onFullDatabase();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text_view);
        exitAnimations = getAnimations();
        startThreads();
    }

    private Animation getRandomAnimation() {
        Random randomGen = new Random();
        Animation randAnim = exitAnimations.get(randomGen.nextInt(exitAnimations.size()));
        randAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return randAnim;
    }

    private void startThreads() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new DatabaseReader(this, handler), 0, 10, TimeUnit.SECONDS);
        DatabaseWriter databaseWriter = new DatabaseWriter(this, handler,this);
        databaseWriter.start();
    }

    private ArrayList<Animation> getAnimations() {
        ArrayList<Animation> animations = new ArrayList<>();
        animations.add(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate));
        animations.add(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in));
        animations.add(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down));
        return animations;
    }

    public void pauseJokes(View view) {
        scheduler.shutdown();
    }

    public void resumeJokes(View view) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new DatabaseReader(this, handler), 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void onDatabaseWriterStart(Handler handler) {
        CountdownView countdown1 = (CountdownView) findViewById(R.id.countdown1);
        CountdownView countdown2 = (CountdownView) findViewById(R.id.countdown2);
        CountdownView countdown3 = (CountdownView) findViewById(R.id.countdown3);
        downloadThreads.add(new DownloadThread(handler, countdown1));
        downloadThreads.add(new DownloadThread(handler, countdown2));
        downloadThreads.add(new DownloadThread(handler, countdown3));
        for (DownloadThread thread : downloadThreads) {
            thread.start();
        }
    }
}
