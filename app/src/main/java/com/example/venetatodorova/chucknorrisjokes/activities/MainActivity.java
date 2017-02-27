package com.example.venetatodorova.chucknorrisjokes.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.example.venetatodorova.chucknorrisjokes.R;
import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderContract;
import com.example.venetatodorova.chucknorrisjokes.database.FeedReaderDBHelper;
import com.example.venetatodorova.chucknorrisjokes.models.Example;
import com.example.venetatodorova.chucknorrisjokes.services.ApiCaller;
import com.example.venetatodorova.chucknorrisjokes.services.DatabaseReader;
import com.example.venetatodorova.chucknorrisjokes.services.DatabaseWriter;
import com.example.venetatodorova.chucknorrisjokes.views.CountdownView;

import java.util.ArrayList;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;

public class MainActivity extends Activity implements DatabaseReader.DatabaseReaderListener {

    private static final String PAUSE = "Pause";
    private TextView textView;
    private ArrayList<ApiCaller> callers;
    private DatabaseReader reader;
    private static FeedReaderDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text_view);
        dbHelper = new FeedReaderDBHelper(this);
        startTask();
    }

    public void startTask() {
        DatabaseWriter databaseWriter = new DatabaseWriter();
        databaseWriter.start();
        callers = new ArrayList<>();

        CountdownView countdownView1 = (CountdownView) findViewById(R.id.countdown1);
        CountdownView countdownView2 = (CountdownView) findViewById(R.id.countdown2);
        CountdownView countdownView3 = (CountdownView) findViewById(R.id.countdown3);

        Function<Example, Observable<Long>> function = s -> {
            Observable<Long> obs;
            if (isDatabaseFull()) {
                obs = Observable.fromCallable(() -> {
                    for(ApiCaller caller : callers){
                        caller.stopTask();
                    }
                    return 1L;
                });
            } else {
                obs = Observable.fromCallable(() -> addToDatabase(s.getValue().getJoke()))
                        .subscribeOn(AndroidSchedulers.from(databaseWriter.getLooper()));
            }
            return obs.observeOn(AndroidSchedulers.mainThread());
        };

        callers.add(new ApiCaller(countdownView1, function));
        callers.add(new ApiCaller(countdownView2, function));
        callers.add(new ApiCaller(countdownView3, function));
        Random random = new Random();
        for (ApiCaller caller : callers) {
            int randomTime = random.nextInt(10) + 1;
            caller.startDownload(randomTime);
        }
        reader = new DatabaseReader(this, this);
        reader.startReading();
    }

    public void pauseJokes(View view) {
        Button button = (Button) view;
        if (button.getText().equals(PAUSE)) {
            reader.stopReader();
            button.setText(R.string.resume);
        } else {
            reader.startReading();
            button.setText(R.string.pause);
        }
    }

    @Override
    public void onJokeRead(String joke) {
        textView.setText(joke);
        Animation animation = getRandomAnimation();
        animation.setAnimationListener(new Animation.AnimationListener() {
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
        textView.startAnimation(animation);
    }

    public Animation getRandomAnimation() {
        ArrayList<Animation> animations = new ArrayList<>();
        animations.add(AnimationUtils.loadAnimation(this, R.anim.rotate));
        animations.add(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        animations.add(AnimationUtils.loadAnimation(this, R.anim.slide_down));
        Random randomGen = new Random();
        return animations.get(randomGen.nextInt(animations.size()));
    }

    public long addToDatabase(String joke) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_JOKES, joke);
        Log.i("Added to DB", "from Thread : " + Thread.currentThread());
        return database.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
    }

    public static boolean isDatabaseFull() {
        Long size = getDatabaseSize();
        return size >= FeedReaderDBHelper.DATABASE_MAX_SIZE;
    }

    public static long getDatabaseSize() {
        return DatabaseUtils.queryNumEntries(dbHelper.getReadableDatabase(), FeedReaderContract.FeedEntry.TABLE_NAME);
    }

}
