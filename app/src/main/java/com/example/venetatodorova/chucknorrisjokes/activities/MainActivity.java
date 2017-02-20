package com.example.venetatodorova.chucknorrisjokes.activities;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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

public class MainActivity extends Activity implements DatabaseWriter.DatabaseWriterListener {

    private ArrayList<Animation> animations;
    private TextView textView;
    private ScheduledExecutorService scheduler;
    private static final String PAUSE = "Pause";
    private static final int READER_DELAY = 10;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            textView.setText((String) msg.obj);
            textView.startAnimation(getRandomAnimation());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text_view);
        animations = getAnimations();
        startThreads();
    }

    private void startThreads() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new DatabaseReader(this, handler), 0, READER_DELAY, TimeUnit.SECONDS);
        DatabaseWriter databaseWriter = new DatabaseWriter(this, this);
        databaseWriter.start();
    }

    private ArrayList<Animation> getAnimations() {
        ArrayList<Animation> animations = new ArrayList<>();
        animations.add(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate));
        animations.add(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
        animations.add(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down));
        return animations;
    }

    private Animation getRandomAnimation() {
        Random randomGen = new Random();
        Animation randAnim = animations.get(randomGen.nextInt(animations.size()));
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

    public void pauseJokes(View view) {
        Button button = (Button) view;
        if (button.getText().equals(PAUSE)) {
            scheduler.shutdown();
            button.setText(R.string.resume);
        } else {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleWithFixedDelay(new DatabaseReader(this, handler), 0, READER_DELAY, TimeUnit.SECONDS);
            button.setText(R.string.pause);
        }
    }

    @Override
    public void onDatabaseWriterStart(Handler handler) {
        CountdownView countdown1 = (CountdownView) findViewById(R.id.countdown1);
        new DownloadThread(handler, countdown1, this).start();
        CountdownView countdown2 = (CountdownView) findViewById(R.id.countdown2);
        new DownloadThread(handler, countdown2, this).start();
        CountdownView countdown3 = (CountdownView) findViewById(R.id.countdown3);
        new DownloadThread(handler, countdown3, this).start();
    }
}
