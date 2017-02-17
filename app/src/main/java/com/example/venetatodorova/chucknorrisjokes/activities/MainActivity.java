package com.example.venetatodorova.chucknorrisjokes.activities;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.venetatodorova.chucknorrisjokes.R;
import com.example.venetatodorova.chucknorrisjokes.services.DatabaseReader;
import com.example.venetatodorova.chucknorrisjokes.services.DatabaseWriter;
import com.example.venetatodorova.chucknorrisjokes.services.DownloadThread;
import com.example.venetatodorova.chucknorrisjokes.views.CountdownView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.venetatodorova.chucknorrisjokes.services.DownloadThread.API_URL;

public class MainActivity extends Activity {

    private ArrayList<DownloadThread> downloadThreads;
    private TextView textView;
    public static final int SET_NEW_JOKE = 1;
    public static final int DATABASE_IS_FULL = 2;
    private ScheduledExecutorService scheduler;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_NEW_JOKE: {
                    String joke = (String) msg.obj;
                    textView.setText(joke);
                    Log.d("UI:", joke);
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
        CountdownView countdown1 = (CountdownView) findViewById(R.id.countdown1);
        CountdownView countdown2 = (CountdownView) findViewById(R.id.countdown2);
        CountdownView countdown3 = (CountdownView) findViewById(R.id.countdown3);
        downloadThreads = new ArrayList<>();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new DatabaseReader(this, handler), 10, 10, TimeUnit.SECONDS);

        DatabaseWriter databaseWriter = new DatabaseWriter(this, handler);
        databaseWriter.start();

        downloadThreads.add(new DownloadThread(databaseWriter.handler, countdown1));
        downloadThreads.add(new DownloadThread(databaseWriter.handler, countdown2));
        downloadThreads.add(new DownloadThread(databaseWriter.handler, countdown3));

        for (DownloadThread thread : downloadThreads) {
            thread.start();
        }
    }

    public void pauseJokes(View view) {
        scheduler.shutdown();
    }

    public void resumeJokes(View view) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new DatabaseReader(this, handler), 0, 10, TimeUnit.SECONDS);
    }
}
